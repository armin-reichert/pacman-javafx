/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.*;
import de.amr.games.pacman.ui.dashboard.Dashboard;
import de.amr.games.pacman.ui.input.GameKeyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.GameClockFX;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_AUTOPILOT;
import static de.amr.games.pacman.uilib.Ufx.createIcon;

/**
 * User interface for all Pac-Man game variants (2D only).
 *
 * @author Armin Reichert
 */
public class PacManGamesUI implements GameUI {

    private static final byte STATUS_ICON_SIZE = 36;

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>();
    protected final ObjectProperty<View> viewPy = new SimpleObjectProperty<>();

    protected final GameAssets assets = new GameAssets();
    protected final GameClockFX clock = new GameClockFX();
    protected final GameKeyboard keyboard = new GameKeyboard();
    protected final GameSound sound = new GameSound();
    protected final UIConfigurationManager uiConfigurationManager = new UIConfigurationManager();

    protected Stage stage;
    protected Scene mainScene;
    protected final StackPane mainSceneRoot = new StackPane();

    protected EditorView editorView;
    protected GameView gameView;
    protected StartPagesView startPagesView;

    public PacManGamesUI() {
        clock.setPauseableAction(this::doSimulationStepAndUpdateGameScene);
        clock.setPermanentAction(() -> currentView().onTick());
        viewPy.addListener((py, oldView, newView) -> handleViewChange(oldView, newView));
        gameScenePy.addListener((py, oldScene, newScene) -> handleGameSceneChange(oldScene, newScene));
    }

    private void doSimulationStepAndUpdateGameScene() {
        THE_GAME_CONTROLLER.update();
        THE_GAME_CONTROLLER.game().eventLog().print(clock.tickCount());
        currentGameScene().ifPresent(GameScene::update);
    }

    private void handleViewChange(View oldView, View newView) {
        mainSceneRoot.getChildren().set(0, newView.node());
        if (oldView != null) {
            oldView.disableActionBindings();
            THE_GAME_CONTROLLER.game().removeGameEventListener(oldView);
        }
        newView.enableActionBindings();
        newView.node().requestFocus();
        THE_GAME_CONTROLLER.game().addGameEventListener(newView);
    }

    private void handleGameSceneChange(GameScene oldScene, GameScene newScene) {
        String oldSceneName = oldScene != null ? oldScene.displayName() : "NONE";
        String newSceneName = newScene != null ? newScene.displayName() : "NONE";
        Logger.info("Game scene changed from {} to {}", oldSceneName, newSceneName);
    }

    protected void createMainScene(Dimension2D size) {
        mainSceneRoot.setBackground(assets.get("background.scene"));
        mainSceneRoot.backgroundProperty().bind(gameScenePy.map(
            gameScene -> uiConfigurationManager.currentGameSceneIsPlayScene3D()
                ? assets.get("background.play_scene3d")
                : assets.get("background.scene"))
        );

        mainSceneRoot.getChildren().add(new Pane()); // placeholder for root of current view
        addStatusIcons(mainSceneRoot);

        mainScene = new Scene(mainSceneRoot, size.getWidth(), size.getHeight());
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
            else if (currentView() instanceof GameActionProvider actionProvider) {
                actionProvider.handleInput();
            }
        });
    }

    private void addStatusIcons(Pane parent) {
        ImageView iconMuted = createIcon(assets.get("icon.mute"), STATUS_ICON_SIZE);
        iconMuted.visibleProperty().bind(sound.mutedProperty());

        ImageView iconAutopilot = createIcon(assets.get("icon.auto"), STATUS_ICON_SIZE);
        iconAutopilot.visibleProperty().bind(PY_AUTOPILOT);

        ImageView iconPaused = createIcon(assets.get("icon.pause"), 64);
        iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() != editorView && clock.isPaused(),
            viewPy, clock.pausedProperty()));

        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        var hBox = new HBox(3, spring, iconAutopilot, iconMuted);
        hBox.setMaxHeight(STATUS_ICON_SIZE);
        hBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> currentView() != editorView, viewPy));

        parent.getChildren().addAll(iconPaused, hBox);
        StackPane.setAlignment(iconPaused, Pos.CENTER);
        StackPane.setAlignment(hBox, Pos.BOTTOM_RIGHT);
    }

    protected void createEditorView() {
        editorView = new EditorView(stage);
        editorView.setOnClose(editor -> {
            editor.stop();
            editor.executeWithCheckForUnsavedChanges(this::bindStageTitle);
            clock.setTargetFrameRate(Globals.TICKS_PER_SECOND);
            THE_GAME_CONTROLLER.restart(GameState.BOOT);
            showStartView();
        });
    }

    protected void createStartView() {
        startPagesView = new StartPagesView();
        startPagesView.setBackground(assets.background("background.scene"));
    }

    protected void createGameView() {
        gameView = new GameView(this);
        gameView.gameSceneProperty().bind(gameScenePy);
        gameView.resize(mainScene.getWidth(), mainScene.getHeight());
    }

    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    public Scene mainScene() {
        return mainScene;
    }

    protected void bindStageTitle() {
        stage.titleProperty().bind(Bindings.createStringBinding(
            () -> {
                // "app.title.pacman" vs. "app.title.pacman.paused"
                String key = "app.title." + uiConfigurationManager.current().assetNamespace();
                if (clock.isPaused()) { key += ".paused"; }
                if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    return assets.text(key, "2D") + " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return assets.text(key, "2D");
            },
            clock.pausedProperty(), gameScenePy, gameView.node().heightProperty())
        );
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
        createMainScene(assertNotNull(mainSceneSize));
        createStartView();
        createGameView();
        createEditorView();
        bindStageTitle();
        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        stage.setScene(mainScene);
    }

    @Override
    public void buildDashboard(DashboardID... ids) {
        gameView.dashboard().addDefaultInfoBoxes(ids);
    }

    @Override
    public void boot() {
        clock.stop();
        clock.setTargetFrameRate(Globals.TICKS_PER_SECOND);
        clock.pausedProperty().set(false);
        clock.start();
        THE_GAME_CONTROLLER.restart(GameState.BOOT);
    }

    @Override
    public GameClockFX clock() {
        return clock;
    }

    @Override
    public UIConfigurationManager configurations() {
        return uiConfigurationManager;
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
    public GameKeyboard keyboard() {
        return keyboard;
    }

    @Override
    public void selectStartPage(int index) {
        startPagesView.selectStartPage(index);
    }

    @Override
    public void selectGameVariant(GameVariant gameVariant) {
        THE_GAME_CONTROLLER.selectGameVariant(gameVariant);
        GameUIConfiguration uiConfig = uiConfigurationManager.configuration(gameVariant);
        sound.selectGameVariant(gameVariant, uiConfig.assetNamespace());
        stage.getIcons().setAll(uiConfig.appIcon());
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
    }

    @Override
    public void show() {
        selectGameVariant(THE_GAME_CONTROLLER.selectedGameVariant());
        showStartView();
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void showEditorView() {
        if (!THE_GAME_CONTROLLER.game().isPlaying()) {
            currentGameScene().ifPresent(GameScene::end);
            clock.stop();
            sound.stopAll();
            editorView.editor().start(stage);
            viewPy.set(editorView);
        }
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        gameView.flashMessageOverlay().showMessage(String.format(message, args), seconds);
    }

    @Override
    public void showGameView() {
        viewPy.set(gameView);
        if (!THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN)) {
            sound.playVoice("voice.explain", 0);
        }
        gameView.resize(mainScene.getWidth(), mainScene.getHeight());
        boot();
    }

    @Override
    public void showStartView() {
        clock.stop();
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
        final GameScene nextGameScene = uiConfigurationManager.current().selectGameScene();
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
        if (uiConfigurationManager.current().is2D3DPlaySceneSwitch(currentGameScene, nextGameScene)) {
            nextGameScene.onSceneVariantSwitch(currentGameScene);
        }
        if (changing) {
            gameScenePy.set(nextGameScene);
        }
    }
}