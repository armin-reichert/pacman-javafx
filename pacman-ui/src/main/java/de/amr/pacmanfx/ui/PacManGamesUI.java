/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static java.util.Objects.requireNonNull;

/**
 * User interface for all Pac-Man game variants.
 *
 * @author Armin Reichert
 */
public class PacManGamesUI implements GameUI {

    private final ObjectProperty<View> viewPy = new SimpleObjectProperty<>();

    private Stage stage;
    private Scene mainScene;
    private final StackPane root = new StackPane();

    private TileMapEditor editor;
    private EditorView editorView;
    private GameView gameView;
    private StartPagesView startPagesView;

    public PacManGamesUI() {
        viewPy.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));
    }

    private void doSimulationStepAndUpdateGameScene() {
        try {
            theSimulationStep().init(theClock().tickCount());
            theGameController().update();
            theSimulationStep().log();
            currentView().update();
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("SOMETHING VERY BAD HAPPENED DURING SIMULATION STEP!");
            showFlashMessageSec(10, "KA-TA-STROPHE!");
        }
    }

    private void drawCurrentView() {
        try {
            currentView().draw();
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("SOMETHING VERY BAD HAPPENED DURING SIMULATION STEP!");
            showFlashMessageSec(10, "KA-TA-STROPHE!");
        }
    }

    private void handleViewChange(View oldView, View newView) {
        root.getChildren().set(0, newView.layoutRoot());
        if (oldView != null) {
            oldView.disableActionBindings();
            theGameEventManager().removeEventListener(oldView);
        }
        newView.enableActionBindings();
        newView.layoutRoot().requestFocus();
        stage.titleProperty().bind(newView.title());
        theGameEventManager().addEventListener(newView);
    }

    private void handleGameSceneChange(GameScene oldScene, GameScene newScene) {
        String oldSceneName = oldScene != null ? oldScene.displayName() : "NONE";
        String newSceneName = newScene != null ? newScene.displayName() : "NONE";
        Logger.info("Game scene changed from {} to {}", oldSceneName, newSceneName);
    }

    private void createMainScene(double width, double height) {
        mainScene = new Scene(root, width, height);
        mainScene.widthProperty() .addListener((py,ov,nv) -> gameView.resize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.heightProperty().addListener((py,ov,nv) -> gameView.resize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, theKeyboard()::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, theKeyboard()::onKeyReleased);
        mainScene.setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(KeyEvent keyPress) {
        if (KEY_FULLSCREEN.match(keyPress)) {
            stage.setFullScreen(true);
        }
        else if (KEY_MUTE.match(keyPress)) {
            theSound().toggleMuted();
        }
        else if (KEY_OPEN_EDITOR.match(keyPress)) {
            showEditorView();
        }
        else {
            currentView().handleKeyboardInput();
        }
    }

    private Pane createIconBox(Node... icons) {
        int height = STATUS_ICON_SIZE + STATUS_ICON_PADDING;
        var iconBox = new HBox(STATUS_ICON_SPACING);
        iconBox.getChildren().addAll(icons);
        iconBox.setPadding(new Insets(STATUS_ICON_PADDING));
        iconBox.setMaxHeight(height);
        // keep box compact, show only visible items
        for (var icon : icons) {
            icon.visibleProperty().addListener(
                (py, ov, nv) -> iconBox.getChildren().setAll(Arrays.stream(icons).filter(Node::isVisible).toList()));
        }
        return iconBox;
    }

    private void addStatusIcons(Pane parent) {
        var iconMuted = FontIcon.of(FontAwesomeSolid.DEAF, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconMuted.visibleProperty().bind(theSound().mutedProperty());

        var iconAutopilot = FontIcon.of(FontAwesomeSolid.TAXI, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconAutopilot.visibleProperty().bind(PY_AUTOPILOT);

        var iconImmune = FontIcon.of(FontAwesomeSolid.USER_SECRET, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconImmune.visibleProperty().bind(PY_IMMUNITY);

        var icon3D = FontIcon.of(FontAwesomeSolid.CUBES, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        icon3D.visibleProperty().bind(PY_3D_ENABLED);

        var iconPaused = FontIcon.of(FontAwesomeSolid.PAUSE, 80, STATUS_ICON_COLOR);
        iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() != editorView && theClock().isPaused(),
            viewPy, theClock().pausedProperty()));

        Pane iconBox = createIconBox(iconMuted, icon3D, iconAutopilot, iconImmune);
        iconBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> currentView() != editorView, viewPy));

        parent.getChildren().addAll(iconPaused, iconBox);
        StackPane.setAlignment(iconPaused, Pos.CENTER);
        StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);
    }

    private void createMapEditor() {
        editor = new TileMapEditor(stage);
        var miQuit = new MenuItem(theAssets().text("back_to_game"));
        miQuit.setOnAction(e -> {
            editor.stop();
            editor.executeWithCheckForUnsavedChanges(this::showStartView);
        });
        editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miQuit);
        editor.init(CUSTOM_MAP_DIR);
    }

    private void createMapEditorView() {
        editorView = new EditorView(editor);
    }

    private void createStartPagesView() {
        startPagesView = new StartPagesView();
        startPagesView.setBackground(theAssets().background("background.scene"));
    }

    private void createGameView() {
        gameView = new GameView(this);
        gameView.resize(mainScene.getWidth(), mainScene.getHeight());
        gameView.gameSceneProperty().addListener((py, oldScene, newScene) -> handleGameSceneChange(oldScene, newScene));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void addStartPage(StartPage startPage) {
        startPagesView.addStartPage(startPage);
    }

    @Override
    public void build(Stage stage, double width, double height) {
        this.stage = requireNonNull(stage);
        root.setBackground(theAssets().get("background.scene"));
        root.getChildren().add(new Pane()); // placeholder for root of current view
        addStatusIcons(root);
        createMapEditor();
        createMainScene(width, height);
        createStartPagesView();
        createGameView();
        createMapEditorView();

        root.backgroundProperty().bind(gameView.gameSceneProperty().map(
            gameScene -> theUIConfig().currentGameSceneIsPlayScene3D()
                ? theAssets().get("background.play_scene3d")
                : theAssets().get("background.scene"))
        );

        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        stage.setScene(mainScene);

        theClock().setPauseableAction(this::doSimulationStepAndUpdateGameScene);
        theClock().setPermanentAction(this::drawCurrentView);
    }

    @Override
    public void buildDashboard(DashboardID... ids) {
        gameView.dashboard().addDefaultInfoBoxes(ids);
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return gameView.currentGameScene();
    }

    @Override
    public View currentView() {
        return viewPy.get();
    }

    @Override
    public Dashboard dashboard() {
        return gameView.dashboard();
    }

    @Override
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameView.gameSceneProperty();
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
        GameUIConfig uiConfig = theUIConfig().configuration(gameVariant);
        theSound().selectGameVariant(gameVariant, uiConfig.assetNamespace());
        stage.getIcons().setAll(uiConfig.appIcon());
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
        // this triggers a game event and calling the event handlers:
        theGameController().select(gameVariant);
    }

    @Override
    public void show() {
        selectGameVariant(gameVariant());
        viewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(StartPage::requestFocus);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showEditorView() {
        if (!game().isPlaying() || theClock().isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            theSound().stopAll();
            theClock().stop();
            editor.start(stage);
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
        viewPy.set(gameView);
        if (!theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN)) {
            theSound().playVoice("voice.explain", 0);
        }
        gameView.resize(mainScene.getWidth(), mainScene.getHeight());
        restart();
    }

    @Override
    public void showStartView() {
        theClock().stop();
        theClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        theSound().stopAll();
        gameView.gameSceneProperty().set(null);
        gameView.setDashboardVisible(false);
        viewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(StartPage::requestFocus);
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
        gameView.updateGameScene(reloadCurrent);
    }
}