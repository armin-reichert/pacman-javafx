/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.config.UISettings;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.dashboard.Dashboard;
import de.amr.pacmanfx.ui.views.dashboard.DashboardConfig;
import de.amr.pacmanfx.ui.views.dashboard.DashboardID;
import de.amr.pacmanfx.ui.views.help.HelpView;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
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
public class GamePlayView implements GameView {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    public static final Background DEBUG_BACKGROUND = paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = border(Color.LIGHTGREEN, 1);

    public static final DecorationPane.Config DECORATION_CONFIG = new DecorationPane.Config(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPane.FrameConfig(26, 10, 5, 55.0, ArcadePalette.ARCADE_WHITE)
    );

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Action Bindings for Play View");

    private Game game;

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

    public GamePlayView(DashboardConfig dashboardConfig) {
        createLayout(requireNonNull(dashboardConfig));
    }

    @Override
    public void connect(Game game) {
        this.game = requireNonNull(game);
        final UISettings settings = game.ui().settings();

        rootPane.setOnContextMenuRequested(new PlayViewContextMenuHandler(game, this));
        miniPlaySceneView.setUI(game);

        pausedIcon.visibleProperty().bind(game.clock().updatesDisabledProperty());

        settings.fontSmoothingOnProperty().addListener((_, _, smoothing) -> setFontSmoothing(smoothing));

        settings.debugModeOnProperty().addListener((_, _, debug) -> {
            gameSceneLayer.setBackground(debug ? DEBUG_BACKGROUND : null);
            gameSceneLayer.setBorder(debug ? DEBUG_BORDER : null);
        });

        overlayLayer.visibleProperty().bind(dashboard.rootPane().visibleProperty());

        miniPlaySceneView.rootPane().visibleProperty().bind(Bindings.createObjectBinding(
            () -> settings.miniView().activeProperty().get()
                && game.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D),
            settings.miniView().activeProperty(),
            game.ui().gameScenes().currentGameSceneProperty()
        ));

        // Keep this view always at the same size as the main scene
        final GameMainScene mainScene = game.ui().window().mainScene();
        final ChangeListener<? super Number> resizeHandler = (_, _, _) -> resizeToFit(mainScene);
        mainScene.widthProperty().addListener(resizeHandler);
        mainScene.heightProperty().addListener(resizeHandler);
    }

    public void resizeToFit(Scene parentSceneFX) {
        gameSceneFrame.stretchTo(parentSceneFX.getWidth(), parentSceneFX.getHeight());
    }

    public Game game() {
        return game;
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

    public void showHelp(Game game) {
        final double scaling = gameSceneFrame.scalingProperty().get();
        helpLayer.showHelpPopup(game, scaling, game.currentGameVariantName());
    }

    public void setGameSceneContent(Node gameSceneContent) {
        gameSceneLayer.setCenter(gameSceneContent);
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
        // First lLook for an action binding in my bindings, if nothing found, delegate to the current game scene if any
        actionBindings.findActionMatchingPressedKeys(input.keyboard()).ifPresentOrElse(
            GameAction::execute,
            () -> game.ui().gameScenes().optCurrentGameScene().ifPresent(AbstractGameScene::onInput)
        );
    }

    @Override
    public void onEnter() {
        rootPane.requestFocus();
        actionBindings.registerAllBindings(game.actions().bindings());
        Logger.info(actionBindings);
        gameSceneFrame.installBindings();
    }

    @Override
    public void onExit() {
        game.stop();
        game.ui().sounds().stopAll();
        game.ui().sounds().stopAndDisposeVoice();
        actionBindings.dispose();
        gameSceneFrame.uninstallBindings();
    }

    @Override
    public void handleQuit(Game game) {
        game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.handleQuit(game));
        game.ui().views().selectStartPagesView();
    }

    @Override
    public StackPane rootPane() {
        return rootPane;
    }

    @Override
    public void render() {

        // Render current 2D game scene
        final AbstractGameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene instanceof GameScene2D gameScene2D) {
            final GameModel game = this.game.currentGameContext().model();
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
        final UIConfig currentConfig = game.currentUIConfig();
        if (gameScene2D.canvas() != null) {
            sceneRenderer = currentConfig.createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
            setFontSmoothing(game.ui().settings().fontSmoothingOnProperty().get());
            hudRenderer = currentConfig.createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
        } else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
    }

    public void configureDashboard(List<DashboardID> dashboardIDList, TranslationManager translations) {
        dashboard.addCommonSections(translations, dashboardIDList);
    }

    // Private

    private void createLayout(DashboardConfig dashboardConfig) {

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
        dashboard = new Dashboard(dashboardConfig);
        dashboard.rootPane().setVisible(false);

        overlayLayer = new BorderPane();
        overlayLayer.setLeft(dashboard.rootPane());

        // Layer 4: Help info
        helpLayer = new HelpView(gameSceneLayer);

        // Layer 4: "Paused" icon
        pausedIcon  = new FontAwesomeIcon(FontAwesomeSymbol.PAUSE);
        pausedIcon.setId("paused-icon");
        pausedIcon.fillProperty().set(ArcadePalette.ARCADE_WHITE);
        pausedIcon.setFocusTraversable(false);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        rootPane = new StackPane(gameSceneLayer, miniPlaySceneView.rootPane(), overlayLayer, helpLayer, pausedIcon);
    }

    private void setFontSmoothing(boolean smoothing) {
        if (sceneRenderer != null) {
            sceneRenderer.ctx().setFontSmoothingType(smoothing ? FontSmoothingType.LCD : FontSmoothingType.GRAY);
        }
    }
}