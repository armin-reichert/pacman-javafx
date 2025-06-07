/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.uilib.GameScene;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static java.util.Objects.requireNonNull;

/**
 * User interface for all Pac-Man game variants.
 */
public class PacManGames_UI_Impl implements PacManGames_UI {

    private final ObjectProperty<PacManGames_View> viewPy = new SimpleObjectProperty<>();
    private final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>();

    private Stage stage;
    private Scene mainScene;
    private StartPagesView startPagesView;
    private GameView gameView;
    private EditorView editorView;

    private void doSimulationStepAndUpdateGameScene() {
        try {
            theSimulationStep().init(theClock().tickCount());
            theGameController().updateGameState();
            theSimulationStep().log();
            currentView().update();
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("SOMETHING VERY BAD HAPPENED DURING SIMULATION STEP!");
            showFlashMessageSec(10, "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!");
        }
    }

    private void drawCurrentView() {
        try {
            currentView().draw();
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("SOMETHING VERY BAD HAPPENED DURING SIMULATION STEP!");
            showFlashMessageSec(10, "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!");
        }
    }

    private void handleViewChange(PacManGames_View oldView, PacManGames_View newView) {
        if (oldView != null) {
            oldView.deleteActionBindings();
            theGameEventManager().removeEventListener(oldView);
        }
        newView.updateActionBindings();
        newView.layoutRoot().requestFocus();
        stage.titleProperty().bind(newView.title());
        theGameEventManager().addEventListener(newView);
        var root = (StackPane) mainScene.getRoot();
        root.getChildren().set(0, newView.layoutRoot());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void addStartPage(StartPage startPage) {
        startPagesView.addStartPage(requireNonNull(startPage));
    }

    @Override
    public void buildUI(Stage stage, double width, double height, DashboardID... dashboardIDs) {
        this.stage = requireNonNull(stage);
        stage.setMinWidth(280);
        stage.setMinHeight(360);

        var root = new StackPane(new Pane()); // placeholder for root of current view

        // Status icons
        {
            var iconMuted = FontIcon.of(FontAwesomeSolid.DEAF, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
            iconMuted.visibleProperty().bind(theSound().mutedProperty());

            var icon3D = FontIcon.of(FontAwesomeSolid.CUBES, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
            icon3D.visibleProperty().bind(PY_3D_ENABLED);

            var iconAutopilot = FontIcon.of(FontAwesomeSolid.TAXI, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
            iconAutopilot.visibleProperty().bind(PY_USING_AUTOPILOT);

            var iconImmune = FontIcon.of(FontAwesomeSolid.USER_SECRET, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
            iconImmune.visibleProperty().bind(PY_IMMUNITY);

            var iconPaused = FontIcon.of(FontAwesomeSolid.PAUSE, 80, STATUS_ICON_COLOR);
            iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> currentView() != editorView && theClock().isPaused(),
                viewPy, theClock().pausedProperty()));

            final FontIcon[] icons = {iconMuted, icon3D, iconAutopilot, iconImmune};
            final HBox iconBox = new HBox(icons);
            iconBox.setMinHeight(STATUS_ICON_SIZE + STATUS_ICON_PADDING);
            iconBox.setMaxHeight(STATUS_ICON_SIZE + STATUS_ICON_PADDING);
            iconBox.setPadding(new Insets(STATUS_ICON_PADDING));
            iconBox.setSpacing(STATUS_ICON_SPACING);
            iconBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> currentView() != editorView, viewPy));
            // keep box compact, show only visible items
            for (FontIcon icon : icons) {
                icon.visibleProperty().addListener(
                    (py, ov, nv) -> iconBox.getChildren().setAll(Arrays.stream(icons).filter(FontIcon::isVisible).toList()));
            }

            root.getChildren().addAll(iconPaused, iconBox);
            StackPane.setAlignment(iconPaused, Pos.CENTER);
            StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);
        }

        // Map editor
        {
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
        {
            startPagesView = new StartPagesView();
            startPagesView.setBackground(theAssets().background("background.scene"));
        }

        // Game view and dashboard
        {
            gameView = new GameView(mainScene);
            gameScenePy.bind(gameView.gameSceneProperty());
            gameView.bindTitle(Bindings.createStringBinding(
                () -> computeTitleText(currentGameScene().orElse(null), PY_3D_ENABLED.get(), PY_DEBUG_INFO_VISIBLE.get()),
                theClock().pausedProperty(), mainScene.heightProperty(), gameSceneProperty(),
                PY_3D_ENABLED, PY_DEBUG_INFO_VISIBLE));

            if (dashboardIDs.length > 0) {
                gameView.dashboard().addInfoBox(DashboardID.README);
                for (DashboardID id : dashboardIDs) {
                    if (id != DashboardID.README) {
                        gameView.dashboard().addInfoBox(id);
                    }
                }
            }
        }

        theClock().setPausableAction(this::doSimulationStepAndUpdateGameScene);
        theClock().setPermanentAction(this::drawCurrentView);

        viewPy.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));
        root.backgroundProperty().bind(gameSceneProperty().map(
            gameScene -> theUIConfig().currentGameSceneIsPlayScene3D()
                ? theAssets().get("background.play_scene3d")
                : theAssets().get("background.scene"))
        );
    }

    // Asset key regex: app.title.(ms_pacman|ms_pacman_xxl|pacman,pacman_xxl|tengen)(.paused)?
    private static String computeTitleText(GameScene currentGameScene, boolean threeDModeEnabled, boolean modeDebug) {
        String ans = theUIConfig().current().assetNamespace();
        String paused = theClock().isPaused() ? ".paused" : "";
        String key = "app.title." + ans + paused;
        String modeText = theAssets().text(threeDModeEnabled ? "threeD" : "twoD");
        if (currentGameScene == null || !modeDebug) {
            return theAssets().text(key, modeText);
        }
        String sceneClassName = currentGameScene.getClass().getSimpleName();
        if (currentGameScene instanceof GameScene2D gameScene2D) {
            return theAssets().text(key, modeText)
                + " [%s]".formatted(sceneClassName)
                + " (%.2fx)".formatted(gameScene2D.scaling());
        }
        return theAssets().text(key, modeText) + " [%s]".formatted(sceneClassName);
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return gameView.currentGameScene();
    }

    @Override
    public PacManGames_View currentView() {
        return viewPy.get();
    }

    @Override
    public Dashboard dashboard() {
        return gameView.dashboard();
    }

    @Override
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    @Override
    public Scene mainScene() {
        return mainScene;
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
    public void selectStartPage(int index) {
        startPagesView.selectStartPage(index);
    }

    @Override
    public void selectGameVariant(GameVariant gameVariant) {
        if (gameVariant == null) {
            Logger.error("Cannot select game variant (NULL)");
            return;
        }
        PacManGames_UIConfiguration uiConfig = theUIConfig().configuration(gameVariant);
        theSound().selectGameVariant(gameVariant, uiConfig.assetNamespace());
        stage.getIcons().setAll(uiConfig.appIcon());
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
        // this triggers a game event and calling the event handlers:
        theGameController().selectGameVariant(gameVariant);
    }

    @Override
    public void show() {
        selectGameVariant(theGameVariant());
        viewPy.set(startPagesView);
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
            editorView.editor().start(stage);
            viewPy.set(editorView);
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
        gameView.resize(mainScene);
        viewPy.set(gameView);
    }

    @Override
    public void showStartView() {
        theClock().stop();
        theClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theSound().stopAll();
        gameView.setDashboardVisible(false);
        viewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(StartPage::requestFocus);
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
        gameView.updateGameScene(reloadCurrent);
    }
}