/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.settings.ui.DashboardSectionSettings;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboard;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboardSection;
import de.amr.pacmanfx.ui.views.help.HelpView;
import de.amr.pacmanfx.ui.vm.Game2DSettingsVM;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
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
 *
 */
public class GamePlayView implements GameView {

    //TODO This class is too large and does too many different things

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final Background DEBUG_BACKGROUND = Ufx.paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = Ufx.border(Color.LIGHTGREEN, 1);

    //TODO use FX controls + CSS
    public static final DecorationPane.Config DECORATION_CONFIG = new DecorationPane.Config(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPane.FrameConfig(26, 10, 5, 55.0, ArcadePalette.ARCADE_WHITE)
    );

    public record Layers(
        BorderPane gameSceneLayer,
        MiniPlaySceneView miniViewLayer,
        BorderPane overlayLayer,
        HelpView helpLayer,
        FontAwesomeIcon pausedIcon) {}

    // non-static members

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Action Bindings for Play View");

    private GameAppContext app;

    private ContextMenuManager contextMenuManager;

    private StackPane rootPane;

    private Layers layers;

    private DecorationPane decorationPane;

    private GameDashboard dashboard;

    private final RenderManager renderManager = new RenderManager();

    public GamePlayView() {
        createLayout();
    }

    public Layers layers() {
        return layers;
    }

    @Override
    public void setApp(GameAppContext app) {
        this.app = requireNonNull(app);

        final GameViewModel vm = app.ui().viewModel();

        layers.miniViewLayer().setGameApp(app);

        layers.pausedIcon().visibleProperty().bind(app.clock().updatesDisabledProperty());

//        vm.common2DSettings().fontSmoothingOnProperty().addListener((_, _, smoothing) -> renderManager.setGameSceneFontSmoothing(smoothing));

        vm.debugModeOnProperty().addListener((_, _, debug) -> {
            layers.gameSceneLayer().setBackground(debug ? DEBUG_BACKGROUND : null);
            layers.gameSceneLayer().setBorder(debug ? DEBUG_BORDER : null);
        });

        layers.overlayLayer().visibleProperty().bind(dashboard.visibleProperty());

        layers.miniViewLayer().rootPane().visibleProperty().bind(Bindings.createObjectBinding(
            () -> vm.miniViewSettings().activeProperty.get()
                && app.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D),
            vm.miniViewSettings().activeProperty,
            app.ui().gameScenes().currentGameSceneProperty()
        ));

        // Always resize to main scene
        final GameMainScene mainScene = app.ui().window().mainScene();

        final ChangeListener<? super Number> mainSceneResizeHandler = (_, _, _) -> resizeToFit(mainScene);
        mainScene.widthProperty() .addListener(mainSceneResizeHandler);
        mainScene.heightProperty().addListener(mainSceneResizeHandler);

        // Context menu
        contextMenuManager = new ContextMenuManager(app, mainScene);
        rootPane.setOnContextMenuRequested(contextMenuManager);
    }

    public void resizeToFit(Scene parentSceneFX) {
        decorationPane.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
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
        layers.helpLayer().showHelpPopup(app, scaling, app.gameVariants().currentVariantName());
    }

    public void setGameSceneContent(Node gameSceneContent) {
        layers.gameSceneLayer().setCenter(gameSceneContent);
    }

    public void onLevelCreated(GameLevel level) {
        showMiniView(level);
        // game scene size might have changed: re-embed
        final GameSceneManager gameSceneManager = app.ui().gameScenes();
        gameSceneManager.optCurrentGameScene().ifPresent(this::embedGameScene);
    }

    public void onLevelCompleted() {
        hideMiniView();
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
            app.ui().gameScenes().optCurrentGameScene().ifPresent(GameScene::onInput);
        }
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();

        actionBindings.registerAllBindings(app.commonActions().bindings());
        Logger.info(actionBindings);

        decorationPane.installBindings();
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
    public void handleQuit(GameAppContext app) {
        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.handleQuit(app));
        app.ui().views().selectStartPagesView();
    }

    @Override
    public void render() {
        final long tick = app.clock().currentTick();
        final GameViewModel viewModel = app.ui().viewModel();
        final boolean debugMode = viewModel.debugModeOnProperty().get();

        renderManager.clearRenderQueue();
        renderManager.addAll(app.game().session().hud().renderables());
        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            renderManager.add(gameScene);
            renderManager.addAll(gameScene.renderables());
        });

        try {
            renderManager.renderFrame(app.game().session(), tick, debugMode);

            //TODO integrate into render manager
            final MiniViewRenderer miniViewRenderer = renderManager.createMiniViewRenderer(
                viewModel,
                app.game().variant().systems().actorSpriteAnimController(),
                app.currentGameVariantUIConfig().renderConfig()
            );
            //miniViewRenderer.render(miniView, tick);
        }
        catch (Exception x) {
            Logger.error(x, "Exception during rendering!");
        }

        // Dashboard must always be updated, so do it in the render step!
        if (layers.overlayLayer().isVisible()) {
            dashboard.update(app);
        }
    }

    @Override
    public StackPane rootPane() {
        return rootPane;
    }

    // Context menu handler


    public void replaceGameScene(GameScene currentGameScene, GameScene nextGameScene) {
        requireNonNull(nextGameScene);
        if (currentGameScene != null) {
            disembedGameScene(currentGameScene);
        }
        nextGameScene.onBeforeEmbedded();
        embedGameScene(nextGameScene);
    }

    public void embedGameScene(GameScene gameScene) {
        final GameMainScene mainScene = app.ui().window().mainScene();
        final GameVariantUIConfig config = app.gameVariants().currentGameVariant().uiConfig();

        contextMenuManager.hideContextMenu();

        //TODO FIXME(We must discriminate 3D, 2D+subscene, 2D without subscene) here!
        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(mainScene, gameScene, gameScene.optSubSceneFX().get());
        } else {
            embedGameScene2D(decorationPane, mainScene, config.gameSceneConfig(), gameScene, app.ui().viewModel().common2DSettings());
        }

        renderManager.updateRenderers(app, gameScene);
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

        if (gameScene.hasComp(SceneCanvasRenderingComp.class)) {
            final SceneCanvasRenderingComp r2D = gameScene.reqComp(SceneCanvasRenderingComp.class);

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

    private void createLayout() {

        // Layer 1: Game scene with or without decoration
        decorationPane = new DecorationPane(
            DECORATION_CONFIG,
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );

        final var gameScenePane = new BorderPane();
        gameScenePane.setCenter(decorationPane);

        // Layer 2: Mini view layer
        final var miniView = new MiniPlaySceneView();
        StackPane.setAlignment(miniView.rootPane(), Pos.TOP_RIGHT);

        // Layer 3: Overlay layer with dashboard
        dashboard = new GameDashboard();
        dashboard.setVisible(false);

        final var overlayPane = new BorderPane();
        overlayPane.setLeft(dashboard);

        // Layer 4: Help info
        final var helpView = new HelpView(gameScenePane);

        // Layer 4: "Paused" icon
        final var pausedIcon = new FontAwesomeIcon(FontAwesomeSymbol.PAUSE);
        pausedIcon.setId("paused-icon");
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        layers = new Layers(gameScenePane, miniView, overlayPane, helpView, pausedIcon);

        rootPane = new StackPane(gameScenePane, miniView.rootPane(), overlayPane, helpView, pausedIcon);
        rootPane.setId("game-play-view");
    }

    private void showMiniView(GameLevel level) {
        layers.miniViewLayer().setWorldSizeInPixel(level.worldMap().terrainLayer().sizeInPixel());
        layers.miniViewLayer().slideIn(app.ui().viewModel().miniViewSettings());
    }

    private void hideMiniView() {
        layers.miniViewLayer().slideOut(app.ui().viewModel().miniViewSettings());
    }

    // 3D scenes or 2D scenes with camera
    private void embedGameSceneWithSubSceneFX(GameMainScene mainScene, GameScene gameScene, SubScene subSceneFX) {
        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(mainScene.widthProperty());
        subSceneFX.heightProperty().bind(mainScene.heightProperty());

        if (gameScene.hasComp(SceneCanvasRenderingComp.class)) {
            final SceneCanvasRenderingComp r2D = gameScene.reqComp(SceneCanvasRenderingComp.class);
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
        final SceneCanvasRenderingComp canvasRendering = gameScene.reqComp(SceneCanvasRenderingComp.class);

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