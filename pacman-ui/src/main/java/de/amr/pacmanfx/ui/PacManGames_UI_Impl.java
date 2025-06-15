/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static java.util.Objects.requireNonNull;

/**
 * User interface for all Pac-Man game variants.
 */
public class PacManGames_UI_Impl implements PacManGames_UI {

    private final Map<String, PacManGames_UIConfig> configByGameVariant = new HashMap<>();

    private final ObjectProperty<PacManGames_View> currentViewPy      = new SimpleObjectProperty<>();
    private final ObjectProperty<GameScene>        currentGameScenePy = new SimpleObjectProperty<>();

    private Stage stage;
    private Scene mainScene;
    private StartPagesView startPagesView;
    private GameView gameView;
    private EditorView editorView; // created on first access

    public PacManGames_UI_Impl(Map<String, Class<? extends PacManGames_UIConfig>> configClassesMap) {
        configClassesMap.forEach((gameVariant, configClass) -> {
            try {
                PacManGames_UIConfig config = configClass.getDeclaredConstructor(PacManGames_Assets.class).newInstance(theAssets());
                setConfiguration(gameVariant, config);
                Logger.info("Game variant {} uses UI configuration: {}", gameVariant, config);
            } catch (Exception x) {
                Logger.error("Could not create UI configuration of class {}", configClass);
                throw new IllegalStateException(x);
            }
        });
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
            theSimulationStep().start(theClock().tickCount());
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
    public void buildUI(Stage stage, double width, double height, DashboardID... dashboardIDs) {
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
                () -> currentView() == gameView() && theClock().isPaused(),
                currentViewProperty(), theClock().pausedProperty()));
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
                    stage.setFullScreen(true);
                }
                else if (KEY_MUTE.match(e)) {
                    theSound().toggleMuted();
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

        theClock().setPausableAction(this::doSimulationStepAndUpdateGameScene);
        theClock().setPermanentAction(this::drawGameView);

        currentViewPy.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));
        root.backgroundProperty().bind(currentGameSceneProperty().map(
            gameScene -> currentGameSceneIsPlayScene3D()
                ? theAssets().get("background.play_scene3d")
                : theAssets().get("background.scene"))
        );
    }

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
        theClock().stop();
        theClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theClock().pausedProperty().set(false);
        theClock().start();
        theGameController().restart(GameState.BOOT);
    }

    @Override
    public void selectGameVariant(String gameVariant) {
        if (gameVariant == null) {
            Logger.error("Cannot select game variant (NULL)");
            return;
        }
        PacManGames_UIConfig uiConfig = configuration(gameVariant);
        theSound().selectGameVariant(gameVariant, uiConfig.assetNamespace());
        stage.getIcons().setAll(uiConfig.appIcon());
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
        // this triggers a game event and the event handlers:
        theGameController().selectGameVariant(gameVariant);
    }

    @Override
    public void show() {
        currentViewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(StartPage::requestFocus);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showEditorView() {
        if (!theGame().isPlaying() || theClock().isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            theSound().stopAll();
            theClock().stop();
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
        theClock().stop();
        theClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theSound().stopAll();
        gameView.setDashboardVisible(false);
        currentViewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(StartPage::requestFocus);
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
        configuration.gameScenes().forEach(scene -> {
            if (scene instanceof GameScene2D gameScene2D) {
                gameScene2D.debugInfoVisibleProperty().bind(PY_DEBUG_INFO_VISIBLE);
            }
        });
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