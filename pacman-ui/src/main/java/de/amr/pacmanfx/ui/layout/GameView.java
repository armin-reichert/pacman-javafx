/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.PacManGames_UI;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.*;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.InfoBox;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.uilib.CameraControlledView;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.ui._2d.GameRenderer.fillCanvas;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GameView implements PacManGames_View {

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

    private final Map<KeyCombination, GameAction> actionBindings = new HashMap<>();

    private final PacManGames_UI ui;
    private final StackPane root = new StackPane();
    private final Scene parentScene;

    private FlashMessageView flashMessageLayer;
    private BorderPane canvasLayer;
    private PopupLayer popupLayer; // help, signature
    private BorderPane dashboardLayer;

    private final Dashboard dashboard = new Dashboard();
    private final Canvas canvas = new Canvas();
    private final CrudeCanvasContainer canvasContainer = new CrudeCanvasContainer(canvas);
    private final MiniGameView miniGameView;
    private final ContextMenu contextMenu = new ContextMenu();
    private final StringBinding titleBinding;

    public GameView(PacManGames_UI ui, Scene parentScene) {
        this.ui = requireNonNull(ui);
        this.parentScene = requireNonNull(parentScene);
        this.miniGameView = new MiniGameView();

        configureMiniGameView();
        configureCanvasContainer();
        createLayers();
        configurePropertyBindings();

        root.setOnContextMenuRequested(this::handleContextMenuRequest);

        //TODO what is the cleanest solution to hide the context menu in all needed cases?

        // game scene changes: hide it
        ui.currentGameSceneProperty().addListener(this::handleGameSceneChange);

        // any other mouse button clicked: hide it
        parentScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                contextMenu.hide();
            }
        });

        parentScene.widthProperty() .addListener((py, ov, width)  -> canvasContainer.resizeTo(width.doubleValue(), parentScene.getHeight()));
        parentScene.heightProperty().addListener((py, ov, height) -> canvasContainer.resizeTo(parentScene.getWidth(), height.doubleValue()));

        titleBinding = Bindings.createStringBinding(
            () -> computeTitleText(PY_3D_ENABLED.get(), PY_DEBUG_INFO_VISIBLE.get()),
            PY_3D_ENABLED,
            PY_DEBUG_INFO_VISIBLE,
            theClock().pausedProperty(),
            parentScene.heightProperty(),
            ui.currentGameSceneProperty()
        );

        bindAction(ACTION_BOOT_SHOW_GAME_VIEW, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_ENTER_FULLSCREEN, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_QUIT_GAME_SCENE, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_SHOW_HELP, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_SIMULATION_SLOWER, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_SIMULATION_FASTER, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_SIMULATION_RESET, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_SIMULATION_ONE_STEP, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_SIMULATION_TEN_STEPS, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_TOGGLE_AUTOPILOT, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_TOGGLE_DEBUG_INFO, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_TOGGLE_MUTED, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_TOGGLE_PAUSED, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_TOGGLE_DASHBOARD, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_TOGGLE_IMMUNITY, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_TOGGLE_PIP_VISIBILITY, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_TOGGLE_PLAY_SCENE_2D_3D, GLOBAL_ACTION_BINDING_MAP);
    }

    private void handleGameSceneChange(ObservableValue<? extends GameScene> obs, GameScene oldScene, GameScene newScene) {
        if (newScene != null) embedGameScene(newScene);
        contextMenu.hide();
    }

    private void handleContextMenuRequest(ContextMenuEvent contextMenuEvent) {
        contextMenu.getItems().clear();
        ui.currentGameScene().ifPresent(gameScene -> {
            if (ui.currentGameSceneIsPlayScene2D()) {
                var miSwitchTo3D = new MenuItem(theAssets().text("use_3D_scene"));
                miSwitchTo3D.setOnAction(actionEvent -> GameAction.executeIfEnabled(ui, ACTION_TOGGLE_PLAY_SCENE_2D_3D));
                contextMenu.getItems().add(menuTitleItem(theAssets().text("scene_display")));
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

    public void showHelp() {
        popupLayer.showHelp(canvasContainer.scaling());
    }

    // Asset key regex: app.title.(ms_pacman|ms_pacman_xxl|pacman,pacman_xxl|tengen)(.paused)?
    private String computeTitleText(boolean threeDModeEnabled, boolean modeDebug) {
        String ans = ui.configuration().assetNamespace();
        String paused = theClock().isPaused() ? ".paused" : "";
        String key = "app.title." + ans + paused;
        String modeText = theAssets().text(threeDModeEnabled ? "threeD" : "twoD");
        GameScene currentGameScene = ui.currentGameScene().orElse(null);
        if (currentGameScene == null || !modeDebug) {
            return theAssets().text(key, modeText);
        }
        String sceneClassName = currentGameScene.getClass().getSimpleName();
        if (currentGameScene instanceof GameScene2D gameScene2D) {
            return theAssets().text(key, modeText)
                + " [%s]".formatted(sceneClassName)
                + " (%.2fx)".formatted(gameScene2D.scaling());
        }
        return theAssets().text(key, modeText) + " [%s]".formatted(sceneClassName);
    }

    @Override
    public Keyboard keyboard() {
        return theKeyboard();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

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

        if (miniGameView.isVisible() && theUI().currentGameSceneIsPlayScene3D() && optGameLevel().isPresent()) {
            miniGameView.draw(theGameLevel());
        }
        flashMessageLayer.update();

        // Dashboard updates must be called from permanent clock task too!
        if (dashboardLayer.isVisible()) {
            dashboard.infoBoxes().filter(InfoBox::isExpanded).forEach(InfoBox::update);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ActionProvider interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public Map<KeyCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    @Override
    public void handleKeyboardInput() {
        runMatchingActionOrElse(ui, () -> ui.currentGameScene().ifPresent(GameScene::handleKeyboardInput));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        Logger.trace("Handle {}", gameEvent);
        switch (gameEvent.type()) {
            case LEVEL_CREATED -> {
                PacManGames_UIConfig config = ui.configuration();
                ActorAnimationMap pacAnimationMap = config.createPacAnimations(theGameLevel().pac());
                theGameLevel().pac().setAnimations(pacAnimationMap);
                theGameLevel().ghosts().forEach(ghost -> {
                    ActorAnimationMap ghostAnimationMap = config.createGhostAnimations(ghost);
                    ghost.setAnimations(ghostAnimationMap);
                });
                miniGameView.onLevelCreated(theGameLevel());

                // size of game scene might have changed, so re-embed
                ui.currentGameScene().ifPresent(this::embedGameScene);
            }
            case GAME_STATE_CHANGED -> {
                if (theGameState() == GameState.LEVEL_COMPLETE) {
                    miniGameView.onLevelCompleted(theGameLevel());
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
        final GameScene nextGameScene = ui.configuration().selectGameScene(theGame(), theGameState());
        if (nextGameScene == null) {
            String errorMessage = " Katastrophe! Could not determine game scene!";
            ui.showFlashMessageSec(60, errorMessage);
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

        // Handle switching between 2D and 3D game variants
        byte sceneSwitchType = identifySceneSwitchType(currentGameScene, nextGameScene);
        switch (sceneSwitchType) {
            case 23 -> nextGameScene.onSwitch_2D_3D(currentGameScene);
            case 32 -> nextGameScene.onSwitch_3D_2D(currentGameScene);
            case  0 -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
        }

        if (changing) {
            ui.currentGameSceneProperty().set(nextGameScene);
        }
    }

    public void quitCurrentGameScene() {
        ui.currentGameScene().ifPresent(gameScene -> {
            gameScene.end();
            theGameController().changeGameState(GameState.BOOT);
            theGame().resetEverything();
            if (!theCoinMechanism().isEmpty()) theCoinMechanism().consumeCoin();
            ui.showStartView();
            Logger.info("Current game scene ({}) has been quit", gameScene.getClass().getSimpleName());
        });
    }

    private void embedGameScene(GameScene gameScene) {
        requireNonNull(gameScene);
        // a camera controlled game scene can also be a 2D game scene!
        if (gameScene instanceof GameScene2D gameScene2D) {
            gameScene2D.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
        }
        switch (gameScene) {
            case CameraControlledView gameSceneWithCamera -> {
                gameSceneWithCamera.viewPortWidthProperty().bind(parentScene.widthProperty());
                gameSceneWithCamera.viewPortHeightProperty().bind(parentScene.heightProperty());

                root.getChildren().set(0, gameSceneWithCamera.viewPort());
            }
            case GameScene2D gameScene2D -> {
                Vector2f sceneSize = gameScene2D.sizeInPx();
                canvasContainer.backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
                canvasContainer.setUnscaledCanvasSize(sceneSize.x(), sceneSize.y());
                canvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());

                gameScene2D.scalingProperty().bind(canvasContainer.scalingProperty()
                        .map(scaling -> Math.min(scaling.doubleValue(), MAX_SCENE_2D_SCALING)));
                gameScene2D.setCanvas(canvas);

                GameRenderer gameRenderer = ui.configuration().createGameRenderer(canvas);
                if (gameRenderer instanceof SpriteGameRenderer spriteGameRenderer) {
                    gameScene2D.setGameRenderer(spriteGameRenderer);
                } else {
                    Logger.error("Currently, only sprite game renderers are supported for 2D game scenes");
                }
                fillCanvas(gameScene2D.canvas(), gameScene2D.backgroundColor());

                root.getChildren().set(0, canvasLayer);
            }
            default -> Logger.error("Cannot embed game scene of class {}", gameScene.getClass().getName());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void configureMiniGameView() {
        miniGameView.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
        miniGameView.debugProperty().bind(PY_DEBUG_INFO_VISIBLE);
        miniGameView.canvasHeightProperty().bind(PY_PIP_HEIGHT);
        miniGameView.opacityProperty().bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
        miniGameView.visibleProperty().bind(Bindings.createObjectBinding(
            () -> PY_MINI_VIEW_ON.get() && ui.currentGameSceneIsPlayScene3D(),
            PY_MINI_VIEW_ON, ui.currentGameSceneProperty()
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
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        PY_CANVAS_FONT_SMOOTHING.addListener((py, ov, on) -> ctx.setFontSmoothingType(on ? FontSmoothingType.LCD : FontSmoothingType.GRAY));
        PY_CANVAS_IMAGE_SMOOTHING.addListener((py, ov, on) -> ctx.setImageSmoothing(on));
        PY_DEBUG_INFO_VISIBLE.addListener((py, ov, debug) -> {
            canvasLayer.setBackground(debug? coloredBackground(Color.TEAL) : null);
            canvasLayer.setBorder(debug? border(Color.LIGHTGREEN, 1) : null);
        });
    }

    private void createLayers() {
        canvasLayer = new BorderPane(canvasContainer);

        dashboardLayer = new BorderPane();
        dashboardLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboard.isVisible() || PY_MINI_VIEW_ON.get(),
            dashboard.visibleProperty(), PY_MINI_VIEW_ON
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