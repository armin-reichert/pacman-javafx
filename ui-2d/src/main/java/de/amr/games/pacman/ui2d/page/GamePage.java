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
import de.amr.games.pacman.ui2d.util.FlashMessageView;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Objects;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage implements Page {

    protected final GameContext context;
    protected final Scene parentScene;
    protected final StackPane stackPane = new StackPane();

    protected final CanvasLayoutPane canvasLayer  = new CanvasLayoutPane();
    protected final BorderPane       infoLayer    = new BorderPane(); // dashboard, picture-in-picture
    protected final Pane             popupLayer   = new Pane(); // help, signature

    protected final FadingPane helpInfoPopUp = new FadingPane();
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
        stackPane.getChildren().addAll(canvasLayer, infoLayer, popupLayer);
    }

    private void configureDebugDrawing() {
        stackPane.borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_DEBUG_INFO.get() && isCurrentGameScene2D() ? Ufx.border(Color.RED, 3) : null,
            PY_DEBUG_INFO, context.gameSceneProperty()
        ));
        canvasLayer.borderProperty().bind(Bindings.createObjectBinding(
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
        pip.heightProperty().bind(PY_PIP_HEIGHT);
        pip.opacityProperty().bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
    }

    private void createDashboard() {
        dashboard = new Dashboard(context);
        dashboard.addInfoBox(context.tt("infobox.general.title"), new InfoBoxGeneral());
        dashboard.addInfoBox(context.tt("infobox.game_control.title"), new InfoBoxGameControl());
        dashboard.addInfoBox("Custom Maps",new InfoCustomMaps()); //TODO incomplete
        dashboard.addInfoBox(context.tt("infobox.game_info.title"), new InfoBoxGameInfo());
        dashboard.addInfoBox(context.tt("infobox.actor_info.title"), new InfoBoxActorInfo());
        dashboard.addInfoBox(context.tt("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        dashboard.addInfoBox(context.tt("infobox.about.title"), new InfoBoxAbout());
    }

    private void createCanvasLayer() {
        canvasLayer.setUnscaledCanvasSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y);
        canvasLayer.setBackground(context.theme().background("wallpaper.background"));
        canvasLayer.decoratedCanvas().setBackground(Ufx.coloredBackground(context.theme().color("canvas.background")));
        canvasLayer.decoratedCanvas().setBorderColor(context.theme().color("palette.pale"));
        canvasLayer.decoratedCanvas().decoratedPy.addListener((py, ov, nv) -> adaptCanvasSizeToCurrentWorld());
    }

    private void createPopupLayer() {
        popupLayer.minHeightProperty().bind(canvasLayer.decoratedCanvas().minHeightProperty());
        popupLayer.maxHeightProperty().bind(canvasLayer.decoratedCanvas().maxHeightProperty());
        popupLayer.prefHeightProperty().bind(canvasLayer.decoratedCanvas().prefHeightProperty());
        popupLayer.minWidthProperty().bind(canvasLayer.decoratedCanvas().minWidthProperty());
        popupLayer.maxWidthProperty().bind(canvasLayer.decoratedCanvas().maxWidthProperty());
        popupLayer.prefWidthProperty().bind(canvasLayer.decoratedCanvas().prefWidthProperty());
        popupLayer.getChildren().addAll(helpInfoPopUp, signature);
    }

    private void createInfoLayer() {
        infoLayer.setLeft(dashboard);
        infoLayer.setRight(pip);
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
        context.gameClock().start();
        Logger.info("Clock started, speed={} Hz", context.gameClock().getTargetFrameRate());
    }

    @Override
    public void setSize(double width, double height) {
        canvasLayer.resizeTo(width, height);
    }

    public void embedGameScene3D(GameScene gameScene) {
        if (gameScene instanceof GameScene2D) {
            Logger.warn("Cannot embed 2D game scene as 3D scene");
            return;
        }
        stackPane.getChildren().set(0, gameScene.root());
    }

    public void embedGameScene2D(GameScene gameScene) {
        if (gameScene instanceof GameScene2D scene2D) {
            stackPane.getChildren().set(0, canvasLayer);
            scene2D.clearCanvas();
            adaptCanvasSizeToCurrentWorld();
        } else {
            Logger.warn("Cannot embed 3D game scene as 2D scene");
        }
    }

    public CanvasLayoutPane canvasPane() {
        return canvasLayer;
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public Signature signature() {
        return signature;
    }

    public void configureSignature(Font font, String... words) {
        signature.setWords(words);

        signature.fontPy.bind(Bindings.createObjectBinding(
            () -> Font.font(font.getFamily(), canvasLayer.scaling() * font.getSize()),
            canvasLayer.scalingPy
        ));
        // keep centered over canvas container
        signature.translateXProperty().bind(Bindings.createDoubleBinding(
            () -> 0.5 * (canvasLayer.decoratedCanvas().getWidth() - signature.getWidth()),
            canvasLayer.scalingPy, canvasLayer.decoratedCanvas().widthProperty()
        ));
        // keep at vertical position over intro scene
        signature.translateYProperty().bind(Bindings.createDoubleBinding(
            () -> canvasLayer.scaling() * 30,
            canvasLayer.scalingPy, canvasLayer.decoratedCanvas().heightProperty()
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
            canvasLayer.setUnscaledCanvasSize(world.numCols() * TS, world.numRows() * TS);
        } else {
            canvasLayer.setUnscaledCanvasSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y);
        }
        canvasLayer.resizeTo(parentScene.getWidth(), parentScene.getHeight());
        Logger.info("Canvas size adapted. w={}, h={}",
            canvasLayer.decoratedCanvas().getWidth(), canvasLayer.decoratedCanvas().getHeight());
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
        } else if (GameKeys.FULLSCREEN.pressed()) {
            context.actionHandler().setFullScreen(true);
        } else if (GameKeys.IMMUNITY.pressed()) {
            context.actionHandler().toggleImmunity();
        } else if (GameKeys.HELP.pressed()) {
            showHelpInfoPopUp();
        } else if (GameKeys.PAUSE.pressed()) {
            context.actionHandler().togglePaused();
        } else if (GameKeys.SIMULATION_STEP.pressed()) {
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
            context.soundHandler().stopVoice();
            context.soundHandler().stopAllSounds();
            context.actionHandler().selectStartPage();
        } else if (GameKeys.TEST_MODE.pressed()) {
            context.actionHandler().startLevelTestMode();
        } else if (GameKeys.TWO_D_THREE_D.pressed()) {
            context.actionHandler().toggle2D3D();
        } else if (GameKeys.DASHBOARD.pressed()) {
            context.actionHandler().toggleDashboard();
        } else if (GameKeys.PIP_VIEW.pressed()) {
            context.actionHandler().togglePipVisible();
        } else if (GameKeys.EDITOR.pressed()) {
            context.actionHandler().enterMapEditor();
        } else {
            context.currentGameScene().ifPresent(GameScene::handleKeyboardInput);
        }
    }

    // Help Info stuff

    public class HelpInfo extends PageInfo {

        public void addLocalizedEntry(String lhsKey, String keyboardKey) {
            addRow(
                label(context.tt(lhsKey), Color.gray(0.9)),
                text("[" + keyboardKey + "]", Color.YELLOW)
            );
        }

        @Override
        public Pane createPane(Color backgroundColor, Font font) {
            var pane = super.createPane(backgroundColor, font);
            var grid = (GridPane) pane.getChildren().get(0); // TODO improve
            // add default entries:
            if (PY_AUTOPILOT.get()) {
                var autoPilotEntry = text(context.tt("help.autopilot_on"), Color.ORANGE);
                autoPilotEntry.setFont(font);
                GridPane.setColumnSpan(autoPilotEntry, 2);
                grid.add(autoPilotEntry, 0, grid.getRowCount());
            }
            if (context.gameController().isPacImmune()) {
                var immunityEntry = text(context.tt("help.immunity_on"), Color.ORANGE);
                immunityEntry.setFont(font);
                GridPane.setColumnSpan(immunityEntry, 2);
                grid.add(immunityEntry, 0, grid.getRowCount() + 1);
            }
            return pane;
        }
    }

    private HelpInfo currentHelpInfo() {
        HelpInfo helpInfo = new HelpInfo();
        switch (context.gameState()) {
            case INTRO -> addInfoForIntroScene(helpInfo);
            case CREDIT -> addInfoForCreditScene(helpInfo);
            case READY, HUNTING, PACMAN_DYING, GHOST_DYING -> {
                if (context.game().isDemoLevel()) {
                    addInfoForDemoLevel(helpInfo);
                } else {
                    addInfoForPlayScene(helpInfo);
                }
            }
            default -> addInfoForQuittingScene(helpInfo);
        }
        return helpInfo;
    }

    private void showHelpInfoPopUp() {
        var bgColor = context.game().variant() == GameVariant.MS_PACMAN
            ? Color.rgb(255, 0, 0, 0.8)
            : Color.rgb(33, 33, 255, 0.8);
        var font = context.theme().font("font.monospaced", Math.max(6, 14 * canvasLayer.scaling()));
        var pane = currentHelpInfo().createPane(bgColor, font);
        helpInfoPopUp.setTranslateX(10 * canvasLayer.scaling());
        helpInfoPopUp.setTranslateY(30 * canvasLayer.scaling());
        helpInfoPopUp.setContent(pane);
        helpInfoPopUp.show(Duration.seconds(1.5));
    }

    private void addInfoForIntroScene(HelpInfo info) {
        if (context.gameController().hasCredit()) {
            info.addLocalizedEntry("help.start_game", "1");
        }
        info.addLocalizedEntry("help.add_credit", "5");
        info.addLocalizedEntry(context.game().variant() == GameVariant.MS_PACMAN
            ? "help.pacman" : "help.ms_pacman", "V");
    }

    private void addInfoForQuittingScene(HelpInfo info) {
        info.addLocalizedEntry("help.show_intro", "Q");
    }

    private void addInfoForCreditScene(HelpInfo info) {
        if (context.gameController().hasCredit()) {
            info.addLocalizedEntry("help.start_game", "1");
        }
        info.addLocalizedEntry("help.add_credit", "5");
        info.addLocalizedEntry("help.show_intro", "Q");
    }

    private void addInfoForPlayScene(HelpInfo info) {
        info.addLocalizedEntry("help.move_left", context.tt("help.cursor_left"));
        info.addLocalizedEntry("help.move_right", context.tt("help.cursor_right"));
        info.addLocalizedEntry("help.move_up", context.tt("help.cursor_up"));
        info.addLocalizedEntry("help.move_down", context.tt("help.cursor_down"));
        info.addLocalizedEntry("help.show_intro", "Q");
    }

    private void addInfoForDemoLevel(HelpInfo info) {
        info.addLocalizedEntry("help.add_credit", "5");
        info.addLocalizedEntry("help.show_intro", "Q");
    }
}