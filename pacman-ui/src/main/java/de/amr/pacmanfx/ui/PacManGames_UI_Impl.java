/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static java.util.Objects.requireNonNull;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public class PacManGames_UI_Impl implements GameUI {

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

    // package-visible to allow access to GameUI interface
    static PacManGames_UI_Impl THE_ONE;

    private final PacManGames_Assets theAssets;
    private final DirectoryWatchdog  theCustomDirWatchdog;
    private final GameClock          theGameClock;
    private final GameContext        theGameContext;
    private final Keyboard           theKeyboard;
    private final Joypad             theJoypad;
    private final UIPreferences theUIPrefs;
    private final Stage              theStage;

    private final ActionBindingManager globalActionBindings = new DefaultActionBindingManager();
    private final Map<String, GameUI_Config> configByGameVariant = new HashMap<>();
    private final MainScene mainScene;
    private final StartPagesView startPagesView;
    private final PlayView playView;
    private       EditorView editorView; // created on first access

    public PacManGames_UI_Impl(GameContext gameContext, Stage stage, double width, double height) {
        requireNonNull(gameContext, "Game context is null");
        requireNonNull(stage, "Stage is null");

        // Input
        theKeyboard = new Keyboard();
        theJoypad = new Joypad(theKeyboard);

        // Game context
        theCustomDirWatchdog = new DirectoryWatchdog(gameContext.theCustomMapDir());
        theGameClock = new GameClock();
        theGameContext = gameContext;

        // Game UI
        theAssets = new PacManGames_Assets();
        theUIPrefs = new PacManGames_Preferences();
        theStage = stage;

        initGlobalActionBindings();

        mainScene = new MainScene(this, width, height);
        // Check if a global action is defined for the key press, otherwise let the current view handle it.
        mainScene.setOnKeyPressed(e -> {
            GameAction matchingAction = globalActionBindings.matchingAction(theKeyboard).orElse(null);
            if (matchingAction != null) {
                runAction(matchingAction);
            } else {
                currentView().handleKeyboardInput(this);
            }
        });
        mainScene.currentGameSceneProperty().bindBidirectional(PROPERTY_CURRENT_GAME_SCENE);
        mainScene.currentViewProperty().bindBidirectional(PROPERTY_CURRENT_VIEW);

        configureStage(stage);

        startPagesView = new StartPagesView(this);
        playView = new PlayView(this, mainScene);

        theGameClock.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        theGameClock.setPermanentAction(this::drawCurrentView);

        PROPERTY_3D_WALL_HEIGHT.set(theUIPrefs.getFloat("3d.obstacle.base_height"));
        PROPERTY_3D_WALL_OPACITY.set(theUIPrefs.getFloat("3d.obstacle.opacity"));
    }

    private void configureStage(Stage stage) {
        stage.setScene(mainScene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
        var title = Bindings.createStringBinding(
            () -> {
                PacManGames_View currentView = currentView();
                return currentView == null
                    ? "No View?"
                    : currentView.title().map(ObservableObjectValue::get)
                        .orElse(mainScene.computeTitle(PROPERTY_3D_ENABLED.get(), PROPERTY_DEBUG_INFO_VISIBLE.get()));
            },
                PROPERTY_CURRENT_GAME_SCENE,
                PROPERTY_CURRENT_VIEW,
                PROPERTY_DEBUG_INFO_VISIBLE,
                PROPERTY_3D_ENABLED,
            theGameClock().pausedProperty(),
            mainScene.heightProperty()
        );
        stage.titleProperty().bind(title);
    }

    private void initGlobalActionBindings() {
        globalActionBindings.use(ACTION_ENTER_FULLSCREEN, DEFAULT_ACTION_BINDINGS);
        globalActionBindings.use(ACTION_OPEN_EDITOR,      DEFAULT_ACTION_BINDINGS);
        globalActionBindings.use(ACTION_TOGGLE_MUTED,     DEFAULT_ACTION_BINDINGS);
        globalActionBindings.updateKeyboard(theKeyboard);
    }

    private void selectView(PacManGames_View view) {
        requireNonNull(view);
        final PacManGames_View oldView = mainScene.currentView();
        if (oldView == view) {
            return;
        }
        if (oldView != null) {
            oldView.actionBindingMap().removeFromKeyboard(theKeyboard);
            theGameContext.theGameEventManager().removeEventListener(oldView);
        }
        view.actionBindingMap().updateKeyboard(theKeyboard);
        theGameContext.theGameEventManager().addEventListener(view);

        GameUI.PROPERTY_CURRENT_VIEW.set(view);
    }

    public void applyConfiguration(String gameVariant, Class<?> configClass) {
        try {
            GameUI_Config config = (GameUI_Config) configClass.getDeclaredConstructor(GameUI.class).newInstance(this);
            config.createGameScenes();
            Logger.info("Game scenes for game variant '{}' created", gameVariant);
            config.gameScenes().forEach(scene -> {
                if (scene instanceof GameScene2D gameScene2D) {
                    gameScene2D.debugInfoVisibleProperty().bind(PROPERTY_DEBUG_INFO_VISIBLE);
                }
            });
            setConfig(gameVariant, config);
        } catch (Exception x) {
            Logger.error("Could not apply UI configuration of class {}", configClass);
            throw new IllegalStateException(x);
        }
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Here.</a>
     */
    private void ka_tas_trooo_phe(Throwable reason) {
        Logger.error(reason);
        Logger.error("SOMETHING VERY BAD HAPPENED!");
        showFlashMessageSec(10, "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!");
    }

    private void doSimulationStepAndUpdateGameScene() {
        try {
            theGameContext.theGame().simulationStep().start(theGameClock.tickCount());
            theGameContext.theGameController().updateGameState();
            theGameContext.theGame().simulationStep().logState();
            currentGameScene().ifPresent(GameScene::update);
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private void drawCurrentView() {
        try {
            if (currentView() == playView) {
                playView.draw();
            }
            mainScene.flashMessageLayer().update();
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private EditorView editorView() {
        if (editorView == null) {
            var editor = new TileMapEditor(theStage, theAssets().theModel3DRepository());
            var miReturnToGame = new MenuItem(theAssets().text("back_to_game"));
            miReturnToGame.setOnAction(e -> {
                editor.stop();
                editor.executeWithCheckForUnsavedChanges(this::showStartView);
            });
            editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miReturnToGame);
            editor.init(theGameContext.theCustomMapDir());
            editorView = new EditorView(editor);
        }
        return editorView;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override public Optional<GameScene>  currentGameScene() { return mainScene.currentGameScene(); }
    @Override public PacManGames_View     currentView() { return mainScene.currentView(); }

    @Override public Optional<EditorView> theEditorView() { return Optional.ofNullable(editorView); }
    @Override public PlayView             thePlayView() { return playView; }
    @Override public StartPagesView       theStartPagesView() { return startPagesView; }

    @Override
    public boolean isCurrentGameSceneID(String id) {
        return mainScene.isCurrentGameSceneID(id);
    }

    @Override public Stage theStage() { return theStage; }

    @Override
    public UIPreferences theUIPrefs() { return theUIPrefs; }

    @Override
    public void restart() {
        theGameClock.stop();
        theGameClock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theGameClock.pausedProperty().set(false);
        theGameClock.start();
        theGameContext.theGameController().restart(GameState.BOOT);
    }

    @Override
    public void selectGameVariant(String gameVariant) {
        if (gameVariant == null) {
            Logger.error("Cannot select game variant (NULL)");
            return;
        }
        String previousVariant = theGameContext.theGameController().selectedGameVariant();
        if (previousVariant != null && !previousVariant.equals(gameVariant)) {
            GameUI_Config previousConfig = config(previousVariant);
            Logger.info("Unloading assets for game variant {}", previousVariant);
            previousConfig.dispose();
            previousConfig.soundManager().mutedProperty().unbind();
        }

        GameUI_Config newConfig = config(gameVariant);
        Logger.info("Loading assets for game variant {}", gameVariant);
        newConfig.storeAssets(theAssets());
        newConfig.soundManager().mutedProperty().bind(PROPERTY_MUTED);

        Image appIcon = theAssets.image(newConfig.assetNamespace() + ".app_icon");
        if (appIcon != null) {
            theStage.getIcons().setAll(appIcon);
        } else {
            Logger.error("Could not find app icon for current game variant {}", gameVariant);
        }

        playView.canvasContainer().roundedBorderProperty().set(newConfig.hasGameCanvasRoundedBorder());

        // this triggers a game event and the event handlers:
        theGameContext.theGameController().selectGameVariant(gameVariant);
    }

    @Override
    public void show() {
        playView.dashboard().init(this);
        showStartView();
        theStage.centerOnScreen();
        theStage.show();
        Platform.runLater(theCustomDirWatchdog::startWatching);
    }

    @Override
    public void showEditorView() {
        if (!theGameContext.theGame().isPlaying() || theGameClock.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            theSound().stopAll();
            theGameClock.stop();
            editorView().editor().start(theStage);
            selectView(editorView());
        } else {
            Logger.info("Editor view cannot be opened, game is playing");
        }
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        mainScene.flashMessageLayer().showMessage(String.format(message, args), seconds);
    }

    @Override
    public void showPlayView() {
        selectView(playView);
    }

    @Override
    public void showStartView() {
        theGameClock.stop();
        theGameClock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theSound().stopAll();
        playView.dashboard().setVisible(false);
        selectView(startPagesView);
            startPagesView.currentStartPage().ifPresent(startPage -> Platform.runLater(() -> {
                startPage.onEnter(this); // sets game variant!
                startPage.layoutRoot().requestFocus();
        }));
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        theGameClock.stop();
        theCustomDirWatchdog.dispose();
    }

    @Override
    public PacManGames_Assets theAssets() {
        return theAssets;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameUI_Config> T theConfiguration() {
        return (T) config(theGameContext.theGameController().selectedGameVariant());
    }

    @Override
    public GameClock theGameClock() {
        return theGameClock;
    }

    @Override
    public GameContext theGameContext() {
        return theGameContext;
    }

    @Override
    public Keyboard theKeyboard() { return theKeyboard; }

    @Override
    public Joypad theJoypad() {
        return theJoypad;
    }

    @Override
    public SoundManager theSound() {
        return theConfiguration().soundManager();
    }

    @Override
    public DirectoryWatchdog theCustomDirWatchdog() {
        return theCustomDirWatchdog;
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
        playView.updateGameScene(reloadCurrent);
    }

    @Override
    public void setConfig(String variant, GameUI_Config config) {
        requireNonNull(variant);
        requireNonNull(config);
        configByGameVariant.put(variant, config);
    }

    @Override
    public GameUI_Config config(String gameVariant) {
        return configByGameVariant.get(gameVariant);
    }
}