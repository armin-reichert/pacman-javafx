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
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.GameView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.ui.sound.PacManGames_Sound;
import de.amr.pacmanfx.uilib.GameClock;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames.theKeyboard;
import static java.util.Objects.requireNonNull;

/**
 * User interface for all Pac-Man game variants.
 */
public class PacManGames_UI_Impl implements PacManGames_UI {

    // package-private access for games env API
    static final PacManGames_Assets ASSETS = new PacManGames_Assets();
    static final GameClock GAME_CLOCK = new GameClock();
    static final PacManGames_Sound SOUND_MANAGER = new PacManGames_Sound();
    static final DirectoryWatchdog WATCHDOG = new DirectoryWatchdog(CUSTOM_MAP_DIR);

    private final Map<String, PacManGames_UIConfig> configByGameVariant = new HashMap<>();

    private final ObjectProperty<PacManGames_View> currentViewPy      = new SimpleObjectProperty<>();
    private final ObjectProperty<GameScene>        currentGameScenePy = new SimpleObjectProperty<>();

    private Stage stage;
    private Scene mainScene;
    private StartPagesView startPagesView;
    private GameView gameView;
    private EditorView editorView; // created on first access

    public void build(Stage stage, double width, double height, DashboardID... dashboardIDs) {
        this.stage = requireNonNull(stage);
        stage.setMinWidth(280);
        stage.setMinHeight(360);

        var root = new StackPane(new Pane()); // placeholder for root of current view

        // Status and "paused" icon
        {
            var iconBox = new StatusIconBox(this);
            StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);

            var iconPaused = FontIcon.of(FontAwesomeSolid.PAUSE, 80, STATUS_ICON_COLOR);
            iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> currentView() == gameView() && GAME_CLOCK.isPaused(),
                currentViewProperty(), GAME_CLOCK.pausedProperty()));
            StackPane.setAlignment(iconPaused, Pos.CENTER);

            root.getChildren().addAll(iconPaused, iconBox);
        }

        // Main scene
        {
            mainScene = new Scene(root, width, height);
            mainScene.widthProperty() .addListener((py,ov,nv) -> gameView.resize(mainScene));
            mainScene.heightProperty().addListener((py,ov,nv) -> gameView.resize(mainScene));
            mainScene.addEventFilter(KeyEvent.KEY_PRESSED, theKeyboard()::onKeyPressed);
            mainScene.addEventFilter(KeyEvent.KEY_RELEASED, theKeyboard()::onKeyReleased);
            mainScene.setOnKeyPressed(e -> {
                if (KEY_FULLSCREEN.match(e)) {
                    PacManGames_GameActions.ACTION_ENTER_FULLSCREEN.execute(this);
                }
                else if (KEY_MUTE.match(e)) {
                    PacManGames_GameActions.ACTION_TOGGLE_MUTED.execute(this);
                }
                else if (KEY_OPEN_EDITOR.match(e)) {
                    showEditorView();
                }
                else {
                    currentView().handleKeyboardInput();
                }
            });
        }
        stage.setScene(mainScene);

        // Start pages view
        startPagesView = new StartPagesView(this);
        startPagesView.setBackground(theAssets().background("background.scene"));

        // Game view (includes dashboard)
        gameView = new GameView(this, mainScene, dashboardIDs);

        GAME_CLOCK.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        GAME_CLOCK.setPermanentAction(this::drawGameView);

        currentViewPy.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));
        root.backgroundProperty().bind(currentGameSceneProperty().map(
            gameScene -> currentGameSceneIsPlayScene3D()
                ? theAssets().get("background.play_scene3d")
                : theAssets().get("background.scene"))
        );
    }

    public void configure(Map<String, Class<? extends PacManGames_UIConfig>> configClassesMap) {
        configClassesMap.forEach((gameVariant, configClass) -> {
            try {
                PacManGames_UIConfig config = configClass.getDeclaredConstructor(PacManGames_Assets.class).newInstance(theAssets());
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

    public Stage stage() {
        return stage;
    }

    /**
     * @param x cause of catastrophe
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

    private void handleViewChange(PacManGames_View oldView, PacManGames_View newView) {
        if (oldView != null) {
            oldView.deleteActionBindings();
            theGameEventManager().removeEventListener(oldView);
        }
        newView.updateActionBindings();
        newView.container().requestFocus();
        stage.titleProperty().bind(newView.titleBinding());
        theGameEventManager().addEventListener(newView);
        var root = (StackPane) mainScene.getRoot();
        root.getChildren().set(0, newView.container());
    }

    private EditorView lazyGetEditorView() {
        if (editorView == null) {
            var editor = new TileMapEditor(stage);
            var miQuit = new MenuItem(theAssets().text("back_to_game"));
            miQuit.setOnAction(e -> {
                editor.stop();
                editor.executeWithCheckForUnsavedChanges(this::showStartView);
            });
            editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miQuit);
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
        return currentGameScenePy;
    }

    @Override
    public void setCurrentGameScene(GameScene gameScene) {
        currentGameScenePy.set(gameScene);
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(currentGameScenePy.get());
    }

    @Override
    public ObjectProperty<PacManGames_View> currentViewProperty() {
        return currentViewPy;
    }

    @Override
    public PacManGames_View currentView() {
        return currentViewPy.get();
    }

    @Override
    public GameView gameView() {
        return gameView;
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
        PacManGames_UIConfig uiConfig = configuration(gameVariant);
        SOUND_MANAGER.selectGameVariant(gameVariant, uiConfig.assetNamespace());
        Image appIcon = ASSETS.image(uiConfig.assetNamespace() + ".app_icon");
        if (appIcon != null) {
            stage.getIcons().setAll(appIcon);
        } else {
            Logger.error("Could not find app icon for current game variant {}", gameVariant);
        }
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
        // this triggers a game event and the event handlers:
        theGameController().selectGameVariant(gameVariant);
    }

    @Override
    public void show() {
        currentViewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
        stage.centerOnScreen();
        stage.show();
        WATCHDOG.startWatching();
    }

    @Override
    public void showEditorView() {
        if (!theGame().isPlaying() || GAME_CLOCK.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            SOUND_MANAGER.stopAll();
            GAME_CLOCK.stop();
            lazyGetEditorView().editor().start(stage);
            currentViewPy.set(lazyGetEditorView());
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
        currentViewPy.set(gameView);
    }

    @Override
    public void showStartView() {
        GAME_CLOCK.stop();
        GAME_CLOCK.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        SOUND_MANAGER.stopAll();
        gameView.setDashboardVisible(false);
        currentViewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(startPage -> startPage.layoutRoot().requestFocus());
    }

    @Override
    public StartPagesView startPagesView() { return startPagesView; }

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