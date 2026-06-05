/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.subviews.playview;

import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.AppConstants;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.subviews.SubView;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.dashboard.Dashboard;
import de.amr.pacmanfx.ui.subviews.dashboard.DashboardConfig;
import de.amr.pacmanfx.ui.subviews.help.HelpView;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.FontAwesomeIcon;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.border;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.paintBackground;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GamePlayView implements SubView {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final Background DEBUG_BACKGROUND = paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = border(Color.LIGHTGREEN, 1);

    public static final DecorationPane.Config DECORATION_CONFIG = new DecorationPane.Config(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPane.FrameConfig(26, 10, 5, 55.0, ArcadePalette.ARCADE_WHITE)
    );

    private final ActionBindingsSet actionBindings = new GameActionBindingsSet("Action Bindings for Play View");

    private final AppContext context;
    private final ContextMenu contextMenu = new ContextMenu();

    private StackPane rootPane;

    // Game scene layer
    private BorderPane gameSceneLayer;
    private DecorationPane gameSceneFrame;

    // Mini view layer
    private MiniPlaySceneView miniPlaySceneView;

    // Overlay layer
    private BorderPane overlayLayer;
    private Dashboard dashboard;

    // Help layer
    private HelpView helpLayer;

    // Icon layer
    private FontAwesomeIcon pausedIcon;

    private GameScene2D_Renderer sceneRenderer;
    private HeadsUpDisplay_Renderer hudRenderer;

    public GamePlayView(AppContext context, DashboardConfig dashboardConfig) {
        this.context = requireNonNull(context);
        createLayout(requireNonNull(dashboardConfig));
        rootPane.setOnContextMenuRequested(new PlayViewContextMenuHandler(context, this));
        miniPlaySceneView.setUI(context);
    }

    public void resizeToFit(Scene parentSceneFX) {
        gameSceneFrame.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
    }

    public AppContext context() {
        return context;
    }

    public ContextMenu contextMenu() {
        return contextMenu;
    }

    public DecorationPane gameSceneFrame() {
        return gameSceneFrame;
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public MiniPlaySceneView miniPlaySceneView() {
        return miniPlaySceneView;
    }

    public void showHelp(AppContext context) {
        final double scaling = gameSceneFrame.scalingProperty().get();
        helpLayer.showHelpPopup(context, scaling, context.currentGameVariantName());
    }

    public void setGameSceneContent(Node gameSceneContent) {
        gameSceneLayer.setCenter(gameSceneContent);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingsSet actionBindings() {
        return actionBindings;
    }

    @Override
    public void onInput(AppContext context, Input input) {
        // First lLook for an action binding in my bindings, if nothing found, delegate to the current game scene if any
        actionBindings.actionMatchingKeyboardState(input.keyboard()).ifPresentOrElse(
            action -> action.executeIfEnabled(context),
            () -> context.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onInput(context))
        );
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();
        actionBindings.registerAllBindings(AppConstants.COMMON_BINDINGS);
        Logger.info(actionBindings);
        gameSceneFrame.installBindings();
    }

    @Override
    public void onExit() {
        actionBindings.dispose();
        gameSceneFrame.uninstallBindings();
    }

    @Override
    public StackPane rootPane() {
        return rootPane;
    }

    @Override
    public void render() {

        // Render current 2D game scene
        final GameScene gameScene = context.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene instanceof GameScene2D gameScene2D) {
            final GameModel game = context.currentGameContext().gameModel();
            if (sceneRenderer != null) {
                sceneRenderer.draw(gameScene2D);
            }
            if (hudRenderer != null) {
                hudRenderer.draw(game.hud(), game, gameScene2D);
            }
        }

        // Render mini view content
        miniPlaySceneView.draw();

        // Dashboard must always be updated even if simulation is stopped!
        if (overlayLayer.isVisible()) {
            dashboard.update();
        }
    }

    public void updateGameSceneRenderers(GameScene2D gameScene2D) {
        final UIConfig currentConfig = context.currentUIConfig();
        if (gameScene2D.canvas() != null) {
            sceneRenderer = currentConfig.createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
            setFontSmoothing(AppConstants.PROPERTY_CANVAS_FONT_SMOOTHING.get());
            hudRenderer = currentConfig.createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
        } else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
    }

    public void configureDashboard(List<CommonDashboardID> dashboardIDList, TranslationManager translations) {
        dashboard.addCommonSections(translations, dashboardIDList);
    }

    // Private

    private void createLayout(DashboardConfig dashboardConfig) {

        // Layer 1: Game scene with or without decoration
        gameSceneFrame = new DecorationPane(
            DECORATION_CONFIG,
            Globals.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            Globals.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );
        gameSceneLayer = new BorderPane();
        gameSceneLayer.setCenter(gameSceneFrame);

        // Layer 2: Mini view layer
        miniPlaySceneView = new MiniPlaySceneView();
        StackPane.setAlignment(miniPlaySceneView.rootPane(), Pos.TOP_RIGHT);

        // Layer 3: Overlay layer with dashboard
        dashboard = new Dashboard(dashboardConfig);
        dashboard.rootPane().setVisible(false);

        overlayLayer = new BorderPane();
        overlayLayer.setLeft(dashboard.rootPane());

        // Layer 4: Help info
        helpLayer = new HelpView(gameSceneLayer);

        // Layer 4: "Paused" icon
        pausedIcon  = FontAwesomeIcon.of(FontAwesomeIcon.Symbol.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        pausedIcon.setFocusTraversable(false);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        rootPane = new StackPane(gameSceneLayer, miniPlaySceneView.rootPane(), overlayLayer, helpLayer, pausedIcon);
    }

    public void connect(AppContext context) {
        pausedIcon.visibleProperty().bind(context.gameClock().updatesDisabledProperty());

        AppConstants.PROPERTY_CANVAS_FONT_SMOOTHING.addListener((_, _, smoothing) -> setFontSmoothing(smoothing));

        AppConstants.PROPERTY_DEBUG_INFO_VISIBLE.addListener((_, _, debug) -> {
            gameSceneLayer.setBackground(debug ? DEBUG_BACKGROUND : null);
            gameSceneLayer.setBorder(debug ? DEBUG_BORDER : null);
        });

        overlayLayer.visibleProperty().bind(dashboard.rootPane().visibleProperty());

        miniPlaySceneView.rootPane().visibleProperty().bind(Bindings.createObjectBinding(
            () -> AppConstants.PROPERTY_MINI_VIEW_ON.get()
                && context.ui().gameScenes().currentGameSceneHasID(context, CommonSceneID.PLAY_SCENE_3D),
            AppConstants.PROPERTY_MINI_VIEW_ON,
            context.ui().gameScenes().gameSceneProperty()
        ));

        dashboard.connect(context);
    }

    private void setFontSmoothing(boolean smoothing) {
        if (sceneRenderer != null) {
            sceneRenderer.ctx().setFontSmoothingType(smoothing ? FontSmoothingType.LCD : FontSmoothingType.GRAY);
        }
    }
}