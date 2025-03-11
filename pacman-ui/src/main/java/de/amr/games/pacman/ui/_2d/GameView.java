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
import de.amr.games.pacman.ui.dashboard.*;
import de.amr.games.pacman.uilib.Ufx;
import javafx.beans.binding.Bindings;
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

import static de.amr.games.pacman.controller.GameController.TICKS_PER_SECOND;
import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static de.amr.games.pacman.lib.Globals.clamp;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.*;
import static de.amr.games.pacman.ui.input.Keyboard.*;
import static de.amr.games.pacman.uilib.Ufx.*;

/**
 * This view shows the game play and the overlays like dashboard and picture-in-picture view of the running play scene.
 */
public class GameView extends StackPane implements GameActionProvider, GameEventListener {

    private static final double MAX_SCENE_SCALING = 5;

    private static final int SIMULATION_SPEED_DELTA = 2;
    private static final int SIMULATION_SPEED_MIN   = 10;
    private static final int SIMULATION_SPEED_MAX   = 240;

    private final GameAction actionShowHelp = context -> showHelp();

    private final GameAction actionSimulationSpeedSlower = context -> {
        double newRate = context.gameClock().getTargetFrameRate() - SIMULATION_SPEED_DELTA;
        newRate = clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
        context.gameClock().setTargetFrameRate(newRate);
        String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
        context.showFlashMessageSec(0.75, prefix + newRate + "Hz");
    };

    private final GameAction actionSimulationSpeedFaster = context -> {
        double newRate = context.gameClock().getTargetFrameRate() + SIMULATION_SPEED_DELTA;
        newRate = clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
        context.gameClock().setTargetFrameRate(newRate);
        String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
        context.showFlashMessageSec(0.75, prefix + newRate + "Hz");
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

    private final GameAction actionToggleDashboard = context -> toggleDashboardVisibility();

    private final GameAction actionToggleDebugInfo = context -> toggle(PY_DEBUG_INFO_VISIBLE);

    private final GameAction actionToggleImmunity = context -> {
        toggle(GlobalProperties2d.PY_IMMUNITY);
        context.showFlashMessage(context.locText(GlobalProperties2d.PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
        context.sound().playVoice(GlobalProperties2d.PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
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
    protected PopupLayer popupLayer; // help, signature
    protected final BorderPane dashboardLayer = new BorderPane();

    protected TooFancyCanvasContainer canvasContainer;
    protected ContextMenu contextMenu;

    private VBox dashboardContainer;
    private Dashboard dashboard;
    private final VBox pipContainer = new VBox();
    private PictureInPictureView pipView;

    public GameView(GameContext context, Scene parentScene) {
        this.context = assertNotNull(context);
        this.parentScene = assertNotNull(parentScene);

        createCanvasLayer();
        createDashboardLayer();

        popupLayer = new PopupLayer(context, canvasContainer);
        popupLayer.setMouseTransparent(true);

        getChildren().addAll(canvasLayer, dashboardLayer, popupLayer);

        setOnContextMenuRequested(this::handleContextMenuRequest);
        //TODO is this the recommended way to close an open context-menu?
        setOnMouseClicked(e -> { if (contextMenu != null) contextMenu.hide(); });
        bindGameActions();
    }

    public ObjectProperty<GameScene> gameSceneProperty() { return gameScenePy; }

    private void createCanvasLayer() {
        canvasContainer = new TooFancyCanvasContainer(canvas);
        canvasContainer.setMinScaling(0.5);
        canvasContainer.setUnscaledCanvasWidth(ARCADE_MAP_SIZE_IN_PIXELS.x());
        canvasContainer.setUnscaledCanvasHeight(ARCADE_MAP_SIZE_IN_PIXELS.y());
        canvasContainer.setBorderColor(Color.valueOf(Arcade.Palette.WHITE));
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

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFontSmoothingType(FontSmoothingType.GRAY);
        g.setImageSmoothing(false);
        PY_CANVAS_FONT_SMOOTHING.addListener((py, ov, smooth) -> g.setFontSmoothingType(
                smooth ? FontSmoothingType.LCD : FontSmoothingType.GRAY));
        PY_CANVAS_IMAGE_SMOOTHING.addListener((py, ov, smooth) -> g.setImageSmoothing(smooth));
    }

    //
    // Dashboard
    //

    public void addDashboardItems(String... ids) {
        for (var id : ids) addDashboardItem(id);
    }

    public void addDashboardItem(String id) {
        switch (id) {
            case "ABOUT"        -> addDashboardItem(id, context.locText("infobox.about.title"),              new InfoBoxAbout());
            case "ACTOR_INFO"   -> addDashboardItem(id, context.locText("infobox.actor_info.title"),         new InfoBoxActorInfo());
            case "CUSTOM_MAPS"  -> addDashboardItem(id, context.locText("infobox.custom_maps.title"),        new InfoBoxCustomMaps());
            case "GENERAL"      -> addDashboardItem(id, context.locText("infobox.general.title"),            new InfoBoxGeneral());
            case "GAME_CONTROL" -> addDashboardItem(id, context.locText("infobox.game_control.title"),       new InfoBoxGameControl());
            case "GAME_INFO"    -> addDashboardItem(id, context.locText("infobox.game_info.title"),          new InfoBoxGameInfo());
            case "JOYPAD"       -> addDashboardItem(id, context.locText("infobox.joypad.title"),             new InfoBoxJoypad());
            case "KEYBOARD"     -> addDashboardItem(id, context.locText("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
            case "README" -> {
                InfoBox readMeBox = new InfoBoxReadmeFirst();
                readMeBox.setExpanded(true);
                addDashboardItem(id, context.locText("infobox.readme.title"), readMeBox);
            }
        }
    }

    public void addDashboardItem(String id, String title, InfoBox infoBox) {
        dashboard.addDashboardItem(id, title, infoBox);
    }

    private void createDashboardLayer() {
        dashboard = new Dashboard(context);
        dashboardContainer = new VBox();

        pipView = new PictureInPictureView(context);
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

    public boolean isDashboardOpen() { return dashboardContainer.isVisible(); }

    public void hideDashboard() {
        dashboardContainer.setVisible(false);
    }

    public void showDashboard() {
        dashboardContainer.getChildren().setAll(dashboard.entries()
            .map(Dashboard.DashboardEntry::infoBox).toArray(InfoBox[]::new));
        dashboardContainer.setVisible(true);
    }

    public void toggleDashboardVisibility() {
        if (dashboardContainer.isVisible()) {
            hideDashboard();
        } else {
            showDashboard();
        }
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
        context.ifTriggeredRunActionElse(this,
            () -> context.currentGameScene().ifPresent(gameScene -> gameScene.handleInput(context)));
    }

    public void onTick() {
        if (dashboardLayer.isVisible()) {
            dashboard.entries().map(Dashboard.DashboardEntry::infoBox).filter(InfoBox::isExpanded).forEach(InfoBox::update);
        }
        if (pipView.isVisible()) {
            pipView.draw();
        }
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

    protected void handleContextMenuRequest(ContextMenuEvent event) {
        contextMenu = new ContextMenu(createContextMenuItems(event).toArray(MenuItem[]::new));
        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
        event.consume();
    }

    protected List<MenuItem> createContextMenuItems(ContextMenuEvent event) {
        GameScene gameScene = gameScenePy.get();
        var menuItems = new ArrayList<>(gameScene.supplyContextMenuItems(event));
        if (actionToOpenEditor != null
                && (context.currentGameSceneHasID("PlayScene2D") || context.currentGameSceneHasID("PlayScene3D"))
                && (context.gameVariant() == GameVariant.MS_PACMAN_XXL || context.gameVariant() == GameVariant.PACMAN_XXL)) {
            menuItems.add(new SeparatorMenuItem());
            var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
            miOpenMapEditor.setOnAction(ae -> actionToOpenEditor.execute(context));
            miOpenMapEditor.setDisable(!actionToOpenEditor.isEnabled(context));
            menuItems.add(miOpenMapEditor);
        }
        return menuItems;
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
                GameRenderer renderer = context.gameConfiguration().createRenderer(context.assets(), canvas);
                Vector2f sceneSize = gameScene2D.size();
                canvasContainer.setUnscaledCanvasWidth(sceneSize.x());
                canvasContainer.setUnscaledCanvasHeight(sceneSize.y());
                canvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
                canvasContainer.backgroundProperty().bind(GlobalProperties2d.PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
                gameScene2D.scalingProperty().bind(
                    canvasContainer.scalingPy.map(scaling -> Math.min(scaling.doubleValue(), MAX_SCENE_SCALING)));
                gameScene2D.setCanvas(canvas);
                gameScene2D.backgroundColorProperty().bind(GlobalProperties2d.PY_CANVAS_BG_COLOR);
                gameScene2D.setGameRenderer(renderer);
                getChildren().set(0, canvasLayer);
            }
            default -> Logger.error("Cannot embed game scene of class {}", gameScene.getClass().getName());
        }
    }

    protected boolean isCurrentGameScene2D() {
        return context.currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
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
        pipView.setScene2D(pipGameScene);

        Logger.info("Game level {} ({}) created", context.level().number, context.gameVariant());
        Logger.info("Actor animations created");
        Logger.info("Sounds {}", context.sound().isEnabled() ? "enabled" : "disabled");
    }
}