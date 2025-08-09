/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.ui.*;
import de.amr.pacmanfx.ui._2d.CrudeCanvasContainer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.PopupLayer;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.InfoBox;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.GameUI.DEFAULT_ACTION_BINDINGS;
import static de.amr.pacmanfx.ui.GameUI_Config.SCENE_ID_PLAY_SCENE_2D;
import static de.amr.pacmanfx.ui.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.uilib.Ufx.border;
import static de.amr.pacmanfx.uilib.Ufx.colorBackground;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class PlayView implements PacManGames_View {

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

    private final ActionBindingManager actionBindings;

    private final GameUI ui;
    private final GameContext gameContext;
    private final StackPane root = new StackPane();
    private final Scene parentScene;

    private BorderPane canvasLayer;
    private PopupLayer popupLayer; // help, signature
    private BorderPane dashboardLayer;

    private final Dashboard dashboard;
    private final Canvas commonCanvas = new Canvas();
    private final CrudeCanvasContainer canvasContainer = new CrudeCanvasContainer(commonCanvas);
    private final MiniGameView miniGameView;
    private final ContextMenu contextMenu = new ContextMenu();

    public PlayView(GameUI ui, GameContext gameContext, Scene parentScene) {
        this.ui = requireNonNull(ui);
        this.gameContext = requireNonNull(gameContext);
        this.parentScene = requireNonNull(parentScene);
        this.miniGameView = new MiniGameView();
        this.dashboard = new Dashboard(ui);
        
        this.actionBindings = new DefaultActionBindingManager();

        configureMiniGameView();
        configureCanvasContainer();
        createLayout();
        configurePropertyBindings();

        //TODO what is the cleanest solution to hide the context menu in all needed cases?
        root.setOnContextMenuRequested(this::handleContextMenuRequest);
        // game scene changes: hide it
        ui.propertyCurrentGameScene().addListener(this::handleGameSceneChange);
        // any other mouse button clicked: hide it
        parentScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                contextMenu.hide();
            }
        });

        parentScene.widthProperty() .addListener((py, ov, width)  -> canvasContainer.resizeTo(width.doubleValue(), parentScene.getHeight()));
        parentScene.heightProperty().addListener((py, ov, height) -> canvasContainer.resizeTo(parentScene.getWidth(), height.doubleValue()));

        actionBindings.use(ACTION_BOOT_SHOW_PLAY_VIEW, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_ENTER_FULLSCREEN, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_QUIT_GAME_SCENE, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_SHOW_HELP, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_SIMULATION_SLOWER, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_SIMULATION_FASTER, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_SIMULATION_RESET, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_SIMULATION_ONE_STEP, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_SIMULATION_TEN_STEPS, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_TOGGLE_AUTOPILOT, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_TOGGLE_DEBUG_INFO, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_TOGGLE_MUTED, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_TOGGLE_PAUSED, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_TOGGLE_DASHBOARD, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_TOGGLE_IMMUNITY, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_TOGGLE_MINI_VIEW_VISIBILITY, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_TOGGLE_PLAY_SCENE_2D_3D, DEFAULT_ACTION_BINDINGS);
    }

    private void handleGameSceneChange(ObservableValue<? extends GameScene> obs, GameScene oldScene, GameScene newScene) {
        if (newScene != null) embedGameScene(newScene);
        contextMenu.hide();
    }

    private void handleContextMenuRequest(ContextMenuEvent contextMenuEvent) {
        contextMenu.getItems().clear();
        ui.currentGameScene().ifPresent(gameScene -> {
            if (ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D)) {
                var miSwitchTo3D = new MenuItem(ui.theAssets().text("use_3D_scene"));
                miSwitchTo3D.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));
                contextMenu.getItems().add(ui.createContextMenuTitle("scene_display"));
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
        contextMenu.show(root, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }

    public void showHelp(GameUI ui) {
        popupLayer.showHelp(ui, canvasContainer.scaling());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // PacManGames_View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingManager actionBindingMap() {
        return actionBindings;
    }

    @Override
    public StackPane rootNode() {
        return root;
    }

    public void draw() {
        ui.currentGameScene().ifPresent(gameScene -> {
            if (gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.draw();
            }
        });

        if (miniGameView.isVisible() && ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D) && gameContext.optGameLevel().isPresent()) {
            miniGameView.draw(ui, gameContext.theGameLevel());
        }

        // Dashboard updates must be called from permanent clock task too!
        if (dashboardLayer.isVisible()) {
            dashboard.infoBoxes().filter(InfoBox::isExpanded).forEach(InfoBox::update);
        }
    }

    @Override
    public void handleKeyboardInput(GameUI ui) {
        GameAction matchingAction = actionBindings.matchingAction(ui.theKeyboard()).orElse(null);
        ui.runActionOrElse(matchingAction,
            () -> ui.currentGameScene().ifPresent(GameScene::handleKeyboardInput)
        );
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        Logger.trace("Handle {}", gameEvent);
        switch (gameEvent.type()) {
            case LEVEL_CREATED -> {
                GameUI_Config config = ui.theConfiguration();
                ActorAnimationMap pacAnimationMap = config.createPacAnimations(gameContext.theGameLevel().pac());
                gameContext.theGameLevel().pac().setAnimations(pacAnimationMap);
                gameContext.theGameLevel().ghosts().forEach(ghost -> {
                    ActorAnimationMap ghostAnimationMap = config.createGhostAnimations(ghost);
                    ghost.setAnimations(ghostAnimationMap);
                });
                miniGameView.onLevelCreated(ui, gameContext.theGameLevel());

                // size of game scene might have changed, so re-embed
                ui.currentGameScene().ifPresent(this::embedGameScene);
            }
            case GAME_STATE_CHANGED -> {
                if (gameContext.theGameState() == GameState.LEVEL_COMPLETE) {
                    miniGameView.onLevelCompleted();
                }
            }
        }
        ui.currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(gameEvent));
        updateGameScene(false);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public Dashboard dashboard() {
        return dashboard;
    }

    public CrudeCanvasContainer canvasContainer() {
        return canvasContainer;
    }

    public void updateGameScene(boolean reloadCurrent) {
        final GameScene nextGameScene = ui.theConfiguration().selectGameScene(gameContext);
        if (nextGameScene == null) {
            String errorMessage = " Katastrophe! Could not determine game scene!";
            ui.showFlashMessageSec(30, errorMessage);
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
        embedGameScene(nextGameScene);
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
            ui.propertyCurrentGameScene().set(nextGameScene);
        }
    }

    public void quitCurrentGameScene() {
        ui.currentGameScene().ifPresent(gameScene -> {
            gameScene.end();
            gameContext.theGameController().changeGameState(GameState.BOOT);
            gameContext.theGame().resetEverything();
            if (!gameContext.theCoinMechanism().isEmpty()) gameContext.theCoinMechanism().consumeCoin();
            ui.showStartView();
            Logger.info("Current game scene ({}) has been quit", gameScene.getClass().getSimpleName());
        });
    }

    private void embedGameScene(GameScene gameScene) {
        if (gameScene.optSubScene().isPresent()) {
            SubScene subScene = gameScene.optSubScene().get();
            subScene.widthProperty().bind(parentScene.widthProperty());
            subScene.heightProperty().bind(parentScene.heightProperty());
            root.getChildren().set(0, subScene);
        }
        else if (gameScene instanceof GameScene2D gameScene2D) {
            embedGameScene2DDirectly(gameScene2D);
            gameScene2D.backgroundColorProperty().bind(ui.propertyCanvasBackgroundColor());
            gameScene2D.clear();
        }
        else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }

    // Game scenes without camera are drawn into the canvas provided by this play view
    private void embedGameScene2DDirectly(GameScene2D gameScene2D) {
        gameScene2D.setCanvas(commonCanvas);
        gameScene2D.setGameRenderer(ui.theConfiguration().createGameRenderer(commonCanvas));
        gameScene2D.scalingProperty().bind(canvasContainer.scalingProperty().map(
            scaling -> Math.min(scaling.doubleValue(), ui.theUserPrefs().getFloat("scene2d.max_scaling"))));
        Vector2f sizePx = gameScene2D.sizeInPx();
        canvasContainer.setUnscaledCanvasSize(sizePx.x(), sizePx.y());
        canvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
        canvasContainer.backgroundProperty().bind(ui.propertyCanvasBackgroundColor().map(Ufx::colorBackground));
        root.getChildren().set(0, canvasLayer);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void configureMiniGameView() {
        miniGameView.backgroundColorProperty().bind(ui.propertyCanvasBackgroundColor());
        miniGameView.debugProperty().bind(ui.propertyDebugInfoVisible());
        miniGameView.canvasHeightProperty().bind(ui.propertyMiniViewHeight());
        miniGameView.opacityProperty().bind(ui.propertyMiniViewOpacityPercent().divide(100.0));
        miniGameView.visibleProperty().bind(Bindings.createObjectBinding(
            () -> ui.propertyMiniViewOn().get() && ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D),
                ui.propertyMiniViewOn(), ui.propertyCurrentGameScene()
        ));
    }

    private void configureCanvasContainer() {
        canvasContainer.setMinScaling(0.5);
        // 28*TS x 36*TS = Arcade map size in pixels
        canvasContainer.setUnscaledCanvasSize(28 *TS, 36 * TS);
        canvasContainer.setBorderColor(Color.rgb(222, 222, 255));
        canvasContainer.roundedBorderProperty().addListener((py, ov, nv) -> ui.currentGameScene().ifPresent(this::embedGameScene));
    }

    private void configurePropertyBindings() {
        GraphicsContext ctx = commonCanvas.getGraphicsContext2D();
        ui.propertyCanvasFontSmoothing().addListener((py, ov, on) -> ctx.setFontSmoothingType(on ? FontSmoothingType.LCD : FontSmoothingType.GRAY));
        ui.propertyCanvasImageSmoothing().addListener((py, ov, on) -> ctx.setImageSmoothing(on));
        ui.propertyDebugInfoVisible().addListener((py, ov, debug) -> {
            canvasLayer.setBackground(debug? colorBackground(Color.TEAL) : null);
            canvasLayer.setBorder(debug? border(Color.LIGHTGREEN, 1) : null);
        });
    }

    private void createLayout() {
        canvasLayer = new BorderPane(canvasContainer);

        dashboardLayer = new BorderPane();
        dashboardLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboard.isVisible() || ui.propertyMiniViewOn().get(),
            dashboard.visibleProperty(), ui.propertyMiniViewOn()
        ));
        dashboardLayer.setLeft(dashboard);
        dashboardLayer.setRight(miniGameView);
        dashboard.setVisible(false);

        //TODO reconsider help functionality
        popupLayer = new PopupLayer(canvasContainer);
        popupLayer.setMouseTransparent(true);

        root.getChildren().addAll(canvasLayer, dashboardLayer, popupLayer);
    }
}