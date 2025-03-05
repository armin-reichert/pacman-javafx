/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.action.GameAction;
import de.amr.games.pacman.ui2d.action.GameActionProvider;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.CameraControlledView;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.TooFancyCanvasContainer;
import de.amr.games.pacman.uilib.Ufx;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.controller.GameController.TICKS_PER_SECOND;
import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.GlobalProperties2d.*;
import static de.amr.games.pacman.ui2d.input.Keyboard.*;
import static de.amr.games.pacman.uilib.Ufx.*;

/**
 * @author Armin Reichert
 */
public class GameView extends StackPane implements GameActionProvider, GameEventListener {

    private static final double MAX_SCENE_SCALING = 5;
    private static final int SIMULATION_SPEED_DELTA = 5;

    private final GameAction actionToggleDebugInfo = context -> toggle(PY_DEBUG_INFO_VISIBLE);

    private final GameAction actionShowHelp = context -> showHelp();

    private final GameAction actionSimulationSpeedSlower = context -> {
        double newRate = context.gameClock().getTargetFrameRate() - SIMULATION_SPEED_DELTA;
        if (newRate > 0) {
            context.gameClock().setTargetFrameRate(newRate);
            context.showFlashMessageSec(0.75, newRate + "Hz");
        }
    };

    private final GameAction actionSimulationSpeedFaster = context -> {
        double newRate = context.gameClock().getTargetFrameRate() + SIMULATION_SPEED_DELTA;
        if (newRate > 0) {
            context.gameClock().setTargetFrameRate(newRate);
            context.showFlashMessageSec(0.75, newRate + "Hz");
        }
    };

    private final GameAction actionSimulationSpeedReset = context -> {
        context.gameClock().setTargetFrameRate(TICKS_PER_SECOND);
        context.showFlashMessageSec(0.75, context.gameClock().getTargetFrameRate() + "Hz");
    };

    private final GameAction actionSimulationOneStep = new GameAction() {
        @Override
        public void execute(GameContext context) {
            context.gameClock().makeStep(true);
        }

        @Override
        public boolean isEnabled(GameContext context) {
            return context.gameClock().isPaused();
        }
    };

    private final GameAction actionSimulationTenSteps = new GameAction() {
        @Override
        public void execute(GameContext context) {
            context.gameClock().makeSteps(10, true);
        }

        @Override
        public boolean isEnabled(GameContext context) {
            return context.gameClock().isPaused();
        }
    };

    private final GameAction actionToggleAutopilot = context -> {
        toggle(PY_AUTOPILOT);
        boolean auto = PY_AUTOPILOT.get();
        context.showFlashMessage(context.locText(auto ? "autopilot_on" : "autopilot_off"));
        context.sound().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
    };

    private final GameAction actionToggleDashboard = context -> toggleDashboard();

    private final GameAction actionToggleImmunity = context -> {
        toggle(PY_IMMUNITY);
        context.showFlashMessage(context.locText(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
        context.sound().playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
    };

    protected GameAction actionToOpenEditor;

    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            handleGameSceneChange(get());
        }
    };

    protected final GameContext context;
    protected final Scene parentScene;
    protected final Canvas canvas = new Canvas();

    protected BorderPane canvasLayer;
    protected DashboardLayer dashboardLayer; // dashboard, picture-in-picture view
    protected PopupLayer popupLayer; // help, signature

    protected TooFancyCanvasContainer canvasContainer;
    protected ContextMenu contextMenu;

    public GameView(GameContext context, Scene parentScene) {
        this.context = assertNotNull(context);
        this.parentScene = assertNotNull(parentScene);

        bindGameActions();

        setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO is this the recommended way to close an open context-menu?
        setOnMouseClicked(e -> {
            if (contextMenu != null) contextMenu.hide();
        });

        createCanvasLayer();
        createDashboardLayer();
        createPopupLayer();
        getChildren().addAll(canvasLayer, dashboardLayer, popupLayer);

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFontSmoothingType(FontSmoothingType.GRAY);
        g.setImageSmoothing(false);
        PY_CANVAS_FONT_SMOOTHING.addListener(
            (py, ov, smooth) -> g.setFontSmoothingType(smooth ? FontSmoothingType.LCD : FontSmoothingType.GRAY
        ));
        PY_CANVAS_IMAGE_SMOOTHING.addListener(
            (py, ov, smooth) -> g.setImageSmoothing(smooth)
        );
    }

    public ObjectProperty<GameScene> gameSceneProperty() { return gameScenePy; }

    private void createCanvasLayer() {
        canvasContainer = new TooFancyCanvasContainer(canvas);
        canvasContainer.setMinScaling(0.5);
        canvasContainer.setUnscaledCanvasWidth(ARCADE_MAP_SIZE_IN_PIXELS.x());
        canvasContainer.setUnscaledCanvasHeight(ARCADE_MAP_SIZE_IN_PIXELS.y());
        canvasContainer.setBorderColor(Color.valueOf(Arcade.Palette.WHITE));
        canvasContainer.decorationEnabledPy.addListener((py, ov, nv) -> embedGameScene(gameScenePy.get()));

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

    private void createDashboardLayer() {
        dashboardLayer = new DashboardLayer(context);
    }

    private void createPopupLayer() {
        popupLayer = new PopupLayer(context, canvasContainer);
        popupLayer.setMouseTransparent(true);
        popupLayer.sign(canvasContainer,
            context.assets().font("font.monospaced", 8), Color.LIGHTGRAY,
            context.locText("app.signature"));
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
    public void handleInput(GameContext context) {
        context.runTriggeredActionOrElse(this,
            () -> context.currentGameScene().ifPresent(gameScene -> gameScene.handleInput(context)));
    }

    public void onTick() {
        dashboardLayer.update();
        context.currentGameScene()
                .filter(GameScene2D.class::isInstance)
                .map(GameScene2D.class::cast)
                .ifPresent(GameScene2D::draw);
    }

    public void setActionToOpenEditor(GameAction action) {
        this.actionToOpenEditor = action;
        if (action != null) {
            bind(action, shift_alt(KeyCode.E));
        }
        //TODO unbind else?
    }

    private void handleContextMenuRequest(ContextMenuEvent event) {
        List<MenuItem> gameSceneItems = gameScenePy.get().supplyContextMenuItems(event);
        var menuItems = new ArrayList<>(gameSceneItems);
        if (actionToOpenEditor != null) {
            menuItems.add(new SeparatorMenuItem());
            var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
            miOpenMapEditor.setOnAction(ae -> actionToOpenEditor.execute(context));
            miOpenMapEditor.setDisable(!actionToOpenEditor.isEnabled(context));
            menuItems.add(miOpenMapEditor);
        }
        contextMenu = new ContextMenu(menuItems.toArray(MenuItem[]::new));
        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
        event.consume();
    }

    protected void handleGameSceneChange(GameScene gameScene) {
        if (gameScene != null) embedGameScene(gameScene);
        if (contextMenu != null) contextMenu.hide();
    }

    public void embedGameScene(GameScene gameScene) {
        // new switch!
        switch (gameScene) {
            case null -> Logger.error("No game scene to embed");
            case CameraControlledView cameraControlledView -> {
                getChildren().set(0, cameraControlledView.viewPort());
                cameraControlledView.viewPortWidthProperty().bind(parentScene.widthProperty());
                cameraControlledView.viewPortHeightProperty().bind(parentScene.heightProperty());
            }
            case GameScene2D gameScene2D -> {
                Vector2f sceneSize = gameScene2D.size();
                canvasContainer.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(Ufx::coloredBackground));
                canvasContainer.setUnscaledCanvasWidth(sceneSize.x());
                canvasContainer.setUnscaledCanvasHeight(sceneSize.y());
                canvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
                gameScene2D.scalingProperty().bind(
                    canvasContainer.scalingPy.map(scaling -> Math.min(scaling.doubleValue(), MAX_SCENE_SCALING)));
                GameRenderer renderer = context.gameConfiguration().createRenderer(context.assets(), canvas);
                gameScene2D.setCanvas(canvas);
                gameScene2D.setGameRenderer(renderer);
                getChildren().set(0, canvasLayer);
            }
            default -> Logger.error("Cannot embed game scene of class {}", gameScene.getClass().getName());
        }
    }

    protected boolean isCurrentGameScene2D() {
        return context.currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
    }

    public void toggleDashboard() {
        dashboardLayer.toggleDashboardVisibility();
    }

    public DashboardLayer dashboardLayer() {
        return dashboardLayer;
    }

    public void showHelp() {
        if (context.gameVariant() != GameVariant.MS_PACMAN_TENGEN) {
            if (isCurrentGameScene2D()) {
                popupLayer.showHelp(canvasContainer.scaling());
            }
        }
    }

    @Override
    public void onGameEvent(GameEvent event) {
        Logger.trace("{} received game event {}", getClass().getSimpleName(), event);
        // dispatch event to overridden methods:
        GameEventListener.super.onGameEvent(event);
        // dispatch event to current game scene if any
        context.currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(event));
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        context.gameConfiguration().createActorAnimations(context.level());
        context.sound().setEnabled(!context.game().isDemoLevel());
        // size of game scene might have changed, so re-embed
        context.currentGameScene().ifPresent(this::embedGameScene);

        GameScene2D pipGameScene = context.gameConfiguration().createPiPScene(context, canvasContainer().canvas());
        dashboardLayer().pipView().setScene2D(pipGameScene);

        Logger.info("Game level {} ({}) created", context.level().number, context.gameVariant());
        Logger.info("Actor animations created");
        Logger.info("Sounds {}", context.sound().isEnabled() ? "enabled" : "disabled");
    }
}