/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.TooFancyGameCanvasContainer;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameAssets2D.ARCADE_PALE;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.util.Ufx.border;

/**
 * @author Armin Reichert
 */
public class GamePage extends StackPane implements Page {

    private static final List<GameAction> GAME_ACTIONS = List.of(
        GameAction2D.BOOT,
        GameAction2D.DEBUG_INFO,
        GameAction2D.HELP,
        GameAction2D.SIMULATION_1_STEP,
        GameAction2D.SIMULATION_10_STEPS,
        GameAction2D.SIMULATION_FASTER,
        GameAction2D.SIMULATION_SLOWER,
        GameAction2D.SIMULATION_NORMAL,
        GameAction2D.SHOW_START_PAGE,
        GameAction2D.TOGGLE_AUTOPILOT,
        GameAction2D.TOGGLE_IMMUNITY,
        GameAction2D.TOGGLE_DASHBOARD,
        GameAction2D.TOGGLE_PAUSED,
        GameAction2D.OPEN_EDITOR
    );

    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            setGameScene(get());
        }
    };

    protected final GameContext context;
    protected final Scene parentScene;
    protected final Canvas gameCanvas;
    protected final BorderPane gameCanvasPane = new BorderPane();
    protected final TooFancyGameCanvasContainer gameCanvasContainer;
    protected final DashboardLayer dashboardLayer; // dashboard, picture-in-picture view
    protected final PopupLayer popupLayer; // help, signature
    protected final ContextMenu contextMenu = new ContextMenu();

    public GamePage(GameContext context, Scene parentScene) {
        this.context = checkNotNull(context);
        this.parentScene = checkNotNull(parentScene);

        gameCanvas = new Canvas();
        gameCanvasContainer = new TooFancyGameCanvasContainer(gameCanvas);
        gameCanvasPane.setCenter(gameCanvasContainer);

        gameCanvasContainer.setMinScaling(0.5);
        gameCanvasContainer.setUnscaledCanvasWidth(GameModel.ARCADE_MAP_SIZE_X);
        gameCanvasContainer.setUnscaledCanvasHeight(GameModel.ARCADE_MAP_SIZE_Y);
        gameCanvasContainer.setBorderColor(ARCADE_PALE);
        gameCanvasContainer.enabledPy.bind(PY_GAME_CANVAS_DECORATED);
        gameCanvasContainer.enabledPy.addListener((py, ov, nv) -> adaptCanvasSizeToCurrentWorld());


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

        getChildren().addAll(gameCanvasPane, dashboardLayer, popupLayer);

        //TODO is this the recommended way to close an open context-menu?
        setOnMouseClicked(e -> contextMenu.hide());

        // Debugging
        borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_DEBUG_INFO.get() && isCurrentGameScene2D() ? border(Color.RED, 3) : null,
            PY_DEBUG_INFO, context.gameSceneProperty()
        ));
        gameCanvasPane.borderProperty().bind(Bindings.createObjectBinding(
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
        //TODO check if booting is always wanted here
        GameAction2D.BOOT.execute(context);
        if (context.gameVariant() != GameVariant.MS_PACMAN_TENGEN) {
            // Tengen has a settings scene where the game is started instead of a credits scene
            context.sounds().playVoice("voice.explain", 0);
        }
    }

    @Override
    public void setSize(double width, double height) {
        gameCanvasContainer.resizeTo(width, height);
    }

    @Override
    public void handleInput() {
        context.execFirstCalledActionOrElse(GAME_ACTIONS.stream(),
            () -> context.currentGameScene().ifPresent(GameScene::handleInput));
    }

    @Override
    public void handleContextMenuRequest(ContextMenuEvent event) {
        if (!context.currentGameSceneIs(GameSceneID.PLAY_SCENE)) {
            return;
        }
        contextMenu.getItems().clear();

        contextMenu.getItems().add(Page.menuTitleItem(context.locText("scene_display")));

        var miCanvasDecorated = new CheckMenuItem(context.locText("canvas_decoration"));
        miCanvasDecorated.selectedProperty().bindBidirectional(PY_GAME_CANVAS_DECORATED);
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

        if (context.gameVariant() == GameVariant.PACMAN_XXL || context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
            miOpenMapEditor.setOnAction(e -> GameAction2D.OPEN_EDITOR.execute(context));
            contextMenu.getItems().add(miOpenMapEditor);
        }

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(e -> GameAction2D.SHOW_START_PAGE.execute(context));
        contextMenu.getItems().add(miQuit);

        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
    }

    public void adaptCanvasSizeToCurrentWorld() {
        Vector2i worldSizePixels = context.worldSizeTilesOrDefault().scaled(TS);
        gameCanvasContainer.setUnscaledCanvasWidth(worldSizePixels.x());
        gameCanvasContainer.setUnscaledCanvasHeight(worldSizePixels.y());
        gameCanvasContainer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
    }

    public void setWorldRenderer(GameWorldRenderer renderer) {
        renderer.setCanvas(gameCanvasContainer.canvas());
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
        getChildren().set(0, gameCanvasPane);
        scene2D.scalingPy.bind(gameCanvasContainer.scalingPy);
        gameCanvasContainer.backgroundProperty().bind(scene2D.backgroundColorPy.map(Ufx::coloredBackground));
        adaptCanvasSizeToCurrentWorld();
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