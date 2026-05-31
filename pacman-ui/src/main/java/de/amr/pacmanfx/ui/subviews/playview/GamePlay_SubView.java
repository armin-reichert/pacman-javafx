/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.subviews.playview;

import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.*;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.subviews.dashboard.Dashboard;
import de.amr.pacmanfx.ui.subviews.dashboard.DashboardConfig;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.subviews.help.HelpLayer;
import de.amr.pacmanfx.ui.subviews.GameUI_SubView;
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

import static de.amr.pacmanfx.uilib.UfxBackgrounds.border;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.paintBackground;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GamePlay_SubView implements GameUI_SubView {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final Background DEBUG_BACKGROUND = paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = border(Color.LIGHTGREEN, 1);

    public static final DecorationPane.Config DECORATION_CONFIG = new DecorationPane.Config(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPane.FrameConfig(26, 10, 5, 55.0, ArcadePalette.ARCADE_WHITE)
    );

    private final ActionBindingsSet actionBindings = new GameActionBindingsSet("Action Bindings for Play View");

    private final GameUI ui;
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
    private HelpLayer helpLayer;

    // Icon layer
    private FontAwesomeIcon pausedIcon;

    private GameScene2D_Renderer sceneRenderer;
    private HeadsUpDisplay_Renderer hudRenderer;

    public GamePlay_SubView(GameUI ui, DashboardConfig dashboardConfig) {
        this.ui = requireNonNull(ui);
        createLayout(requireNonNull(dashboardConfig));
        rootPane.setOnContextMenuRequested(new PlayViewContextMenuHandler(ui, this));
        miniPlaySceneView.setUI(ui);
    }

    public void resizeToFit(Scene parentSceneFX) {
        gameSceneFrame.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
    }

    public GameUI ui() {
        return ui;
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

    public void showHelp(GameUI ui) {
        final double scaling = gameSceneFrame.scalingProperty().get();
        helpLayer.showHelpPopup(ui, scaling, ui.access().gameContext().gameVariantName());
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
    public void onInput(GameUI ui, Input input) {
        // First lLook for an action binding in my bindings, if nothing found, delegate to the current game scene if any
        actionBindings.actionMatchingKeyboardState(input.keyboard).ifPresentOrElse(
            action -> action.executeIfEnabled(ui),
            () -> ui.access().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onInput(ui))
        );
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();
        actionBindings.registerAllBindings(GameUI_Constants.COMMON_BINDINGS);
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
        final GameScene gameScene = ui.access().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene instanceof GameScene2D gameScene2D) {
            final Game game = ui.access().gameContext().game();
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
            dashboard.update(ui);
        }
    }

    public void updateGameSceneRenderers(GameScene2D gameScene2D) {
        final UIConfig currentConfig = ui.access().currentUIConfig();
        if (gameScene2D.canvas() != null) {
            sceneRenderer = currentConfig.createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
            setFontSmoothing(GameUI_Constants.PROPERTY_CANVAS_FONT_SMOOTHING.get());
            hudRenderer = currentConfig.createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
        } else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
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
        helpLayer = new HelpLayer(gameSceneLayer);

        // Layer 4: "Paused" icon
        pausedIcon  = FontAwesomeIcon.of(FontAwesomeIcon.Symbol.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        pausedIcon.setFocusTraversable(false);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        rootPane = new StackPane(gameSceneLayer, miniPlaySceneView.rootPane(), overlayLayer, helpLayer, pausedIcon);
    }

    public void configurePropertyBindings(GameUI ui) {
        pausedIcon.visibleProperty().bind(ui.access().gameClock().updatesDisabledProperty());

        GameUI_Constants.PROPERTY_CANVAS_FONT_SMOOTHING.addListener((_, _, smoothing) -> setFontSmoothing(smoothing));

        GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE.addListener((_, _, debug) -> {
            gameSceneLayer.setBackground(debug ? DEBUG_BACKGROUND : null);
            gameSceneLayer.setBorder(debug ? DEBUG_BORDER : null);
        });

        overlayLayer.visibleProperty().bind(dashboard.rootPane().visibleProperty());

        miniPlaySceneView.rootPane().visibleProperty().bind(Bindings.createObjectBinding(
            () -> GameUI_Constants.PROPERTY_MINI_VIEW_ON.get()
                && ui.access().gameScenes().currentGameSceneHasID(ui, CommonSceneID.PLAY_SCENE_3D),
            GameUI_Constants.PROPERTY_MINI_VIEW_ON,
            ui.access().gameScenes().gameSceneProperty()
        ));
    }

    private void setFontSmoothing(boolean smoothing) {
        if (sceneRenderer != null) {
            sceneRenderer.ctx().setFontSmoothingType(smoothing ? FontSmoothingType.LCD : FontSmoothingType.GRAY);
        }
    }
}