/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tilemap.editor.TileMapEditor;
import de.amr.games.pacman.ui.dashboard.Dashboard;
import de.amr.games.pacman.ui.input.GameKeyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.GameClockFX;
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

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.Globals.*;

/**
 * User interface for all Pac-Man game variants.
 *
 * @author Armin Reichert
 */
public class PacManGamesUI implements GameUI {

    private final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>();
    private final ObjectProperty<View> viewPy = new SimpleObjectProperty<>();

    private final GameAssets assets = new GameAssets();
    private final GameClockFX clock = new GameClockFX();
    private final GameKeyboard keyboard = new GameKeyboard();
    private final GameSound sound = new GameSound();
    private final GameUIConfigManager gameUiConfigManager = new GameUIConfigManager();

    private Stage stage;
    private Scene mainScene;
    private final StackPane root = new StackPane();

    private TileMapEditor editor;
    private EditorView editorView;
    private GameView gameView;
    private StartPagesView startPagesView;

    public PacManGamesUI() {
        clock.setPauseableAction(this::doSimulationStepAndUpdateGameScene);
        clock.setPermanentAction(() -> currentView().update());
        viewPy.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));
        gameScenePy.addListener((py, oldScene, newScene) -> handleGameSceneChange(oldScene, newScene));
    }

    private void doSimulationStepAndUpdateGameScene() {
        try {
            THE_GAME_CONTROLLER.update();
            THE_GAME_CONTROLLER.game().eventLog().print(clock.tickCount());
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("SOMETHING VERY BAD HAPPENED DURING SIMULATION STEP!");
            showFlashMessageSec(10, "KA-TA-STROPHE!");
        }
    }

    private void handleViewChange(View oldView, View newView) {
        root.getChildren().set(0, newView.layoutRoot());
        if (oldView != null) {
            oldView.disableActionBindings(keyboard);
            THE_GAME_CONTROLLER.game().removeGameEventListener(oldView);
        }
        newView.enableActionBindings(keyboard);
        newView.layoutRoot().requestFocus();
        stage.titleProperty().bind(newView.title());
        THE_GAME_CONTROLLER.game().addGameEventListener(newView);
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

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

        mainScene.setOnKeyPressed(keyPress -> {
            if (GameKeyboard.KEY_FULLSCREEN.match(keyPress)) {
                stage.setFullScreen(true);
            }
            else if (GameKeyboard.KEY_MUTE.match(keyPress)) {
                sound.toggleMuted();
            }
            else if (GameKeyboard.KEY_OPEN_EDITOR.match(keyPress)) {
                showEditorView();
            }
            else {
                currentView().handleInput(keyboard);
            }
        });
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
        iconMuted.visibleProperty().bind(sound.mutedProperty());

        var iconAutopilot = FontIcon.of(FontAwesomeSolid.TAXI, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconAutopilot.visibleProperty().bind(PY_AUTOPILOT);

        var iconImmune = FontIcon.of(FontAwesomeSolid.USER_SECRET, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconImmune.visibleProperty().bind(PY_IMMUNITY);

        var iconPaused = FontIcon.of(FontAwesomeSolid.PAUSE, 80, STATUS_ICON_COLOR);
        iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() != editorView && clock.isPaused(),
            viewPy, clock.pausedProperty()));

        Pane iconBox = createIconBox(iconMuted, iconAutopilot, iconImmune);
        iconBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> currentView() != editorView, viewPy));

        parent.getChildren().addAll(iconPaused, iconBox);
        StackPane.setAlignment(iconPaused, Pos.CENTER);
        StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);
    }

    private void createMapEditor() {
        editor = new TileMapEditor(stage);
        var miQuit = new MenuItem(assets.text("back_to_game"));
        miQuit.setOnAction(e -> {
            editor.stop();
            editor.executeWithCheckForUnsavedChanges(this::showStartView);
        });
        editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miQuit);
        editor.init(GameModel.CUSTOM_MAP_DIR);
    }

    private void createMapEditorView() {
        editorView = new EditorView(editor);
    }

    private void createStartPagesView() {
        startPagesView = new StartPagesView();
        startPagesView.setBackground(assets.background("background.scene"));
    }

    private void createGameView() {
        gameView = new GameView(this);
        gameView.resize(mainScene.getWidth(), mainScene.getHeight());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void addStartPage(StartPage startPage) {
        startPagesView.addStartPage(startPage);
    }

    @Override
    public GameAssets assets() {
        return assets;
    }

    @Override
    public void build(Stage stage, Dimension2D mainSceneSize) {
        this.stage = assertNotNull(stage);
        root.setBackground(assets.get("background.scene"));
        root.backgroundProperty().bind(gameScenePy.map(
                gameScene -> gameUiConfigManager.currentGameSceneIsPlayScene3D()
                        ? assets.get("background.play_scene3d")
                        : assets.get("background.scene"))
        );
        root.getChildren().add(new Pane()); // placeholder for root of current view
        addStatusIcons(root);
        createMapEditor();
        createMainScene(assertNotNull(mainSceneSize));
        createStartPagesView();
        createGameView();
        createMapEditorView();
        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        stage.setScene(mainScene);
    }

    @Override
    public void buildDashboard(DashboardID... ids) {
        gameView.dashboard().addDefaultInfoBoxes(ids);
    }

    @Override
    public GameClockFX clock() {
        return clock;
    }

    @Override
    public GameUIConfigManager configurations() {
        return gameUiConfigManager;
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(gameScenePy.get());
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
        return gameScenePy;
    }

    @Override
    public GameKeyboard keyboard() {
        return keyboard;
    }

    @Override
    public Scene mainScene() {
        return mainScene;
    }

    @Override
    public void restart() {
        clock.stop();
        clock.setTargetFrameRate(Globals.TICKS_PER_SECOND);
        clock.pausedProperty().set(false);
        clock.start();
        THE_GAME_CONTROLLER.restart(GameState.BOOT);
    }

    @Override
    public void selectStartPage(int index) {
        startPagesView.selectStartPage(index);
    }

    @Override
    public void selectGameVariant(GameVariant gameVariant) {
        THE_GAME_CONTROLLER.selectGameVariant(gameVariant);
        GameUIConfig uiConfig = gameUiConfigManager.configuration(gameVariant);
        sound.selectGameVariant(gameVariant, uiConfig.assetNamespace());
        stage.getIcons().setAll(uiConfig.appIcon());
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
    }

    @Override
    public void show() {
        selectGameVariant(THE_GAME_CONTROLLER.selectedGameVariant());
        viewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(StartPage::requestFocus);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showEditorView() {
        if (!THE_GAME_CONTROLLER.game().isPlaying() || clock().isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            THE_GAME_CONTROLLER.game().endGame();
            clock.stop();
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
            sound.playVoice("voice.explain", 0);
        }
        gameView.resize(mainScene.getWidth(), mainScene.getHeight());
        restart();
    }

    @Override
    public void showStartView() {
        clock.stop();
        clock.setTargetFrameRate(Globals.TICKS_PER_SECOND);
        gameScenePy.set(null);
        gameView.setDashboardVisible(false);
        viewPy.set(startPagesView);
        startPagesView.currentStartPage().ifPresent(StartPage::requestFocus);
    }

    @Override
    public GameSound sound() {
        return sound;
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
        final GameScene nextGameScene = gameUiConfigManager.current().selectGameScene();
        if (nextGameScene == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }
        final GameScene currentGameScene = gameScenePy.get();
        final boolean changing = nextGameScene != currentGameScene;
        if (!changing && !reloadCurrent) {
            return;
        }
        if (currentGameScene != null) {
            currentGameScene.end();
            Logger.info("Game scene ended: {}", currentGameScene.displayName());
        }
        gameView.embedGameScene(nextGameScene);
        nextGameScene.init();
        if (gameUiConfigManager.current().is2D3DPlaySceneSwitch(currentGameScene, nextGameScene)) {
            nextGameScene.onSceneVariantSwitch(currentGameScene);
        }
        if (changing) {
            gameScenePy.set(nextGameScene);
        }
    }
}