/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.CanvasLayoutPane;
import de.amr.games.pacman.ui2d.util.DecoratedCanvas;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
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
import static de.amr.games.pacman.ui2d.GameAction.executeCalledAction;
import static de.amr.games.pacman.ui2d.GameAssets2D.PALETTE_PALE;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.util.Ufx.border;

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

        canvasLayer.setMinScaling(0.75);
        canvasLayer.setUnscaledCanvasSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y);

        DecoratedCanvas decoratedCanvas = canvasLayer.canvas();
        decoratedCanvas.setBorderColor(PALETTE_PALE);
        decoratedCanvas.decoratedPy.bind(PY_CANVAS_DECORATED);
        decoratedCanvas.decoratedPy.addListener((py, ov, nv) -> adaptCanvasSizeToCurrentWorld());

        dashboardLayer = new DashboardLayer(context);
        dashboardLayer.addDashboardItem(context.locText("infobox.general.title"), new InfoBoxGeneral());
        dashboardLayer.addDashboardItem(context.locText("infobox.game_control.title"), new InfoBoxGameControl());
        dashboardLayer.addDashboardItem(context.locText("infobox.game_info.title"), new InfoBoxGameInfo());
        dashboardLayer.addDashboardItem(context.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        dashboardLayer.addDashboardItem(context.locText("infobox.actor_info.title"), new InfoBoxActorInfo());
        dashboardLayer.addDashboardItem(context.locText("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        dashboardLayer.addDashboardItem(context.locText("infobox.about.title"), new InfoBoxAbout());

        popupLayer = new PopupLayer(context, decoratedCanvas);
        popupLayer.setMouseTransparent(true);
        popupLayer.configureSignature(canvasLayer, context.assets().font("font.monospaced", 10),
            Color.grayRgb(200), context.locText("app.signature"));

        getChildren().addAll(canvasLayer, dashboardLayer, popupLayer);

        //TODO is this the recommended way to close an open context-menu?
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
    public void onPageSelected() {
        adaptCanvasSizeToCurrentWorld();
        //TODO check if this is always what is wanted
        GameAction2D.BOOT.execute(context);
        context.updateCustomMaps();
        context.sounds().playVoice("voice.explain", 0);
    }

    @Override
    public void setSize(double width, double height) {
        canvasLayer.resizeTo(width, height);
    }

    @Override
    public void handleInput() {
        if (!executeCalledAction(context,
            GameAction2D.BOOT,
            GameAction2D.DEBUG_INFO,
            GameAction2D.HELP,
            GameAction2D.SIMULATION_1_STEP,
            GameAction2D.SIMULATION_10_STEPS,
            GameAction2D.SIMULATION_FASTER,
            GameAction2D.SIMULATION_SLOWER,
            GameAction2D.SIMULATION_NORMAL,
            GameAction2D.SHOW_START_PAGE,
            GameAction2D.TEST_LEVELS,
            GameAction2D.TOGGLE_AUTOPILOT,
            GameAction2D.TOGGLE_IMMUNITY,
            GameAction2D.TOGGLE_DASHBOARD,
            GameAction2D.TOGGLE_PAUSED,
            GameAction2D.OPEN_EDITOR))
        {
            context.currentGameScene().ifPresent(GameScene::handleInput);
        }
    }

    @Override
    public void handleContextMenuRequest(ContextMenuEvent event) {
        if (!context.currentGameSceneIs(GameSceneID.PLAY_SCENE)) {
            return;
        }
        contextMenu.getItems().clear();

        contextMenu.getItems().add(Page.menuTitleItem(context.locText("scene_display")));

        var miCanvasDecorated = new CheckMenuItem(context.locText("canvas_decoration"));
        miCanvasDecorated.selectedProperty().bindBidirectional(PY_CANVAS_DECORATED);
        contextMenu.getItems().add(miCanvasDecorated);

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

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(e -> GameAction2D.SHOW_START_PAGE.execute(context));
        contextMenu.getItems().add(miQuit);

        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
    }

    public Canvas canvas() {
        return canvasLayer.canvas().canvas();
    }

    public void adaptCanvasSizeToCurrentWorld() {
        Vector2i worldSizePixels = context.worldSizeTilesOrDefault().scaled(TS);
        canvasLayer.setUnscaledCanvasSize(worldSizePixels.x(), worldSizePixels.y());
        canvasLayer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
        Logger.info("Canvas size adapted. w={0.00}, h={0.00}", canvas().getWidth(), canvas().getHeight());
    }

    protected void setGameScene(GameScene gameScene) {
        if (gameScene == null) {
            return; // happens when app is initialized
        }
        contextMenu.hide();
        if (gameScene instanceof GameScene2D scene2D) {
            setGameScene2D(scene2D);
        } else {
            Logger.error("Cannot embed non-2D game scene: {}", gameScene);
        }
    }

    protected void setGameScene2D(GameScene2D scene2D) {
        getChildren().set(0, canvasLayer);
        scene2D.scalingPy.bind(canvasLayer.canvas().scalingPy);
        canvasLayer.canvas().backgroundProperty().bind(scene2D.backgroundColorPy.map(Ufx::coloredBackground));
        adaptCanvasSizeToCurrentWorld();
    }

    protected boolean isCurrentGameScene2D() {
        return context.currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
    }

    public void showSignature() {
        popupLayer.showSignature(1, 3, 5);
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
            popupLayer.showHelp(canvasLayer.canvas().scaling());
        }
    }
}