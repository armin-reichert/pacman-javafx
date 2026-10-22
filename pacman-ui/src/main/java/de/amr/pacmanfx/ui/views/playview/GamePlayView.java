/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.app.config.GameVariantUIConfig;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.engine.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.action.DefaultActionBindingsRegistry;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.rendering.GamePlayViewRenderer;
import de.amr.pacmanfx.ui.settings.ui.DashboardSectionSettings;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboard;
import de.amr.pacmanfx.ui.views.help.HelpInfo;
import de.amr.pacmanfx.ui.views.help.HelpView;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.window.MainScene;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.engine.action.ActionExecutor.executeMatchingAction;
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

    private final ActionBindingsRegistry<EngineUIContext> actionBindings;
    private final StackPane root;
    private final Layers layers;
    private final GameDashboard dashboard;
    private final EngineUIContext engine;
    private final ContextMenuManager contextMenuManager;
    private final GamePlayViewRenderer renderer = new GamePlayViewRenderer(this);

    public GamePlayView(EngineUIContext engine) {
        this.engine = requireNonNull(engine);

        actionBindings = new DefaultActionBindingsRegistry<>("Action Bindings for GamePlayView");

        // Layer 1: Game scene container
        final var gameSceneContainer = new GameScenePane();

        // Layer 2: Mini view layer
        final var miniView = new MiniPlaySceneView();
        miniView.setViewModel(engine.ui().viewModel());

        // Layer 3: Overlay layer with dashboard
        dashboard = new GameDashboard();
        dashboard.setVisible(false);

        final var overlayPane = new BorderPane();
        overlayPane.setLeft(dashboard);
        overlayPane.visibleProperty().bind(dashboard.visibleProperty());

        // Layer 4: Help view
        final var helpView = new HelpView(gameSceneContainer);

        // Layer 5: "Paused" icon
        final var pausedIcon = new FontAwesomeIcon(FontAwesomeSymbol.PAUSE, "paused-icon");

        final StackPane iconLayer = new StackPane(pausedIcon);
        iconLayer.setMouseTransparent(true);
        iconLayer.visibleProperty().bind(engine.clock().updatesDisabledProperty());

        layers = new Layers(gameSceneContainer, miniView, overlayPane, helpView, iconLayer);

        // Arrange layers in stack pane
        root = new StackPane(gameSceneContainer, miniView.root(), overlayPane, helpView, iconLayer);
        StackPane.setAlignment(miniView.root(), Pos.TOP_RIGHT);
        StackPane.setAlignment(iconLayer, Pos.CENTER);
        root.setId("game-play-view");

        contextMenuManager = new ContextMenuManager(engine);
        root.setOnContextMenuRequested(contextMenuManager);

        engine.ui().viewModel().debugModeOnProperty().addListener((_, _, debug) -> gameSceneContainer.setDebugMode(debug));

        installMainSceneResizeHandler();
    }

    public Layers layers() {
        return layers;
    }

    public GameDashboard dashboard() {
        return dashboard;
    }

    public void populateDashboard(DashboardFactory dashboardFactory){
        requireNonNull(dashboardFactory);
        List<DashboardSectionSettings> settings = engine.ui().settings().dashboard();
        dashboard.populate(engine, dashboardFactory, settings);
    }

    public void showHelp(String variantName) {
        requireNonNull(variantName);
        layers.helpLayer().popupHelpInfo(new HelpInfo(engine), 2, variantName);
    }

    public void acceptLevel(GameScene currentGameScene, GameLevel level) {
        requireNonNull(currentGameScene);
        requireNonNull(level);

        if (!(currentGameScene instanceof AbstractGameScene gameScene)) {
            Logger.error("Illegal game scene class {}", currentGameScene.getClass());
            return;
        }

        final GameVariantUIConfig uiConfig = engine.uiConfig(engine.gameVariantManager().currentRuntime());

        // Level changed: adjust game scene size by reembedding
        layers.gameSceneLayer().embedGameScene(engine.ui(), uiConfig, gameScene);
        layers.miniViewLayer().setLevel(level);

        contextMenuManager.hideContextMenu();
    }

    public void replaceGameScene(GameScene currentGameScene, GameScene nextGameScene) {
        // current game scene may be NULL!
        requireNonNull(nextGameScene);

        if (currentGameScene != null) {
            currentGameScene.deactivate();
        }

        nextGameScene.onBeforeEmbedded();

        final GameVariantRuntime runtime = engine.gameVariantManager().currentRuntime();
        layers.gameSceneLayer().embedGameScene(engine.ui(), engine.uiConfig(runtime), (AbstractGameScene) nextGameScene);

        contextMenuManager.hideContextMenu();
    }

    public void render() {
        updateDashboard();
        updateMiniView();
        renderer.render(engine);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameView interface
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingsRegistry<EngineUIContext> actionBindings() {
        return actionBindings;
    }

    @Override
    public void onEnter() {
        root.requestFocus();
        actionBindings.registerAllBindings(CommonGameActions.instance().bindings());
        layers.gameSceneLayer().installKeyBindings();
        Logger.debug(actionBindings);
    }

    @Override
    public void onExit() {
        engine.lifeCycle().suspendGame();
        engine.soundManager().stopAll();
        engine.soundManager().voice().stop();
        actionBindings.dispose();
        layers.gameSceneLayer().uninstallKeyBindings();
    }

    @Override
    public void onInput(Input input) {
        // First look for an action of the play view itself that is triggered by the input.
        // If none is found, delegate to the current game scene.
        if (executeMatchingAction(actionBindings, engine).isEmpty()) {
            engine.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::onInput);
        }
    }

    @Override
    public void onQuit() {
        engine.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::onQuit);
        engine.viewManager().currentViewIDProperty().set(GameViewID.START_PAGES);
    }

    @Override
    public StackPane rootPane() {
        return root;
    }

    // Private

    private void updateDashboard() {
        if (layers.overlayLayer().isVisible()) {
            dashboard.update(engine);
        }
    }

    private void updateMiniView() {
        final boolean playScene3DActive = engine.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
        layers.miniViewLayer().update(playScene3DActive);
    }

    private void installMainSceneResizeHandler() {
        final MainScene mainScene = engine.ui().mainScene();
        final ChangeListener<? super Number> handler = (_, _, _)
            -> layers.gameSceneLayer().resizeTo(mainScene.scene().getWidth(), mainScene.scene().getHeight());
        mainScene.scene().widthProperty() .addListener(handler);
        mainScene.scene().heightProperty().addListener(handler);
    }
}