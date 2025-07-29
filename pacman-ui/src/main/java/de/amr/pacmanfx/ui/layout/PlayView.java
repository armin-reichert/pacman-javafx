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
import de.amr.pacmanfx.ui.ActionBindingMap;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.CrudeCanvasContainer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.PopupLayer;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.InfoBox;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
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
import static de.amr.pacmanfx.ui.GameUI.GAME_ACTION_KEY_COMBINATIONS;
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

    private final ActionBindingMap actionBindings;

    private final GameUI ui;
    private final GameContext gameContext;
    private final StackPane root = new StackPane();
    private final Scene parentScene;

    private FlashMessageView flashMessageLayer;
    private BorderPane canvasLayer;
    private PopupLayer popupLayer; // help, signature
    private BorderPane dashboardLayer;

    private final Dashboard dashboard;
    private final Canvas commonCanvas = new Canvas();
    private final CrudeCanvasContainer canvasContainer = new CrudeCanvasContainer(commonCanvas);
    private final MiniGameView miniGameView;
    private final ContextMenu contextMenu = new ContextMenu();
    private final StringBinding titleBinding;

    public PlayView(GameUI ui, GameContext gameContext, Scene parentScene) {
        this.ui = requireNonNull(ui);
        this.gameContext = requireNonNull(gameContext);
        this.parentScene = requireNonNull(parentScene);
        this.miniGameView = new MiniGameView();
        this.dashboard = new Dashboard(ui);
        
        this.actionBindings = new ActionBindingMap(ui.theKeyboard());

        configureMiniGameView();
        configureCanvasContainer();
        createLayers();
        configurePropertyBindings();

        root.setOnContextMenuRequested(this::handleContextMenuRequest);

        //TODO what is the cleanest solution to hide the context menu in all needed cases?

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

        titleBinding = Bindings.createStringBinding(
            () -> computeTitleText(ui.property3DEnabled().get(), ui.propertyDebugInfoVisible().get()),
            ui.property3DEnabled(),
            ui.propertyDebugInfoVisible(),
            ui.theGameClock().pausedProperty(),
            parentScene.heightProperty(),
            ui.propertyCurrentGameScene()
        );
        actionBindings.use(ACTION_BOOT_SHOW_PLAY_VIEW, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_ENTER_FULLSCREEN, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_QUIT_GAME_SCENE, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_SHOW_HELP, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_SIMULATION_SLOWER, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_SIMULATION_FASTER, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_SIMULATION_RESET, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_SIMULATION_ONE_STEP, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_SIMULATION_TEN_STEPS, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_TOGGLE_AUTOPILOT, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_TOGGLE_DEBUG_INFO, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_TOGGLE_MUTED, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_TOGGLE_PAUSED, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_TOGGLE_DASHBOARD, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_TOGGLE_IMMUNITY, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_TOGGLE_PIP_VISIBILITY, GAME_ACTION_KEY_COMBINATIONS);
        actionBindings.use(ACTION_TOGGLE_PLAY_SCENE_2D_3D, GAME_ACTION_KEY_COMBINATIONS);
    }

    private void handleGameSceneChange(ObservableValue<? extends GameScene> obs, GameScene oldScene, GameScene newScene) {
        if (newScene != null) embedGameScene(newScene);
        contextMenu.hide();
    }

    private void handleContextMenuRequest(ContextMenuEvent contextMenuEvent) {
        contextMenu.getItems().clear();
        ui.currentGameScene().ifPresent(gameScene -> {
            if (ui.currentGameSceneIsPlayScene2D()) {
                var miSwitchTo3D = new MenuItem(ui.theAssets().text("use_3D_scene"));
                miSwitchTo3D.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));
                contextMenu.getItems().add(ui.createContextMenuTitleItem(ui.theAssets().text("scene_display")));
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

    // Asset key regex: app.title.(ms_pacman|ms_pacman_xxl|pacman,pacman_xxl|tengen)(.paused)?
    private String computeTitleText(boolean threeDModeEnabled, boolean modeDebug) {
        String ans = ui.theConfiguration().assetNamespace();
        String paused = ui.theGameClock().isPaused() ? ".paused" : "";
        String key = "app.title." + ans + paused;
        String modeText = ui.theAssets().text(threeDModeEnabled ? "threeD" : "twoD");
        GameScene currentGameScene = ui.currentGameScene().orElse(null);
        if (currentGameScene == null || !modeDebug) {
            return ui.theAssets().text(key, modeText);
        }
        String sceneClassName = currentGameScene.getClass().getSimpleName();
        if (currentGameScene instanceof GameScene2D gameScene2D) {
            return ui.theAssets().text(key, modeText)
                + " [%s]".formatted(sceneClassName)
                + " (%.2fx)".formatted(gameScene2D.scaling());
        }
        return ui.theAssets().text(key, modeText) + " [%s]".formatted(sceneClassName);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // PacManGames_View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ActionBindingMap actionBindingMap() {
        return actionBindings;
    }

    @Override
    public StackPane rootNode() {
        return root;
    }

    @Override
    public StringBinding title() {
        return titleBinding;
    }

    public void draw() {
        ui.currentGameScene().ifPresent(gameScene -> {
            if (gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.draw();
            }
        });

        if (miniGameView.isVisible() && ui.currentGameSceneIsPlayScene3D() && gameContext.optGameLevel().isPresent()) {
            miniGameView.draw(ui, gameContext.theGameLevel());
        }
        flashMessageLayer.update();

        // Dashboard updates must be called from permanent clock task too!
        if (dashboardLayer.isVisible()) {
            dashboard.infoBoxes().filter(InfoBox::isExpanded).forEach(InfoBox::update);
        }
    }

    @Override
    public void handleKeyboardInput(GameUI ui) {
        actionBindings.runMatchingActionOrElse(ui, () -> ui.currentGameScene().ifPresent(GameScene::handleKeyboardInput));
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

    public FlashMessageView flashMessageLayer() {
        return flashMessageLayer;
    }

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
            scaling -> Math.min(scaling.doubleValue(), ui.thePrefs().getFloat("scene2d.max_scaling"))));
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
            () -> ui.propertyMiniViewOn().get() && ui.currentGameSceneIsPlayScene3D(),
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

    private void createLayers() {
        canvasLayer = new BorderPane(canvasContainer);

        dashboardLayer = new BorderPane();
        dashboardLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboard.isVisible() || ui.propertyMiniViewOn().get(),
            dashboard.visibleProperty(), ui.propertyMiniViewOn()
        ));
        dashboardLayer.setLeft(dashboard);
        dashboardLayer.setRight(miniGameView);

        dashboard.setVisible(false);

        //TODO reconsider this and the help functionality
        popupLayer = new PopupLayer(canvasContainer);
        popupLayer.setMouseTransparent(true);

        flashMessageLayer = new FlashMessageView();

        root.getChildren().addAll(canvasLayer, dashboardLayer, popupLayer, flashMessageLayer);
    }
}