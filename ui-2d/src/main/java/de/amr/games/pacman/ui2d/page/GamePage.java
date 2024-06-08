/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.Keys;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameContext;
import de.amr.games.pacman.ui2d.util.*;
import javafx.beans.binding.Bindings;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage implements Page {

    protected final GameContext context;
    protected final CanvasLayoutPane layout;
    protected final Pane popupLayer = new Pane();
    protected final FlashMessageView flashMessageView = new FlashMessageView();
    protected final FadingPane helpInfoPopUp = new FadingPane();
    protected final Signature signature = new Signature();
    protected final BorderPane dashboardLayer;
    protected final Dashboard dashboard;
    protected final PictureInPictureView pip;
    protected ContextMenu contextMenu;

    public GamePage(GameContext context) {
        this.context = checkNotNull(context);

        layout = new CanvasLayoutPane();
        layout.canvasLayer().setBackground(context.theme().background("wallpaper.background"));
        layout.decoratedCanvas().decoratedPy.addListener((py, ov, nv) -> adaptCanvasSizeToCurrentWorld());
        layout.decoratedCanvas().setBackground(Ufx.coloredBackground(context.theme().color("canvas.background")));
        layout.decoratedCanvas().setBorderColor(context.theme().color("palette.pale"));
        layout.setUnscaledCanvasSize(DEFAULT_CANVAS_WIDTH_UNSCALED, DEFAULT_CANVAS_HEIGHT_UNSCALED);

        // keep popup layer size same as (decorated) canvas
        popupLayer.minHeightProperty().bind(layout.decoratedCanvas().minHeightProperty());
        popupLayer.maxHeightProperty().bind(layout.decoratedCanvas().maxHeightProperty());
        popupLayer.prefHeightProperty().bind(layout.decoratedCanvas().prefHeightProperty());
        popupLayer.minWidthProperty().bind(layout.decoratedCanvas().minWidthProperty());
        popupLayer.maxWidthProperty().bind(layout.decoratedCanvas().maxWidthProperty());
        popupLayer.prefWidthProperty().bind(layout.decoratedCanvas().prefWidthProperty());
        popupLayer.getChildren().addAll(helpInfoPopUp, signature);

        pip = new PictureInPictureView(context);

        dashboard = new Dashboard(context);
        dashboard.addInfoBox(context.tt("infobox.general.title"), new InfoBoxGeneral());
        dashboard.addInfoBox(context.tt("infobox.game_control.title"), new InfoBoxGameControl());
        dashboard.addInfoBox(context.tt("infobox.game_info.title"), new InfoBoxGameInfo());
        dashboard.addInfoBox(context.tt("infobox.actor_info.title"), new InfoBoxActorInfo());
        dashboard.addInfoBox(context.tt("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        dashboard.addInfoBox(context.tt("infobox.about.title"), new InfoBoxAbout());

        dashboardLayer = new BorderPane();
        dashboardLayer.setLeft(dashboard);
        dashboardLayer.setRight(pip);

        layout.getChildren().add(dashboardLayer);
        layout.canvasLayer().setBackground(context.theme().background("wallpaper.background"));

        // data binding
        pip.heightProperty().bind(PY_PIP_HEIGHT);
        PY_PIP_ON.addListener((py, ov, nv) -> updateDashboardLayer());
        dashboard.visibleProperty().addListener((py, ov, nv) -> updateDashboardLayer());

        updateDashboardLayer();

        layout.getChildren().addAll(popupLayer, flashMessageView);
        createDebugInfoBindings();
    }

    @Override
    public Pane rootPane() {
        return layout;
    }

    @Override
    public void onSelected() {
        //TODO check if this is always what is wanted
        context.actionHandler().reboot();
        context.gameClock().start();
        Logger.info("Clock started, speed={} Hz", context.gameClock().getTargetFrameRate());
    }

    @Override
    public void setSize(double width, double height) {
        layout.resizeTo(width, height);
    }

    public CanvasLayoutPane layout() {
        return layout;
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    protected void updateDashboardLayer() {
        dashboardLayer.setVisible(dashboard.isVisible() || PY_PIP_ON.get());
        layout.requestFocus();
    }

    public Signature signature() {
        return signature;
    }

    public void configureSignature(Font font, String... words) {
        signature.setWords(words);

        signature.fontPy.bind(Bindings.createObjectBinding(
            () -> Font.font(font.getFamily(), layout.scaling() * font.getSize()),
            layout.scalingPy
        ));
        // keep centered over canvas container
        signature.translateXProperty().bind(Bindings.createDoubleBinding(
            () -> 0.5 * (layout.decoratedCanvas().getWidth() - signature.getWidth()),
            layout.scalingPy, layout.decoratedCanvas().widthProperty()
        ));
        // keep at vertical position over intro scene
        signature.translateYProperty().bind(Bindings.createDoubleBinding(
            () -> layout.scaling() * 30,
            layout.scalingPy, layout.decoratedCanvas().heightProperty()
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
        if (!context.isCurrentGameScene(PLAY_SCENE)) {
            return;
        }
        contextMenu = new ContextMenu();
        contextMenu.getItems().add(menuTitleItem(context.tt("pacman")));

        var miAutopilot = new CheckMenuItem(context.tt("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_USE_AUTOPILOT);
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

    public void onGameSceneChanged(GameScene newGameScene) {
        if (newGameScene instanceof GameScene2D scene2D) {
            scene2D.clearCanvas();
            adaptCanvasSizeToCurrentWorld();
        }
        updateDashboardLayer();
        hideContextMenu();
    }

    public void adaptCanvasSizeToCurrentWorld() {
        var world = context.game().world();
        if (world != null) {
            layout.setUnscaledCanvasSize(world.numCols() * TS, world.numRows() * TS);
        } else {
            layout.setUnscaledCanvasSize(DEFAULT_CANVAS_WIDTH_UNSCALED, DEFAULT_CANVAS_HEIGHT_UNSCALED);
        }
    }

    private void createDebugInfoBindings() {
        layout.borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_SHOW_DEBUG_INFO.get() && isCurrentGameScene2D() ? Ufx.border(Color.RED, 3) : null,
            PY_SHOW_DEBUG_INFO, context.gameSceneProperty()
        ));
        layout.canvasLayer().borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_SHOW_DEBUG_INFO.get() && isCurrentGameScene2D() ? Ufx.border(Color.YELLOW, 3) : null,
            PY_SHOW_DEBUG_INFO, context.gameSceneProperty()
        ));
        popupLayer.borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_SHOW_DEBUG_INFO.get() && isCurrentGameScene2D() ? Ufx.border(Color.GREENYELLOW, 3) : null,
            PY_SHOW_DEBUG_INFO, context.gameSceneProperty()
        ));
        popupLayer.mouseTransparentProperty().bind(PY_SHOW_DEBUG_INFO);
    }

    protected boolean isCurrentGameScene2D() {
        return context.currentGameScene().isPresent()
            && context.currentGameScene().get() instanceof GameScene2D;
    }

    public FlashMessageView flashMessageView() {
        return flashMessageView;
    }

    public void render() {
        context.currentGameScene().ifPresent(GameScene::draw);
        flashMessageView.update();
        popupLayer.setVisible(true);
        dashboard.update();
        pip.setVisible(PY_PIP_ON.get() && !isCurrentGameScene2D()); //TODO
        pip.draw();
    }

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.pressed(Keys.AUTOPILOT)) {
            context.actionHandler().toggleAutopilot();
        } else if (Keyboard.pressed(Keys.BOOT)) {
            context.actionHandler().reboot();
        } else if (Keyboard.pressed(Keys.DEBUG_INFO)) {
            Ufx.toggle(PY_SHOW_DEBUG_INFO);
        } else if (Keyboard.pressed(Keys.FULLSCREEN)) {
            context.actionHandler().setFullScreen(true);
        } else if (Keyboard.pressed(Keys.IMMUNITY)) {
            context.actionHandler().toggleImmunity();
        } else if (Keyboard.pressed(Keys.HELP)) {
            showHelpInfoPopUp();
        } else if (Keyboard.pressed(Keys.PAUSE)) {
            context.actionHandler().togglePaused();
        } else if (Keyboard.pressed(Keys.SIMULATION_STEP)) {
            context.actionHandler().doSimulationSteps(1);
        } else if (Keyboard.pressed(Keys.SIMULATION_10_STEPS)) {
            context.actionHandler().doSimulationSteps(10);
        } else if (Keyboard.pressed(Keys.SIMULATION_FASTER)) {
            context.actionHandler().changeSimulationSpeed(5);
        } else if (Keyboard.pressed(Keys.SIMULATION_SLOWER)) {
            context.actionHandler().changeSimulationSpeed(-5);
        } else if (Keyboard.pressed(Keys.SIMULATION_NORMAL)) {
            context.actionHandler().resetSimulationSpeed();
        } else if (Keyboard.pressed(Keys.QUIT)) {
            context.soundHandler().stopVoice();
            context.soundHandler().stopAllSounds();
            context.actionHandler().selectPage(START_PAGE);
        } else if (Keyboard.pressed(Keys.TEST_MODE)) {
            context.actionHandler().startLevelTestMode();
        } else if (Keyboard.pressed(Keys.TWO_D_THREE_D)) {
            context.actionHandler().toggle2D3D();
        } else if (Keyboard.pressed(Keys.DASHBOARD)) {
            context.actionHandler().toggleDashboard();
        } else if (Keyboard.pressed(Keys.PIP_VIEW)) {
            context.actionHandler().togglePipVisible();
        } else if (Keyboard.pressed(Keys.EDITOR)) {
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
            var grid = (GridPane) pane.getChildren().getFirst(); // TODO improve
            // add default entries:
            if (PY_USE_AUTOPILOT.get()) {
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
        var font = context.theme().font("font.monospaced", Math.max(6, 14 * layout.scaling()));
        var pane = currentHelpInfo().createPane(bgColor, font);
        helpInfoPopUp.setTranslateX(10 * layout.scaling());
        helpInfoPopUp.setTranslateY(30 * layout.scaling());
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