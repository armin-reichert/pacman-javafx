/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledGameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.util.TooFancyGameCanvasContainer;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.input.Keyboard.*;
import static de.amr.games.pacman.ui2d.util.Ufx.*;

/**
 * @author Armin Reichert
 */
public class GamePage extends StackPane implements Page {

    static final double MAX_SCENE_SCALING = 5;

    private final GameAction actionToggleDebugInfo = context -> Ufx.toggle(PY_DEBUG_INFO_VISIBLE);

    private final GameAction actionShowHelp = context -> context.gamePage().showHelp();

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

    private final GameAction actionToggleDashboard = context -> context.gamePage().toggleDashboard();

    private final GameAction actionToggleImmunity = context -> {
        toggle(PY_IMMUNITY);
        context.showFlashMessage(context.locText(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
        context.sound().playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
    };

    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            handleGameSceneChange(get());
        }
    };

    protected final GameContext context;
    protected final Scene parentScene;
    protected final Canvas gameCanvas;
    protected final BorderPane gameCanvasLayer = new BorderPane();
    protected final TooFancyGameCanvasContainer gameCanvasContainer;
    protected final DashboardLayer dashboardLayer; // dashboard, picture-in-picture view
    protected final PopupLayer popupLayer; // help, signature
    protected final ContextMenu contextMenu = new ContextMenu();

    public GamePage(GameContext context, Scene parentScene) {
        this.context = checkNotNull(context);
        this.parentScene = checkNotNull(parentScene);

        bindGameActions();

        gameCanvas = new Canvas();
        GraphicsContext g = gameCanvas.getGraphicsContext2D();
        g.setFontSmoothingType(FontSmoothingType.GRAY);
        g.setImageSmoothing(false);
        PY_CANVAS_FONT_SMOOTHING.addListener((py, ov, nv) -> g.setFontSmoothingType(nv ? FontSmoothingType.LCD : FontSmoothingType.GRAY));
        PY_CANVAS_IMAGE_SMOOTHING.addListener((py, ov, nv) -> g.setImageSmoothing(nv));

        gameCanvasContainer = new TooFancyGameCanvasContainer(gameCanvas);
        gameCanvasContainer.setMinScaling(0.5);

        // default: Arcade aspect
        gameCanvasContainer.setUnscaledCanvasWidth(ARCADE_MAP_SIZE_IN_PIXELS.x());
        gameCanvasContainer.setUnscaledCanvasHeight(ARCADE_MAP_SIZE_IN_PIXELS.y());

        gameCanvasContainer.setBorderColor(Color.valueOf(Arcade.Palette.WHITE));
        gameCanvasContainer.decorationEnabledPy.addListener((py, ov, nv) -> embedGameScene(gameScenePy.get()));

        gameCanvasLayer.setCenter(gameCanvasContainer);

        dashboardLayer = new DashboardLayer(context);
        InfoBox readMeBox = new InfoBoxReadmeFirst();
        readMeBox.setExpanded(true);
        dashboardLayer.addDashboardItem("Welcome to the Pleasuredome!", readMeBox);
        dashboardLayer.addDashboardItem(context.locText("infobox.general.title"), new InfoBoxGeneral());
        dashboardLayer.addDashboardItem(context.locText("infobox.game_control.title"), new InfoBoxGameControl());
        dashboardLayer.addDashboardItem(context.locText("infobox.game_info.title"), new InfoBoxGameInfo());
        dashboardLayer.addDashboardItem(context.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        dashboardLayer.addDashboardItem(context.locText("infobox.actor_info.title"), new InfoBoxActorInfo());
        dashboardLayer.addDashboardItem(context.locText("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        dashboardLayer.addDashboardItem(
            /*context.locText("infobox.keyboard_shortcuts_tengen.title"*/
            "Joypad Settings", new InfoBoxJoypad());
        dashboardLayer.addDashboardItem(context.locText("infobox.about.title"), new InfoBoxAbout());

        popupLayer = new PopupLayer(context, gameCanvasContainer);
        popupLayer.setMouseTransparent(true);
        popupLayer.sign(gameCanvasContainer,
            context.assets().font("font.monospaced", 8), Color.LIGHTGRAY,
            context.locText("app.signature"));

        getChildren().addAll(gameCanvasLayer, dashboardLayer, popupLayer);

        //TODO is this the recommended way to close an open context-menu?
        setOnMouseClicked(e -> contextMenu.hide());

        PY_DEBUG_INFO_VISIBLE.addListener((py, ov, debug) -> {
            if (debug) {
                gameCanvasLayer.setBackground(coloredBackground(Color.DARKGREEN));
                gameCanvasLayer.setBorder(border(Color.LIGHTGREEN, 2));
            } else {
                gameCanvasLayer.setBackground(null);
                gameCanvasLayer.setBorder(null);
            }
        });
    }

    @Override
    public void bindGameActions() {
        bind(GameActions2D.BOOT,            KeyCode.F3);
        bind(GameActions2D.SHOW_START_PAGE, KeyCode.Q);
        bind(GameActions2D.TOGGLE_PAUSED,   KeyCode.P);
        bind(GameActions2D.OPEN_EDITOR,     shift_alt(KeyCode.E));
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
        return gameCanvasContainer;
    }

    @Override
    public Pane rootPane() {
        return this;
    }

    @Override
    public void onPageSelected() {
        //TODO check if booting is always wanted here
        GameActions2D.BOOT.execute(context);
    }

    @Override
    public void setSize(double width, double height) {
        gameCanvasContainer.resizeTo(width, height);
    }

    @Override
    public void handleInput(GameContext context) {
        context.ifGameActionRunElse(this,
            () -> context.currentGameScene().ifPresent(gameScene -> gameScene.handleInput(context)));
    }

    @Override
    public void handleContextMenuRequest(ContextMenuEvent event) {
        if (!context.currentGameSceneHasID("PlayScene2D")) {
            return;
        }
        contextMenu.getItems().clear();

        contextMenu.getItems().add(Page.menuTitleItem(context.locText("scene_display")));

        contextMenu.getItems().add(Page.menuTitleItem(context.locText("pacman")));

        var miAutopilot = new CheckMenuItem(context.locText("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        contextMenu.getItems().add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.locText("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        contextMenu.getItems().add(miImmunity);

        contextMenu.getItems().add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(context.locText("muted"));
        miMuted.selectedProperty().bindBidirectional(context.sound().mutedProperty());
        contextMenu.getItems().add(miMuted);

        if (context.currentGameVariant() == GameVariant.PACMAN_XXL || context.currentGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
            miOpenMapEditor.setOnAction(e -> GameActions2D.OPEN_EDITOR.execute(context));
            contextMenu.getItems().add(miOpenMapEditor);
        }

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(e -> GameActions2D.SHOW_START_PAGE.execute(context));
        contextMenu.getItems().add(miQuit);

        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
    }

    protected void handleGameSceneChange(GameScene gameScene) {
        if (gameScene != null) {
            embedGameScene(gameScene);
        }
        contextMenu.hide();
    }

    public void embedGameScene(GameScene gameScene) {
        // new switch feature
        switch (gameScene) {
            case null -> Logger.error("No game scene to embed");
            case CameraControlledGameScene cameraControlledGameScene -> {
                getChildren().set(0, cameraControlledGameScene.viewPort());
                cameraControlledGameScene.viewPortWidthProperty().bind(parentScene.widthProperty());
                cameraControlledGameScene.viewPortHeightProperty().bind(parentScene.heightProperty());
                if (gameScene instanceof GameScene2D gameScene2D) {
                    gameScene2D.scalingProperty().bind(
                        gameCanvasContainer.scalingPy.map(scaling -> Math.min(scaling.doubleValue(), MAX_SCENE_SCALING)));
                }
            }
            case GameScene2D gameScene2D -> {
                getChildren().set(0, gameCanvasLayer);
                gameCanvasContainer.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(Ufx::coloredBackground));
                Vector2f sceneSize = gameScene.size();
                gameCanvasContainer.setUnscaledCanvasWidth(sceneSize.x());
                gameCanvasContainer.setUnscaledCanvasHeight(sceneSize.y());
                gameCanvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
                gameScene2D.setCanvas(gameCanvas);
                gameScene2D.scalingProperty().bind(
                    gameCanvasContainer.scalingPy.map(scaling -> Math.min(scaling.doubleValue(), MAX_SCENE_SCALING)));
            }
            default -> Logger.error("Cannot embed game scene of class {}", gameScene.getClass().getName());
        }
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
        if (context.currentGameVariant() != GameVariant.MS_PACMAN_TENGEN) {
            if (isCurrentGameScene2D()) {
                popupLayer.showHelp(gameCanvasContainer.scaling());
            }
        }
    }
}