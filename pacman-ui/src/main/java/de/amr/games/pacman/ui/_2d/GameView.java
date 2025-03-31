/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.*;
import de.amr.games.pacman.ui.dashboard.Dashboard;
import de.amr.games.pacman.ui.dashboard.InfoBox;
import de.amr.games.pacman.uilib.FlashMessageView;
import de.amr.games.pacman.uilib.Ufx;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.*;
import static de.amr.games.pacman.uilib.Keyboard.*;
import static de.amr.games.pacman.uilib.Ufx.border;
import static de.amr.games.pacman.uilib.Ufx.coloredBackground;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GameView implements View {

    private static final double MAX_SCENE_SCALING = 5;

    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            handleGameSceneChange(get());
        }
    };

    protected final StackPane root = new StackPane();
    protected final FlashMessageView flashMessageOverlay = new FlashMessageView();
    protected final Scene parentScene;
    protected final BorderPane canvasLayer;
    protected final PopupLayer popupLayer; // help, signature
    protected final BorderPane dashboardLayer;
    protected final VBox dashboardContainer;
    protected final Dashboard dashboard;
    protected final TooFancyCanvasContainer canvasContainer;
    protected final Canvas canvas;
    protected final VBox pipContainer;
    protected final PictureInPictureView pipView;

    protected ContextMenu contextMenu;

    public GameView(PacManGamesUI ui) {
        this.parentScene = ui.mainScene();

        canvas = new Canvas();
        canvasContainer = new TooFancyCanvasContainer(canvas);
        canvasLayer = new BorderPane(canvasContainer);
        popupLayer = new PopupLayer(canvasContainer);

        dashboardLayer = new BorderPane();
        dashboardContainer = new VBox();
        dashboard = new Dashboard();

        pipView = new PictureInPictureView();
        pipContainer = new VBox(pipView, new HBox());

        root.getChildren().addAll(canvasLayer, dashboardLayer, popupLayer, flashMessageOverlay);

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

        canvasContainer.setMinScaling(0.5);
        canvasContainer.setUnscaledCanvasWidth(ARCADE_MAP_SIZE_IN_PIXELS.x());
        canvasContainer.setUnscaledCanvasHeight(ARCADE_MAP_SIZE_IN_PIXELS.y());
        canvasContainer.setBorderColor(Color.web(Arcade.Palette.WHITE));
        canvasContainer.decorationEnabledPy.addListener((py, ov, nv) -> {
            GameScene gameScene = gameScenePy.get();
            if (gameScene != null) {
                embedGameScene(gameScene); //TODO check this
            }
        });

        pipView.backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Background::fill));
        pipView.opacityProperty().bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
        pipView.visibleProperty().bind(Bindings.createObjectBinding(
                () -> PY_PIP_ON.get() && ui.configurations().currentGameSceneIsPlayScene3D(),
                PY_PIP_ON, ui.gameSceneProperty()
        ));

        dashboardLayer.visibleProperty().bind(Bindings.createObjectBinding(
                () -> dashboardContainer.isVisible() || PY_PIP_ON.get(),
                dashboardContainer.visibleProperty(), PY_PIP_ON
        ));
        dashboardLayer.setLeft(dashboardContainer);
        dashboardLayer.setRight(pipContainer);

        popupLayer.setMouseTransparent(true);
        root.setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO is this the recommended way to close an open context-menu?
        root.setOnMouseClicked(e -> { if (contextMenu != null) contextMenu.hide(); });

        bindGameActions();
    }

    @Override
    public StackPane node() {
        return root;
    }

    public ObjectProperty<GameScene> gameSceneProperty() { return gameScenePy; }

    public FlashMessageView flashMessageOverlay() {
        return flashMessageOverlay;
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

    @Override
    public void bindGameActions() {
        bind(GameActions2D.BOOT,                 KeyCode.F3);
        bind(GameActions2D.SHOW_START_PAGE,      KeyCode.Q);
        bind(GameActions2D.TOGGLE_PAUSED,        KeyCode.P);
        bind(GameActions2D.TOGGLE_DEBUG_INFO,    alt(KeyCode.D));
        bind(this::showGameSceneHelp,                     KeyCode.H);
        bind(GameActions2D.SIMULATION_SLOWER,    alt(KeyCode.MINUS));
        bind(GameActions2D.SIMULATION_FASTER,    alt(KeyCode.PLUS));
        bind(GameActions2D.SIMULATION_RESET,     alt(KeyCode.DIGIT0));
        bind(GameActions2D.SIMULATION_ONE_STEP,  shift(KeyCode.P));
        bind(GameActions2D.SIMULATION_TEN_STEPS, shift(KeyCode.SPACE));
        bind(GameActions2D.TOGGLE_AUTOPILOT,     alt(KeyCode.A));
        bind(this::toggleDashboardVisibility,    naked(KeyCode.F1), alt(KeyCode.B));
        bind(GameActions2D.TOGGLE_IMMUNITY,      alt(KeyCode.I));
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    @Override
    public void handleInput() {
        ifTriggeredRunActionElse(() -> THE_UI.currentGameScene().ifPresent(GameActionProvider::handleInput));
    }

    public TooFancyCanvasContainer canvasContainer() {
        return canvasContainer;
    }

    public void resize(double width, double height) {
        canvasContainer.resizeTo(width, height);
    }

    public void onTick() {
        flashMessageOverlay.update();
        if (dashboardLayer.isVisible()) {
            dashboard.infoBoxes().filter(InfoBox::isExpanded).forEach(InfoBox::update);
        }
        if (pipView.isVisible()) {
            pipView.draw();
        }
        THE_UI.currentGameScene()
                .filter(GameScene2D.class::isInstance)
                .map(GameScene2D.class::cast)
                .ifPresent(GameScene2D::draw);
    }

    protected void handleContextMenuRequest(ContextMenuEvent event) {
        contextMenu = new ContextMenu(createContextMenuItems(event).toArray(MenuItem[]::new));
        contextMenu.show(root, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
        event.consume();
    }

    protected List<MenuItem> createContextMenuItems(ContextMenuEvent event) {
        GameScene gameScene = gameScenePy.get();
        return new ArrayList<>(gameScene.supplyContextMenuItems(event));
    }

    protected void handleGameSceneChange(GameScene gameScene) {
        if (gameScene != null) embedGameScene(gameScene);
        if (contextMenu != null) contextMenu.hide();
    }

    public void embedGameScene(GameScene gameScene) {
        assertNotNull(gameScene);
        switch (gameScene) {
            case CameraControlledView gameSceneUsingCamera -> {
                root.getChildren().set(0, gameSceneUsingCamera.viewPort());
                gameSceneUsingCamera.viewPortWidthProperty().bind(parentScene.widthProperty());
                gameSceneUsingCamera.viewPortHeightProperty().bind(parentScene.heightProperty());
            }
            case GameScene2D gameScene2D -> {
                GameRenderer renderer = THE_UI.configurations().current().createRenderer(canvas);
                Vector2f sceneSize = gameScene2D.sizeInPx();
                canvasContainer.setUnscaledCanvasWidth(sceneSize.x());
                canvasContainer.setUnscaledCanvasHeight(sceneSize.y());
                canvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
                canvasContainer.backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
                gameScene2D.scalingProperty().bind(
                    canvasContainer.scalingPy.map(scaling -> Math.min(scaling.doubleValue(), MAX_SCENE_SCALING)));
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

    public void showGameSceneHelp() {
        if (!THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN)
            && THE_UI.configurations().currentGameSceneIs2D()) {
                popupLayer.showHelp(canvasContainer.scaling());
        }
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
        THE_UI.currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(event));
        THE_UI.updateGameScene(false);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
            Logger.info("Game level {} ({}) created", level.number(), THE_GAME_CONTROLLER.selectedGameVariant());
            THE_UI.configurations().current().createActorAnimations(level);
            Logger.info("Actor animations ({}) created", THE_GAME_CONTROLLER.selectedGameVariant());
            THE_UI.sound().setEnabled(!THE_GAME_CONTROLLER.game().isDemoLevel());
            Logger.info("Sounds ({}) {}", THE_GAME_CONTROLLER.selectedGameVariant(), THE_UI.sound().isEnabled() ? "enabled" : "disabled");
            // size of game scene might have changed, so re-embed
            THE_UI.currentGameScene().ifPresent(this::embedGameScene);
            GameScene2D pipGameScene = THE_UI.configurations().current().createPiPScene(canvasContainer().canvas());
            pipView.setScene2D(pipGameScene);
        });
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        THE_UI.sound().stopAll();
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO this is only used by game state GameState.TESTING_CUT_SCENES
        THE_UI.updateGameScene(true);
    }
}