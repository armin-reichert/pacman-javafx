/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.GameView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_ENTER_FULLSCREEN;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_MUTED;
import static java.util.Objects.requireNonNull;

/**
 * User interface for all Pac-Man game variants.
 */
public class PacManGames_UI_Impl implements PacManGames_UI {

    static final PacManGames_Assets ASSETS = new PacManGames_Assets();
    static final GameClock          GAME_CLOCK = new GameClock();
    static final Keyboard           KEYBOARD = new Keyboard();
    static final Joypad             JOYPAD = new Joypad(KEYBOARD);
    static       DirectoryWatchdog  WATCHDOG;
    static       PacManGames_UI_Impl THE_ONE;

    private final Map<String, PacManGames_UIConfig> configByGameVariant = new HashMap<>();

    private final ObjectProperty<PacManGames_View> currentViewProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<GameScene> currentGameSceneProperty   = new SimpleObjectProperty<>();
    private final BooleanProperty mutedProperty                        = new SimpleBooleanProperty(false);

    private final Model3DRepository model3DRepository = new Model3DRepository();

    private final StackPane rootPane = new StackPane();
    private final Stage stage;
    private final StartPagesView startPagesView;
    private final GameView gameView;
    private       EditorView editorView; // created on first access
    private final StatusIconBox iconBox;
    private final FontIcon iconPaused;

    public PacManGames_UI_Impl(Stage stage, double width, double height) {
        this.stage = requireNonNull(stage);

        Scene mainScene = new Scene(rootPane, width, height);
        stage.setScene(mainScene);

        stage.setMinWidth(280);
        stage.setMinHeight(360);

        startPagesView = new StartPagesView(this);
        startPagesView.setBackground(theAssets().background("background.scene"));
        gameView = new GameView(this, mainScene);

        rootPane.getChildren().add(startPagesView.rootNode());

        // "paused" icon appears on center of game view
        iconPaused = FontIcon.of(FontAwesomeSolid.PAUSE, 80, STATUS_ICON_COLOR);
        iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() == gameView && GAME_CLOCK.isPaused(),
            currentViewProperty, GAME_CLOCK.pausedProperty()));
        StackPane.setAlignment(iconPaused, Pos.CENTER);

        // status icon box appears at bottom-left corner of any view except editor
        iconBox = new StatusIconBox(this);
        StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);

        rootPane.getChildren().addAll(iconPaused, iconBox);

        GAME_CLOCK.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        GAME_CLOCK.setPermanentAction(this::drawGameView);

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, theKeyboard()::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, theKeyboard()::onKeyReleased);

        //TODO should I use key binding for global actions too?
        mainScene.setOnKeyPressed(e -> {
            if (KEY_FULLSCREEN.match(e)) {
                ACTION_ENTER_FULLSCREEN.execute(this);
            }
            else if (KEY_MUTE.match(e)) {
                ACTION_TOGGLE_MUTED.execute(this);
            }
            else if (KEY_OPEN_EDITOR.match(e)) {
                showEditorView();
            }
            else {
                currentView().handleKeyboardInput();
            }
        });
        currentViewProperty.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));

        rootPane.backgroundProperty().bind(currentGameSceneProperty().map(gameScene ->
            currentGameSceneIsPlayScene3D()
                ? theAssets().get("background.play_scene3d")
                : theAssets().get("background.scene"))
        );
    }

    public void configure(Map<String, Class<? extends PacManGames_UIConfig>> configClassesMap) {
        configClassesMap.forEach((gameVariant, configClass) -> {
            try {
                PacManGames_UIConfig config = configClass.getDeclaredConstructor().newInstance();
                setConfiguration(gameVariant, config);
            } catch (Exception x) {
                Logger.error("Could not create UI configuration of class {}", configClass);
                throw new IllegalStateException(x);
            }
        });
        configByGameVariant.forEach((gameVariant, config) -> {
            config.createGameScenes();
            config.gameScenes().forEach(scene -> {
                if (scene instanceof GameScene2D gameScene2D) {
                    gameScene2D.debugInfoVisibleProperty().bind(PY_DEBUG_INFO_VISIBLE);
                }
            });
            Logger.info("Game scenes for game variant {} created", gameVariant);
        });
    }

    private void handleViewChange(PacManGames_View oldView, PacManGames_View newView) {
        requireNonNull(newView);
        if (oldView != null) {
            oldView.clearActionBindings();
            theGameEventManager().removeEventListener(oldView);
        }
        newView.updateActionBindings();
        newView.rootNode().requestFocus();
        stage.titleProperty().bind(newView.title());
        theGameEventManager().addEventListener(newView);

        rootPane.getChildren().set(0, newView.rootNode());
    }

    /**
     * @param x what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Here.</a>
     */
    private void ka_tas_trooo_phe(Throwable x) {
        Logger.error(x);
        Logger.error("SOMETHING VERY BAD HAPPENED DURING SIMULATION STEP!");
        showFlashMessageSec(10, "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!");
    }

    private void doSimulationStepAndUpdateGameScene() {
        try {
            theSimulationStep().start(GAME_CLOCK.tickCount());
            theGameController().updateGameState();
            theSimulationStep().log();
            currentGameScene().ifPresent(GameScene::update);
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private void drawGameView() {
        try {
            gameView.draw();
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private EditorView getEditorViewCreateIfNeeded() {
        if (editorView == null) {
            var editor = new TileMapEditor(stage, model3DRepository);
            var miReturnToGame = new MenuItem(theAssets().text("back_to_game"));
            miReturnToGame.setOnAction(e -> {
                editor.stop();
                editor.executeWithCheckForUnsavedChanges(this::showStartView);
            });
            editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miReturnToGame);
            editor.init(CUSTOM_MAP_DIR);
            editorView = new EditorView(editor);
        }
        return editorView;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return currentGameSceneProperty;
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(currentGameSceneProperty.get());
    }

    @Override
    public ObjectProperty<PacManGames_View> currentViewProperty() {
        return currentViewProperty;
    }

    @Override
    public PacManGames_View currentView() {
        return currentViewProperty.get();
    }

    @Override
    public GameView gameView() {
        return gameView;
    }

    @Override
    public Model3DRepository model3DRepository() {
        return model3DRepository;
    }

    @Override
    public BooleanProperty mutedProperty() {
        return mutedProperty;
    }

    @Override
    public void restart() {
        GAME_CLOCK.stop();
        GAME_CLOCK.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        GAME_CLOCK.pausedProperty().set(false);
        GAME_CLOCK.start();
        theGameController().restart(GameState.BOOT);
    }

    @Override
    public void selectGameVariant(String gameVariant) {
        if (gameVariant == null) {
            Logger.error("Cannot select game variant (NULL)");
            return;
        }
        String previousVariant = theGameController().selectedGameVariant();
        if (previousVariant != null && !previousVariant.equals(gameVariant)) {
            PacManGames_UIConfig previousConfig = configuration(previousVariant);
            Logger.info("Unloading assets for game variant {}", previousVariant);
            previousConfig.unloadAssets(theAssets());
            Logger.info(theAssets().summary(Map.of(
                Image.class, "Images",
                AudioClip.class, "Sounds")
            ));
            previousConfig.soundManager().mutedProperty().unbind();
        }

        PacManGames_UIConfig newConfig = configuration(gameVariant);
        Logger.info("Loading assets for game variant {}", gameVariant);
        newConfig.loadAssets(theAssets());
        Logger.info(theAssets().summary(Map.of(
            Image.class, "Images",
            AudioClip.class, "Sounds")
        ));
        newConfig.soundManager().mutedProperty().bind(mutedProperty);

        Image appIcon = ASSETS.image(newConfig.assetNamespace() + ".app_icon");
        if (appIcon != null) {
            stage.getIcons().setAll(appIcon);
        } else {
            Logger.error("Could not find app icon for current game variant {}", gameVariant);
        }

        gameView.canvasContainer().roundedBorderProperty().set(newConfig.hasGameCanvasRoundedBorder());

        // this triggers a game event and the event handlers:
        theGameController().selectGameVariant(gameVariant);
    }

    @Override
    public void show() {
        currentViewProperty.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
        gameView.dashboard().init();

        iconBox.iconMuted().visibleProperty().bind(mutedProperty());
        iconBox.icon3D().visibleProperty().bind(PY_3D_ENABLED);
        iconBox.iconAutopilot().visibleProperty().bind(PY_USING_AUTOPILOT);
        iconBox.iconImmune().visibleProperty().bind(PY_IMMUNITY);

        stage.centerOnScreen();
        stage.show();
        WATCHDOG.startWatching();
    }

    @Override
    public void showEditorView() {
        if (!theGame().isPlaying() || GAME_CLOCK.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            theSound().stopAll();
            GAME_CLOCK.stop();
            getEditorViewCreateIfNeeded().editor().start(stage);
            currentViewProperty.set(getEditorViewCreateIfNeeded());
        } else {
            Logger.info("Editor view cannot be opened, game is playing");
        }
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        gameView.flashMessageLayer().showMessage(String.format(message, args), seconds);
    }

    @Override
    public void showGameView() {
        currentViewProperty.set(gameView);
    }

    @Override
    public void showStartView() {
        GAME_CLOCK.stop();
        GAME_CLOCK.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theSound().stopAll();
        gameView.dashboard().setVisible(false);
        currentViewProperty.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public StartPagesView startPagesView() { return startPagesView; }

    @Override
    public void terminateApp() {
        Platform.exit();
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
        gameView.updateGameScene(reloadCurrent);
    }

    // UI configuration

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param configuration the UI configuration for this variant
     */
    @Override
    public void setConfiguration(String variant, PacManGames_UIConfig configuration) {
        requireNonNull(variant);
        requireNonNull(configuration);
        configByGameVariant.put(variant, configuration);
    }

    @Override
    public PacManGames_UIConfig configuration(String gameVariant) {
        return configByGameVariant.get(gameVariant);
    }

    @Override
    public PacManGames_UIConfig configuration() {
        return configByGameVariant.get(theGameController().selectedGameVariant());
    }

    @Override
    public boolean currentGameSceneIsPlayScene2D() {
        GameScene currentGameScene = currentGameScene().orElse(null);
        return currentGameScene != null && configuration().gameSceneHasID(currentGameScene, "PlayScene2D");
    }

    @Override
    public boolean currentGameSceneIsPlayScene3D() {
        GameScene currentGameScene = currentGameScene().orElse(null);
        return currentGameScene != null && configuration().gameSceneHasID(currentGameScene, "PlayScene3D");
    }
}