/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.runtime.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.runtime.action.GameActionBindingsRegistry;
import de.amr.pacmanfx.engine.runtime.action.GameActionContext;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.rendering.GamePlayViewRenderer;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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

    private final ActionBindingsRegistry actionBindings;
    private final StackPane root;
    private final Layers layers;
    private final GameDashboard dashboard;

    private GameActionContext actionContext;

    private ContextMenuManager contextMenuManager;

    public GamePlayView() {
        // Layer 1: Game scene container
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

        // Layer 5: "Paused" icon
        final StackPane iconLayer = new StackPane();
        iconLayer.setMouseTransparent(true);
        final var pausedIcon = new FontAwesomeIcon(FontAwesomeSymbol.PAUSE);
        pausedIcon.setId("paused-icon");
        iconLayer.getChildren().add(pausedIcon);

        layers = new Layers(gameScenePane, miniView, overlayPane, helpView, iconLayer);

        root = new StackPane(
            layers.gameSceneLayer(),
            layers.miniViewLayer().root(),
            layers.overlayLayer(),
            layers.helpLayer(),
            layers.iconLayer()
        );
        root.setId("game-play-view");

        StackPane.setAlignment(layers.miniViewLayer().root(), Pos.TOP_RIGHT);
        StackPane.setAlignment(layers.iconLayer(), Pos.CENTER);

        actionBindings = new GameActionBindingsRegistry("Action Bindings for GamePlayView");
    }

    public Layers layers() {
        return layers;
    }

    public GameDashboard dashboard() {
        return dashboard;
    }

    public void showHelp(GameActionContext actionContext) {
        layers.helpLayer().showHelpPopup(actionContext, 2, actionContext.gameVariantManager().currentVariantName());
    }

    public void acceptLevel(GameScene currentGameScene, GameLevel level) {
        requireNonNull(currentGameScene);
        requireNonNull(level);

        // Level changed: adjust game scene size by reembedding
        layers.gameSceneLayer().embedGameScene(
            actionContext.ui(),
            actionContext.gameVariantManager().currentRuntime().uiConfig(),
            (AbstractGameScene) currentGameScene);

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

        layers.gameSceneLayer().embedGameScene(
            actionContext.ui(),
            actionContext.gameVariantManager().currentRuntime().uiConfig(),
            (AbstractGameScene) nextGameScene);

        contextMenuManager.hideContextMenu();
    }

    public void render() {
        GamePlayViewRenderer.render(this, actionContext, actionContext.gameSceneManager().currentGameScene());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameView interface
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void setActionContext(GameActionContext actionContext) {
        this.actionContext = actionContext;

        final GameViewModel viewModel = actionContext.ui().viewModel();
        final GameMainScene mainScene = actionContext.ui().window().mainScene();

        contextMenuManager = new ContextMenuManager(actionContext);
        root.setOnContextMenuRequested(contextMenuManager);

        dashboard.setExecutionContext(actionContext);

        layers.miniViewLayer().setViewModel(viewModel);

        layers.iconLayer().visibleProperty().bind(actionContext.clock().updatesDisabledProperty());

        layers.overlayLayer().visibleProperty().bind(dashboard.visibleProperty());

        viewModel.debugModeOnProperty().addListener(
            (_, _, debug) -> layers.gameSceneLayer.setDebugMode(debug));

        installMainSceneResizeHandler(mainScene);
    }

    @Override
    public ActionBindingsRegistry actionBindings() {
        return actionBindings;
    }

    @Override
    public GameActionContext actionContext() {
        return actionContext;
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
        actionContext.engineLife().suspendGame();
        actionContext.soundManager().stopAll();
        actionContext.soundManager().voice().stop();
        actionBindings.dispose();
        layers.gameSceneLayer().uninstallKeyBindings();
    }

    @Override
    public void onQuit() {
        actionContext.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::onQuit);
        actionContext.ui().viewManager().selectStartPagesView();
    }

    @Override
    public void onInput(Input input) {
        // First look for an action of the play view itself that is triggered by the input.
        // If none is found, delegate to the current game scene.
        if (actionBindings.executeMatchingAction(actionContext).isEmpty()) {
            actionContext.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::onInput);
        }
    }

    @Override
    public StackPane rootPane() {
        return root;
    }

    // --- Component update

    public void update() {
        updateDashboard();
        updateMiniView();
    }

    private void updateDashboard() {
        if (layers.overlayLayer().isVisible()) {
            dashboard.update(actionContext);
        }
    }

    private void updateMiniView() {
        final boolean playScene3DActive = actionContext.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
        layers.miniViewLayer().update(playScene3DActive);
    }

    // Private

    private void installMainSceneResizeHandler(GameMainScene mainScene) {
        final ChangeListener<? super Number> handler = (_, _, _)
            -> layers.gameSceneLayer().resizeTo(mainScene.getWidth(), mainScene.getHeight());

        mainScene.widthProperty() .addListener(handler);
        mainScene.heightProperty().addListener(handler);
    }
}