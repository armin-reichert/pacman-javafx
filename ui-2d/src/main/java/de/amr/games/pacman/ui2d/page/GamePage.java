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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage extends CanvasLayoutPane implements Page {

    protected final GameSceneContext context;
    private final FlashMessageView flashMessageView = new FlashMessageView();
    private final Pane popupLayer = new Pane();
    private final FadingPane helpInfoPopUp = new FadingPane();
    private BorderPane helpButton;

    public GamePage(GameSceneContext context, double width, double height) {
        this.context = context;
        createHelpButton();
        createDebugInfoBindings();
        popupLayer.getChildren().addAll(helpButton, helpInfoPopUp);
        layersContainer.getChildren().addAll(popupLayer, flashMessageView);
        layersContainer.setOnKeyPressed(this::handle);
        setSize(width, height);
    }

    public void sign(Font unscaledFont, String... words) {
        var signature = new Signature(checkNotNull(words));
        //TODO can this be done by binding?
        scalingPy.addListener((py, ov, nv) -> signature.fontPy.set(Font.font(unscaledFont.getFamily(),
            scalingPy.get() * unscaledFont.getSize())));
        // keep centered over canvas container
        signature.translateXProperty().bind(Bindings.createDoubleBinding(
            () -> 0.5 * (canvasContainer.getWidth() - signature.getWidth()), canvasContainer.widthProperty()
        ));

        popupLayer.getChildren().stream()
            .filter(Signature.class::isInstance)
            .forEach(popupLayer.getChildren()::remove);
        popupLayer.getChildren().add(signature);
    }

    public Optional<Signature> signature() {
        return popupLayer.getChildren().stream()
            .filter(Signature.class::isInstance)
            .map(Signature.class::cast)
            .findFirst();
    }

    @Override
    public Pane rootPane() {
        return layersContainer;
    }

    @Override
    public void onSelected() {
        context.actionHandler().reboot();
        context.gameClock().start();
        Logger.info("Clock started, speed={} Hz", context.gameClock().getTargetFrameRate());
    }

    protected void rescale(double newScaling, boolean always) {
        super.rescale(newScaling, always);
        resizeRegion(popupLayer, canvasContainer.getWidth(), canvasContainer.getHeight());
    }

    public void onGameSceneChanged(GameScene newGameScene) {
        updateHelpButton();
        rescale(getScaling(), true);
        if (newGameScene instanceof GameScene2D scene2D) {
            scene2D.setCanvas(getCanvas());
            scene2D.clearCanvas();
            World world = context.game().world();
            if (world != null) {
                setUnscaledCanvasHeight(world.numRows() * TS);
                setUnscaledCanvasWidth(world.numCols() * TS);
            }
        }
    }

    private void createDebugInfoBindings() {
        layersContainer.borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_SHOW_DEBUG_INFO.get() && isCurrentGameScene2D() ? Ufx.border(Color.RED, 3) : null,
            PY_SHOW_DEBUG_INFO, context.gameSceneProperty()
        ));
        canvasLayer.borderProperty().bind(Bindings.createObjectBinding(
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

    protected void handleKeyboardInput() {
        var handler = context.actionHandler();
        if (Keyboard.pressed(KEY_AUTOPILOT)) {
            handler.toggleAutopilot();
        } else if (Keyboard.pressed(KEY_BOOT)) {
            if (context.gameState() != GameState.BOOT) {
                handler.reboot();
            }
        } else if (Keyboard.pressed(KEY_DEBUG_INFO)) {
            Ufx.toggle(PY_SHOW_DEBUG_INFO);
        } else if (Keyboard.pressed(KEY_FULLSCREEN)) {
            handler.setFullScreen(true);
        } else if (Keyboard.pressed(KEY_IMMUNITY)) {
            handler.toggleImmunity();
        } else if (Keyboard.pressed(KEY_SHOW_HELP)) {
            showHelpInfoPopUp();
        } else if (Keyboard.pressed(KEY_PAUSE)) {
            handler.togglePaused();
        } else if (Keyboard.pressed(KEYS_SINGLE_STEP)) {
            handler.doSimulationSteps(1);
        } else if (Keyboard.pressed(KEY_TEN_STEPS)) {
            handler.doSimulationSteps(10);
        } else if (Keyboard.pressed(KEY_SIMULATION_FASTER)) {
            handler.changeSimulationSpeed(5);
        } else if (Keyboard.pressed(KEY_SIMULATION_SLOWER)) {
            handler.changeSimulationSpeed(-5);
        } else if (Keyboard.pressed(KEY_SIMULATION_NORMAL)) {
            handler.resetSimulationSpeed();
        } else if (Keyboard.pressed(KEY_QUIT)) {
            handleQuit();
        } else if (Keyboard.pressed(KEY_TEST_LEVELS)) {
            handler.startLevelTestMode();
        } else {
            context.currentGameScene().ifPresent(GameScene::handleKeyboardInput);
        }
    }

    private void handleQuit() {
        var handler = context.actionHandler();
        switch (context.gameState()) {
            case BOOT -> {}
            case INTRO -> {
                handler.reboot();
                handler.selectPage("startPage");
            }
            default -> handler.restartIntro();
        }
    }

    // Help Info stuff

    private void createHelpButton() {
        helpButton = new BorderPane();
        helpButton.setCenter(new ImageView());
        helpButton.setCursor(Cursor.HAND);
        helpButton.setOnMouseClicked(e -> showHelpInfoPopUp());
        scalingPy.addListener((py, ov, nv) -> updateHelpButton());
        updateHelpButton();
    }

    protected void updateHelpButton() {
        ImageView imageView = (ImageView) helpButton.getCenter();
        String rk = context.game().variant().resourceKey();
        if (context.game().variant() == GameVariant.PACMAN_XXL) {
            rk = GameVariant.PACMAN.resourceKey();
        }
        var image = context.theme().image(rk + ".helpButton.icon");
        double size = Math.ceil(12 * getScaling());
        imageView.setImage(image);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        helpButton.setTranslateX(unscaledCanvasWidth * getScaling());
        helpButton.setTranslateY(10 * getScaling());
        helpButton.setVisible(isHelpButtonVisible());
        Logger.trace("Updated help icon, scaling: {}", getScaling());
    }

    protected boolean isHelpButtonVisible() {
        if (context.currentGameScene().isEmpty() || !isCurrentGameScene2D()) {
            return false;
        }
        var gameScene = context.currentGameScene().get();
        return gameScene != context.sceneConfig().get("boot");
    }

    private void handle(KeyEvent e) {
        handleKeyboardInput();
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
        var font = context.theme().font("font.monospaced", Math.max(6, 14 * getScaling()));
        var pane = currentHelpInfo().createPane(bgColor, font);
        helpInfoPopUp.setTranslateX(10 * getScaling());
        helpInfoPopUp.setTranslateY(30 * getScaling());
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