/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.PacManGames_ActionBindings;
import de.amr.pacmanfx.ui.PacManGames_Actions;
import de.amr.pacmanfx.ui.PacManGames_UIConfiguration;
import de.amr.pacmanfx.ui._2d.*;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.InfoBox;
import de.amr.pacmanfx.ui.dashboard.InfoBoxReadmeFirst;
import de.amr.pacmanfx.uilib.*;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static de.amr.pacmanfx.uilib.input.Keyboard.nude;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GameView implements PacManGames_View, PacManGames_ActionBindings {

    private final Map<KeyCombination, GameAction> actionBindings = new HashMap<>();

    private final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            GameScene gameScene = get();
            if (gameScene != null) embedGameScene(theUI().currentConfig(), gameScene);
            contextMenu.hide();
        }
    };

    private final StackPane root = new StackPane();
    private final Scene parentScene;

    private FlashMessageView flashMessageLayer;
    private BorderPane canvasLayer;
    private PopupLayer popupLayer; // help, signature
    private BorderPane dashboardLayer;

    private final VBox dashboardContainer = new VBox();
    private final Dashboard dashboard = new Dashboard();
    private final Canvas canvas = new Canvas();
    private final TooFancyCanvasContainer canvasContainer = new TooFancyCanvasContainer(canvas);
    private final PictureInPictureView pipView = new PictureInPictureView();
    private final VBox pipContainer = new VBox(pipView, new HBox());
    private final ContextMenu contextMenu = new ContextMenu();

    private StringBinding titleBinding;

    public GameView(Scene parentScene) {
        this.parentScene = parentScene;
        configureCanvasContainer();
        configurePiPView();
        createLayers();
        configurePropertyBindings();
        root.setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO is this the recommended way to close a context-menu?
        parentScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (contextMenu.isShowing() && e.getButton() == MouseButton.PRIMARY) {
                contextMenu.hide();
            }
        });
        bindActions();
    }

    private void bindActions() {
        bindActionToCommonKeys(PacManGames_Actions.ACTION_BOOT_SHOW_GAME_VIEW);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_QUIT_GAME_SCENE);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_SIMULATION_SLOWER);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_SIMULATION_FASTER);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_SIMULATION_RESET);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_SIMULATION_ONE_STEP);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_SIMULATION_TEN_STEPS);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_TOGGLE_AUTOPILOT);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_TOGGLE_DEBUG_INFO);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_TOGGLE_PAUSED);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_TOGGLE_DASHBOARD);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_TOGGLE_IMMUNITY);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_TOGGLE_PIP_VISIBILITY);
        bindActionToCommonKeys(PacManGames_Actions.ACTION_TOGGLE_PLAY_SCENE_2D_3D);
        bind(this::showGameSceneHelp, nude(KeyCode.H));
    }

    public void setTitleBinding(StringBinding binding) {
        titleBinding = requireNonNull(binding);
    }

    public void doSimulationStepAndUpdateGameScene() {
        theSimulationStep().start(theClock().tickCount());
        theGameController().updateGameState();
        theSimulationStep().log();
        currentGameScene().ifPresent(GameScene::update);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public StackPane layoutRoot() {
        return root;
    }

    @Override
    public StringBinding titleBinding() {
        return titleBinding;
    }

    public void draw() {
        currentGameScene().ifPresent(gameScene -> {
            if (gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.draw();
            }
        });
        pipView.draw();
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
        runMatchingActionOrElse(() -> currentGameScene().ifPresent(ActionBindingsProvider::handleKeyboardInput));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent event) {
        Logger.trace("{} received game event {}", getClass().getSimpleName(), event);
        // dispatch event to event specific method:
        PacManGames_View.super.onGameEvent(event);
        // dispatch to current game scene
        currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(event));
        updateGameScene(false);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        //TODO find another point in time to do this
        optGameLevel().ifPresent(level -> {
            PacManGames_UIConfiguration config = theUI().currentConfig();
            config.createActorAnimations(level);
            theSound().setEnabled(!level.isDemoLevel());
            // size of game scene might have changed, so re-embed
            currentGameScene().ifPresent(gameScene -> embedGameScene(config, gameScene));
            pipView.setScene2D(config.createPiPScene(canvas));
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    public ObjectProperty<GameScene> gameSceneProperty() { return gameScenePy; }

    public Optional<GameScene> currentGameScene() { return Optional.ofNullable(gameScenePy.get()); }

    public FlashMessageView flashMessageLayer() {
        return flashMessageLayer;
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public void setDashboardVisible(boolean visible) {
        if (visible) {
            dashboardContainer.setVisible(true);
            updateDashboard();
        } else {
            dashboardContainer.setVisible(false);
        }
    }

    public void updateDashboard() {
        InfoBox[] infoBoxes = dashboard.infoBoxes()
                .filter(infoBox -> !(infoBox instanceof InfoBoxReadmeFirst infoBoxReadmeFirst) || !infoBoxReadmeFirst.isRead())
                .toArray(InfoBox[]::new);
        dashboard.infoBoxes().filter(infoBox -> infoBox instanceof InfoBoxReadmeFirst).findFirst().ifPresent(infoBox -> {
            InfoBoxReadmeFirst readmeFirst = (InfoBoxReadmeFirst) infoBox;
            readmeFirst.setActionIfRead(this::updateDashboard);
        });
        dashboardContainer.getChildren().setAll(infoBoxes);
    }

    public void toggleDashboardVisibility() {
        setDashboardVisible(!dashboardContainer.isVisible());
    }

    public TooFancyCanvasContainer canvasContainer() {
        return canvasContainer;
    }

    public void resize(Scene containingScene) {
        canvasContainer.resizeTo(containingScene.getWidth(), containingScene.getHeight());
    }

    public void updateGameScene(boolean reloadCurrent) {
        PacManGames_UIConfiguration uiConfig = theUI().currentConfig();
        final GameScene nextGameScene = uiConfig.selectGameScene(theGame(), theGameState());
        if (nextGameScene == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }
        final GameScene currentGameScene = gameScenePy.get();
        final boolean changing = nextGameScene != currentGameScene;
        if (!changing && !reloadCurrent) {
            return;
        }
        if (currentGameScene != null) {
            currentGameScene.end();
            Logger.info("Game scene ended: {}", currentGameScene.displayName());
        }
        embedGameScene(uiConfig, nextGameScene);
        nextGameScene.init();

        // Handle switching between 2D and 3D game variants
        byte sceneSwitchType = identifySceneSwitchType(uiConfig, currentGameScene, nextGameScene);
        switch (sceneSwitchType) {
            case 23 -> nextGameScene.onSwitch_2D_3D(currentGameScene);
            case 32 -> nextGameScene.onSwitch_3D_2D(currentGameScene);
            case  0 -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
        }

        if (changing) {
            gameScenePy.set(nextGameScene);
        }
    }

    /**
     * @param config UI configuration
     * @param sceneBefore scene displayed before switching
     * @param sceneAfter scene displayed after switching
     * @return <code>23</code> if 2D -> 3D switch, <code>32</code> if 3D -> 2D switch</code>,
     *  <code>0</code> if scene before switch is not yet available
     */
    private byte identifySceneSwitchType(PacManGames_UIConfiguration config, GameScene sceneBefore, GameScene sceneAfter) {
        if (sceneBefore == null && sceneAfter == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (sceneBefore) {
            case null -> 0; // may happen, it's ok
            case GameScene2D playScene2D when sceneAfter instanceof PlayScene3D -> 23;
            case PlayScene3D playScene3D when sceneAfter instanceof GameScene2D -> 32;
            default -> 0;
        };
    }


    public void embedGameScene(PacManGames_UIConfiguration gameUIConfig, GameScene gameScene) {
        requireNonNull(gameScene);
        switch (gameScene) {
            case CameraControlledView gameSceneUsingCamera -> {
                root.getChildren().set(0, gameSceneUsingCamera.viewPort());
                gameSceneUsingCamera.viewPortWidthProperty().bind(parentScene.widthProperty());
                gameSceneUsingCamera.viewPortHeightProperty().bind(parentScene.heightProperty());
                if (gameScene instanceof GameScene2D gameScene2D) {
                    gameScene2D.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
                }
            }
            case GameScene2D gameScene2D -> {
                GameRenderer renderer = gameUIConfig.createRenderer(canvas);
                Vector2f sceneSize = gameScene2D.sizeInPx();
                canvasContainer.setUnscaledCanvasWidth(sceneSize.x());
                canvasContainer.setUnscaledCanvasHeight(sceneSize.y());
                canvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
                canvasContainer.backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
                gameScene2D.scalingProperty().bind(
                    canvasContainer.scalingPy.map(scaling -> Math.min(scaling.doubleValue(), MAX_SCENE_2D_SCALING)));
                gameScene2D.setCanvas(canvas);
                // avoid showing old content before new scene is rendered
                canvas.getGraphicsContext2D().setFill(PY_CANVAS_BG_COLOR.get());
                canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gameScene2D.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
                gameScene2D.setGameRenderer(renderer);
                root.getChildren().set(0, canvasLayer);
            }
            default -> Logger.error("Cannot embed game scene of class {}", gameScene.getClass().getName());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void configureCanvasContainer() {
        canvasContainer.setMinScaling(0.5);
        canvasContainer.setUnscaledCanvasWidth(28*TS); // 28x36 = Arcade map size
        canvasContainer.setUnscaledCanvasHeight(36*TS);
        canvasContainer.setBorderColor(Color.rgb(222, 222, 255));
        //TODO check this:
        canvasContainer.decorationEnabledPy.addListener((py, ov, nv) ->
            currentGameScene().ifPresent(gameScene -> embedGameScene(theUI().currentConfig(), gameScene)));
    }

    private void configurePiPView() {
        pipView.backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Background::fill));
        pipView.opacityProperty().bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
        pipView.visibleProperty().bind(Bindings.createObjectBinding(
            () -> PY_PIP_ON.get() && theUI().currentGameSceneIsPlayScene3D(),
            PY_PIP_ON, gameScenePy
        ));
    }

    private void configurePropertyBindings() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        PY_CANVAS_FONT_SMOOTHING.addListener((py, ov, on) -> g.setFontSmoothingType(on ? FontSmoothingType.LCD : FontSmoothingType.GRAY));
        PY_CANVAS_IMAGE_SMOOTHING.addListener((py, ov, on) -> g.setImageSmoothing(on));
        PY_DEBUG_INFO_VISIBLE.addListener((py, ov, debug) -> {
            if (debug) {
                canvasLayer.setBackground(coloredBackground(Color.DARKGREEN));
                canvasLayer.setBorder(border(Color.LIGHTGREEN, 2));
            } else {
                canvasLayer.setBackground(null);
                canvasLayer.setBorder(null);
            }
        });
    }

    private void createLayers() {
        canvasLayer = new BorderPane(canvasContainer);

        dashboardContainer.setVisible(false);
        dashboardLayer = new BorderPane();
        dashboardLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboardContainer.isVisible() || PY_PIP_ON.get(),
            dashboardContainer.visibleProperty(), PY_PIP_ON
        ));
        dashboardLayer.setLeft(dashboardContainer);
        dashboardLayer.setRight(pipContainer);

        //TODO reconsider this and the help functionality
        popupLayer = new PopupLayer(canvasContainer);
        popupLayer.setMouseTransparent(true);

        flashMessageLayer = new FlashMessageView();

        root.getChildren().addAll(canvasLayer, dashboardLayer, popupLayer, flashMessageLayer);
    }

    private void handleContextMenuRequest(ContextMenuEvent e) {
        var menuItems = new ArrayList<>(gameScenePy.get().supplyContextMenuItems(e));
        if (theUI().currentGameSceneIsPlayScene2D()) {
            var item = new MenuItem(theAssets().text("use_3D_scene"));
            item.setOnAction(ae -> PacManGames_Actions.ACTION_TOGGLE_PLAY_SCENE_2D_3D.execute());
            menuItems.addFirst(item);
            menuItems.addFirst(contextMenuTitleItem(theAssets().text("scene_display")));
        }
        contextMenu.getItems().setAll(menuItems);
        contextMenu.show(root, e.getScreenX(), e.getScreenY());
        contextMenu.requestFocus();
    }

    private void showGameSceneHelp() {
        if (!theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN)
            && theUI().currentGameSceneIs2D()) {
            popupLayer.showHelp(canvasContainer.scaling());
        }
    }
}