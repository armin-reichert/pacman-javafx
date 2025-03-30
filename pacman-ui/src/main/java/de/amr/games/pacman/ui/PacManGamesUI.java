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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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

    protected final GameAction actionShowEditorView = new GameAction() {
        @Override
        public void execute() {
            currentGameScene().ifPresent(GameScene::end);
            clock.stop();
            sound.stopAll();
            editorView.editor().start(stage);
            viewPy.set(editorView);
        }

        @Override
        public boolean isEnabled() {
            return !THE_GAME_CONTROLLER.game().isPlaying();
        }
    };

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            GameScene gameScene = get();
            Logger.info("Game scene is now: {}", gameScene != null ? gameScene.displayName() : "NONE");
        }
    };

    protected final ObjectProperty<View> viewPy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            View view = get();
            if (view == startPageView) {
                startPageView.currentStartPage().ifPresent(
                    startPage -> startPage.onEnter(THE_GAME_CONTROLLER.selectedGameVariant()));
            }
        }
    };

    protected final GameAssets assets = new GameAssets();
    protected final GameClockFX clock = new GameClockFX();
    protected final GameKeyboard keyboard = new GameKeyboard();
    protected final GameSound sound = new GameSound();
    protected final UIConfigurationManager uiConfigurationManager = new UIConfigurationManager();

    protected Stage stage;
    protected Scene mainScene;
    protected final StackPane sceneRoot = new StackPane();

    protected EditorView editorView;
    protected GameView gameView;
    protected StartPagesCarousel startPageView;

    protected boolean scoreVisible;

    public PacManGamesUI() {
        clock.setPauseableAction(this::makeSimulationStepAndUpdateCurrentGameScene);
        clock.setPermanentAction(this::tickCurrentView);
        viewPy.addListener((py, oldView, newView) -> {
            sceneRoot.getChildren().set(0, newView.node());
            if (oldView != null) {
                oldView.disableActionBindings();
                THE_GAME_CONTROLLER.game().removeGameEventListener(oldView);
            }
            newView.enableActionBindings();
            newView.node().requestFocus();
            THE_GAME_CONTROLLER.game().addGameEventListener(newView);
        });
    }

    private void tickCurrentView() {
        View currentView = viewPy.get();
        if (currentView != null) {
            currentView.onTick();
        }
    }

    protected void makeSimulationStepAndUpdateCurrentGameScene() {
        THE_GAME_CONTROLLER.update();
        THE_GAME_CONTROLLER.game().eventLog().print(clock.tickCount());
        currentGameScene().ifPresent(GameScene::update);
    }

    protected void createMainScene(Dimension2D size) {
        sceneRoot.getChildren().add(new Pane()); // placeholder for view
        sceneRoot.setBackground(assets.get("background.scene"));
        sceneRoot.backgroundProperty().bind(gameScenePy.map(
            gameScene -> uiConfigurationManager.currentGameSceneIsPlayScene3D()
                ? assets.get("background.play_scene3d")
                : assets.get("background.scene"))
        );
        addStatusIcons();

        mainScene = new Scene(sceneRoot, size.getWidth(), size.getHeight());
        mainScene.widthProperty() .addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.heightProperty().addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

        mainScene.setOnKeyPressed(keyPress -> {
            if (GameKeyboard.KEY_FULLSCREEN.match(keyPress)) {
                enterFullScreenMode();
            }
            else if (GameKeyboard.KEY_MUTE.match(keyPress)) {
                sound.toggleMuted();
            }
            else if (GameKeyboard.KEY_OPEN_EDITOR.match(keyPress)) {
                showEditorView();
            }
            else if (viewPy.get() instanceof GameActionProvider actionProvider) {
                actionProvider.handleInput();
            }
        });
    }

    private void addStatusIcons() {
        ImageView iconMuted = createIcon(assets.get("icon.mute"), 48, sound.mutedProperty());
        ImageView iconAutopilot = createIcon(assets.get("icon.auto"), 48, PY_AUTOPILOT);
        ImageView iconPaused = createIcon(assets.get("icon.pause"), 64, clock.pausedProperty());

        var pane = new HBox(iconAutopilot, iconMuted);
        pane.setMaxWidth(128);
        pane.setMaxHeight(64);
        pane.visibleProperty().bind(Bindings.createBooleanBinding(() -> viewPy.get() != editorView, viewPy));
        StackPane.setAlignment(pane, Pos.BOTTOM_RIGHT);

        iconPaused.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> viewPy.get() != editorView && clock.isPaused(), viewPy, clock.pausedProperty()));
        StackPane.setAlignment(iconPaused, Pos.CENTER);

        sceneRoot.getChildren().addAll(iconPaused, pane);
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
        startPageView = new StartPagesCarousel();
        startPageView.setBackground(assets.background("background.scene"));
    }

    protected void createGameView() {
        gameView = new GameView(mainScene);
        gameView.gameSceneProperty().bind(gameScenePy);
        gameView.setSize(mainScene.getWidth(), mainScene.getHeight());
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
            clock.pausedProperty(), gameScenePy, gameView.heightProperty())
        );
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameUI interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void addDefaultDashboardItems(String... ids) {
        gameView.dashboard().addDefaultInfoBoxes(ids);
    }

    @Override
    public void addStartPage(GameVariant gameVariant, StartPage startPage) {
        startPageView.addStartPage(gameVariant, startPage);
    }

    /**
     * Builds the layout and configures the stage.
     *
     * @param stage the stage (window)
     * @param mainSceneSize initial main scene size
     */
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
        stage.centerOnScreen();
        stage.setOnShowing(e -> showStartView());
    }

    @Override
    public GameAssets assets() {
        return assets;
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
    public Dashboard dashboard() {
        return gameView.dashboard();
    }

    @Override
    public void enterFullScreenMode() {
        stage.setFullScreen(true);
    }

    @Override
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    @Override
    public void init(GameVariant gameVariant) {
        Logger.info("Init UI for game variant {}...", gameVariant);
        THE_GAME_CONTROLLER.selectGameVariant(gameVariant);
        onGameVariantChange(gameVariant);
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public GameKeyboard keyboard() {
        return keyboard;
    }

    @Override
    public void onGameVariantChange(GameVariant gameVariant) {
        GameUIConfiguration uiConfig = uiConfigurationManager.configuration(gameVariant);
        sound.selectGameVariant(gameVariant, uiConfig.assetNamespace());
        stage.getIcons().setAll(uiConfig.appIcon());
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
    }

    @Override
    public void showEditorView() {
        if (actionShowEditorView.isEnabled()) {
            actionShowEditorView.execute();
        }
    }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisible = visible;
    }

    @Override
    public void show() {
        stage.show();
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
        gameView.setSize(mainScene.getWidth(), mainScene.getHeight());
        GameActions2D.BOOT.execute();
    }

    @Override
    public void showStartView() {
        clock.stop();
        gameScenePy.set(null);
        gameView.setDashboardVisible(false);
        viewPy.set(startPageView);
        //TODO this is needed for XXL option menu
        startPageView.currentStartPage().ifPresent(startPage -> startPage.onEnter(THE_GAME_CONTROLLER.selectedGameVariant()));
    }

    @Override
    public GameSound sound() {
        return sound;
    }

    @Override
    public void togglePlayScene2D3D() {}

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