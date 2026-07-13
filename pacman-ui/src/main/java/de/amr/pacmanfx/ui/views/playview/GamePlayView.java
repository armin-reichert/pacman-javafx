/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.ui.config.ui.DashboardSectionSettings;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.model.GameViewModel;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboard;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboardSection;
import de.amr.pacmanfx.ui.views.help.HelpView;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
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

import java.util.List;

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

    private GameActionContext actionContext;

    private final ContextMenu contextMenu = new ContextMenu();

    private StackPane rootPane;

    // Game scene layer
    private BorderPane gameSceneLayer;
    private DecorationPane gameSceneFrame;

    // Mini view layer
    private MiniPlaySceneView miniPlaySceneView;

    // Overlay layer
    private BorderPane overlayLayer;
    private GameDashboard dashboard;

    // Help layer
    private HelpView helpLayer;

    // Icon layer
    private FontAwesomeIcon pausedIcon;

    private GameScene2D_Renderer sceneRenderer;
    private HeadsUpDisplay_Renderer hudRenderer;

    public GamePlayView() {
        createLayout();
    }

    @Override
    public void setGameActionContext(GameActionContext actionContext) {
        this.actionContext = requireNonNull(actionContext);
        final GameViewModel settings = actionContext.ui().viewModel();

        rootPane.setOnContextMenuRequested(this);
        actionContext.ui().window().mainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
        });

        miniPlaySceneView.setActionContext(actionContext);

        pausedIcon.visibleProperty().bind(actionContext.clock().updatesDisabledProperty());

        settings.common2D.fontSmoothingOnProperty.addListener((_, _, smoothing) -> setFontSmoothing(smoothing));

        settings.debugModeOnProperty.addListener((_, _, debug) -> {
            gameSceneLayer.setBackground(debug ? DEBUG_BACKGROUND : null);
            gameSceneLayer.setBorder(debug ? DEBUG_BORDER : null);
        });

        overlayLayer.visibleProperty().bind(dashboard.visibleProperty());

        miniPlaySceneView.rootPane().visibleProperty().bind(Bindings.createObjectBinding(
            () -> settings.miniView.activeProperty.get()
                && actionContext.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D),
            settings.miniView.activeProperty,
            actionContext.ui().gameScenes().currentGameSceneProperty()
        ));

        // Keep this view always at the same size as the main scene
        final GameMainScene mainScene = actionContext.ui().window().mainScene();
        final ChangeListener<? super Number> resizeHandler = (_, _, _) -> resizeToFit(mainScene);
        mainScene.widthProperty().addListener(resizeHandler);
        mainScene.heightProperty().addListener(resizeHandler);
    }

    public void resizeToFit(Scene parentSceneFX) {
        gameSceneFrame.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
    }

    public DecorationPane gameSceneFrame() {
        return gameSceneFrame;
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
        return miniPlaySceneView;
    }

    public void showHelp(GameActionContext actionContext) {
        final double scaling = gameSceneFrame.scalingProperty().get();
        helpLayer.showHelpPopup(actionContext, scaling, actionContext.variants().currentVariantName());
    }

    public void setGameSceneContent(Node gameSceneContent) {
        gameSceneLayer.setCenter(gameSceneContent);
    }

    public void onLevelCreated(GameLevel level) {
        showMiniPlayView(level);
        // game scene size might have changed: re-embed
        final GameSceneManager gameSceneManager = actionContext.ui().gameScenes();
        gameSceneManager.optCurrentGameScene().ifPresent(this::embedGameScene);
    }

    public void onLevelCompleted() {
        hideMiniPlayView();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingsRegistry actionBindings() {
        return actionBindings;
    }

    @Override
    public void onInput(Input input) {
        // First look for a matching action of the play view itself; if none found, delegate to the current game scene.
        if (actionBindings.executeMatchingAction(input).isEmpty()) {
            actionContext.ui().gameScenes().optCurrentGameScene().ifPresent(GameScene::onInput);
        }
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();

        actionBindings.registerAllBindings(actionContext.commonActions().bindings());
        Logger.info(actionBindings);

        gameSceneFrame.installBindings();
    }

    @Override
    public void onExit() {
        actionContext.lifecycle().suspendGamePlay();
        actionContext.ui().sounds().stopAll();
        actionContext.ui().sounds().stopAndDisposeVoice();
        actionBindings.dispose();
        gameSceneFrame.uninstallBindings();
    }

    @Override
    public void handleQuit(GameActionContext actionContext) {
        actionContext.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.handleQuit(actionContext));
        actionContext.ui().views().selectStartPagesView();
    }

    @Override
    public StackPane rootPane() {
        return rootPane;
    }

    @Override
    public void render() {
        final long tick = actionContext.clock().currentTick();
        // Render current 2D game scene
        final GameScene gameScene = actionContext.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            final GameModel gameModel = actionContext.currentGameContext().model();
            if (sceneRenderer != null) {
                sceneRenderer.draw(gameScene2D, tick);
            }
            if (hudRenderer != null) {
                hudRenderer.draw(gameModel.hudState(), actionContext.currentGameContext(), gameScene2D, tick);
            }
        }

        // Render mini view content
        miniPlaySceneView.draw();

        // Dashboard must always be updated even if simulation is stopped!
        if (overlayLayer.isVisible()) {
            dashboard.update(actionContext);
        }
    }

    // Context menu handler

    @Override
    public void handle(ContextMenuEvent event) {
        contextMenu.getItems().clear();

        actionContext.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            final TranslationManager translations = actionContext.ui().translations();
            // Add 2D play scene-specific entries
            if (actionContext.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D)) {
                addLocalizedTitleItem(contextMenu, translations, "context_menu.scene_display");
                addLocalizedActionItem(contextMenu, translations, actionContext.commonActions().uiSettingsActions().actionTogglePlayScene2D3D(),
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

    public void updateGameSceneRenderers(AbstractGameScene2D gameScene2D) {
        final GameVariantConfig gameVariantConfig = actionContext.variants().currentVariant().config();
        if (gameScene2D.canvas() != null) {
            sceneRenderer = gameVariantConfig.createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
            setFontSmoothing(actionContext.ui().viewModel().common2D.fontSmoothingOnProperty.get());
            hudRenderer = gameVariantConfig.createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
        } else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
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
        final GameVariantConfig config = actionContext.variants().currentVariant().config();

        contextMenu.hide();

        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(gameScene, gameScene.optSubSceneFX().get());
        } else if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            embedGameScene2D(config.gameSceneConfig(), gameScene2D);
        } else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
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

        if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            gameSceneFrame.canvas().widthProperty().unbind();
            gameSceneFrame.canvas().heightProperty().unbind();
            gameSceneFrame.unscaledWidthProperty().unbind();
            gameSceneFrame.unscaledHeightProperty().unbind();
            gameSceneFrame.backgroundProperty().unbind();
            gameScene2D.backgroundColorProperty().unbind();
            gameScene2D.scalingProperty().unbind();
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
        miniPlaySceneView = new MiniPlaySceneView();
        StackPane.setAlignment(miniPlaySceneView.rootPane(), Pos.TOP_RIGHT);

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

        rootPane = new StackPane(gameSceneLayer, miniPlaySceneView.rootPane(), overlayLayer, helpLayer, pausedIcon);
        rootPane.setId("game-play-view");
    }

    private void setFontSmoothing(boolean smoothing) {
        if (sceneRenderer != null) {
            sceneRenderer.ctx().setFontSmoothingType(smoothing ? FontSmoothingType.LCD : FontSmoothingType.GRAY);
        }
    }

    private void showMiniPlayView(GameLevel level) {
        final GameVariantConfig config = actionContext.variants().currentVariant().config();
        miniPlaySceneView.setVariantConfig(config);
        miniPlaySceneView.setWorldSizeInPixel(level.worldMap().terrainLayer().sizeInPixel());
        miniPlaySceneView.slideIn();
    }

    private void hideMiniPlayView() {
        miniPlaySceneView.slideOut();
    }

    // 3D scenes or 2D scenes with camera
    private void embedGameSceneWithSubSceneFX(GameScene gameScene, SubScene subSceneFX) {
        final GameMainScene mainScene = actionContext.ui().window().mainScene();

        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(mainScene.widthProperty());
        subSceneFX.heightProperty().bind(mainScene.heightProperty());

        if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            // use the canvas of the decorated pane for 2D scene even though the decoration is not used
            gameScene2D.setCanvas(gameSceneFrame().canvas());
            updateGameSceneRenderers(gameScene2D);
        }
        setGameSceneContent(subSceneFX);
    }

    // 2D scenes without camera which are shown at full size
    private void embedGameScene2D(GameSceneConfig gameSceneConfig, AbstractGameScene2D gameScene2D) {
        final GameMainScene mainScene = actionContext.ui().window().mainScene();
        final GamePlayView playView = actionContext.ui().views().gamePlayView();
        final DecorationPane frame = playView.gameSceneFrame();

        gameScene2D.backgroundColorProperty().bind(actionContext.ui().viewModel().common2D.canvasBackgroundColorProperty);

        final boolean decorated = gameSceneConfig.sceneDecorationRequested(gameScene2D);
        if (decorated) {
            frame.newCanvas(); //TODO check why creating a new canvas is needed
            frame.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(Ufx::paintBackground));

            // set unscaled decoration pane size to game scene (=world map) size
            frame.unscaledWidthProperty().bind(gameScene2D.unscaledWidthProperty());
            frame.unscaledHeightProperty().bind(gameScene2D.unscaledHeightProperty());

            // Limit scaling
            gameScene2D.scalingProperty().bind(frame.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), GamePlayView.MAX_GAME_SCENE_SCALING)));

            frame.stretchTo(mainScene.getWidth(), mainScene.getHeight());

            playView.setGameSceneContent(frame);
        }
        else {
            // Undecorated game scene taking complete height
            frame.canvas().heightProperty().bind(mainScene.heightProperty());

            frame.canvas().widthProperty().bind(mainScene.heightProperty()
                .map(h -> h.doubleValue() * gameScene2D.aspectRatio()));


            gameScene2D.scalingProperty().bind(mainScene.heightProperty().divide(gameScene2D.unscaledHeight()));

            playView.setGameSceneContent(frame.canvas());
        }

        gameScene2D.setCanvas(frame.canvas());
        playView.updateGameSceneRenderers(gameScene2D);
        frame.clearCanvas();
    }
}