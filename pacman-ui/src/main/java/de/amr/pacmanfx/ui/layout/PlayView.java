/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.AbstractGameAction;
import de.amr.pacmanfx.ui.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui._2d.*;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.api.GameUI_Config.SCENE_ID_PLAY_SCENE_2D;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.*;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class PlayView extends StackPane implements GameUI_View {

    /**
     * @param sceneBefore scene displayed before switching
     * @param sceneAfter scene displayed after switching
     * @return <code>23</code> if 2D -> 3D switch, <code>32</code> if 3D -> 2D switch</code>,
     *  <code>0</code> if scene before switch is not yet available
     */
    private static byte identifySceneSwitchType(GameScene sceneBefore, GameScene sceneAfter) {
        if (sceneBefore == null && sceneAfter == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (sceneBefore) {
            case GameScene2D ignored when sceneAfter instanceof PlayScene3D -> 23;
            case PlayScene3D ignored when sceneAfter instanceof GameScene2D -> 32;
            case null, default -> 0; // may happen, it's ok
        };
    }

    private final ActionBindingsManager actionBindings = new DefaultActionBindingsManager();
    private final GameUI ui;
    private final MainScene parentScene;
    private final Dashboard dashboard;
    private final DecoratedCanvasContainer decoratedCanvasContainer = new DecoratedCanvasContainer();
    private final MiniGameView miniView;
    private final ContextMenu contextMenu = new ContextMenu();

    private final BorderPane canvasLayer = new BorderPane();
    private final BorderPane dashboardAndMiniViewLayer = new BorderPane();
    private HelpLayer helpLayer; // help

    public PlayView(GameUI ui, MainScene parentScene) {
        this.ui = requireNonNull(ui);
        this.parentScene = requireNonNull(parentScene);

        dashboard = new Dashboard(ui);
        dashboard.setVisible(false);

        miniView = new MiniGameView(ui);
        miniView.visibleProperty().bind(Bindings.createObjectBinding(
            () -> PROPERTY_MINI_VIEW_ON.get() && ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D),
            PROPERTY_MINI_VIEW_ON, parentScene.currentGameSceneProperty()
        ));

        configureCanvasWithFrame();
        createLayout();
        configurePropertyBindings();

        setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO what is the recommended way to hide the context menu?
        parentScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
        });

        parentScene.currentGameSceneProperty().addListener((py, ov, gameScene) -> {
            contextMenu.hide();
            if (gameScene != null) embedGameScene(parentScene, gameScene);
        });

        parentScene.widthProperty() .addListener((py, ov, w) -> decoratedCanvasContainer.resizeTo(w.doubleValue(), parentScene.getHeight()));
        parentScene.heightProperty().addListener((py, ov, h) -> decoratedCanvasContainer.resizeTo(parentScene.getWidth(), h.doubleValue()));

        actionBindings.useBindings(ACTION_BOOT_SHOW_PLAY_VIEW, ui.actionBindings());
        actionBindings.useBindings(ACTION_ENTER_FULLSCREEN, ui.actionBindings());
        actionBindings.useBindings(ACTION_QUIT_GAME_SCENE, ui.actionBindings());
        actionBindings.useBindings(ACTION_SHOW_HELP, ui.actionBindings());
        actionBindings.useBindings(ACTION_SIMULATION_SLOWER, ui.actionBindings());
        actionBindings.useBindings(ACTION_SIMULATION_SLOWEST, ui.actionBindings());
        actionBindings.useBindings(ACTION_SIMULATION_FASTER, ui.actionBindings());
        actionBindings.useBindings(ACTION_SIMULATION_FASTEST, ui.actionBindings());
        actionBindings.useBindings(ACTION_SIMULATION_RESET, ui.actionBindings());
        actionBindings.useBindings(ACTION_SIMULATION_ONE_STEP, ui.actionBindings());
        actionBindings.useBindings(ACTION_SIMULATION_TEN_STEPS, ui.actionBindings());
        actionBindings.useBindings(ACTION_TOGGLE_AUTOPILOT, ui.actionBindings());
        actionBindings.useBindings(ACTION_TOGGLE_DEBUG_INFO, ui.actionBindings());
        actionBindings.useBindings(ACTION_TOGGLE_MUTED, ui.actionBindings());
        actionBindings.useBindings(ACTION_TOGGLE_PAUSED, ui.actionBindings());
        actionBindings.useBindings(ACTION_TOGGLE_DASHBOARD, ui.actionBindings());
        actionBindings.useBindings(ACTION_TOGGLE_IMMUNITY, ui.actionBindings());
        actionBindings.useBindings(ACTION_TOGGLE_MINI_VIEW_VISIBILITY, ui.actionBindings());
        actionBindings.useBindings(ACTION_TOGGLE_PLAY_SCENE_2D_3D, ui.actionBindings());
    }

    private void handleContextMenuRequest(ContextMenuEvent contextMenuEvent) {
        contextMenu.getItems().clear();
        ui.currentGameScene().ifPresent(gameScene -> {
            if (ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D)) {
                var miSwitchTo3D = new MenuItem(ui.assets().translated("use_3D_scene"));
                miSwitchTo3D.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));
                contextMenu.getItems().add(createContextMenuTitle("scene_display", ui.preferences(), ui.assets()));
                contextMenu.getItems().add(miSwitchTo3D);
            }
            List<MenuItem> gameSceneItems = gameScene.supplyContextMenuItems(contextMenuEvent, contextMenu);
            contextMenu.getItems().addAll(gameSceneItems);
        });
        // wrap all action handlers into menu closing actions
        contextMenu.getItems().stream()
            .filter(item -> item.getOnAction() != null)
            .forEach(item -> {
                var handler = item.getOnAction();
                item.setOnAction(e -> { handler.handle(e); contextMenu.hide(); });
            });
        contextMenu.requestFocus();
        contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }

    public void showHelp(GameUI ui) {
        helpLayer.showHelp(ui, decoratedCanvasContainer.scaling());
    }

    public void draw() {
        ui.currentGameScene().filter(GameScene2D.class::isInstance).map(GameScene2D.class::cast).ifPresent(GameScene2D::draw);
        miniView.draw();
        // Dashboard must also be updated if simulation is stopped
        if (dashboardAndMiniViewLayer.isVisible()) {
            dashboard.updateContent();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // PacManGames_View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingsManager actionBindingsManager() {
        return actionBindings;
    }

    @Override
    public StackPane root() {
        return this;
    }

    @Override
    public void handleKeyboardInput(GameUI ui) {
        AbstractGameAction matchingAction = actionBindings.matchingAction(ui.keyboard()).orElse(null);
        if (matchingAction != null) {
            matchingAction.executeIfEnabled(ui);
        } else {
            ui.currentGameScene().ifPresent(GameScene::handleKeyboardInput);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        switch (gameEvent.type()) {
            case LEVEL_CREATED -> onLevelCreated();
            case GAME_STATE_CHANGED -> {
                if (ui.gameContext().gameState() == GamePlayState.LEVEL_COMPLETE) {
                    miniView.slideOut();
                }
            }
        }
        ui.currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(gameEvent));
        updateGameScene(false);
    }

    private void onLevelCreated() {
        GameLevel gameLevel = ui.gameContext().gameLevel();
        GameUI_Config uiConfig = ui.currentConfig();

        gameLevel.pac().setAnimationManager(uiConfig.createPacAnimations());
        gameLevel.ghosts().forEach(ghost -> ghost.setAnimationManager(uiConfig.createGhostAnimations(ghost.personality())));

        miniView.onGameLevelCreated(gameLevel);
        miniView.slideIn();

        // size of game scene might have changed, so re-embed
        ui.currentGameScene().ifPresent(gameScene -> embedGameScene(parentScene, gameScene));
    }

    // -----------------------------------------------------------------------------------------------------------------

    public DecoratedCanvasContainer canvasFrame() {
        return decoratedCanvasContainer;
    }

    public void updateGameScene(boolean reloadCurrent) {
        final GameScene nextGameScene = ui.currentConfig().selectGameScene(ui.gameContext());
        if (nextGameScene == null) {
            String errorMessage = " Katastrophe! Could not determine game scene!";
            ui.showFlashMessage(Duration.seconds(30), errorMessage);
            return;
        }
        final GameScene currentGameScene = ui.currentGameScene().orElse(null);
        final boolean changing = nextGameScene != currentGameScene;
        if (!changing && !reloadCurrent) {
            return;
        }
        if (currentGameScene != null) {
            currentGameScene.end();
            Logger.info("Game scene ended: {}", currentGameScene.getClass().getSimpleName());
        }
        embedGameScene(parentScene, nextGameScene);
        nextGameScene.init();
        Logger.info("Game scene initialized: {}", nextGameScene.getClass().getSimpleName());

        // Handle switching between 2D and 3D game variants
        byte sceneSwitchType = identifySceneSwitchType(currentGameScene, nextGameScene);
        switch (sceneSwitchType) {
            case 23 -> nextGameScene.onSwitch_2D_3D(currentGameScene);
            case 32 -> nextGameScene.onSwitch_3D_2D(currentGameScene);
            case  0 -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
        }

        if (changing) {
            parentScene.currentGameSceneProperty().set(nextGameScene);
        }
    }

    private void embedGameScene(Scene parentScene, GameScene gameScene) {
        if (gameScene instanceof SubSceneProvider subSceneProvider) {
            // 1. Play scene with integrated sub-scene: 3D scene or scrolling 2D scene as in Tengen Ms. Pac-Man:
            SubScene subScene = subSceneProvider.subScene();
            // Let sub-scene take full size o parent scene
            subScene.widthProperty().bind(parentScene.widthProperty());
            subScene.heightProperty().bind(parentScene.heightProperty());

            getChildren().set(0, subScene);

            //TODO reconsider this
            if (gameScene instanceof CanvasProvider canvasProvider && gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.createRenderers(canvasProvider.canvas());
            }
        }
        else if (gameScene instanceof GameScene2D gameScene2D) {
            // 2D scene with canvas
            getChildren().set(0, canvasLayer);

            gameScene2D.createRenderers(decoratedCanvasContainer.canvas());

            Vector2i gameSceneSizePx = gameScene2D.sizeInPx();
            gameScene2D.scalingProperty().bind(decoratedCanvasContainer.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), ui.preferences().getFloat("scene2d.max_scaling"))));
            decoratedCanvasContainer.setUnscaledCanvasSize(gameSceneSizePx.x(), gameSceneSizePx.y());
            decoratedCanvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
            decoratedCanvasContainer.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR.map(Ufx::colorBackground));
        }
        else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }

    private void configureCanvasWithFrame() {
        decoratedCanvasContainer.setMinScaling(0.5);
        // 28*TS x 36*TS = Arcade map size in pixels
        decoratedCanvasContainer.setUnscaledCanvasSize(28 * TS, 36 * TS);
        decoratedCanvasContainer.setBorderColor(Color.rgb(222, 222, 255));
    }

    private void configurePropertyBindings() {
        PROPERTY_CANVAS_FONT_SMOOTHING.addListener((py, ov, smooth)
            -> decoratedCanvasContainer.canvas().getGraphicsContext2D().setFontSmoothingType(smooth ? FontSmoothingType.LCD : FontSmoothingType.GRAY));

        PROPERTY_DEBUG_INFO_VISIBLE.addListener((py, ov, debug)
            -> {
               canvasLayer.setBackground(debug ? colorBackground(Color.TEAL) : null);
               canvasLayer.setBorder(debug ? border(Color.LIGHTGREEN, 1) : null);
            });
    }

    private void createLayout() {
        canvasLayer.setCenter(decoratedCanvasContainer);

        dashboardAndMiniViewLayer.setLeft(dashboard);
        dashboardAndMiniViewLayer.setRight(miniView);
        dashboardAndMiniViewLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboard.isVisible() || PROPERTY_MINI_VIEW_ON.get(),
            dashboard.visibleProperty(), PROPERTY_MINI_VIEW_ON
        ));

        //TODO reconsider help functionality
        helpLayer = new HelpLayer(decoratedCanvasContainer);
        helpLayer.setMouseTransparent(true);

        getChildren().addAll(canvasLayer, dashboardAndMiniViewLayer, helpLayer);
    }

    // Dashboard access

    public Dashboard dashboard() {
        return dashboard;
    }
}