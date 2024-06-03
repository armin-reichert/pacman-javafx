/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import de.amr.games.pacman.ui2d.util.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage implements Page {

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant");

    protected final GameSceneContext context;

    private final CanvasLayoutPane layout;
    private final Pane popupLayer = new Pane();
    private final FlashMessageView flashMessageView = new FlashMessageView();
    private final FadingPane helpInfoPopUp = new FadingPane();
    private final ImageView helpButtonIcon = new ImageView();
    private final Signature signature = new Signature();

    public GamePage(GameSceneContext context, double width, double height) {
        this.context = checkNotNull(context);

        layout = new CanvasLayoutPane();
        layout.canvasDecoratedPy.bind(PY_CANVAS_DECORATION);

        createHelpButton();
        createDebugInfoBindings();

        // keep popup layer size same as canvas container
        var canvasContainer = layout.getCanvasContainer();
        popupLayer.minHeightProperty().bind(canvasContainer.minHeightProperty());
        popupLayer.maxHeightProperty().bind(canvasContainer.maxHeightProperty());
        popupLayer.prefHeightProperty().bind(canvasContainer.prefHeightProperty());
        popupLayer.minWidthProperty().bind(canvasContainer.minWidthProperty());
        popupLayer.maxWidthProperty().bind(canvasContainer.maxWidthProperty());
        popupLayer.prefWidthProperty().bind(canvasContainer.prefWidthProperty());

        popupLayer.getChildren().addAll(helpButtonIcon, helpInfoPopUp, signature);

        layout.getChildren().addAll(popupLayer, flashMessageView);
        setSize(width, height);
    }

    public void configureSignature(Font font, String... words) {
        signature.setWords(words);

        signature.fontPy.bind(Bindings.createObjectBinding(
            () -> Font.font(font.getFamily(), layout.getScaling() * font.getSize()),
            layout.scalingPy
        ));
        // keep centered over canvas container
        signature.translateXProperty().bind(Bindings.createDoubleBinding(
            () -> 0.5 * (layout.getCanvasContainer().getWidth() - signature.getWidth()),
            layout.scalingPy, layout.getCanvasContainer().widthProperty()
        ));
        // keep at vertical position over intro scene
        signature.translateYProperty().bind(Bindings.createDoubleBinding(
            () -> layout.getScaling() * 30,
            layout.scalingPy, layout.getCanvasContainer().heightProperty()
        ));
    }

    @Override
    public Pane rootPane() {
        return layout;
    }

    @Override
    public void onSelected() {
        context.actionHandler().reboot();
        context.gameClock().start();
        Logger.info("Clock started, speed={} Hz", context.gameClock().getTargetFrameRate());
    }

    @Override
    public void setSize(double width, double height) {
        layout.setSize(width, height);
    }

    public CanvasLayoutPane layout() {
        return layout;
    }

    public Signature signature() {
        return signature;
    }

    public void onGameSceneChanged(GameScene newGameScene) {
        World world = context.game().world();
        if (world != null) {
            layout.setUnscaledCanvasHeight(world.numRows() * TS);
            layout.setUnscaledCanvasWidth(world.numCols() * TS);
        }
        updateHelpButton();
        layout.doLayout(layout.getScaling(), true); //TODO check this
    }

    private void createDebugInfoBindings() {
        layout.borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_SHOW_DEBUG_INFO.get() && isCurrentGameScene2D() ? Ufx.border(Color.RED, 3) : null,
            PY_SHOW_DEBUG_INFO, context.gameSceneProperty()
        ));
        layout.getCanvasLayer().borderProperty().bind(Bindings.createObjectBinding(
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
        return true;
    }

    public FlashMessageView flashMessageView() {
        return flashMessageView;
    }

    public void render() {
        context.currentGameScene().ifPresent(gameScene -> {
            if (gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.draw();
            }
        });
        flashMessageView.update();
        popupLayer.setVisible(true);
    }

    @Override
    public void handleKeyboardInput() {
        var actionHandler = context.actionHandler();
        if (Keyboard.pressed(KEY_AUTOPILOT)) {
            actionHandler.toggleAutopilot();
        } else if (Keyboard.pressed(KEY_BOOT)) {
            if (context.gameState() != GameState.BOOT) {
                actionHandler.reboot();
            }
        } else if (Keyboard.pressed(KEY_DEBUG_INFO)) {
            Ufx.toggle(PY_SHOW_DEBUG_INFO);
        } else if (Keyboard.pressed(KEY_FULLSCREEN)) {
            actionHandler.setFullScreen(true);
        } else if (Keyboard.pressed(KEY_IMMUNITY)) {
            actionHandler.toggleImmunity();
        } else if (Keyboard.pressed(KEY_SHOW_HELP)) {
            showHelpInfoPopUp();
        } else if (Keyboard.pressed(KEY_PAUSE)) {
            actionHandler.togglePaused();
        } else if (Keyboard.pressed(KEYS_SINGLE_STEP)) {
            actionHandler.doSimulationSteps(1);
        } else if (Keyboard.pressed(KEY_TEN_STEPS)) {
            actionHandler.doSimulationSteps(10);
        } else if (Keyboard.pressed(KEY_SIMULATION_FASTER)) {
            actionHandler.changeSimulationSpeed(5);
        } else if (Keyboard.pressed(KEY_SIMULATION_SLOWER)) {
            actionHandler.changeSimulationSpeed(-5);
        } else if (Keyboard.pressed(KEY_SIMULATION_NORMAL)) {
            actionHandler.resetSimulationSpeed();
        } else if (Keyboard.pressed(KEY_QUIT)) {
            context.soundHandler().stopVoice();
            context.soundHandler().stopAllSounds();
            actionHandler.selectPage(START_PAGE);
        } else if (Keyboard.pressed(KEY_TEST_LEVELS)) {
            actionHandler.startLevelTestMode();
        } else {
            context.currentGameScene().ifPresent(GameScene::handleKeyboardInput);
        }
    }

    // Help Info stuff

    //TODO this is all too complicated for such a simple feature
    private void createHelpButton() {
        helpButtonIcon.setCursor(Cursor.HAND);
        helpButtonIcon.setOnMouseClicked(e -> showHelpInfoPopUp());
        helpButtonIcon.translateXProperty().bind(layout.unscaledCanvasWidthPy.multiply(layout.scalingPy));
        helpButtonIcon.translateYProperty().bind(layout.scalingPy.multiply(10));
        layout.scalingPy.addListener((py, ov, nv) -> updateHelpButton());
        updateHelpButton();
    }

    protected void updateHelpButton() {
        boolean visible = context.currentGameScene().isPresent()
            && isCurrentGameScene2D()
            && !context.isCurrentGameScene(BOOT_SCENE);
        helpButtonIcon.setVisible(visible);
        if (visible) {
            helpButtonIcon.setImage(helpButtonImage());
            helpButtonIcon.setFitWidth(Math.ceil(12 * layout.getScaling()));
            helpButtonIcon.setFitHeight(helpButtonIcon.getFitWidth());
        }
    }

    private Image helpButtonImage() {
        String rk = context.game().variant().resourceKey();
        if (context.game().variant() == GameVariant.PACMAN_XXL) {
            rk = GameVariant.PACMAN.resourceKey();
        }
        return context.theme().image(rk + ".helpButton.icon");
    }

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
        var font = context.theme().font("font.monospaced", Math.max(6, 14 * layout.getScaling()));
        var pane = currentHelpInfo().createPane(bgColor, font);
        helpInfoPopUp.setTranslateX(10 * layout.getScaling());
        helpInfoPopUp.setTranslateY(30 * layout.getScaling());
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