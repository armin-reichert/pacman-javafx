/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui._2d.*;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.InfoBox;
import de.amr.pacmanfx.uilib.Action;
import de.amr.pacmanfx.uilib.ActionProvider;
import de.amr.pacmanfx.uilib.CameraControlledView;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
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
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;
import static java.util.Objects.requireNonNull;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GameView implements View {

    private final Map<KeyCodeCombination, Action> actionBindings = new HashMap<>();

    private final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            GameScene gameScene = get();
            if (gameScene != null) embedGameScene(theUIConfig().current(), gameScene);
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
    private final StringExpression titleExpression;
    private final ContextMenu contextMenu = new ContextMenu();

    public GameView(GameUI ui) {
        this.parentScene = ui.mainScene();
        titleExpression = Bindings.createStringBinding(() -> computeTitleText(ui),
                theClock().pausedProperty(), ui.mainScene().heightProperty(), gameScenePy,
                PY_3D_ENABLED, PY_DEBUG_INFO_VISIBLE);
        configureCanvasContainer();
        configurePiPView();
        createLayers();
        configurePropertyBindings();
        root.setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO is this the recommended way to close a context-menu?
        ui.mainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (contextMenu.isShowing() && e.getButton() == MouseButton.PRIMARY) {
                contextMenu.hide();
            }
        });
        bindActions();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // View interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public StackPane layoutRoot() {
        return root;
    }

    @Override
    public StringExpression title() {
        return titleExpression;
    }

    @Override
    public void update() {
        currentGameScene().ifPresent(GameScene::update);
        if (dashboardLayer.isVisible()) {
            dashboard.infoBoxes().filter(InfoBox::isExpanded).forEach(InfoBox::update);
        }
    }

    public void draw() {
        currentGameScene().ifPresent(gameScene -> {
            if (gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.draw();
            }
        });
        flashMessageLayer.update();
        pipView.draw();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ActionProvider interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public Map<KeyCodeCombination, Action> actionBindings() {
        return actionBindings;
    }

    @Override
    public void bindActions() {
        bind(theUI()::restart,                   naked(KeyCode.F3));
        bind(this::showGameSceneHelp,            naked(KeyCode.H));
        bind(GameAction.QUIT_GAME_SCENE,         naked(KeyCode.Q));
        bind(GameAction.SIMULATION_SLOWER,       alt(KeyCode.MINUS));
        bind(GameAction.SIMULATION_FASTER,       alt(KeyCode.PLUS));
        bind(GameAction.SIMULATION_RESET,        alt(KeyCode.DIGIT0));
        bind(GameAction.SIMULATION_ONE_STEP,     shift(KeyCode.P));
        bind(GameAction.SIMULATION_TEN_STEPS,    shift(KeyCode.SPACE));
        bind(GameAction.TOGGLE_AUTOPILOT,        alt(KeyCode.A));
        bind(GameAction.TOGGLE_DEBUG_INFO,       alt(KeyCode.D));
        bind(GameAction.TOGGLE_PAUSED,           naked(KeyCode.P));
        bind(this::toggleDashboardVisibility,    naked(KeyCode.F1), alt(KeyCode.B));
        bind(GameAction.TOGGLE_IMMUNITY,         alt(KeyCode.I));
        // 3D only
        bind(GameAction.TOGGLE_PIP_VISIBILITY,   naked(KeyCode.F2));
        bind(GameAction.TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3));
    }

    @Override
    public void handleKeyboardInput() {
        runTriggeredActionElse(() -> currentGameScene().ifPresent(ActionProvider::handleKeyboardInput));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent event) {
        Logger.trace("{} received game event {}", getClass().getSimpleName(), event);
        // dispatch event to event specific method:
        View.super.onGameEvent(event);
        // dispatch to current game scene
        currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(event));
        updateGameScene(false);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        //TODO find another point in time to do this
        gameLevel().ifPresent(level -> {
            GameUIConfig config = theUIConfig().current();
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
            InfoBox[] infoBoxes = dashboard.infoBoxes().toArray(InfoBox[]::new);
            dashboardContainer.getChildren().setAll(infoBoxes);
            dashboardContainer.setVisible(true);

        } else {
            dashboardContainer.setVisible(false);
        }
    }

    public void toggleDashboardVisibility() {
        setDashboardVisible(!dashboardContainer.isVisible());
    }

    public TooFancyCanvasContainer canvasContainer() {
        return canvasContainer;
    }

    public void resize(double width, double height) {
        canvasContainer.resizeTo(width, height);
    }

    public void updateGameScene(boolean reloadCurrent) {
        GameUIConfig uiConfig = theUIConfig().current();
        final GameScene nextGameScene = uiConfig.selectGameScene(game(), gameState());
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
        if (theUIConfig().is2D3DPlaySceneSwitch(uiConfig, currentGameScene, nextGameScene)) {
            nextGameScene.onSceneVariantSwitch(currentGameScene);
        }
        if (changing) {
            gameScenePy.set(nextGameScene);
        }
    }

    public void embedGameScene(GameUIConfig gameUIConfig, GameScene gameScene) {
        requireNonNull(gameScene);
        switch (gameScene) {
            case CameraControlledView gameSceneUsingCamera -> {
                root.getChildren().set(0, gameSceneUsingCamera.viewPort());
                gameSceneUsingCamera.viewPortWidthProperty().bind(parentScene.widthProperty());
                gameSceneUsingCamera.viewPortHeightProperty().bind(parentScene.heightProperty());
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
        canvasContainer.setUnscaledCanvasWidth(ARCADE_MAP_SIZE_IN_PIXELS.x());
        canvasContainer.setUnscaledCanvasHeight(ARCADE_MAP_SIZE_IN_PIXELS.y());
        canvasContainer.setBorderColor(GameAssets.ARCADE_WHITE);
        //TODO check this:
        canvasContainer.decorationEnabledPy.addListener((py, ov, nv) ->
            currentGameScene().ifPresent(gameScene -> embedGameScene(theUIConfig().current(), gameScene)));
    }

    private void configurePiPView() {
        pipView.backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Background::fill));
        pipView.opacityProperty().bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
        pipView.visibleProperty().bind(Bindings.createObjectBinding(
            () -> PY_PIP_ON.get() && theUIConfig().currentGameSceneIsPlayScene3D(),
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

    private String computeTitleText(GameUI ui) {
        String keyPattern = "app.title." + theUIConfig().current().assetNamespace() + (theClock().isPaused() ? ".paused" : "");
        if (ui.currentGameScene().isPresent()) {
            return computeTitleIfGameScenePresent(ui.currentGameScene().get(), keyPattern);
        }
        return theAssets().text(keyPattern, theAssets().text(PY_3D_ENABLED.get() ? "threeD" : "twoD"));
    }

    private String computeTitleIfGameScenePresent(GameScene gameScene, String keyPattern) {
        String sceneNameSuffix = PY_DEBUG_INFO_VISIBLE.get() ? " [%s]".formatted(gameScene.getClass().getSimpleName()) : "";
        String modeKey = theAssets().text(PY_3D_ENABLED.get() ? "threeD" : "twoD");
        return theAssets().text(keyPattern, modeKey)
            + sceneNameSuffix
            + (gameScene instanceof GameScene2D gameScene2D  ? " (%.2fx)".formatted(gameScene2D.scaling()) : "");
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
        if (theUIConfig().currentGameSceneIsPlayScene2D()) {
            var item = new MenuItem(theAssets().text("use_3D_scene"));
            item.setOnAction(ae -> GameAction.TOGGLE_PLAY_SCENE_2D_3D.execute());
            menuItems.addFirst(item);
            menuItems.addFirst(contextMenuTitleItem(theAssets().text("scene_display")));
        }
        contextMenu.getItems().setAll(menuItems);
        contextMenu.show(root, e.getScreenX(), e.getScreenY());
        contextMenu.requestFocus();
    }

    private void showGameSceneHelp() {
        if (!theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN)
            && theUIConfig().currentGameSceneIs2D()) {
            popupLayer.showHelp(canvasContainer.scaling());
        }
    }
}