/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameKey;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.AssetMap;
import de.amr.games.pacman.ui2d.util.CanvasLayoutPane;
import de.amr.games.pacman.ui2d.util.DecoratedCanvas;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameParameters.*;
import static de.amr.games.pacman.ui2d.util.Ufx.border;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;

/**
 * @author Armin Reichert
 */
public class GamePage extends StackPane implements Page {

    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            setGameScene(get());
        }
    };

    protected final GameContext context;
    protected final Scene parentScene;
    protected final CanvasLayoutPane canvasLayer = new CanvasLayoutPane();
    protected final DashboardLayer dashboardLayer; // dashboard, picture-in-picture view
    protected final PopupLayer popupLayer; // help, signature
    protected final ContextMenu contextMenu = new ContextMenu();

    public GamePage(GameContext context, Scene parentScene) {
        this.context = checkNotNull(context);
        this.parentScene = checkNotNull(parentScene);

        AssetMap assets = context.assets();

        canvasLayer.setBackground(assets.background("wallpaper.background"));
        canvasLayer.setMinScaling(0.75);
        canvasLayer.setUnscaledCanvasSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y);

        DecoratedCanvas decoratedCanvas = canvasLayer.decoratedCanvas();
        decoratedCanvas.setBorderColor(assets.color("palette.pale"));
        decoratedCanvas.decoratedPy.bind(PY_CANVAS_DECORATED);
        decoratedCanvas.decoratedPy.addListener((py, ov, nv) -> adaptCanvasSizeToCurrentWorld());

        dashboardLayer = new DashboardLayer(context);

        popupLayer = new PopupLayer(context, decoratedCanvas);
        popupLayer.prepareSignature(canvasLayer, assets.font("font.monospaced", 9), context.locText("app.signature"));

        getChildren().addAll(canvasLayer, dashboardLayer, popupLayer);

        setOnMouseClicked(e -> contextMenu.hide());

        // Debugging
        borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_DEBUG_INFO.get() && isCurrentGameScene2D() ? border(Color.RED, 3) : null,
            PY_DEBUG_INFO, context.gameSceneProperty()
        ));
        canvasLayer.borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_DEBUG_INFO.get() && isCurrentGameScene2D() ? border(Color.YELLOW, 3) : null,
            PY_DEBUG_INFO, context.gameSceneProperty()
        ));
    }

    @Override
    public Pane rootPane() {
        return this;
    }

    @Override
    public void onSelected() {
        adaptCanvasSizeToCurrentWorld();
        //TODO check if this is always what is wanted
        context.actionHandler().reboot();
        GameSounds.playVoice("voice.explain", 0);
    }

    @Override
    public void setSize(double width, double height) {
        canvasLayer.resizeTo(width, height);
    }

    protected void setGameScene(GameScene gameScene) {
        contextMenu.hide();
        if (gameScene instanceof GameScene2D scene2D) {
            setGameScene2D(scene2D);
        } else {
            Logger.error("Cannot embed non-2D game scene");
        }
    }

    protected void setGameScene2D(GameScene2D scene2D) {
        getChildren().set(0, canvasLayer);
        scene2D.setCanvas(canvasLayer.decoratedCanvas().canvas());
        scene2D.scalingPy.bind(canvasLayer.scalingPy);
        canvasLayer.decoratedCanvas().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> coloredBackground(scene2D.backgroundColorPy.get()), scene2D.backgroundColorPy
        ));
        scene2D.clearCanvas();
        adaptCanvasSizeToCurrentWorld();
    }

    public void toggleDashboard() {
        dashboardLayer.dashboard().toggleVisibility();
    }

    @Override
    public void handleContextMenuRequest(ContextMenuEvent event) {
        if (!context.currentGameSceneIs(GameSceneID.PLAY_SCENE)) {
            return;
        }
        contextMenu.getItems().clear();
        contextMenu.getItems().add(Page.menuTitleItem(context.locText("pacman")));

        var miAutopilot = new CheckMenuItem(context.locText("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        contextMenu.getItems().add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.locText("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        contextMenu.getItems().add(miImmunity);

        contextMenu.getItems().add(new SeparatorMenuItem());

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(e -> quit());
        contextMenu.getItems().add(miQuit);

        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
    }

    public void adaptCanvasSizeToCurrentWorld() {
        var world = context.game().world();
        if (world != null) {
            canvasLayer.setUnscaledCanvasSize(world.map().terrain().numCols() * TS, world.map().terrain().numRows() * TS);
        } else {
            canvasLayer.setUnscaledCanvasSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y);
        }
        canvasLayer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
        Logger.info("Canvas size adapted. w={}, h={}",
            canvasLayer.decoratedCanvas().getWidth(), canvasLayer.decoratedCanvas().getHeight());
    }

    protected boolean isCurrentGameScene2D() {
        return context.currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
    }

    public void render() {
        context.currentGameScene().filter(GameScene2D.class::isInstance).map(GameScene2D.class::cast).ifPresent(GameScene2D::draw);
        popupLayer.setVisible(true);
        dashboardLayer.update();
    }

    @Override
    public void handleKeyboardInput(ActionHandler actions) {
        if (GameKey.AUTOPILOT.pressed()) {
            actions.toggleAutopilot();
        } else if (GameKey.BOOT.pressed()) {
            actions.reboot();
        } else if (GameKey.DEBUG_INFO.pressed()) {
            Ufx.toggle(PY_DEBUG_INFO);
        } else if (GameKey.IMMUNITY.pressed()) {
            actions.toggleImmunity();
        } else if (GameKey.HELP.pressed()) {
            showHelp();
        } else if (GameKey.PAUSE.pressed()) {
            actions.togglePaused();
        } else if (GameKey.SIMULATION_1_STEP.pressed()) {
            actions.doSimulationSteps(1);
        } else if (GameKey.SIMULATION_10_STEPS.pressed()) {
            actions.doSimulationSteps(10);
        } else if (GameKey.SIMULATION_FASTER.pressed()) {
            actions.changeSimulationSpeed(5);
        } else if (GameKey.SIMULATION_SLOWER.pressed()) {
            actions.changeSimulationSpeed(-5);
        } else if (GameKey.SIMULATION_NORMAL.pressed()) {
            actions.resetSimulationSpeed();
        } else if (GameKey.QUIT.pressed()) {
            quit();
        } else if (GameKey.TEST_MODE.pressed()) {
            actions.startLevelTestMode();
        } else if (GameKey.TWO_D_THREE_D.pressed()) {
            actions.toggle2D3D();
        } else if (GameKey.DASHBOARD.pressed()) {
            actions.toggleDashboard();
        } else if (GameKey.PIP_VIEW.pressed()) {
            actions.togglePipVisible();
        } else if (GameKey.EDITOR.pressed()) {
            actions.openMapEditor();
        } else {
            context.currentGameScene().ifPresent(gameScene -> gameScene.handleKeyboardInput(actions));
        }
    }

    public void showSignature() {
        popupLayer.signature().show(2, 3);
    }

    public void hideSignature() {
        popupLayer.signature().hide();
    }

    protected void quit() {
        GameSounds.stopAll();
        context.gameController().changeCredit(-1);
        context.actionHandler().selectStartPage();
    }

    private void showHelp() {
        if (isCurrentGameScene2D()) {
            popupLayer.showHelp(canvasLayer.scaling());
        }
    }
}