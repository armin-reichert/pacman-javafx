/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.ui.settings.ui.DashboardSectionSettings;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboard;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboardSection;
import de.amr.pacmanfx.ui.views.help.HelpView;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.amr.pacmanfx.ui.views.ContextMenuSupport.addLocalizedActionItem;
import static de.amr.pacmanfx.ui.views.ContextMenuSupport.addLocalizedTitleItem;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GamePlayView implements GameView, EventHandler<ContextMenuEvent> {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final Background DEBUG_BACKGROUND = Ufx.paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = Ufx.border(Color.LIGHTGREEN, 1);

    //TODO use FX controls + CSS
    public static final DecorationPane.Config DECORATION_CONFIG = new DecorationPane.Config(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPane.FrameConfig(26, 10, 5, 55.0, ArcadePalette.ARCADE_WHITE)
    );

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Action Bindings for Play View");

    private GameAppContext app;

    private final ContextMenu contextMenu = new ContextMenu();

    private StackPane rootPane;

    // Game scene layer
    private BorderPane gameSceneLayer;
    private DecorationPane gameSceneFrame;

    // Mini view layer
    private MiniPlaySceneView miniView;

    // Overlay layer
    private BorderPane overlayLayer;
    private GameDashboard dashboard;

    // Help layer
    private HelpView helpLayer;

    // Icon layer
    private FontAwesomeIcon pausedIcon;

    private final RendererRegistry rendererRegistry = new  RendererRegistry();

    public GamePlayView() {
        createLayout();
    }

    @Override
    public void setApp(GameAppContext app) {
        this.app = requireNonNull(app);

        final GameViewModel vm = app.ui().viewModel();

        // Context menu

        rootPane.setOnContextMenuRequested(this);
        app.ui().window().mainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
        });

        miniView.setGameApp(app);

        pausedIcon.visibleProperty().bind(app.clock().updatesDisabledProperty());

        vm.common2DSettings().fontSmoothingOnProperty().addListener((_, _, smoothing) -> rendererRegistry.setFontSmoothing(smoothing));

        vm.debugModeOnProperty().addListener((_, _, debug) -> {
            gameSceneLayer.setBackground(debug ? DEBUG_BACKGROUND : null);
            gameSceneLayer.setBorder(debug ? DEBUG_BORDER : null);
        });

        overlayLayer.visibleProperty().bind(dashboard.visibleProperty());

        miniView.rootPane().visibleProperty().bind(Bindings.createObjectBinding(
            () -> vm.miniViewSettings().activeProperty.get()
                && app.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D),
            vm.miniViewSettings().activeProperty,
            app.ui().gameScenes().currentGameSceneProperty()
        ));

        // Keep this view always at the same size as the main scene
        final GameMainScene mainScene = app.ui().window().mainScene();
        final ChangeListener<? super Number> resizeHandler = (_, _, _) -> resizeToFit(mainScene);
        mainScene.widthProperty().addListener(resizeHandler);
        mainScene.heightProperty().addListener(resizeHandler);
    }

    public void resizeToFit(Scene parentSceneFX) {
        gameSceneFrame.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
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

    public MiniPlaySceneView miniPlaySceneView() {
        return miniView;
    }

    public void showHelp(GameAppContext app) {
        final double scaling = gameSceneFrame.scalingProperty().get();
        helpLayer.showHelpPopup(app, scaling, app.gameVariants().currentVariantName());
    }

    public void setGameSceneContent(Node gameSceneContent) {
        gameSceneLayer.setCenter(gameSceneContent);
    }

    public void onLevelCreated(GameContext game, GameLevel level) {
        showMiniView(game, level);
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

        gameSceneFrame.installBindings();
    }

    @Override
    public void onExit() {
        app.suspendGame();
        app.ui().soundManager().stopAll();
        app.ui().soundManager().voice().stop();
        actionBindings.dispose();
        gameSceneFrame.uninstallBindings();
    }

    @Override
    public void handleQuit(GameAppContext app) {
        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.handleQuit(app));
        app.ui().views().selectStartPagesView();
    }

    @Override
    public StackPane rootPane() {
        return rootPane;
    }

    // Context menu handler

    @Override
    public void handle(ContextMenuEvent event) {
        contextMenu.getItems().clear();

        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            final TranslationManager translations = app.ui().translations();
            // Add 2D play scene-specific entries
            if (app.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D)) {
                addLocalizedTitleItem(contextMenu, translations, "context_menu.scene_display");
                addLocalizedActionItem(
                    app,
                    contextMenu,
                    translations,
                    app.commonActions().uiSettingsActions().actionTogglePlayScene2D3D(),
                    "context_menu.use_3D_scene");
            }
            // Add scene-specific entries
            gameScene.optContextMenu().ifPresent(sceneMenu -> contextMenu.getItems().addAll(sceneMenu.getItems()));
        });

        if (!contextMenu.getItems().isEmpty()) {
            contextMenu.show(rootPane, event.getScreenX(), event.getScreenY());
            contextMenu.requestFocus();
        }
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
        final GameVariantUIConfig config = app.gameVariants().currentGameVariant().uiConfig();

        contextMenu.hide();

        //TODO FIXME(We must discriminate 3D, 2D+subscene, 2D without subscene) here!
        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(gameScene, gameScene.optSubSceneFX().get());
        } else {
            embedGameScene2D(config.gameSceneConfig(), gameScene);
        }

        gameScene.activate();

        Logger.info("Game scene {} EMBEDDED into play view!", gameScene.getClass().getSimpleName());
    }

    public void disembedGameScene(GameScene gameScene) {
        requireNonNull(gameScene);

        gameScene.deactivate();
        contextMenu.hide();

        gameScene.optSubSceneFX().ifPresent(subSceneFX -> {
            subSceneFX.widthProperty().unbind();
            subSceneFX.heightProperty().unbind();
        });

        if (gameScene.hasComp(CanvasRenderingComp.class)) {
            final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);

            gameSceneFrame.canvas().widthProperty().unbind();
            gameSceneFrame.canvas().heightProperty().unbind();
            gameSceneFrame.unscaledWidthProperty().unbind();
            gameSceneFrame.unscaledHeightProperty().unbind();
            gameSceneFrame.backgroundProperty().unbind();

            r2D.backgroundColorProperty().unbind();
            r2D.scalingProperty().unbind();
        }

        Logger.info("Game scene {} DISEMBEDDED from play view!", gameScene.getClass().getSimpleName());
    }


    // Private

    private void createLayout() {

        // Layer 1: Game scene with or without decoration
        gameSceneFrame = new DecorationPane(
            DECORATION_CONFIG,
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );
        gameSceneLayer = new BorderPane();
        gameSceneLayer.setCenter(gameSceneFrame);

        // Layer 2: Mini view layer
        miniView = new MiniPlaySceneView();
        StackPane.setAlignment(miniView.rootPane(), Pos.TOP_RIGHT);

        // Layer 3: Overlay layer with dashboard
        dashboard = new GameDashboard();
        dashboard.setVisible(false);

        overlayLayer = new BorderPane();
        overlayLayer.setLeft(dashboard);

        // Layer 4: Help info
        helpLayer = new HelpView(gameSceneLayer);

        // Layer 4: "Paused" icon
        pausedIcon = new FontAwesomeIcon(FontAwesomeSymbol.PAUSE);
        pausedIcon.setId("paused-icon");
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        rootPane = new StackPane(gameSceneLayer, miniView.rootPane(), overlayLayer, helpLayer, pausedIcon);
        rootPane.setId("game-play-view");
    }

    private void showMiniView(GameContext game, GameLevel level) {
        final GameVariantRenderConfig renderConfig = app.gameVariants().currentGameVariant().uiConfig().renderConfig();
        final ActorSpriteAnimController animController = game.variant().systems().actorSpriteAnimController();
        miniView.setRenderConfig(animController, renderConfig);
        miniView.setWorldSizeInPixel(level.worldMap().terrainLayer().sizeInPixel());
        miniView.slideIn(app.ui().viewModel().miniViewSettings());
    }

    private void hideMiniView() {
        miniView.slideOut(app.ui().viewModel().miniViewSettings());
    }

    // 3D scenes or 2D scenes with camera
    private void embedGameSceneWithSubSceneFX(GameScene gameScene, SubScene subSceneFX) {
        final GameMainScene mainScene = app.ui().window().mainScene();

        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(mainScene.widthProperty());
        subSceneFX.heightProperty().bind(mainScene.heightProperty());

        if (gameScene.hasComp(CanvasRenderingComp.class)) {
            final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);
            // use the canvas of the decorated pane for 2D scene even though the decoration is not used
            r2D.setCanvas(gameSceneFrame.canvas());
            updateRenderers(gameScene);
        }
        setGameSceneContent(subSceneFX);
    }

    // 2D scenes without camera which are shown at full size
    private void embedGameScene2D(GameSceneConfig gameSceneConfig, GameScene gameScene) {
        final GameMainScene mainScene = app.ui().window().mainScene();
        final CanvasRenderingComp canvasRendering = gameScene.reqComp(CanvasRenderingComp.class);

        canvasRendering.backgroundColorProperty().bind(app.ui().viewModel().common2DSettings().canvasBackgroundColorProperty());

        final boolean decorated = gameSceneConfig.sceneDecorationRequested(gameScene);
        if (decorated) {
            gameSceneFrame.newCanvas(); //TODO check if creating a new canvas is needed
            gameSceneFrame.backgroundProperty().bind(canvasRendering.backgroundColorProperty().map(Ufx::paintBackground));

            // Set unscaled decoration pane size to game scene (=world map) size
            gameSceneFrame.unscaledWidthProperty().bind(canvasRendering.unscaledWidthProperty());
            gameSceneFrame.unscaledHeightProperty().bind(canvasRendering.unscaledHeightProperty());

            // Limit scaling
            canvasRendering.scalingProperty().bind(gameSceneFrame.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), GamePlayView.MAX_GAME_SCENE_SCALING)));

            gameSceneFrame.stretchTo(mainScene.getWidth(), mainScene.getHeight());
            setGameSceneContent(gameSceneFrame);
        }
        else {
            final Canvas canvas = gameSceneFrame.canvas();
            // Undecorated game scene takes complete available height
            canvas.heightProperty().bind(mainScene.heightProperty());
            // Width adapts according to aspect ratio
            canvas.widthProperty().bind(mainScene.heightProperty().map(h -> h.doubleValue() * canvasRendering.aspectRatio()));
            canvasRendering.scalingProperty().bind(mainScene.heightProperty().divide(canvasRendering.unscaledHeight()));
            setGameSceneContent(gameSceneFrame.canvas());
        }

        canvasRendering.setCanvas(gameSceneFrame.canvas());
        updateRenderers(gameScene);
        gameSceneFrame.clearCanvas();
    }


    // ---- Rendering

    @Override
    public void render() {
        final GameSession session = app.game().session();
        final long tick = app.clock().currentTick();

        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            try {
                gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
                    if (canvasRendering.clearCanvasBeforeRendering()) {
                        rendererRegistry.sceneRenderer().clearCanvas();
                    }
                    rendererRegistry.sceneRenderer().render(gameScene, tick);
                    rendererRegistry.hudRenderer().drawHUD(session.hud(), session, gameScene, tick);
                    rendererRegistry.hudRenderer().drawMessage(session);

                    session.optLevel().ifPresent(level -> {
                        entitiesInRenderingOrder(level.entities()).forEach(
                            actor -> rendererRegistry.actorRenderer.render(actor, tick));
                    });

                    if (gameScene.viewModel().debugModeOnProperty().get()) {
                        rendererRegistry.debugRenderer().render(gameScene, tick);
                    }
                });
                miniView.draw();
            } catch (Exception x) {
                Logger.error(x, "Exception during rendering!");
            }
        });

        // Dashboard must always be updated even if simulation is stopped!
        if (overlayLayer.isVisible()) {
            dashboard.update(app);
        }
    }

    private List<GameEntity> entitiesInRenderingOrder(GameLevelEntitySet entities) {
        return entities.all()
            .filter(e -> e.hasComp(RenderingComp.class))
            .sorted((e1, e2) -> RenderingComp.RENDERING_ORDER.compare(
                e1.reqComp(RenderingComp.class),
                e2.reqComp(RenderingComp.class)))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private void updateRenderers(GameScene gameScene) {
        requireNonNull(gameScene);

        final CanvasRenderingComp canvasRendering = gameScene.reqComp(CanvasRenderingComp.class);
        final ActorSpriteAnimController animController = app.game().variant().systems().actorSpriteAnimController();
        final GameVariantRenderConfig renderConfig = app.currentGameVariantUIConfig().renderConfig();
        final Canvas canvas = canvasRendering.canvas();

        if (canvas != null) {
            rendererRegistry.setActorRenderer(canvasRendering.configureRenderer(renderConfig.createActorRenderer(animController, canvas)));
            rendererRegistry.setSceneRenderer(renderConfig.createGameSceneRenderer(gameScene, animController, canvas));
            rendererRegistry.setHudRenderer(renderConfig.createHUDRenderer(gameScene, animController, canvas)); // may return null!
            rendererRegistry.setFontSmoothing(app.ui().viewModel().common2DSettings().fontSmoothingOnProperty().get());
            rendererRegistry.setDebugRenderer(canvasRendering.configureRenderer(new BaseGameSceneDebugInfoRenderer(animController, canvas)));
        } else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
    }

    static class RendererRegistry {
        private BaseRenderer actorRenderer;
        private BaseRenderer sceneRenderer;
        private HUD_Renderer hudRenderer;
        private BaseRenderer debugRenderer;

        public void setFontSmoothing(boolean smoothing) {
            sceneRenderer.ctx().setFontSmoothingType(smoothing ? FontSmoothingType.LCD : FontSmoothingType.GRAY);
        }

        public BaseRenderer actorRenderer() {
            return actorRenderer;
        }

        public void setActorRenderer(BaseRenderer actorRenderer) {
            this.actorRenderer = actorRenderer;
        }

        public BaseRenderer sceneRenderer() {
            return sceneRenderer;
        }

        public void setSceneRenderer(BaseRenderer sceneRenderer) {
            this.sceneRenderer = sceneRenderer;
        }

        public HUD_Renderer hudRenderer() {
            return hudRenderer;
        }

        public void setHudRenderer(HUD_Renderer hudRenderer) {
            this.hudRenderer = hudRenderer;
        }

        public BaseRenderer debugRenderer() {
            return debugRenderer;
        }

        public void setDebugRenderer(BaseRenderer debugRenderer) {
            this.debugRenderer = debugRenderer;
        }
    }
}