/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameKey;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.util.CanvasLayoutPane;
import de.amr.games.pacman.ui2d.util.DecoratedCanvas;
import de.amr.games.pacman.ui2d.util.FadingPane;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameParameters.*;

/**
 * @author Armin Reichert
 */
public class GamePage implements Page {

    public static final Font CONTEXT_MENU_TITLE_FONT = Font.font("Dialog", FontWeight.BLACK, 14);
    public static final Color CONTEXT_MENU_TITLE_BACKGROUND = Color.CORNFLOWERBLUE; // "Kornblumenblau, sind die Augen der Frauen beim Weine..."

    protected static MenuItem menuTitleItem(String titleText) {
        var text = new Text(titleText);
        text.setFont(CONTEXT_MENU_TITLE_FONT);
        text.setFill(CONTEXT_MENU_TITLE_BACKGROUND);
        return new CustomMenuItem(text);
    }

    protected final GameContext context;
    protected final Scene parentScene;
    protected final StackPane stackPane = new StackPane();
    protected final CanvasLayoutPane canvasPane = new CanvasLayoutPane();
    protected final BorderPane infoLayer = new BorderPane(); // dashboard, picture-in-picture
    protected final Pane popupLayer = new Pane(); // help, signature
    protected final FadingPane helpPopUp = new FadingPane();
    protected final Signature signature = new Signature();
    protected final ContextMenu contextMenu = new ContextMenu();
    protected final Dashboard dashboard;
    protected final PictureInPictureView pip;

    public GamePage(GameContext context, Scene parentScene) {
        this.context = checkNotNull(context);
        this.parentScene = parentScene;

        dashboard = new Dashboard(context);
        dashboard.addInfoBox(context.locText("infobox.general.title"), new InfoBoxGeneral());
        dashboard.addInfoBox(context.locText("infobox.game_control.title"), new InfoBoxGameControl());
        dashboard.addInfoBox("Custom Maps",new InfoBoxCustomMaps()); //TODO incomplete
        dashboard.addInfoBox(context.locText("infobox.game_info.title"), new InfoBoxGameInfo());
        dashboard.addInfoBox(context.locText("infobox.actor_info.title"), new InfoBoxActorInfo());
        dashboard.addInfoBox(context.locText("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        dashboard.addInfoBox(context.locText("infobox.about.title"), new InfoBoxAbout());

        DecoratedCanvas canvas = canvasPane.decoratedCanvas();
        canvasPane.setUnscaledCanvasSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y);
        canvasPane.setMinScaling(0.75);
        canvasPane.setBackground(context.assets().background("wallpaper.background"));
        canvas.setBorderColor(context.assets().color("palette.pale"));
        canvas.decoratedPy.addListener((py, ov, nv) -> adaptCanvasSizeToCurrentWorld());
        canvas.decoratedPy.bind(PY_CANVAS_DECORATED);

        pip = new PictureInPictureView(new PlayScene2D(), context);
        pip.heightPy.bind(PY_PIP_HEIGHT);
        pip.opacityPy.bind(PY_PIP_OPACITY_PERCENT.divide(100.0));

        popupLayer.minHeightProperty().bind(canvas.minHeightProperty());
        popupLayer.maxHeightProperty().bind(canvas.maxHeightProperty());
        popupLayer.prefHeightProperty().bind(canvas.prefHeightProperty());
        popupLayer.minWidthProperty().bind(canvas.minWidthProperty());
        popupLayer.maxWidthProperty().bind(canvas.maxWidthProperty());
        popupLayer.prefWidthProperty().bind(canvas.prefWidthProperty());
        popupLayer.getChildren().addAll(helpPopUp, signature);

        infoLayer.setLeft(dashboard);
        var spacer = new HBox();
        infoLayer.setRight(new VBox(pip.node(), spacer));
        infoLayer.visibleProperty().bind(Bindings.createObjectBinding(
                () -> dashboard.isVisible() || PY_PIP_ON.get(),
                dashboard.visibleProperty(), PY_PIP_ON
        ));

        stackPane.borderProperty().bind(Bindings.createObjectBinding(
                () -> PY_DEBUG_INFO.get() && isCurrentGameScene2D() ? Ufx.border(Color.RED, 3) : null,
                PY_DEBUG_INFO, context.gameSceneProperty()
        ));
        canvasPane.borderProperty().bind(Bindings.createObjectBinding(
                () -> PY_DEBUG_INFO.get() && isCurrentGameScene2D() ? Ufx.border(Color.YELLOW, 3) : null,
                PY_DEBUG_INFO, context.gameSceneProperty()
        ));
        popupLayer.borderProperty().bind(Bindings.createObjectBinding(
                () -> PY_DEBUG_INFO.get() && isCurrentGameScene2D() ? Ufx.border(Color.GREENYELLOW, 3) : null,
                PY_DEBUG_INFO, context.gameSceneProperty()
        ));
        popupLayer.mouseTransparentProperty().bind(PY_DEBUG_INFO);

        stackPane.getChildren().addAll(canvasPane, infoLayer, popupLayer);
    }

    @Override
    public Pane rootPane() {
        return stackPane;
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
        canvasPane.resizeTo(width, height);
    }

    public void embedGameScene(GameScene gameScene) {
        contextMenu.hide();
        if (gameScene instanceof GameScene2D gameScene2D) {
            embedGameScene2D(gameScene2D);
        } else {
            Logger.error("Cannot embed 3D game scene");
        }
    }

    protected void embedGameScene2D(GameScene2D scene2D) {
        stackPane.getChildren().set(0, canvasPane);
        scene2D.setCanvas(canvasPane.decoratedCanvas().canvas());
        scene2D.scalingPy.bind(canvasPane.scalingPy);
        canvasPane.decoratedCanvas().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> Ufx.coloredBackground(scene2D.backgroundColorPy.get()), scene2D.backgroundColorPy
        ));
        scene2D.clearCanvas();
        adaptCanvasSizeToCurrentWorld();
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public Signature signature() {
        return signature;
    }

    public void sign(Font font, String... words) {
        signature.setWords(words);

        signature.fontPy.bind(Bindings.createObjectBinding(
            () -> Font.font(font.getFamily(), canvasPane.scaling() * font.getSize()),
            canvasPane.scalingPy
        ));

        // keep centered over canvas container
        signature.translateXProperty().bind(Bindings.createDoubleBinding(
            () -> 0.5 * (canvasPane.decoratedCanvas().getWidth() - signature.getWidth()),
            canvasPane.scalingPy, canvasPane.decoratedCanvas().widthProperty()
        ));

        // keep at vertical position over intro scene
        signature.translateYProperty().bind(Bindings.createDoubleBinding(
            () -> canvasPane.scaling() * 30,
            canvasPane.scalingPy, canvasPane.decoratedCanvas().heightProperty()
        ));
    }

    @Override
    public void onMouseClicked(MouseEvent e) {
        contextMenu.hide(); //TODO is this the recommended way?
    }

    public void onContextMenuRequested(ContextMenuEvent event) {
        contextMenu.hide();
        if (!context.isCurrentGameSceneRegisteredAs(GameSceneID.PLAY_SCENE)) {
            return;
        }
        contextMenu.getItems().clear();
        contextMenu.getItems().add(menuTitleItem(context.locText("pacman")));

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

        contextMenu.show(rootPane(), event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
    }

    public void adaptCanvasSizeToCurrentWorld() {
        var world = context.game().world();
        if (world != null) {
            canvasPane.setUnscaledCanvasSize(world.map().terrain().numCols() * TS, world.map().terrain().numRows() * TS);
        } else {
            canvasPane.setUnscaledCanvasSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y);
        }
        canvasPane.resizeTo(parentScene.getWidth(), parentScene.getHeight());
        Logger.info("Canvas size adapted. w={}, h={}",
            canvasPane.decoratedCanvas().getWidth(), canvasPane.decoratedCanvas().getHeight());
    }

    protected boolean isCurrentGameScene2D() {
        return context.currentGameScene().isPresent()
            && context.currentGameScene().get() instanceof GameScene2D;
    }

    public void render() {
        context.currentGameScene().ifPresent(GameScene::draw);
        popupLayer.setVisible(true);
        dashboard.update();
        pip.setVisible(PY_PIP_ON.get() && !isCurrentGameScene2D()); //TODO
        pip.draw();
    }

    @Override
    public void handleKeyboardInput(ActionHandler handler) {
        if (GameKey.AUTOPILOT.pressed()) {
            handler.toggleAutopilot();
        } else if (GameKey.BOOT.pressed()) {
            handler.reboot();
        } else if (GameKey.DEBUG_INFO.pressed()) {
            Ufx.toggle(PY_DEBUG_INFO);
        } else if (GameKey.IMMUNITY.pressed()) {
            handler.toggleImmunity();
        } else if (GameKey.HELP.pressed()) {
            showHelp();
        } else if (GameKey.PAUSE.pressed()) {
            handler.togglePaused();
        } else if (GameKey.SIMULATION_1_STEP.pressed()) {
            handler.doSimulationSteps(1);
        } else if (GameKey.SIMULATION_10_STEPS.pressed()) {
            handler.doSimulationSteps(10);
        } else if (GameKey.SIMULATION_FASTER.pressed()) {
            handler.changeSimulationSpeed(5);
        } else if (GameKey.SIMULATION_SLOWER.pressed()) {
            handler.changeSimulationSpeed(-5);
        } else if (GameKey.SIMULATION_NORMAL.pressed()) {
            handler.resetSimulationSpeed();
        } else if (GameKey.QUIT.pressed()) {
            quit();
        } else if (GameKey.TEST_MODE.pressed()) {
            handler.startLevelTestMode();
        } else if (GameKey.TWO_D_THREE_D.pressed()) {
            handler.toggle2D3D();
        } else if (GameKey.DASHBOARD.pressed()) {
            handler.toggleDashboard();
        } else if (GameKey.PIP_VIEW.pressed()) {
            handler.togglePipVisible();
        } else if (GameKey.EDITOR.pressed()) {
            handler.openMapEditor();
        } else {
            context.currentGameScene().ifPresent(gameScene -> gameScene.handleKeyboardInput(handler));
        }
    }

    protected void quit() {
        GameSounds.stopVoice();
        GameSounds.stopAll();
        context.actionHandler().selectStartPage();
        context.gameController().changeCredit(-1);
    }

    private void showHelp() {
        if (isCurrentGameScene2D()) {
            var bgColor = context.game().variant() == GameVariant.MS_PACMAN
                ? context.assets().color("palette.red")
                : context.assets().color("palette.blue");
            var font = context.assets().font("font.monospaced", Math.max(6, 14 * canvasPane.scaling()));
            var helpPane = HelpInfo.build(context).createPane(Ufx.opaqueColor(bgColor, 0.8), font);
            helpPopUp.setTranslateX(10 * canvasPane.scaling());
            helpPopUp.setTranslateY(30 * canvasPane.scaling());
            helpPopUp.setContent(helpPane);
            helpPopUp.show(Duration.seconds(1.5));
        }
    }
}