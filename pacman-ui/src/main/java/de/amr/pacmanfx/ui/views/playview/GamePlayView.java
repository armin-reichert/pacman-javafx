/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneDebugView;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneRendering2DComp;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;
import de.amr.pacmanfx.ui.rendering.RenderManager;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboard;
import de.amr.pacmanfx.ui.views.help.HelpView;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Layered view showing the game play and the overlays like dashboard and mini view of the running play scene.
 */
public class GamePlayView implements GameView {

    public record Layers(
        GameScenePane gameSceneLayer,
        MiniPlaySceneView miniViewLayer,
        BorderPane overlayLayer,
        HelpView helpLayer,
        StackPane iconLayer)
    {}

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final Background DEBUG_BACKGROUND = Ufx.paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = Ufx.border(Color.LIGHTGREEN, 1);

    // non-static members

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsRegistry("Action Bindings for Play View");

    private GameApp app;

    private ContextMenuManager contextMenuManager;

    private final StackPane rootPane;

    private Layers layers;

    private GameDashboard dashboard;

    public GamePlayView() {
        createLayers();

        rootPane = new StackPane(
            layers.gameSceneLayer(),
            layers.miniViewLayer().root(),
            layers.overlayLayer(),
            layers.helpLayer(),
            layers.iconLayer()
        );
        rootPane.setId("game-play-view");

        StackPane.setAlignment(layers.miniViewLayer().root(), Pos.TOP_RIGHT);
        StackPane.setAlignment(layers.iconLayer(), Pos.CENTER);
    }

    public Layers layers() {
        return layers;
    }

    @Override
    public void setApp(GameApp app) {
        this.app = requireNonNull(app);

        initLayers(app.ui().viewModel());
        installResizeHandler(app.ui().window().mainScene());

        contextMenuManager = new ContextMenuManager(app, app.ui().window().mainScene());
        rootPane.setOnContextMenuRequested(contextMenuManager);

        dashboard.setAppContext(app);
    }

    public GameDashboard dashboard() {
        return dashboard;
    }

    public void showHelp(GameApp app) {
//        final double scaling = framedContainer.scalingProperty().get();
//        layers.helpLayer().showHelpPopup(app, scaling, app.variantManager().currentVariantName());
    }

    public void acceptLevel(GameScene currentGameScene, GameLevel level) {
        requireNonNull(currentGameScene);
        requireNonNull(level);

        // Level changed: adjust game scene size by reembedding
        layers.gameSceneLayer().embedGameScene(
            app.ui(),
            app.variantManager().currentRuntime().uiConfig(),
            currentGameScene);

        layers.miniViewLayer().setLevel(level);

        contextMenuManager.hideContextMenu();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingsRegistry actionBindings() {
        return actionBindings;
    }

    @Override
    public void onInput(GameApp app) {
        // First look for a matching action of the play view itself; if none found, delegate to the current game scene.
        if (actionBindings.executeMatchingAction(app).isEmpty()) {
            app.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::onInput);
        }
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();
        actionBindings.registerAllBindings(app.commonActions().bindings());
        layers.gameSceneLayer().installKeyBindings();
        Logger.info(actionBindings);
    }

    @Override
    public void onExit() {
        app.suspendGame();
        app.ui().soundManager().stopAll();
        app.ui().soundManager().voice().stop();
        actionBindings.dispose();
        layers.gameSceneLayer().uninstallKeyBindings();
    }

    @Override
    public void onQuit() {
        app.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::onQuit);
        app.ui().viewManager().selectStartPagesView();
    }

    @Override
    public StackPane rootPane() {
        return rootPane;
    }

    public void render(long tick) {
        final GameVariantRuntime runtime = app.variantManager().currentRuntime();
        final RenderManager renderManager = app.renderManager();
        final GameScene gameScene = app.gameSceneManager().optCurrentGameScene().orElse(null);
        final boolean debugMode = app.ui().viewModel().debugModeOnProperty().get();

        final GameSceneRendering2DComp r2d = gameScene instanceof AbstractGameScene abstractGameScene
            ? abstractGameScene.optRendering2D().orElse(null)
            : null;

        renderManager.updateRenderers(
            runtime.playConfig().systems().actorSpriteAnimController(),
            runtime.uiConfig().renderConfig(),
            gameScene,
            r2d,
            layers.miniViewLayer()
        );

        // Clear canvases
        layers.miniViewLayer().renderingSurface().clear();
        if (r2d != null && r2d.autoClearCanvas()) {
            renderManager.variantRenderer().clearCanvas();
        }

        populateRenderQueue(renderManager, gameScene);
        renderManager.renderFrame(tick, debugMode);
    }

    public void updateDashboard() {
        if (layers.overlayLayer().isVisible()) {
            dashboard.update(app);
        }
    }

    public void updateMiniView() {
        layers.miniViewLayer().update(app.gameSceneManager());
    }

    public void replaceGameScene(GameScene currentGameScene, GameScene nextGameScene) {
        requireNonNull(nextGameScene);
        if (currentGameScene != null) {
            layers.gameSceneLayer().disembedGameScene(currentGameScene);
        }
        nextGameScene.onBeforeEmbedded();
        layers.gameSceneLayer().embedGameScene(
            app.ui(),
            app.variantManager().currentRuntime().uiConfig(),
            nextGameScene);
        contextMenuManager.hideContextMenu();
    }

    // Private

    private void installResizeHandler(GameMainScene mainScene) {
        final ChangeListener<? super Number> handler = (_, _, _) ->
            layers.gameSceneLayer().resizeTo(mainScene.getWidth(), mainScene.getHeight());
        mainScene.widthProperty() .addListener(handler);
        mainScene.heightProperty().addListener(handler);
    }

    private void populateRenderQueue(RenderManager renderManager, GameScene gameScene) {
        renderManager.clearRenderQueue();

        // HUD
        final GameSession session = app.game().session();
        if (session.isHUDVisible()) {
            GameEntityViewBuilder.streamOfViews(session.hud().allEntities(), RenderingLayer.HUD)
                .forEach(renderManager::addRenderable);
        }

        // Mini view
        layers.miniViewLayer().renderables().forEach(renderManager::addRenderable);

        // Game scene content
        if (gameScene != null) {
            gameScene.renderables().forEach(renderManager::addRenderable);
        }

        // Debug mode rendering
        if (app.ui().viewModel().debugModeOnProperty().get()) {
            renderManager.addRenderable(new GameSceneDebugView(gameScene));
        }
    }

    private void createLayers() {
        // Layer 1: Game scene with optional decoration
        final var gameScenePane = new GameScenePane();

        // Layer 2: Mini view layer
        final var miniView = new MiniPlaySceneView();

        // Layer 3: Overlay layer with dashboard
        final var overlayPane = new BorderPane();
        dashboard = new GameDashboard();
        dashboard.setVisible(false);
        overlayPane.setLeft(dashboard);

        // Layer 4: Help info
        final var helpView = new HelpView(gameScenePane);

        // Layer 4: "Paused" icon
        final StackPane iconLayer = new StackPane();
        final var pausedIcon = new FontAwesomeIcon(FontAwesomeSymbol.PAUSE);
        pausedIcon.setId("paused-icon");
        iconLayer.getChildren().add(pausedIcon);

        layers = new Layers(gameScenePane, miniView, overlayPane, helpView, iconLayer);
    }

    private void initLayers(GameViewModel viewModel) {
        layers.miniViewLayer().setViewModel(viewModel);

        layers.iconLayer().visibleProperty().bind(app.clock().updatesDisabledProperty());

        viewModel.debugModeOnProperty().addListener((_, _, debug) -> {
            layers.gameSceneLayer().setBackground(debug ? DEBUG_BACKGROUND : null);
            layers.gameSceneLayer().setBorder(debug ? DEBUG_BORDER : null);
        });

        layers.overlayLayer().visibleProperty().bind(dashboard.visibleProperty());
    }
}