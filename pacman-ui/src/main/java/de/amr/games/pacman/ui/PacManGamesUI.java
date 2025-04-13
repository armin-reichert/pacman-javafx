/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tilemap.editor.TileMapEditor;
import de.amr.games.pacman.ui.dashboard.Dashboard;
import de.amr.games.pacman.uilib.model3D.Model3D;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.Globals.*;
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
        THE_CLOCK.setPauseableAction(this::doSimulationStepAndUpdateGameScene);
        THE_CLOCK.setPermanentAction(this::updateCurrentView);
        viewPy.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));
    }

    private void doSimulationStepAndUpdateGameScene() {
        try {
            THE_GAME_CONTROLLER.update();
            THE_GAME_CONTROLLER.game().eventLog().print(THE_CLOCK.tickCount());
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("SOMETHING VERY BAD HAPPENED DURING SIMULATION STEP!");
            showFlashMessageSec(10, "KA-TA-STROPHE!");
        }
    }

    private void updateCurrentView() {
        try {
            currentView().update();
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("SOMETHING VERY BAD HAPPENED DURING SIMULATION STEP!");
            showFlashMessageSec(10, "KA-TA-STROPHE!");
        }
    }

    private void handleViewChange(View oldView, View newView) {
        root.getChildren().set(0, newView.layoutRoot());
        if (oldView != null) {
            oldView.disableActionBindings(THE_KEYBOARD);
            THE_GAME_EVENT_MANAGER.removeEventListener(oldView);
        }
        newView.enableActionBindings(THE_KEYBOARD);
        newView.layoutRoot().requestFocus();
        stage.titleProperty().bind(newView.title());
        THE_GAME_EVENT_MANAGER.addEventListener(newView);
    }

    private void handleGameSceneChange(GameScene oldScene, GameScene newScene) {
        String oldSceneName = oldScene != null ? oldScene.displayName() : "NONE";
        String newSceneName = newScene != null ? newScene.displayName() : "NONE";
        Logger.info("Game scene changed from {} to {}", oldSceneName, newSceneName);
    }

    private void createMainScene(Dimension2D size) {
        mainScene = new Scene(root, size.getWidth(), size.getHeight());
        mainScene.widthProperty() .addListener((py,ov,nv) -> gameView.resize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.heightProperty().addListener((py,ov,nv) -> gameView.resize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, THE_KEYBOARD::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, THE_KEYBOARD::onKeyReleased);
        mainScene.setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(KeyEvent keyPress) {
        if (KEY_FULLSCREEN.match(keyPress)) {
            stage.setFullScreen(true);
        }
        else if (KEY_MUTE.match(keyPress)) {
            THE_SOUND.toggleMuted();
        }
        else if (KEY_OPEN_EDITOR.match(keyPress)) {
            showEditorView();
        }
        else {
            currentView().handleKeyboardInput(THE_KEYBOARD);
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
        iconMuted.visibleProperty().bind(THE_SOUND.mutedProperty());

        var iconAutopilot = FontIcon.of(FontAwesomeSolid.TAXI, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconAutopilot.visibleProperty().bind(PY_AUTOPILOT);

        var iconImmune = FontIcon.of(FontAwesomeSolid.USER_SECRET, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconImmune.visibleProperty().bind(PY_IMMUNITY);

        var icon3D = FontIcon.of(FontAwesomeSolid.CUBES, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        icon3D.visibleProperty().bind(PY_3D_ENABLED);

        var iconCutScenesOff = FontIcon.of(FontAwesomeSolid.AMBULANCE, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        //TODO make this work:
        /*
        iconCutScenesOff.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> !THE_GAME_CONTROLLER.game().cutScenesEnabledProperty().get(),
            THE_GAME_CONTROLLER.game().cutScenesEnabledProperty(), THE_GAME_CONTROLLER.gameVariantProperty()
        ));
         */
        iconCutScenesOff.setVisible(false);

        var iconPaused = FontIcon.of(FontAwesomeSolid.PAUSE, 80, STATUS_ICON_COLOR);
        iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() != editorView && THE_CLOCK.isPaused(),
            viewPy, THE_CLOCK.pausedProperty()));

        Pane iconBox = createIconBox(iconMuted, icon3D, iconAutopilot, iconImmune, iconCutScenesOff);
        iconBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> currentView() != editorView, viewPy));

        parent.getChildren().addAll(iconPaused, iconBox);
        StackPane.setAlignment(iconPaused, Pos.CENTER);
        StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);
    }

    private void createMapEditor() {
        editor = new TileMapEditor(stage);
        var miQuit = new MenuItem(THE_ASSETS.text("back_to_game"));
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
        startPagesView.setBackground(THE_ASSETS.background("background.scene"));
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
    public void build(Stage stage, Dimension2D mainSceneSize) {
        this.stage = requireNonNull(stage);
        root.setBackground(THE_ASSETS.get("background.scene"));
        root.getChildren().add(new Pane()); // placeholder for root of current view
        addStatusIcons(root);
        createMapEditor();
        createMainScene(requireNonNull(mainSceneSize));
        createStartPagesView();
        createGameView();
        createMapEditorView();

        root.backgroundProperty().bind(gameView.gameSceneProperty().map(
            gameScene -> THE_UI_CONFIGS.currentGameSceneIsPlayScene3D()
                ? THE_ASSETS.get("background.play_scene3d")
                : THE_ASSETS.get("background.scene"))
        );

        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        stage.setScene(mainScene);

        Logger.info("Assets: {}", THE_ASSETS.summary(Map.of(
            Model3D.class,"3D models",
            Image.class, "images",
            Font.class, "fonts",
            Color.class, "colors",
            AudioClip.class, "audio clips")));
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
        THE_CLOCK.stop();
        THE_CLOCK.setTargetFrameRate(Globals.TICKS_PER_SECOND);
        THE_CLOCK.pausedProperty().set(false);
        THE_CLOCK.start();
        THE_GAME_CONTROLLER.restart(GameState.BOOT);
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
        GameUIConfig uiConfig = THE_UI_CONFIGS.configuration(gameVariant);
        THE_SOUND.selectGameVariant(gameVariant, uiConfig.assetNamespace());
        stage.getIcons().setAll(uiConfig.appIcon());
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
        // this triggers a game event and calling the event handlers:
        THE_GAME_CONTROLLER.gameVariantProperty().set(gameVariant);
    }

    @Override
    public void show() {
        selectGameVariant(THE_GAME_CONTROLLER.gameVariantProperty().get());
        viewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(StartPage::requestFocus);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showEditorView() {
        if (!THE_GAME_CONTROLLER.game().playingProperty().get() || THE_CLOCK.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            THE_GAME_CONTROLLER.game().endGame();
            THE_CLOCK.stop();
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
        if (!THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN)) {
            THE_SOUND.playVoice("voice.explain", 0);
        }
        gameView.resize(mainScene.getWidth(), mainScene.getHeight());
        restart();
    }

    @Override
    public void showStartView() {
        THE_CLOCK.stop();
        THE_CLOCK.setTargetFrameRate(Globals.TICKS_PER_SECOND);
        gameView.gameSceneProperty().set(null);
        gameView.setDashboardVisible(false);
        viewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(StartPage::requestFocus);
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
        gameView.updateGameScene(THE_UI_CONFIGS.current(), reloadCurrent);
    }
}