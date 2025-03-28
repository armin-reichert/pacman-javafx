/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.*;
import static de.amr.games.pacman.uilib.Keyboard.*;
import static de.amr.games.pacman.uilib.Ufx.*;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GameView extends StackPane implements View, GameEventListener {

    private static final double MAX_SCENE_SCALING = 5;

    private static final int SIMULATION_SPEED_DELTA = 2;
    private static final int SIMULATION_SPEED_MIN   = 10;
    private static final int SIMULATION_SPEED_MAX   = 240;

    private final GameAction actionShowHelp = this::showHelp;

    private final GameAction actionSimulationSpeedSlower = () -> {
        double newRate = THE_UI.clock().getTargetFrameRate() - SIMULATION_SPEED_DELTA;
        newRate = clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
        THE_UI.clock().setTargetFrameRate(newRate);
        String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
        THE_UI.showFlashMessageSec(0.75, prefix + newRate + "Hz");
    };

    private final GameAction actionSimulationSpeedFaster = () -> {
        double newRate = THE_UI.clock().getTargetFrameRate() + SIMULATION_SPEED_DELTA;
        newRate = clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
        THE_UI.clock().setTargetFrameRate(newRate);
        String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
        THE_UI.showFlashMessageSec(0.75, prefix + newRate + "Hz");
    };

    private final GameAction actionSimulationSpeedReset = () -> {
        THE_UI.clock().setTargetFrameRate(TICKS_PER_SECOND);
        THE_UI.showFlashMessageSec(0.75, THE_UI.clock().getTargetFrameRate() + "Hz");
    };

    private final GameAction actionSimulationOneStep = new GameAction() {
        @Override
        public void execute() {
            THE_UI.clock().makeOneStep(true);
        }

        @Override
        public boolean isEnabled() {
            return THE_UI.clock().isPaused();
        }
    };

    private final GameAction actionSimulationTenSteps = new GameAction() {
        @Override
        public void execute() {
            THE_UI.clock().makeSteps(10, true);
        }

        @Override
        public boolean isEnabled() {
            return THE_UI.clock().isPaused();
        }
    };

    private final GameAction actionToggleAutopilot = () -> {
        toggle(PY_AUTOPILOT);
        boolean auto = PY_AUTOPILOT.get();
        THE_UI.showFlashMessage(THE_UI.assets().text(auto ? "autopilot_on" : "autopilot_off"));
        THE_UI.sound().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
    };

    private final GameAction actionToggleDashboard = this::toggleDashboardVisibility;

    private final GameAction actionToggleDebugInfo = () -> toggle(PY_DEBUG_INFO_VISIBLE);

    private final GameAction actionToggleImmunity = () -> {
        toggle(GlobalProperties2d.PY_IMMUNITY);
        THE_UI.showFlashMessage(THE_UI.assets().text(GlobalProperties2d.PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
        THE_UI.sound().playVoice(GlobalProperties2d.PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
    };

    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            handleGameSceneChange(get());
        }
    };

    protected final Scene parentScene;

    // Common canvas for rendering 2D scenes
    protected final Canvas gameScenesCanvas = new Canvas();

    protected BorderPane canvasLayer;
    protected PopupLayer popupLayer; // help, signature
    protected final BorderPane dashboardLayer = new BorderPane();
    protected final FlashMessageView flashMessageOverlay = new FlashMessageView();

    protected TooFancyCanvasContainer canvasContainer;
    protected ContextMenu contextMenu;

    protected VBox dashboardContainer;
    protected Dashboard dashboard;
    protected final VBox pipContainer = new VBox();
    protected PictureInPictureView pipView;

    public GameView(Scene parentScene) {
        this.parentScene = assertNotNull(parentScene);

        GraphicsContext g = gameScenesCanvas.getGraphicsContext2D();
        PY_CANVAS_FONT_SMOOTHING.addListener((py, ov, on) -> g.setFontSmoothingType(on ? FontSmoothingType.LCD : FontSmoothingType.GRAY));
        PY_CANVAS_IMAGE_SMOOTHING.addListener((py, ov, on) -> g.setImageSmoothing(on));

        createCanvasLayer();
        createDashboardLayer();

        popupLayer = new PopupLayer(canvasContainer);
        popupLayer.setMouseTransparent(true);

        getChildren().addAll(canvasLayer, dashboardLayer, popupLayer, flashMessageOverlay);

        setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO is this the recommended way to close an open context-menu?
        setOnMouseClicked(e -> { if (contextMenu != null) contextMenu.hide(); });
        bindGameActions();
    }

    @Override
    public Node node() {
        return this;
    }

    public ObjectProperty<GameScene> gameSceneProperty() { return gameScenePy; }

    public FlashMessageView flashMessageOverlay() {
        return flashMessageOverlay;
    }

    private void createCanvasLayer() {
        canvasContainer = new TooFancyCanvasContainer(gameScenesCanvas);
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

        canvasLayer = new BorderPane(canvasContainer);

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

    //
    // Dashboard
    //

    private void createDashboardLayer() {
        dashboard = new Dashboard();
        dashboardContainer = new VBox();

        pipView = new PictureInPictureView();
        pipContainer.getChildren().setAll(pipView, new HBox());

        dashboardLayer.setLeft(dashboardContainer);
        dashboardLayer.setRight(pipContainer);

        dashboardLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboardContainer.isVisible() || PY_PIP_ON.get(),
            dashboardContainer.visibleProperty(), PY_PIP_ON
        ));
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
        bind(GameActions2D.BOOT,            KeyCode.F3);
        bind(GameActions2D.SHOW_START_PAGE, KeyCode.Q);
        bind(GameActions2D.TOGGLE_PAUSED,   KeyCode.P);
        bind(actionToggleDebugInfo,         alt(KeyCode.D));
        bind(actionShowHelp,                KeyCode.H);
        bind(actionSimulationSpeedSlower,   alt(KeyCode.MINUS));
        bind(actionSimulationSpeedFaster,   alt(KeyCode.PLUS));
        bind(actionSimulationSpeedReset,    alt(KeyCode.DIGIT0));
        bind(actionSimulationOneStep,       shift(KeyCode.P));
        bind(actionSimulationTenSteps,      shift(KeyCode.SPACE));
        bind(actionToggleAutopilot,         alt(KeyCode.A));
        bind(actionToggleDashboard,         naked(KeyCode.F1), alt(KeyCode.B));
        bind(actionToggleImmunity,          alt(KeyCode.I));
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    public TooFancyCanvasContainer canvasContainer() {
        return canvasContainer;
    }

    public void setSize(double width, double height) {
        canvasContainer.resizeTo(width, height);
        Logger.debug("Game page size set to w={} h={}", canvasContainer.getWidth(), canvasContainer.getHeight());
    }

    @Override
    public void handleInput() {
        ifTriggeredRunActionElse(() -> THE_UI.currentGameScene().ifPresent(GameActionProvider::handleInput));
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
        contextMenu.show(this, event.getScreenX(), event.getScreenY());
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
            case CameraControlledView cameraControlledView -> {
                getChildren().set(0, cameraControlledView.viewPort());
                cameraControlledView.viewPortWidthProperty().bind(parentScene.widthProperty());
                cameraControlledView.viewPortHeightProperty().bind(parentScene.heightProperty());
            }
            case GameScene2D gameScene2D -> {
                GameRenderer renderer = THE_UI.configurations().current().createRenderer(gameScenesCanvas);
                Vector2f sceneSize = gameScene2D.sizeInPx();
                canvasContainer.setUnscaledCanvasWidth(sceneSize.x());
                canvasContainer.setUnscaledCanvasHeight(sceneSize.y());
                canvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
                canvasContainer.backgroundProperty().bind(GlobalProperties2d.PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
                gameScene2D.scalingProperty().bind(
                    canvasContainer.scalingPy.map(scaling -> Math.min(scaling.doubleValue(), MAX_SCENE_SCALING)));
                gameScene2D.setCanvas(gameScenesCanvas);
                gameScene2D.backgroundColorProperty().bind(GlobalProperties2d.PY_CANVAS_BG_COLOR);
                gameScene2D.setGameRenderer(renderer);
                getChildren().set(0, canvasLayer);
            }
            default -> Logger.error("Cannot embed game scene of class {}", gameScene.getClass().getName());
        }
    }

    protected boolean isCurrentGameScene2D() {
        return THE_UI.currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
    }

    public void showHelp() {
        if (THE_GAME_CONTROLLER.selectedGameVariant() != GameVariant.MS_PACMAN_TENGEN) {
            if (isCurrentGameScene2D()) {
                popupLayer.showHelp(canvasContainer.scaling());
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent event) {
        Logger.trace("{} received game event {}", getClass().getSimpleName(), event);
        // dispatch event to event specific method:
        GameEventListener.super.onGameEvent(event);
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
    public void onGameVariantChanged(GameEvent event) {
        // TODO check if there is a cleaner solution
        THE_UI.onGameVariantChange(THE_GAME_CONTROLLER.selectedGameVariant());
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