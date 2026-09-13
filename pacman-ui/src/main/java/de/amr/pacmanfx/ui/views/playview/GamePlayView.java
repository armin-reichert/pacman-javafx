/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.RenderManager;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.settings.ui.DashboardSectionSettings;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboard;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboardSection;
import de.amr.pacmanfx.ui.views.help.HelpView;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.vm.Game2DSettingsVM;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPane;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneBorderConfig;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneConfig;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GamePlayView implements GameView {

    public record Layers(
        BorderPane gameSceneLayer,
        MiniPlaySceneView miniViewLayer,
        BorderPane overlayLayer,
        HelpView helpLayer,
        StackPane iconLayer)
    {}

    //TODO This class is too large and does too many different things

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final Background DEBUG_BACKGROUND = Ufx.paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = Ufx.border(Color.LIGHTGREEN, 1);

    //TODO use FX controls + CSS
    public static final DecorationPaneConfig DECORATION_PANE_CONFIG = new DecorationPaneConfig(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPaneBorderConfig(26, 10, 5, 55.0, ArcadePalette.ARCADE_WHITE)
    );

    // non-static members

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsRegistry("Action Bindings for Play View");

    private GameAppContext app;

    private ContextMenuManager contextMenuManager;

    private final StackPane rootPane;

    private Layers layers;

    private DecorationPane decorationPane;

    private GameDashboard dashboard;

    public GamePlayView() {
        createLayers();

        rootPane = new StackPane(
            layers.gameSceneLayer(),
            layers.miniViewLayer(),
            layers.overlayLayer(),
            layers.helpLayer(),
            layers.iconLayer()
        );
        rootPane.setId("game-play-view");

        StackPane.setAlignment(layers.miniViewLayer(), Pos.TOP_RIGHT);
        StackPane.setAlignment(layers.iconLayer(), Pos.CENTER);
    }

    public Layers layers() {
        return layers;
    }

    @Override
    public void setApp(GameAppContext app) {
        this.app = requireNonNull(app);

        initLayers(app.ui().viewModel());
        installResizeHandler();

        contextMenuManager = new ContextMenuManager(app, app.ui().window().mainScene());
        rootPane.setOnContextMenuRequested(contextMenuManager);

        dashboard.setAppContext(app);
    }

    private void installResizeHandler() {
        final GameMainScene mainScene = app.ui().window().mainScene();
        final ChangeListener<? super Number> handler = (_, _, _) ->
            decorationPane.stretchTo(mainScene.getWidth(), mainScene.getHeight());
        mainScene.widthProperty() .addListener(handler);
        mainScene.heightProperty().addListener(handler);
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

    public GameDashboard dashboard() {
        return dashboard;
    }

    public void populateDashboard(
        DashboardFactory factory,
        List<DashboardSectionSettings> sectionDefinitions,
        TranslationManager translations)
    {
        for (var sectionDef : sectionDefinitions) {
            factory.identify(sectionDef.id()).ifPresentOrElse(dashboardID -> {
                final GameDashboardSection section = factory.createSection(dashboard, dashboardID, translations);
                dashboard.addSection(section);
                section.setDisplayedStandalone(sectionDef.standalone());
                section.setExpanded(sectionDef.expanded());
            }, () -> Logger.error("Unknown dashboard ID: {}", sectionDef.id()));
        }
    }

    public void showHelp(GameAppContext app) {
        final double scaling = decorationPane.scalingProperty().get();
        layers.helpLayer().showHelpPopup(app, scaling, app.variantManager().currentVariantName());
    }

    public void setGameSceneContent(Node gameSceneContent) {
        layers.gameSceneLayer().setCenter(gameSceneContent);
    }

    public void onLevelCreated(GameLevel level) {
        layers.miniViewLayer().setLevel(level);

        // game scene size might have changed: re-embed
        app.gameSceneManager().optCurrentGameScene().ifPresent(this::embedGameScene);
    }

    public void onLevelCompleted() {
    }

    // -----------------------------------------------------------------------------------------------------------------
    // View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingsRegistry actionBindings() {
        return actionBindings;
    }

    @Override
    public void onInput(GameAppContext app) {
        // First look for a matching action of the play view itself; if none found, delegate to the current game scene.
        if (actionBindings.executeMatchingAction(app).isEmpty()) {
            app.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::onInput);
        }
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();
        actionBindings.registerAllBindings(app.commonActions().bindings());
        decorationPane.installBindings();

        Logger.info(actionBindings);
    }

    @Override
    public void onExit() {
        app.suspendGame();
        app.ui().soundManager().stopAll();
        app.ui().soundManager().voice().stop();
        actionBindings.dispose();
        decorationPane.uninstallBindings();
    }

    @Override
    public void onQuit() {
        app.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::onQuit);
        app.ui().viewManager().selectStartPagesView();
    }

    public void render(RenderManager renderManager, long tick) {
        final GameViewModel viewModel = app.ui().viewModel();
        final boolean debugMode = viewModel.debugModeOnProperty().get();

        renderManager.renderQueue().clear();

        final GameSession session = app.game().session();
        if (session.isHUDVisible()) {
            renderManager.renderQueue().addAll(session.hud().renderables());
        }

        renderManager.renderQueue().addAll(layers.miniViewLayer().renderables());

        // Add game scene renderables
        final GameScene currentGameScene = app.gameSceneManager().optCurrentGameScene().orElse(null);
        if (currentGameScene != null) {
            renderManager.updateRenderers(
                app.currentGameVariantPlayConfig(),
                app.currentGameVariantUIConfig().renderConfig(),
                currentGameScene,
                layers.miniViewLayer()
            );
            renderManager.renderQueue().add(currentGameScene); //TODO rethink this
            renderManager.renderQueue().addAll(currentGameScene.renderables());
        }

        // Clear canvases
        layers.miniViewLayer().clearCanvas();
        if (currentGameScene != null && currentGameScene.wantsClearCanvas()) {
            renderManager.clearSceneCanvas(currentGameScene);
        }

        renderManager.renderFrame(tick, debugMode);

        // Dashboard must always be updated, so do it in the render step!
        if (layers.overlayLayer().isVisible()) {
            dashboard.update(app);
        }

        layers.miniViewLayer().update(app.gameSceneManager());
    }

    @Override
    public StackPane rootPane() {
        return rootPane;
    }

    public void replaceGameScene(GameScene currentGameScene, GameScene nextGameScene) {
        requireNonNull(nextGameScene);
        if (currentGameScene != null) {
            disembedGameScene(currentGameScene);
        }
        nextGameScene.onBeforeEmbedded();
        embedGameScene(nextGameScene);
    }

    public void embedGameScene(GameScene gameScene) {
        requireNonNull(gameScene);

        final GameMainScene mainScene = app.ui().window().mainScene();
        final GameVariantUIConfig config = app.variantManager().currentVariantConfig().uiConfig();

        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(mainScene, gameScene, gameScene.optSubSceneFX().get());
        } else {
            embedGameScene2D(decorationPane, mainScene, config.gameSceneConfig(), gameScene, app.ui().viewModel().common2DSettings());
        }

        contextMenuManager.hideContextMenu();
        gameScene.activate();

        Logger.info("Game scene {} EMBEDDED into play view!", gameScene.getClass().getSimpleName());
    }

    public void disembedGameScene(GameScene gameScene) {
        requireNonNull(gameScene);

        gameScene.deactivate();
        contextMenuManager.hideContextMenu();

        gameScene.optSubSceneFX().ifPresent(subSceneFX -> {
            subSceneFX.widthProperty().unbind();
            subSceneFX.heightProperty().unbind();
        });

        if (gameScene.hasComp(GameSceneCanvasRenderingComp.class)) {
            final GameSceneCanvasRenderingComp r2D = gameScene.reqComp(GameSceneCanvasRenderingComp.class);

            decorationPane.canvas().widthProperty().unbind();
            decorationPane.canvas().heightProperty().unbind();
            decorationPane.unscaledWidthProperty().unbind();
            decorationPane.unscaledHeightProperty().unbind();
            decorationPane.backgroundProperty().unbind();

            r2D.backgroundColorProperty().unbind();
            r2D.scalingProperty().unbind();
        }

        Logger.info("Game scene {} DISEMBEDDED from play view!", gameScene.getClass().getSimpleName());
    }


    // Private

    private void createLayers() {
        // Layer 1: Game scene with optional decoration
        final var gameScenePane = new BorderPane();
        decorationPane = new DecorationPane(
            DECORATION_PANE_CONFIG,
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );
        gameScenePane.setCenter(decorationPane);

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

    // 3D scenes or 2D scenes with camera
    private void embedGameSceneWithSubSceneFX(GameMainScene mainScene, GameScene gameScene, SubScene subSceneFX) {
        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(mainScene.widthProperty());
        subSceneFX.heightProperty().bind(mainScene.heightProperty());

        if (gameScene.hasComp(GameSceneCanvasRenderingComp.class)) {
            final GameSceneCanvasRenderingComp r2D = gameScene.reqComp(GameSceneCanvasRenderingComp.class);
            // use the canvas of the decorated pane for 2D scene even though the decoration is not used
            r2D.setCanvas(decorationPane.canvas());
        }
        setGameSceneContent(subSceneFX);
    }

    // 2D scenes without camera which are shown at full size
    private void embedGameScene2D(
        DecorationPane decorationPane,
        GameMainScene mainScene,
        GameSceneConfig gameSceneConfig,
        GameScene gameScene,
        Game2DSettingsVM settingsViewModel)
    {
        final GameSceneCanvasRenderingComp canvasRendering = gameScene.reqComp(GameSceneCanvasRenderingComp.class);

        canvasRendering.backgroundColorProperty().bind(settingsViewModel.canvasBackgroundColorProperty());

        final boolean decorated = gameSceneConfig.sceneDecorationRequested(gameScene);
        if (decorated) {
            decorationPane.newCanvas(); //TODO check if creating a new canvas is needed
            decorationPane.backgroundProperty().bind(canvasRendering.backgroundColorProperty().map(Ufx::paintBackground));

            // Set unscaled decoration pane size to game scene (=world map) size
            decorationPane.unscaledWidthProperty().bind(canvasRendering.unscaledWidthProperty());
            decorationPane.unscaledHeightProperty().bind(canvasRendering.unscaledHeightProperty());

            // Limit scaling
            canvasRendering.scalingProperty().bind(decorationPane.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), GamePlayView.MAX_GAME_SCENE_SCALING)));

            decorationPane.stretchTo(mainScene.getWidth(), mainScene.getHeight());
            setGameSceneContent(decorationPane);
        }
        else {
            final Canvas canvas = decorationPane.canvas();
            // Undecorated game scene takes complete available height
            canvas.heightProperty().bind(mainScene.heightProperty());
            // Width adapts according to aspect ratio
            canvas.widthProperty().bind(mainScene.heightProperty().map(h -> h.doubleValue() * canvasRendering.aspectRatio()));
            canvasRendering.scalingProperty().bind(mainScene.heightProperty().divide(canvasRendering.unscaledHeight()));
            setGameSceneContent(decorationPane.canvas());
        }

        canvasRendering.setCanvas(decorationPane.canvas());
        decorationPane.clearCanvas();
    }
}