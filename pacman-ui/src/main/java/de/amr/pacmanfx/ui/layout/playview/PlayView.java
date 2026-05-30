/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardConfig;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.layout.HelpLayer;
import de.amr.pacmanfx.ui.layout.View;
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

import de.amr.pacmanfx.ui.CommonSceneID;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.border;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.paintBackground;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class PlayView implements View {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final Background DEBUG_BACKGROUND = paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = border(Color.LIGHTGREEN, 1);

    public static final DecorationPane.Config DECORATION_CONFIG = new DecorationPane.Config(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPane.FrameConfig(26, 10, 5, 55.0, ArcadePalette.ARCADE_WHITE)
    );

    private final ActionBindingsSet actionBindings = new GameActionBindingsSet();

    private final GameUI ui;
    private final ContextMenu contextMenu = new ContextMenu();

    private StackPane rootPane;

    // Game scene layer
    private BorderPane gameSceneLayer;
    private DecorationPane gameSceneFrame;

    // Overlay layer
    private BorderPane overlayLayer;
    private Dashboard dashboard;
    private MiniPlaySceneView miniPlaySceneView;

    // Help layer
    private HelpLayer helpLayer;

    // Icon layer
    private FontAwesomeIcon pausedIcon;

    private GameScene2D_Renderer sceneRenderer;
    private HeadsUpDisplay_Renderer hudRenderer;

    public PlayView(GameUI ui, DashboardConfig dashboardConfig) {
        this.ui = requireNonNull(ui);
        createLayout(requireNonNull(dashboardConfig));
        configurePropertyBindings();
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

    public MiniPlaySceneView miniPlaySceneView() {
        return miniPlaySceneView;
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public void showHelp(GameUI ui) {
        final double scaling = gameSceneFrame.scalingProperty().get();
        helpLayer.showHelpPopup(ui, scaling, ui.gameContext().gameVariantName());
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
        actionBindings.matchingAction(input.keyboard).ifPresentOrElse(
            action -> action.executeIfEnabled(ui),
            () -> ui.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::onInput)
        );
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();
        actionBindings.registerAllBindingsFromSet(GameUIConstants.COMMON_BINDINGS);
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
        final GameScene gameScene = ui.gameSceneManager().optCurrentGameScene().orElse(null);
        if (gameScene instanceof GameScene2D gameScene2D) {
            final Game game = ui.gameContext().game();
            if (sceneRenderer != null) {
                sceneRenderer.draw(gameScene2D);
            }
            if (hudRenderer != null) {
                hudRenderer.draw(game.hud(), game, gameScene2D);
            }
        }

        // Render mini view content
        if (miniPlaySceneView.canDraw()) {
            miniPlaySceneView.draw();
        }

        // Dashboard must always be updated even if simulation is stopped!
        if (overlayLayer.isVisible()) {
            dashboard.update(ui);
        }
    }

    public void updateGameSceneRenderers(GameScene2D gameScene2D) {
        if (gameScene2D.canvas() != null) {
            sceneRenderer = ui.currentConfig().createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
            setFontSmoothing(GameUIConstants.PROPERTY_CANVAS_FONT_SMOOTHING.get());
            hudRenderer = ui.currentConfig().createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
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

        // Layer 2: Overlay layer with dashboard and mini-view for 3D scene
        miniPlaySceneView = new MiniPlaySceneView();

        dashboard = new Dashboard(dashboardConfig);
        dashboard.rootPane().setVisible(false);

        overlayLayer = new BorderPane();
        overlayLayer.setLeft(dashboard.rootPane());
        overlayLayer.setRight(miniPlaySceneView.rootPane());

        // Layer 3: Help info
        helpLayer = new HelpLayer(gameSceneLayer);

        // Layer 4: "Paused" icon
        pausedIcon  = FontAwesomeIcon.of(FontAwesomeIcon.Symbol.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        pausedIcon.setFocusTraversable(false);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        rootPane = new StackPane(gameSceneLayer, overlayLayer, helpLayer, pausedIcon);
    }

    private void configurePropertyBindings() {
        pausedIcon.visibleProperty().bind(ui.gameContext().clock().updatesDisabledProperty());

        GameUIConstants.PROPERTY_CANVAS_FONT_SMOOTHING.addListener((_, _, smoothing) -> setFontSmoothing(smoothing));

        GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE.addListener((_, _, debug) -> {
            gameSceneLayer.setBackground(debug ? DEBUG_BACKGROUND : null);
            gameSceneLayer.setBorder(debug ? DEBUG_BORDER : null);
        });

        overlayLayer.visibleProperty().bind(Bindings.or(dashboard.rootPane().visibleProperty(), GameUIConstants.PROPERTY_MINI_VIEW_ON));

        miniPlaySceneView.rootPane().visibleProperty().bind(Bindings.createObjectBinding(
            () -> GameUIConstants.PROPERTY_MINI_VIEW_ON.get()
                && ui.gameSceneManager().currentGameSceneHasID(CommonSceneID.PLAY_SCENE_3D),
            GameUIConstants.PROPERTY_MINI_VIEW_ON, ui.gameSceneManager().gameSceneProperty()
        ));
    }

    private void setFontSmoothing(boolean smoothing) {
        if (sceneRenderer != null) {
            sceneRenderer.ctx().setFontSmoothingType(smoothing ? FontSmoothingType.LCD : FontSmoothingType.GRAY);
        }
    }
}