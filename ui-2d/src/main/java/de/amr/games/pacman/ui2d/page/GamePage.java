/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameKeys;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.util.CanvasLayoutPane;
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

    protected final GameContext context;
    protected final Scene parentScene;
    protected final StackPane stackPane = new StackPane();

    protected final CanvasLayoutPane canvasPane = new CanvasLayoutPane();
    protected final BorderPane       infoLayer    = new BorderPane(); // dashboard, picture-in-picture
    protected final Pane             popupLayer   = new Pane(); // help, signature

    protected final FadingPane helpPopUp = new FadingPane();
    protected final Signature signature = new Signature();
    protected Dashboard dashboard;
    protected PictureInPictureView pip;
    protected ContextMenu contextMenu;

    public GamePage(GameContext context, Scene parentScene) {
        this.context = checkNotNull(context);
        this.parentScene = parentScene;
        createCanvasLayer();
        createPictureInPicture();
        createDashboard();
        createPopupLayer();
        createInfoLayer();
        configureDebugDrawing();
        stackPane.getChildren().addAll(canvasPane, infoLayer, popupLayer);
        canvasPane().decoratedCanvas().decoratedPy.bind(PY_CANVAS_DECORATED);
    }

    private void configureDebugDrawing() {
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
    }

    private void createPictureInPicture() {
        pip = new PictureInPictureView(new PlayScene2D(), context);
        pip.heightPy.bind(PY_PIP_HEIGHT);
        pip.opacityPy.bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
    }

    private void createDashboard() {
        dashboard = new Dashboard(context);
        dashboard.addInfoBox(context.tt("infobox.general.title"), new InfoBoxGeneral());
        dashboard.addInfoBox(context.tt("infobox.game_control.title"), new InfoBoxGameControl());
        dashboard.addInfoBox("Custom Maps",new InfoBoxCustomMaps()); //TODO incomplete
        dashboard.addInfoBox(context.tt("infobox.game_info.title"), new InfoBoxGameInfo());
        dashboard.addInfoBox(context.tt("infobox.actor_info.title"), new InfoBoxActorInfo());
        dashboard.addInfoBox(context.tt("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        dashboard.addInfoBox(context.tt("infobox.about.title"), new InfoBoxAbout());
    }

    private void createCanvasLayer() {
        canvasPane.setUnscaledCanvasSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y);
        canvasPane.setMinScaling(0.75);
        canvasPane.setBackground(context.theme().background("wallpaper.background"));
        canvasPane.decoratedCanvas().setBorderColor(context.theme().color("palette.pale"));
        canvasPane.decoratedCanvas().decoratedPy.addListener((py, ov, nv) -> adaptCanvasSizeToCurrentWorld());
    }

    private void createPopupLayer() {
        popupLayer.minHeightProperty().bind(canvasPane.decoratedCanvas().minHeightProperty());
        popupLayer.maxHeightProperty().bind(canvasPane.decoratedCanvas().maxHeightProperty());
        popupLayer.prefHeightProperty().bind(canvasPane.decoratedCanvas().prefHeightProperty());
        popupLayer.minWidthProperty().bind(canvasPane.decoratedCanvas().minWidthProperty());
        popupLayer.maxWidthProperty().bind(canvasPane.decoratedCanvas().maxWidthProperty());
        popupLayer.prefWidthProperty().bind(canvasPane.decoratedCanvas().prefWidthProperty());
        popupLayer.getChildren().addAll(helpPopUp, signature);
    }

    private void createInfoLayer() {
        infoLayer.setLeft(dashboard);
        var spacer = new HBox();
        infoLayer.setRight(new VBox(pip.node(), spacer));
        infoLayer.visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboard.isVisible() || PY_PIP_ON.get(),
            dashboard.visibleProperty(), PY_PIP_ON
        ));
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
        context.soundHandler().playVoice("voice.explain", 0);
    }

    @Override
    public void setSize(double width, double height) {
        canvasPane.resizeTo(width, height);
    }

    public void embedGameScene(GameScene gameScene) {
        hideContextMenu();
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

    public CanvasLayoutPane canvasPane() {
        return canvasPane;
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
        if (contextMenu != null) {
            contextMenu.hide(); //TODO is this the recommended way?
        }
    }

    public void onContextMenuRequested(ContextMenuEvent event) {
        if (contextMenu != null) {
            contextMenu.hide();
        }
        if (!context.isCurrentGameSceneRegisteredAs(GameSceneID.PLAY_SCENE)) {
            return;
        }
        contextMenu = new ContextMenu();
        contextMenu.getItems().add(menuTitleItem(context.tt("pacman")));

        var miAutopilot = new CheckMenuItem(context.tt("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        contextMenu.getItems().add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.tt("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        contextMenu.getItems().add(miImmunity);

        contextMenu.getItems().add(new SeparatorMenuItem());
        if (context.game().variant() == GameVariant.PACMAN_XXL) {
            var miOpenMapEditor = new MenuItem(context.tt("open_editor"));
            contextMenu.getItems().add(miOpenMapEditor);
            miOpenMapEditor.setOnAction(e -> context.actionHandler().openMapEditor());
        }

        var miQuit = new MenuItem(context.tt("quit"));
        miQuit.setOnAction(e -> quit());
        contextMenu.getItems().add(miQuit);

        contextMenu.requestFocus();
        contextMenu.show(rootPane(), event.getScreenX(), event.getScreenY());
    }

    public void hideContextMenu() {
        if (contextMenu != null) {
            contextMenu.hide();
        }
    }

    protected MenuItem menuTitleItem(String titleText) {
        var text = new Text(titleText);
        text.setFont(Font.font("Dialog", FontWeight.BLACK, 14));
        text.setFill(Color.CORNFLOWERBLUE); // "Kornblumenblau, sind die Augen der Frauen beim Weine..."
        return new CustomMenuItem(text);
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
    public void handleKeyboardInput() {
        if (GameKeys.AUTOPILOT.pressed()) {
            context.actionHandler().toggleAutopilot();
        } else if (GameKeys.BOOT.pressed()) {
            context.actionHandler().reboot();
        } else if (GameKeys.DEBUG_INFO.pressed()) {
            Ufx.toggle(PY_DEBUG_INFO);
        } else if (GameKeys.IMMUNITY.pressed()) {
            context.actionHandler().toggleImmunity();
        } else if (GameKeys.HELP.pressed()) {
            showHelp();
        } else if (GameKeys.PAUSE.pressed()) {
            context.actionHandler().togglePaused();
        } else if (GameKeys.SIMULATION_1_STEP.pressed()) {
            context.actionHandler().doSimulationSteps(1);
        } else if (GameKeys.SIMULATION_10_STEPS.pressed()) {
            context.actionHandler().doSimulationSteps(10);
        } else if (GameKeys.SIMULATION_FASTER.pressed()) {
            context.actionHandler().changeSimulationSpeed(5);
        } else if (GameKeys.SIMULATION_SLOWER.pressed()) {
            context.actionHandler().changeSimulationSpeed(-5);
        } else if (GameKeys.SIMULATION_NORMAL.pressed()) {
            context.actionHandler().resetSimulationSpeed();
        } else if (GameKeys.QUIT.pressed()) {
            quit();
        } else if (GameKeys.TEST_MODE.pressed()) {
            context.actionHandler().startLevelTestMode();
        } else if (GameKeys.TWO_D_THREE_D.pressed()) {
            context.actionHandler().toggle2D3D();
        } else if (GameKeys.DASHBOARD.pressed()) {
            context.actionHandler().toggleDashboard();
        } else if (GameKeys.PIP_VIEW.pressed()) {
            context.actionHandler().togglePipVisible();
        } else if (GameKeys.EDITOR.pressed()) {
            context.actionHandler().openMapEditor();
        } else {
            context.currentGameScene().ifPresent(GameScene::handleKeyboardInput);
        }
    }

    protected void quit() {
        context.soundHandler().stopVoice();
        context.soundHandler().stopAllSounds();
        context.actionHandler().selectStartPage();
        context.gameController().changeCredit(-1);
    }

    private void showHelp() {
        if (isCurrentGameScene2D()) {
            var bgColor = context.game().variant() == GameVariant.MS_PACMAN
                ? context.theme().color("palette.red")
                : context.theme().color("palette.blue");
            var font = context.theme().font("font.monospaced", Math.max(6, 14 * canvasPane.scaling()));
            var helpPane = HelpInfo.build(context).createPane(Ufx.opaqueColor(bgColor, 0.8), font);
            helpPopUp.setTranslateX(10 * canvasPane.scaling());
            helpPopUp.setTranslateY(30 * canvasPane.scaling());
            helpPopUp.setContent(helpPane);
            helpPopUp.show(Duration.seconds(1.5));
        }
    }
}