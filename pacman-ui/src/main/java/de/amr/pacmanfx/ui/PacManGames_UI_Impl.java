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
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.ui.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.GameUI_Properties.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public class PacManGames_UI_Impl implements GameUI {

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

    // package-visible to allow access from GameUI interface
    static PacManGames_UI_Impl THE_ONE;

    private final List<ActionBinding> defaultActionBindings = List.of(
        new ActionBinding(ACTION_ARCADE_INSERT_COIN,          nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        new ActionBinding(ACTION_ARCADE_START_GAME,           nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        new ActionBinding(ACTION_BOOT_SHOW_PLAY_VIEW,         nude(KeyCode.F3)),
        new ActionBinding(ACTION_CHEAT_EAT_ALL_PELLETS,       alt(KeyCode.E)),
        new ActionBinding(ACTION_CHEAT_ADD_LIVES,             alt(KeyCode.L)),
        new ActionBinding(ACTION_CHEAT_ENTER_NEXT_LEVEL,      alt(KeyCode.N)),
        new ActionBinding(ACTION_CHEAT_KILL_GHOSTS,           alt(KeyCode.X)),
        new ActionBinding(ACTION_ENTER_FULLSCREEN,            nude(KeyCode.F11)),
        new ActionBinding(ACTION_OPEN_EDITOR,                 alt_shift(KeyCode.E)),
        new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,        alt(KeyCode.LEFT)),
        new ActionBinding(ACTION_PERSPECTIVE_NEXT,            alt(KeyCode.RIGHT)),
        new ActionBinding(ACTION_SHOW_HELP,                   nude(KeyCode.H)),
        new ActionBinding(ACTION_STEER_UP,                    nude(KeyCode.UP), control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,                  nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,                  nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT,                 nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        new ActionBinding(ACTION_QUIT_GAME_SCENE,             nude(KeyCode.Q)),
        new ActionBinding(ACTION_SIMULATION_SLOWER,           alt(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_FASTER,           alt(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_RESET,            alt(KeyCode.DIGIT0)),
        new ActionBinding(ACTION_SIMULATION_ONE_STEP,         shift(KeyCode.P), shift(KeyCode.F5)),
        new ActionBinding(ACTION_SIMULATION_TEN_STEPS,        shift(KeyCode.SPACE)),
        new ActionBinding(ACTION_TEST_CUT_SCENES,             alt(KeyCode.C)),
        new ActionBinding(ACTION_TEST_LEVELS_BONI,            alt(KeyCode.T)),
        new ActionBinding(ACTION_TEST_LEVELS_TEASERS,         alt_shift(KeyCode.T)),
        new ActionBinding(ACTION_TOGGLE_AUTOPILOT,            alt(KeyCode.A)),
        new ActionBinding(ACTION_TOGGLE_DEBUG_INFO,           alt(KeyCode.D)),
        new ActionBinding(ACTION_TOGGLE_MUTED,                alt(KeyCode.M)),
        new ActionBinding(ACTION_TOGGLE_PAUSED,               nude(KeyCode.P), nude(KeyCode.F5)),
        new ActionBinding(ACTION_TOGGLE_DASHBOARD,            nude(KeyCode.F1), alt(KeyCode.B)),
        new ActionBinding(ACTION_TOGGLE_IMMUNITY,             alt(KeyCode.I)),
        new ActionBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY, nude(KeyCode.F2)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D,     alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        new ActionBinding(ACTION_TOGGLE_DRAW_MODE,            alt(KeyCode.W))
    );

    private final PacManGames_Assets theAssets;
    private final DirectoryWatchdog  theCustomDirWatchdog;
    private final GameClock          theGameClock;
    private final GameContext        theGameContext;
    private final Joypad             theJoypad;
    private final Keyboard           theKeyboard;
    private final Stage              theStage;
    private final UIPreferences      theUIPrefs;

    private final ActionBindingsManager globalActionBindings = new DefaultActionBindingsManager();
    private final Map<String, GameUI_Config> configByGameVariant = new HashMap<>();
    private final MainScene mainScene;

    // These are lazily created
    private StartPagesView startPagesView;
    private PlayView playView;
    private EditorView editorView;

    public PacManGames_UI_Impl(Map<String, Class<?>> configurationMap, GameContext gameContext, Stage stage, double width, double height) {
        requireNonNull(configurationMap, "UI configuration map is null");
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

        configurationMap.forEach(this::applyConfiguration);
        initGlobalActionBindings();

        mainScene = new MainScene(this, width, height);
        configureMainScene();
        configureStage(stage);

        theGameClock.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        theGameClock.setPermanentAction(this::drawCurrentView);

        PROPERTY_3D_WALL_HEIGHT.set(theUIPrefs.getFloat("3d.obstacle.base_height"));
        PROPERTY_3D_WALL_OPACITY.set(theUIPrefs.getFloat("3d.obstacle.opacity"));
    }

    private void applyConfiguration(String gameVariant, Class<?> configClass) {
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

    private void configureMainScene() {
        mainScene.currentGameSceneProperty().bindBidirectional(PROPERTY_CURRENT_GAME_SCENE);
        mainScene.currentViewProperty().bindBidirectional(PROPERTY_CURRENT_VIEW);

        // Check if a global action is defined for the key press, otherwise let the current view handle it.
        mainScene.setOnKeyPressed(e -> {
            GameAction matchingAction = globalActionBindings.matchingAction(theKeyboard).orElse(null);
            if (matchingAction != null) {
                matchingAction.executeIfEnabled(this);
            } else {
                currentView().handleKeyboardInput(this);
            }
        });

        mainScene.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> theAssets.get(isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D) ? "background.play_scene3d" : "background.scene"),
            PROPERTY_CURRENT_VIEW, PROPERTY_CURRENT_GAME_SCENE
        ));

        // Show paused icon only in play view
        mainScene.pausedIcon().visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() == thePlayView() && theGameClock.isPaused(),
            PROPERTY_CURRENT_VIEW, theGameClock.pausedProperty())
        );

        // hide icon box if editor view is active, avoid creation of editor view in binding expression!
        StatusIconBox statusIcons = mainScene.statusIconBox();
        statusIcons.visibleProperty().bind(PROPERTY_CURRENT_VIEW
            .map(currentView -> theEditorView().isEmpty() || currentView != theEditorView().get()));

        statusIcons.iconMuted()    .visibleProperty().bind(PROPERTY_MUTED);
        statusIcons.icon3D()       .visibleProperty().bind(PROPERTY_3D_ENABLED);
        statusIcons.iconAutopilot().visibleProperty().bind(theGameContext().theGameController().propertyUsingAutopilot());
        statusIcons.iconImmune()   .visibleProperty().bind(theGameContext().theGameController().propertyImmunity());
    }

    private void configureStage(Stage stage) {
        stage.setScene(mainScene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
        stage.titleProperty().bind(createStringBinding(
            this::computeTitle,
            PROPERTY_CURRENT_VIEW,
            PROPERTY_CURRENT_GAME_SCENE,
            PROPERTY_DEBUG_INFO_VISIBLE,
            PROPERTY_3D_ENABLED,
            theGameClock().pausedProperty(),
            mainScene.heightProperty()
        ));
    }

    // Asset key regex: app.title.(ms_pacman|ms_pacman_xxl|pacman,pacman_xxl|tengen)(.paused)?
    private String computeTitle() {
        var currentView = PROPERTY_CURRENT_VIEW.get();
        if (currentView == null) {
            return "No View?";
        }
        if (currentView.title().isPresent()) {
            return currentView.title().get().get();
        }

        boolean mode3D = PROPERTY_3D_ENABLED.get();
        boolean modeDebug = PROPERTY_DEBUG_INFO_VISIBLE.get();
        String namespace      = theConfiguration().assetNamespace();
        String paused         = theGameClock().isPaused() ? ".paused" : "";
        String assetKey       = "app.title.%s%s".formatted(namespace, paused);
        String translatedMode = theAssets().text(mode3D ? "threeD" : "twoD");
        String shortTitle     = theAssets().text(assetKey, translatedMode);

        var currentGameScene = currentGameScene().orElse(null);
        if (currentGameScene == null || !modeDebug) {
            return shortTitle;
        }
        String sceneClassName = currentGameScene.getClass().getSimpleName();
        return currentGameScene instanceof GameScene2D gameScene2D
            ? shortTitle + " [%s]".formatted(sceneClassName) + " (%.2fx)".formatted(gameScene2D.scaling())
            : shortTitle + " [%s]".formatted(sceneClassName);
    }

    private void initGlobalActionBindings() {
        globalActionBindings.useFirst(ACTION_ENTER_FULLSCREEN, defaultActionBindings);
        globalActionBindings.useFirst(ACTION_OPEN_EDITOR, defaultActionBindings);
        globalActionBindings.useFirst(ACTION_TOGGLE_MUTED, defaultActionBindings);
        globalActionBindings.updateKeyboard(theKeyboard);
    }

    private void selectView(GameUI_View view) {
        requireNonNull(view);
        final GameUI_View oldView = mainScene.currentView();
        if (oldView == view) {
            return;
        }
        if (oldView != null) {
            oldView.actionBindingsManager().removeFromKeyboard(theKeyboard);
            theGameContext.theGameEventManager().removeEventListener(oldView);
        }
        view.actionBindingsManager().updateKeyboard(theKeyboard);
        theGameContext.theGameEventManager().addEventListener(view);

        PROPERTY_CURRENT_VIEW.set(view);
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
            if (currentView() == thePlayView()) {
                thePlayView().draw();
            }
            mainScene.flashMessageLayer().update();
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private EditorView ensureEditorViewExists() {
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

    @Override public Optional<GameScene>         currentGameScene() { return mainScene.currentGameScene(); }
    @Override public GameUI_View currentView() { return mainScene.currentView(); }

    @Override public PacManGames_Assets          theAssets() {return theAssets; }
    @SuppressWarnings("unchecked")
    @Override public <T extends GameUI_Config> T theConfiguration() { return (T) config(theGameContext.theGameController().selectedGameVariant()); }
    @Override public DirectoryWatchdog           theCustomDirWatchdog() { return theCustomDirWatchdog; }
    @Override public GameClock                   theGameClock() { return theGameClock; }
    @Override public GameContext                 theGameContext() { return theGameContext; }
    @Override public Joypad                      theJoypad() { return theJoypad; }
    @Override public Keyboard                    theKeyboard() { return theKeyboard; }
    @Override public SoundManager                theSound() { return theConfiguration().soundManager(); }
    @Override public Stage                       theStage() { return theStage; }
    @Override public UIPreferences               theUIPrefs() { return theUIPrefs; }

    @Override
    public Optional<EditorView> theEditorView() {
        return Optional.ofNullable(editorView);
    }

    @Override public PlayView thePlayView() {
        if (playView == null) {
            playView = new PlayView(this, mainScene);
        }
        return playView;
    }

    @Override public StartPagesView theStartPagesView() {
        if (startPagesView == null) {
            startPagesView = new StartPagesView(this);
        }
        return startPagesView;
    }

    @Override
    public boolean isCurrentGameSceneID(String id) {
        GameScene currentGameScene = mainScene.currentGameScene().orElse(null);
        return currentGameScene != null && theConfiguration().gameSceneHasID(currentGameScene, id);
    }

    @Override
    public void quitCurrentGameScene() {
        currentGameScene().ifPresent(gameScene -> {
            gameScene.end();
            theGameContext.theGameController().changeGameState(GameState.BOOT);
            theGameContext.theGame().resetEverything();
            if (!theGameContext.theCoinMechanism().isEmpty()) {
                theGameContext.theCoinMechanism().consumeCoin();
            }
            Logger.info("Current game scene ({}) has been quit, returning to start view", gameScene.getClass().getSimpleName());
            showStartView();
        });
    }

    @Override
    public void restart() {
        theGameClock.stop();
        theGameClock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
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
        if (gameVariant.equals(previousVariant)) {
            return;
        }

        if (previousVariant != null) {
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

        thePlayView().canvasFrame().roundedBorderProperty().set(newConfig.hasGameCanvasRoundedBorder());

        // this triggers a game event and the event handlers:
        theGameContext.theGameController().selectGameVariant(gameVariant);
    }

    @Override
    public void show() {
        thePlayView().initDashboard();
        theStartPagesView().selectStartPage(0);
        showStartView();
        theStage.centerOnScreen();
        theStage.show();
        Platform.runLater(theCustomDirWatchdog::startWatching);
        theGameContext.theGameController().setEventsEnabled(true);
    }

    @Override
    public void showEditorView() {
        if (!theGameContext.theGame().isPlaying() || theGameClock.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            theSound().stopAll();
            theGameClock.stop();
            ensureEditorViewExists().editor().start(theStage);
            selectView(editorView);
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
        selectView(thePlayView());
    }

    @Override
    public void showStartView() {
        theGameClock.stop();
        theGameClock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theSound().stopAll();
        selectView(theStartPagesView());
        theStartPagesView().currentStartPage().ifPresent(startPage -> Platform.runLater(() -> {
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
    public void updateGameScene(boolean reloadCurrent) {
        thePlayView().updateGameScene(reloadCurrent);
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

    @Override
    public List<ActionBinding> actionBindings() {
        return defaultActionBindings;
    }
}