/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.*;
import de.amr.pacmanfx.ui._2d.CanvasWithFrame;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.HelpLayer;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
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
import static de.amr.pacmanfx.ui.GameUI_Config.SCENE_ID_PLAY_SCENE_2D;
import static de.amr.pacmanfx.ui.GameUI_Properties.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
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
    private final Scene parentScene;
    private final Dashboard dashboard;
    private final CanvasWithFrame canvasWithFrame = new CanvasWithFrame();
    private final MiniGameView miniView = new MiniGameView();
    private final ContextMenu contextMenu = new ContextMenu();

    private final BorderPane canvasLayer = new BorderPane();
    private final BorderPane dashboardAndMiniViewLayer = new BorderPane();
    private HelpLayer helpLayer; // help

    public PlayView(GameUI ui, Scene parentScene) {
        this.ui = requireNonNull(ui);
        this.parentScene = requireNonNull(parentScene);

        dashboard = new Dashboard(ui);
        dashboard.setVisible(false);

        miniView.setGameUI(ui);
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

        PROPERTY_CURRENT_GAME_SCENE.addListener((py, ov, gameScene) -> {
            contextMenu.hide();
            if (gameScene != null) embedGameScene(parentScene, gameScene);
        });

        parentScene.widthProperty() .addListener((py, ov, w) -> canvasWithFrame.resizeTo(w.doubleValue(), parentScene.getHeight()));
        parentScene.heightProperty().addListener((py, ov, h) -> canvasWithFrame.resizeTo(parentScene.getWidth(), h.doubleValue()));

        actionBindings.useFirst(ACTION_BOOT_SHOW_PLAY_VIEW, ui.actionBindings());
        actionBindings.useFirst(ACTION_ENTER_FULLSCREEN, ui.actionBindings());
        actionBindings.useFirst(ACTION_QUIT_GAME_SCENE, ui.actionBindings());
        actionBindings.useFirst(ACTION_SHOW_HELP, ui.actionBindings());
        actionBindings.useFirst(ACTION_SIMULATION_SLOWER, ui.actionBindings());
        actionBindings.useFirst(ACTION_SIMULATION_FASTER, ui.actionBindings());
        actionBindings.useFirst(ACTION_SIMULATION_RESET, ui.actionBindings());
        actionBindings.useFirst(ACTION_SIMULATION_ONE_STEP, ui.actionBindings());
        actionBindings.useFirst(ACTION_SIMULATION_TEN_STEPS, ui.actionBindings());
        actionBindings.useFirst(ACTION_TOGGLE_AUTOPILOT, ui.actionBindings());
        actionBindings.useFirst(ACTION_TOGGLE_DEBUG_INFO, ui.actionBindings());
        actionBindings.useFirst(ACTION_TOGGLE_MUTED, ui.actionBindings());
        actionBindings.useFirst(ACTION_TOGGLE_PAUSED, ui.actionBindings());
        actionBindings.useFirst(ACTION_TOGGLE_DASHBOARD, ui.actionBindings());
        actionBindings.useFirst(ACTION_TOGGLE_IMMUNITY, ui.actionBindings());
        actionBindings.useFirst(ACTION_TOGGLE_MINI_VIEW_VISIBILITY, ui.actionBindings());
        actionBindings.useFirst(ACTION_TOGGLE_PLAY_SCENE_2D_3D, ui.actionBindings());
    }

    private void handleContextMenuRequest(ContextMenuEvent contextMenuEvent) {
        contextMenu.getItems().clear();
        ui.currentGameScene().ifPresent(gameScene -> {
            if (ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D)) {
                var miSwitchTo3D = new MenuItem(ui.assets().text("use_3D_scene"));
                miSwitchTo3D.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));
                contextMenu.getItems().add(createContextMenuTitle("scene_display", ui.prefs(), ui.assets()));
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
        helpLayer.showHelp(ui, canvasWithFrame.scaling());
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
        GameAction matchingAction = actionBindings.matchingAction(ui.keyboard()).orElse(null);
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
                if (ui.gameContext().theGameState() == GameState.LEVEL_COMPLETE) {
                    miniView.slideOut();
                }
            }
        }
        ui.currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(gameEvent));
        updateGameScene(false);
    }

    private void onLevelCreated() {
        GameLevel gameLevel = ui.gameContext().theGameLevel();
        GameUI_Config uiConfig = ui.currentConfig();

        gameLevel.pac().setAnimations(uiConfig.createPacAnimations(gameLevel.pac()));
        gameLevel.ghosts().forEach(ghost -> ghost.setAnimations(uiConfig.createGhostAnimations(ghost)));

        miniView.setGameLevel(gameLevel);
        miniView.slideIn();

        // size of game scene might have changed, so re-embed
        ui.currentGameScene().ifPresent(gameScene -> embedGameScene(parentScene, gameScene));
    }

    // -----------------------------------------------------------------------------------------------------------------

    public CanvasWithFrame canvasFrame() {
        return canvasWithFrame;
    }

    public void updateGameScene(boolean reloadCurrent) {
        final GameScene nextGameScene = ui.currentConfig().selectGameScene(ui.gameContext());
        if (nextGameScene == null) {
            String errorMessage = " Katastrophe! Could not determine game scene!";
            ui.showFlashMessageSec(Duration.seconds(30), errorMessage);
            return;
        }
        final GameScene currentGameScene = ui.currentGameScene().orElse(null);
        final boolean changing = nextGameScene != currentGameScene;
        if (!changing && !reloadCurrent) {
            return;
        }
        if (currentGameScene != null) {
            currentGameScene.end();
            Logger.info("Game scene ended: {}", currentGameScene.displayName());
        }
        embedGameScene(parentScene, nextGameScene);
        nextGameScene.init();
        Logger.info("Game scene initialized: {}", nextGameScene.displayName());

        // Handle switching between 2D and 3D game variants
        byte sceneSwitchType = identifySceneSwitchType(currentGameScene, nextGameScene);
        switch (sceneSwitchType) {
            case 23 -> nextGameScene.onSwitch_2D_3D(currentGameScene);
            case 32 -> nextGameScene.onSwitch_3D_2D(currentGameScene);
            case  0 -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
        }

        if (changing) {
            PROPERTY_CURRENT_GAME_SCENE.set(nextGameScene);
        }
    }

    private void embedGameScene(Scene parentScene, GameScene gameScene) {
        if (gameScene.optSubScene().isPresent()) {
            SubScene subScene = gameScene.optSubScene().get();
            subScene.widthProperty().bind(parentScene.widthProperty());
            subScene.heightProperty().bind(parentScene.heightProperty());
            getChildren().set(0, subScene);
        }
        else if (gameScene instanceof GameScene2D gameScene2D) {
            embedGameScene2DWithoutSubScene(gameScene2D);
            getChildren().set(0, canvasLayer);
        }
        else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }

    // 2D game scenes without sub-scene/camera (Arcade play scene, cut scenes) are drawn into the canvas provided by this play view
    private void embedGameScene2DWithoutSubScene(GameScene2D gameScene2D) {
        gameScene2D.setCanvas(canvasWithFrame.canvas());
        gameScene2D.setGameRenderer(ui.currentConfig().createGameRenderer(canvasWithFrame.canvas()));
        gameScene2D.clear();
        gameScene2D.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        gameScene2D.scalingProperty().bind(canvasWithFrame.scalingProperty().map(
            scaling -> Math.min(scaling.doubleValue(), ui.prefs().getFloat("scene2d.max_scaling"))));

        Vector2f gameSceneSizePx = gameScene2D.sizeInPx();
        canvasWithFrame.setUnscaledCanvasSize(gameSceneSizePx.x(), gameSceneSizePx.y());
        canvasWithFrame.resizeTo(parentScene.getWidth(), parentScene.getHeight());
        canvasWithFrame.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR.map(Ufx::colorBackground));
    }

    private void configureCanvasWithFrame() {
        canvasWithFrame.setMinScaling(0.5);
        // 28*TS x 36*TS = Arcade map size in pixels
        canvasWithFrame.setUnscaledCanvasSize(28 * TS, 36 * TS);
        canvasWithFrame.setBorderColor(Color.rgb(222, 222, 255));
    }

    private void configurePropertyBindings() {
        PROPERTY_CANVAS_FONT_SMOOTHING.addListener((py, ov, smooth)
            -> canvasWithFrame.canvas().getGraphicsContext2D().setFontSmoothingType(smooth ? FontSmoothingType.LCD : FontSmoothingType.GRAY));

        PROPERTY_CANVAS_IMAGE_SMOOTHING.addListener((py, ov, smooth)
            -> canvasWithFrame.canvas().getGraphicsContext2D().setImageSmoothing(smooth));

        PROPERTY_DEBUG_INFO_VISIBLE.addListener((py, ov, debug)
            -> {
               canvasLayer.setBackground(debug ? colorBackground(Color.TEAL) : null);
               canvasLayer.setBorder(debug ? border(Color.LIGHTGREEN, 1) : null);
            });
    }

    private void createLayout() {
        canvasLayer.setCenter(canvasWithFrame);

        dashboardAndMiniViewLayer.setLeft(dashboard);
        dashboardAndMiniViewLayer.setRight(miniView);
        dashboardAndMiniViewLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboard.isVisible() || PROPERTY_MINI_VIEW_ON.get(),
            dashboard.visibleProperty(), PROPERTY_MINI_VIEW_ON
        ));

        //TODO reconsider help functionality
        helpLayer = new HelpLayer(canvasWithFrame);
        helpLayer.setMouseTransparent(true);

        getChildren().addAll(canvasLayer, dashboardAndMiniViewLayer, helpLayer);
    }

    // Dashboard access

    public void configureDashboard(List<DashboardID> dashboardIDs) {
        dashboard.configure(dashboardIDs);
    }

    public void initDashboard() {
        dashboard.init(ui);
    }

    public void showDashboard(boolean show) {
        dashboard.setVisible(show);
    }

    public void toggleDashboard() {
        dashboard.toggleVisibility();
    }
}