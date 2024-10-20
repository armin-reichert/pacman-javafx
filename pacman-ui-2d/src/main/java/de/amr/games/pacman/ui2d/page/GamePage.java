/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.pacman.PacManArcadeGame;
import de.amr.games.pacman.ui2d.AbstractGameAction;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GlobalGameActions2D;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledGameScene;
import de.amr.games.pacman.ui2d.util.TooFancyGameCanvasContainer;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameAssets2D.ARCADE_PALE;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.util.KeyInput.*;
import static de.amr.games.pacman.ui2d.util.Ufx.*;

/**
 * @author Armin Reichert
 */
public class GamePage extends StackPane implements Page {

    static final double MAX_SCENE_SCALING = 5;

    private final GameAction actionToggleDebugInfo = new AbstractGameAction(alt(KeyCode.D)) {
        public void execute(GameContext context) {
            Ufx.toggle(PY_DEBUG_INFO);
        }
    };

    private final GameAction actionShowHelp = new AbstractGameAction(key(KeyCode.H)) {
        @Override
        public void execute(GameContext context) {
            context.gamePage().showHelp();
        }
    };

    private final GameAction actionSimulationSlower = new AbstractGameAction(alt(KeyCode.MINUS)) {
        @Override
        public void execute(GameContext context) {
            double newRate = context.gameClock().getTargetFrameRate() - 5;
            if (newRate > 0) {
                context.gameClock().setTargetFrameRate(newRate);
                context.showFlashMessageSeconds(0.75, newRate + "Hz");
            }
        }
    };

    private final GameAction actionSimulationFaster = new AbstractGameAction(alt(KeyCode.PLUS)) {
        @Override
        public void execute(GameContext context) {
            double newRate = context.gameClock().getTargetFrameRate() + 5;
            if (newRate > 0) {
                context.gameClock().setTargetFrameRate(newRate);
                context.showFlashMessageSeconds(0.75, newRate + "Hz");
            }
        }
    };

    private final GameAction actionSimulationNormalSpeed = new AbstractGameAction(alt(KeyCode.DIGIT0)) {
        @Override
        public void execute(GameContext context) {
            context.gameClock().setTargetFrameRate(GameModel.TICKS_PER_SECOND);
            context.showFlashMessageSeconds(0.75, context.gameClock().getTargetFrameRate() + "Hz");
        }
    };

    private final GameAction actionSimulationOneStep = new AbstractGameAction(shift(KeyCode.P)) {
        @Override
        public void execute(GameContext context) {
            if (context.gameClock().isPaused()) {
                context.gameClock().makeStep(true);
            }
        }
    };

    private final GameAction actionSimulationTenSteps = new AbstractGameAction(shift(KeyCode.SPACE)) {
        @Override
        public void execute(GameContext context) {
            if (context.gameClock().isPaused()) {
                context.gameClock().makeSteps(10, true);
            }
        }
    };

    private final GameAction actionToggleAutopilot = new AbstractGameAction(alt(KeyCode.A)) {
        @Override
        public void execute(GameContext context) {
            toggle(PY_AUTOPILOT);
            boolean auto = PY_AUTOPILOT.get();
            context.showFlashMessage(context.locText(auto ? "autopilot_on" : "autopilot_off"));
            context.sounds().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
        }
    };

    private final GameAction actionToggleDashboard = new AbstractGameAction(key(KeyCode.F1), alt(KeyCode.B)) {
        @Override
        public void execute(GameContext context) {
            context.gamePage().toggleDashboard();
        }
    };

    private final GameAction actionToggleImmunity = new AbstractGameAction(alt(KeyCode.I)) {
        @Override
        public void execute(GameContext context) {
            toggle(PY_IMMUNITY);
            context.showFlashMessage(context.locText(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
            context.sounds().playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
        }
    };

    private final List<GameAction> actions = List.of(
        GlobalGameActions2D.BOOT,
        GlobalGameActions2D.SHOW_START_PAGE,
        GlobalGameActions2D.TOGGLE_PAUSED,
        GlobalGameActions2D.OPEN_EDITOR,
        actionToggleDebugInfo,
        actionShowHelp,
        actionSimulationOneStep,
        actionSimulationTenSteps,
        actionSimulationFaster,
        actionSimulationSlower,
        actionSimulationNormalSpeed,
        actionToggleAutopilot,
        actionToggleImmunity,
        actionToggleDashboard
    );

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

        for (GameAction action : actions) {
            context.keyboard().register(action.trigger());
        }

        gameCanvas = new Canvas();

        gameCanvasContainer = new TooFancyGameCanvasContainer(gameCanvas);
        gameCanvasContainer.setMinScaling(0.5);

        // default: Arcade aspect
        gameCanvasContainer.setUnscaledCanvasWidth(PacManArcadeGame.ARCADE_MAP_SIZE_X);
        gameCanvasContainer.setUnscaledCanvasHeight(PacManArcadeGame.ARCADE_MAP_SIZE_Y);

        gameCanvasContainer.setBorderColor(ARCADE_PALE);
        gameCanvasContainer.decorationEnabledPy.addListener((py, ov, nv) -> embedGameScene(gameScenePy.get()));

        gameCanvasLayer.setCenter(gameCanvasContainer);

        dashboardLayer = new DashboardLayer(context);
        dashboardLayer.addDashboardItem(context.locText("infobox.general.title"), new InfoBoxGeneral());
        dashboardLayer.addDashboardItem(context.locText("infobox.game_control.title"), new InfoBoxGameControl());
        dashboardLayer.addDashboardItem(context.locText("infobox.game_info.title"), new InfoBoxGameInfo());
        dashboardLayer.addDashboardItem(context.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        dashboardLayer.addDashboardItem(context.locText("infobox.actor_info.title"), new InfoBoxActorInfo());
        dashboardLayer.addDashboardItem(context.locText("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        dashboardLayer.addDashboardItem(context.locText("infobox.about.title"), new InfoBoxAbout());

        popupLayer = new PopupLayer(context, gameCanvasContainer);
        popupLayer.setMouseTransparent(true);
        popupLayer.sign(gameCanvasContainer,
            context.assets().font("font.monospaced", 8), Color.LIGHTGRAY,
            context.locText("app.signature"));

        getChildren().addAll(gameCanvasLayer, dashboardLayer, popupLayer);

        //TODO is this the recommended way to close an open context-menu?
        setOnMouseClicked(e -> contextMenu.hide());

        PY_DEBUG_INFO.addListener((py,ov,debug) -> {
            if (debug) {
                gameCanvasLayer.setBackground(coloredBackground(Color.DARKGREEN));
                gameCanvasLayer.setBorder(border(Color.LIGHTGREEN, 2));
            } else {
                gameCanvasLayer.setBackground(null);
                gameCanvasLayer.setBorder(null);
            }
        });
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
        GlobalGameActions2D.BOOT.execute(context);
    }

    @Override
    public void setSize(double width, double height) {
        gameCanvasContainer.resizeTo(width, height);
    }

    @Override
    public void handleInput() {
        context.doFirstCalledActionElse(actions, () -> context.currentGameScene().ifPresent(GameScene::handleInput));
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
        miMuted.selectedProperty().bindBidirectional(context.sounds().mutedProperty());
        contextMenu.getItems().add(miMuted);

        if (context.gameVariant() == GameVariant.PACMAN_XXL || context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
            miOpenMapEditor.setOnAction(e -> GlobalGameActions2D.OPEN_EDITOR.execute(context));
            contextMenu.getItems().add(miOpenMapEditor);
        }

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(e -> GlobalGameActions2D.SHOW_START_PAGE.execute(context));
        contextMenu.getItems().add(miQuit);

        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
    }

    public void setWorldRenderer(GameRenderer renderer) {
        renderer.setCanvas(gameCanvasContainer.canvas());
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
                gameCanvasContainer.backgroundProperty().bind(gameScene2D.backgroundColorPy.map(Ufx::coloredBackground));
                Vector2f sceneSize = gameScene.size();
                gameCanvasContainer.setUnscaledCanvasWidth(sceneSize.x());
                gameCanvasContainer.setUnscaledCanvasHeight(sceneSize.y());
                gameCanvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
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
        if (isCurrentGameScene2D()) {
            popupLayer.showHelp(gameCanvasContainer.scaling());
        }
    }
}