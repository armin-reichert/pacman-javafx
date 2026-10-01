/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.ui.assets.ArcadeColor;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneDebugView;
import de.amr.pacmanfx.ui.gamescene.common.GameVariantGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneRendering2DComp;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;
import de.amr.pacmanfx.ui.rendering.RenderManager;
import de.amr.pacmanfx.ui.settings.ui.DashboardSectionSettings;
import de.amr.pacmanfx.ui.viewmodel.Game2DSettingsVM;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboard;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboardSection;
import de.amr.pacmanfx.ui.views.help.HelpView;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import de.amr.pacmanfx.uilib.widgets.decorationpane.FramedGameSceneContainer;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneBorderConfig;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneConfig;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SubScene;
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
        new DecorationPaneBorderConfig(26, 10, 5, 55.0, ArcadeColor.WHITE.color())
    );

    // non-static members

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsRegistry("Action Bindings for Play View");

    private GameApp app;

    private ContextMenuManager contextMenuManager;

    private final StackPane rootPane;

    private Layers layers;

    private final BorderPane gameScenePane = new BorderPane();

    private FramedGameSceneContainer framedContainer;

    private GameSceneContainer plainContainer;

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
        installResizeHandler();

        contextMenuManager = new ContextMenuManager(app, app.ui().window().mainScene());
        rootPane.setOnContextMenuRequested(contextMenuManager);

        dashboard.setAppContext(app);
    }

    private void installResizeHandler() {
        final GameMainScene mainScene = app.ui().window().mainScene();
        final ChangeListener<? super Number> handler = (_, _, _) ->
            framedContainer.stretchTo(mainScene.getWidth(), mainScene.getHeight());
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

    public void showHelp(GameApp app) {
        final double scaling = framedContainer.scalingProperty().get();
        layers.helpLayer().showHelpPopup(app, scaling, app.variantManager().currentVariantName());
    }

    public void setGameSceneContainer(Node gameSceneContent) {
        layers.gameSceneLayer().setCenter(gameSceneContent);
    }

    public void onLevelCreated(GameLevel level) {
        layers.miniViewLayer().setLevel(level);

        // game scene size might have changed: re-embed
        app.gameSceneManager().optCurrentGameScene().ifPresent(this::embedGameScene);
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
        framedContainer.installBindings();

        Logger.info(actionBindings);
    }

    @Override
    public void onExit() {
        app.suspendGame();
        app.ui().soundManager().stopAll();
        app.ui().soundManager().voice().stop();
        actionBindings.dispose();
        framedContainer.uninstallBindings();
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
            disembedGameScene(currentGameScene);
        }
        nextGameScene.onBeforeEmbedded();
        embedGameScene(nextGameScene);
    }

    public void disembedGameScene(GameScene gameScene) {
        requireNonNull(gameScene);

        gameScene.deactivate();
        contextMenuManager.hideContextMenu();

        gameScene.optSubSceneFX().ifPresent(subSceneFX -> {
            subSceneFX.widthProperty().unbind();
            subSceneFX.heightProperty().unbind();
        });

        framedContainer.unscaledWidthProperty().unbind();
        framedContainer.unscaledHeightProperty().unbind();
        framedContainer.backgroundProperty().unbind();

        Logger.info("Game scene {} DISEMBEDDED from play view!", gameScene.getClass().getSimpleName());
    }


    // Private

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

    private void embedGameScene(GameScene gameScene) {
        requireNonNull(gameScene);

        final GameMainScene mainScene = app.ui().window().mainScene();
        final GameVariantUIConfig config = app.variantManager().currentRuntime().uiConfig();

        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(mainScene, gameScene, gameScene.optSubSceneFX().get());
        } else {
            embedGameScene2D(mainScene, config.gameSceneConfig(), gameScene, app.ui().viewModel().common2DSettings());
        }

        contextMenuManager.hideContextMenu();
        gameScene.activate();

        Logger.info("Game scene {} EMBEDDED into play view!", gameScene.getClass().getSimpleName());
    }

    private void createLayers() {
        // Layer 1: Game scene with optional decoration
        framedContainer = new FramedGameSceneContainer(
            DECORATION_PANE_CONFIG,
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );

        plainContainer = new GameSceneContainer();

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

        if (!(gameScene instanceof AbstractGameScene abstractGameScene)) {
            Logger.error("Current game scene is not an AbstractGameScene");
            return;
        }

        if (abstractGameScene.hasComp(GameSceneRendering2DComp.class)) {
            final GameSceneRendering2DComp r2D = abstractGameScene.reqComp(GameSceneRendering2DComp.class);
            r2D.setRenderingSurface(plainContainer.renderingSurface());
        }
        setGameSceneContainer(subSceneFX);
        gameScenePane.setCenter(plainContainer);
    }

    // 2D scenes without camera which are shown at full size
    private void embedGameScene2D(
        GameMainScene mainScene,
        GameVariantGameSceneConfig gameSceneConfig,
        GameScene gameScene,
        Game2DSettingsVM settingsViewModel)
    {
        if (!(gameScene instanceof AbstractGameScene abstractGameScene)) {
            Logger.error("Current game scene is not an AbstractGameScene");
            return;
        }
        final GameSceneRendering2DComp r2d = abstractGameScene.reqComp(GameSceneRendering2DComp.class);
        final ObservableValue<Background> containerBackground = settingsViewModel.canvasBackgroundColorProperty().map(Ufx::paintBackground);

        final boolean decorated = gameSceneConfig.sceneDecorationRequested(gameScene);
        if (decorated) {
            framedContainer.newRenderingSurface(); //TODO check if creating a new canvas is needed
            framedContainer.backgroundProperty().bind(containerBackground);

            // Set unscaled decoration pane size to game scene (=world map) size
            framedContainer.unscaledWidthProperty().bind(r2d.unscaledWidthProperty());
            framedContainer.unscaledHeightProperty().bind(r2d.unscaledHeightProperty());

            // Limit scaling
            framedContainer.renderingSurface().scalingProperty().bind(framedContainer.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), GamePlayView.MAX_GAME_SCENE_SCALING)));

            framedContainer.renderingSurface().clear();
            framedContainer.stretchTo(mainScene.getWidth(), mainScene.getHeight());

            r2d.setRenderingSurface(framedContainer.renderingSurface());
            gameScenePane.setCenter(framedContainer);

            setGameSceneContainer(framedContainer);
        }
        else {
            plainContainer.backgroundProperty().bind(containerBackground);

            final RenderingSurface surface = plainContainer.renderingSurface();

            surface.heightProperty().bind(mainScene.heightProperty());

            surface.widthProperty().bind(
                mainScene.heightProperty()
                    .multiply(r2d.unscaledWidthProperty())
                    .divide(r2d.unscaledHeightProperty()));

            surface.scalingProperty().bind(
                mainScene.heightProperty()
                    .divide(r2d.unscaledHeightProperty()));

            r2d.setRenderingSurface(plainContainer.renderingSurface());
            gameScenePane.setCenter(plainContainer);

            setGameSceneContainer(plainContainer);
        }

    }
}