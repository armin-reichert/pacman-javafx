/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.model.GameUIViewModel;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.dashboard.Dashboard;
import de.amr.pacmanfx.ui.views.dashboard.DashboardConfig;
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
    public static final DashboardConfig DEFAULT_DASHBOARD_CONFIG = new DashboardConfig(
        110, // label width
        320); // width

    //TODO use FX controls + CSS
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

    public GamePlayView() {
        createLayout();
    }

    @Override
    public void connect(Game game) {
        this.game = requireNonNull(game);
        final GameUIViewModel settings = game.ui().viewModel();

        rootPane.setOnContextMenuRequested(this);
        game.ui().window().mainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
        });

        miniPlaySceneView.setUI(game);

        pausedIcon.visibleProperty().bind(game.clock().updatesDisabledProperty());

        settings.common2D.fontSmoothingOnProperty.addListener((_, _, smoothing) -> setFontSmoothing(smoothing));

        settings.debugModeOnProperty.addListener((_, _, debug) -> {
            gameSceneLayer.setBackground(debug ? DEBUG_BACKGROUND : null);
            gameSceneLayer.setBorder(debug ? DEBUG_BORDER : null);
        });

        overlayLayer.visibleProperty().bind(dashboard.rootPane().visibleProperty());

        miniPlaySceneView.rootPane().visibleProperty().bind(Bindings.createObjectBinding(
            () -> settings.miniView.activeProperty.get()
                && game.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D),
            settings.miniView.activeProperty,
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
        // First look for a matching action of the play view itself; if none found, delegate to the current game scene.
        if (actionBindings.executeMatchingAction(input).isEmpty()) {
            game.ui().gameScenes().optCurrentGameScene().ifPresent(AbstractGameScene::onInput);
        }
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
            final GameModel gameModel = game.currentGameContext().model();
            if (sceneRenderer != null) {
                sceneRenderer.draw(gameScene2D);
            }
            if (hudRenderer != null) {
                hudRenderer.draw(gameModel.hud(), gameModel, gameScene2D);
            }
        }

        // Render mini view content
        miniPlaySceneView.draw();

        // Dashboard must always be updated even if simulation is stopped!
        if (overlayLayer.isVisible()) {
            dashboard.update(game);
        }
    }

    // Context menu handler

    @Override
    public void handle(ContextMenuEvent event) {
        contextMenu.getItems().clear();

        game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            final TranslationManager translations = game.ui().translations();
            // Add 2D play scene-specific entries
            if (game.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D)) {
                addLocalizedTitleItem(contextMenu, translations, "context_menu.scene_display");
                addLocalizedActionItem(contextMenu, translations, game.actions().uiSettingsActions().actionTogglePlayScene2D3D(),
                    "context_menu.use_3D_scene");
            }
            // Add scene-specific entries
            gameScene.supplyContextMenu().ifPresent(sceneMenu -> contextMenu.getItems().addAll(sceneMenu.getItems()));
        });

        if (!contextMenu.getItems().isEmpty()) {
            contextMenu.show(rootPane, event.getScreenX(), event.getScreenY());
            contextMenu.requestFocus();
        }
    }

    public void updateGameSceneRenderers(GameScene2D gameScene2D) {
        final GameVariantConfig currentConfig = game.currentVariantConfig();
        if (gameScene2D.canvas() != null) {
            sceneRenderer = currentConfig.createGameSceneRenderer(gameScene2D, gameScene2D.canvas());
            setFontSmoothing(game.ui().viewModel().common2D.fontSmoothingOnProperty.get());
            hudRenderer = currentConfig.createHUDRenderer(gameScene2D, gameScene2D.canvas()); // may return null!
        } else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
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
        dashboard = new Dashboard(DEFAULT_DASHBOARD_CONFIG);
        dashboard.rootPane().setVisible(false);
        dashboard.rootPane().setId("dashboard");

        overlayLayer = new BorderPane();
        overlayLayer.setLeft(dashboard.rootPane());

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
}