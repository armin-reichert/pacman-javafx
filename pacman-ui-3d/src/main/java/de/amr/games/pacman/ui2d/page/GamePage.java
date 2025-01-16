/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameRenderer;
import de.amr.games.pacman.ui2d.action.GameAction;
import de.amr.games.pacman.ui2d.action.GameActionProvider;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui2d.lib.Ufx;
import de.amr.games.pacman.ui2d.scene.CameraControlledView;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.TooFancyGameCanvasContainer;
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

import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.GlobalProperties2d.*;
import static de.amr.games.pacman.ui2d.input.Keyboard.*;
import static de.amr.games.pacman.ui2d.lib.Ufx.*;

/**
 * @author Armin Reichert
 */
public class GamePage extends StackPane implements GameActionProvider {

    static final double MAX_SCENE_SCALING = 5;

    private final GameAction actionToggleDebugInfo = context -> toggle(PY_DEBUG_INFO_VISIBLE);

    private final GameAction actionShowHelp = context -> showHelp();

    private final GameAction actionSimulationSlower = context -> {
        double newRate = context.gameClock().getTargetFrameRate() - 5;
        if (newRate > 0) {
            context.gameClock().setTargetFrameRate(newRate);
            context.showFlashMessageSec(0.75, newRate + "Hz");
        }
    };

    private final GameAction actionSimulationFaster = context -> {
        double newRate = context.gameClock().getTargetFrameRate() + 5;
        if (newRate > 0) {
            context.gameClock().setTargetFrameRate(newRate);
            context.showFlashMessageSec(0.75, newRate + "Hz");
        }
    };

    private final GameAction actionSimulationNormalSpeed = context -> {
        context.gameClock().setTargetFrameRate(GameModel.TICKS_PER_SECOND);
        context.showFlashMessageSec(0.75, context.gameClock().getTargetFrameRate() + "Hz");
    };

    private final GameAction actionSimulationOneStep = context -> {
        if (context.gameClock().isPaused()) {
            context.gameClock().makeStep(true);
        }
    };

    private final GameAction actionSimulationTenSteps = context -> {
        if (context.gameClock().isPaused()) {
            context.gameClock().makeSteps(10, true);
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

    protected GameAction actionOpenEditor;

    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            handleGameSceneChange(get());
        }
    };

    protected final GameContext context;
    protected final Scene parentScene;
    protected final Canvas canvas = new Canvas();
    protected final TooFancyGameCanvasContainer canvasContainer;

    protected final BorderPane canvasLayer = new BorderPane();
    protected final DashboardLayer dashboardLayer; // dashboard, picture-in-picture view
    protected final PopupLayer popupLayer; // help, signature

    protected ContextMenu contextMenu;

    public GamePage(GameContext context, Scene parentScene) {
        this.context = Globals.assertNotNull(context);
        this.parentScene = Globals.assertNotNull(parentScene);

        bindGameActions();

        setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO is this the recommended way to close an open context-menu?
        setOnMouseClicked(e -> {
            if (contextMenu != null) contextMenu.hide();
        });

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFontSmoothingType(FontSmoothingType.GRAY);
        g.setImageSmoothing(false);

        canvasContainer = new TooFancyGameCanvasContainer(canvas);
        canvasContainer.setMinScaling(0.5);
        // default: Arcade games aspect ratio
        canvasContainer.setUnscaledCanvasWidth(ARCADE_MAP_SIZE_IN_PIXELS.x());
        canvasContainer.setUnscaledCanvasHeight(ARCADE_MAP_SIZE_IN_PIXELS.y());
        canvasContainer.setBorderColor(Color.valueOf(Arcade.Palette.WHITE));
        canvasContainer.decorationEnabledPy.addListener((py, ov, nv) -> embedGameScene(gameScenePy.get()));

        canvasLayer.setCenter(canvasContainer);

        dashboardLayer = new DashboardLayer(context);

        popupLayer = new PopupLayer(context, canvasContainer);
        popupLayer.setMouseTransparent(true);
        popupLayer.sign(canvasContainer,
            context.assets().font("font.monospaced", 8), Color.LIGHTGRAY,
            context.locText("app.signature"));

        getChildren().addAll(canvasLayer, dashboardLayer, popupLayer);

        PY_CANVAS_FONT_SMOOTHING.addListener((py, ov, nv) -> g.setFontSmoothingType(nv ? FontSmoothingType.LCD : FontSmoothingType.GRAY));
        PY_CANVAS_IMAGE_SMOOTHING.addListener((py, ov, nv) -> g.setImageSmoothing(nv));
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

    @Override
    public void bindGameActions() {
        bind(GameActions2D.BOOT,            KeyCode.F3);
        bind(GameActions2D.SHOW_START_PAGE, KeyCode.Q);
        bind(GameActions2D.TOGGLE_PAUSED,   KeyCode.P);
        bind(actionToggleDebugInfo,         alt(KeyCode.D));
        bind(actionShowHelp,                KeyCode.H);
        bind(actionSimulationSlower,        alt(KeyCode.MINUS));
        bind(actionSimulationFaster,        alt(KeyCode.PLUS));
        bind(actionSimulationNormalSpeed,   alt(KeyCode.DIGIT0));
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

    public TooFancyGameCanvasContainer gameCanvasContainer() {
        return canvasContainer;
    }

    public void setSize(double width, double height) {
        canvasContainer.resizeTo(width, height);
    }

    @Override
    public void handleInput(GameContext context) {
        context.ifGameActionTriggeredRunItElse(this,
            () -> context.currentGameScene().ifPresent(gameScene -> gameScene.handleInput(context)));
    }

    public void setActionOpenEditor(GameAction actionOpenEditor) {
        this.actionOpenEditor = actionOpenEditor;
        bind(actionOpenEditor, shift_alt(KeyCode.E));
    }

    private void handleContextMenuRequest(ContextMenuEvent event) {
        List<MenuItem> menuItems = new ArrayList<>(gameScenePy.get().supplyContextMenuItems(event));
        if (actionOpenEditor != null) {
            menuItems.add(new SeparatorMenuItem());
            var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
            miOpenMapEditor.setOnAction(ae -> actionOpenEditor.execute(context));
            miOpenMapEditor.setDisable(!actionOpenEditor.isEnabled(context));
            menuItems.add(miOpenMapEditor);
        }
        contextMenu = new ContextMenu(menuItems.toArray(MenuItem[]::new));
        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
        event.consume();
    }

    protected void handleGameSceneChange(GameScene gameScene) {
        if (gameScene != null) {
            embedGameScene(gameScene);
        }
        if (contextMenu != null) contextMenu.hide();
    }

    public void embedGameScene(GameScene gameScene) {
        // new switch feature
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

    public void draw() {
        context.currentGameScene()
            .filter(GameScene2D.class::isInstance)
            .map(GameScene2D.class::cast)
            .ifPresent(GameScene2D::draw);
    }

    protected boolean isCurrentGameScene2D() {
        return context.currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
    }

    public void showSignature() {
        popupLayer.showSignature(1, 2, 1);
    }

    public void hideSignature() {
        popupLayer.hideSignature();
    }

    public void toggleDashboard() {
        dashboardLayer.toggleDashboardVisibility();
    }

    public void updateDashboard() {
        dashboardLayer.update();
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
}