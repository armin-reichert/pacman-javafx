/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.page;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModels;
import de.amr.games.pacman.ui.fx.GameScene;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.util.*;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.border;

/**
 * @author Armin Reichert
 */
public class GamePage extends CanvasLayoutPane implements Page {

    protected final GameSceneContext sceneContext;
    protected final FlashMessageView flashMessageLayer = new FlashMessageView();
    protected final Pane popupLayer = new Pane();
    protected final FadingPane helpInfoPopUp = new FadingPane();
    private BorderPane helpButton;
    private TextFlow signature;
    private Transition signatureAnimation;

    public GamePage(GameSceneContext sceneContext, double width, double height) {
        this.sceneContext = sceneContext;

        createHelpButton();
        createSignature();
        createDebugInfoBindings();

        popupLayer.getChildren().addAll(helpButton, signature, helpInfoPopUp);
        layersContainer.getChildren().addAll(popupLayer, flashMessageLayer);

        flashMessageLayer.setMouseTransparent(true);

        layersContainer.setOnKeyPressed(this::handle);

        getCanvas().setOnMouseMoved(e -> {
            double factor = getScaling() * TS;
            Vector2i tile = new Vector2i((int)(e.getX() / factor), (int)(e.getY() / factor));
            Logger.info("tile={}", tile);
        });

        setSize(width, height);
    }

    @Override
    public Pane rootPane() {
        return layersContainer;
    }

    protected void rescale(double newScaling, boolean always) {
        super.rescale(newScaling, always);
        resizeRegion(popupLayer, canvasContainer.getWidth(), canvasContainer.getHeight());
        sceneContext.currentGameScene().ifPresent(gameScene -> {
            if (gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.setScaling(getScaling());
            }
        });
    }

    public void onGameSceneChanged(GameScene newGameScene) {
        if (newGameScene == sceneContext.sceneConfig().get("intro")) {
            signatureAnimation.play();
        } else {
            signatureAnimation.stop();
            signature.setOpacity(0);
        }
        updateHelpButton();
        rescale(getScaling(), true);
        if (newGameScene instanceof GameScene2D scene2D) {
            scene2D.setCanvas(getCanvas());
            scene2D.clearCanvas();
        }
    }

    private void createDebugInfoBindings() {
        layersContainer.borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_SHOW_DEBUG_INFO.get() && isCurrentGameScene2D() ? border(Color.RED, 3) : null,
            PY_SHOW_DEBUG_INFO, sceneContext.gameSceneProperty()
        ));
        canvasLayer.borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_SHOW_DEBUG_INFO.get() && isCurrentGameScene2D() ? border(Color.YELLOW, 3) : null,
            PY_SHOW_DEBUG_INFO, sceneContext.gameSceneProperty()
        ));
        popupLayer.borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_SHOW_DEBUG_INFO.get() && isCurrentGameScene2D() ? border(Color.GREENYELLOW, 3) : null,
            PY_SHOW_DEBUG_INFO, sceneContext.gameSceneProperty()
        ));
        popupLayer.mouseTransparentProperty().bind(PY_SHOW_DEBUG_INFO);
    }

    protected boolean isCurrentGameScene2D() {
        return true;
    }

    public FlashMessageView flashMessageView() {
        return flashMessageLayer;
    }

    public void render() {
        sceneContext.currentGameScene().ifPresent(gameScene -> {
            if (gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.draw();
            }
        });
        flashMessageLayer.update();
        popupLayer.setVisible(true);
    }

    protected void handleKeyboardInput() {
        var actionHandler = sceneContext.actionHandler();
        if (Keyboard.pressed(KEY_AUTOPILOT)) {
            actionHandler.toggleAutopilot();
        } else if (Keyboard.pressed(KEY_BOOT)) {
            if (sceneContext.gameState() != GameState.BOOT) {
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
            if (sceneContext.gameState() != GameState.BOOT && sceneContext.gameState() != GameState.INTRO) {
                actionHandler.restartIntro();
            }
        } else if (Keyboard.pressed(KEY_TEST_LEVELS)) {
            actionHandler.startLevelTestMode();
        } else {
            sceneContext.currentGameScene().ifPresent(GameScene::handleKeyboardInput);
        }
    }

    // Signature stuff

    private void createSignature() {
        var remake = new Text("Remake (2023) by ");
        remake.setFill(Color.WHEAT);
        remake.fontProperty().bind(Bindings.createObjectBinding(
            () -> Font.font("Helvetica", Math.floor(10 * getScaling())), scalingPy));

        var author = new Text("Armin Reichert");
        author.setFill(Color.WHEAT);
        author.fontProperty().bind(Bindings.createObjectBinding(
            () -> sceneContext.theme().font("font.handwriting", Math.floor(11 * getScaling())), scalingPy));

        signature = new TextFlow(remake, author);

        signature.translateXProperty().bind(Bindings.createDoubleBinding(
            () -> (canvasContainer.getWidth() - signature.getWidth()) * 0.5, canvasContainer.widthProperty()
        ));

        signature.translateYProperty().bind(Bindings.createDoubleBinding(
            () -> switch (sceneContext.game()) {
                case MS_PACMAN -> 40 * getScaling(); // TODO fixme
                case PACMAN -> 28 * getScaling(); // TODO fixme
            }, scalingPy
        ));

        var fadeIn = new FadeTransition(Duration.seconds(5), signature);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.seconds(3));

        var fadeOut = new FadeTransition(Duration.seconds(1), signature);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        signatureAnimation = new SequentialTransition(fadeIn, fadeOut);
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
        var image = sceneContext.theme().image(switch (sceneContext.game()) {
            case MS_PACMAN -> "mspacman.helpButton.icon";
            case PACMAN -> "pacman.helpButton.icon";
        });
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
        if (sceneContext.currentGameScene().isEmpty() || !isCurrentGameScene2D()) {
            return false;
        }
        var gameScene = sceneContext.currentGameScene().get();
        return gameScene != sceneContext.sceneConfig().get("boot");
    }

    private void handle(KeyEvent e) {
        handleKeyboardInput();
    }

    public class HelpInfo extends PageInfo {

        public void addLocalizedEntry(String lhsKey, String keyboardKey) {
            addRow(
                label(sceneContext.tt(lhsKey), Color.gray(0.9)),
                text("[" + keyboardKey + "]", Color.YELLOW)
            );
        }

        @Override
        public Pane createPane(Color backgroundColor, Font font) {
            var pane = super.createPane(backgroundColor, font);
            var grid = (GridPane) pane.getChildren().getFirst(); // TODO improve
            // add default entries:
            int nextFreeIndex = grid.getRowCount();
            if (PY_USE_AUTOPILOT.get()) {
                var autoPilotEntry = text(sceneContext.tt("help.autopilot_on"), Color.ORANGE);
                autoPilotEntry.setFont(font);
                GridPane.setColumnSpan(autoPilotEntry, 2);
                grid.add(autoPilotEntry, 0, nextFreeIndex);
                nextFreeIndex += 1;
            }
            if (sceneContext.gameController().isPacImmune()) {
                var immunityEntry = text(sceneContext.tt("help.immunity_on"), Color.ORANGE);
                immunityEntry.setFont(font);
                GridPane.setColumnSpan(immunityEntry, 2);
                grid.add(immunityEntry, 0, nextFreeIndex);
                nextFreeIndex += 1;
            }
            return pane;
        }
    }

    private HelpInfo currentHelpInfo() {
        HelpInfo helpInfo = new HelpInfo();
        switch (sceneContext.gameState()) {
            case INTRO -> addInfoForIntroScene(helpInfo);
            case CREDIT -> addInfoForCreditScene(helpInfo);
            case READY, HUNTING, PACMAN_DYING, GHOST_DYING -> {
                if (sceneContext.gameLevel().isPresent()) {
                    if (sceneContext.gameLevel().get().isDemoLevel()) {
                        addInfoForDemoLevel(helpInfo);
                    } else {
                        addInfoForPlayScene(helpInfo);
                    }
                }
            }
            default -> addInfoForQuittingScene(helpInfo);
        }
        return helpInfo;
    }

    private void showHelpInfoPopUp() {
        var bgColor = sceneContext.game() == GameModels.MS_PACMAN
            ? Color.rgb(255, 0, 0, 0.8)
            : Color.rgb(33, 33, 255, 0.8);
        var font = sceneContext.theme().font("font.monospaced", Math.max(6, 14 * getScaling()));
        var pane = currentHelpInfo().createPane(bgColor, font);
        helpInfoPopUp.setTranslateX(10 * getScaling());
        helpInfoPopUp.setTranslateY(30 * getScaling());
        helpInfoPopUp.setContent(pane);
        helpInfoPopUp.show(Duration.seconds(1.5));
    }

    private void addInfoForIntroScene(HelpInfo info) {
        if (sceneContext.gameController().hasCredit()) {
            info.addLocalizedEntry("help.start_game", "1");
        }
        info.addLocalizedEntry("help.add_credit", "5");
        info.addLocalizedEntry(sceneContext.game() == GameModels.MS_PACMAN ? "help.pacman" : "help.ms_pacman", "V");
    }

    private void addInfoForQuittingScene(HelpInfo info) {
        info.addLocalizedEntry("help.show_intro", "Q");
    }

    private void addInfoForCreditScene(HelpInfo info) {
        if (sceneContext.gameController().hasCredit()) {
            info.addLocalizedEntry("help.start_game", "1");
        }
        info.addLocalizedEntry("help.add_credit", "5");
        info.addLocalizedEntry("help.show_intro", "Q");
    }

    private void addInfoForPlayScene(HelpInfo info) {
        info.addLocalizedEntry("help.move_left", sceneContext.tt("help.cursor_left"));
        info.addLocalizedEntry("help.move_right", sceneContext.tt("help.cursor_right"));
        info.addLocalizedEntry("help.move_up", sceneContext.tt("help.cursor_up"));
        info.addLocalizedEntry("help.move_down", sceneContext.tt("help.cursor_down"));
        info.addLocalizedEntry("help.show_intro", "Q");
    }

    private void addInfoForDemoLevel(HelpInfo info) {
        info.addLocalizedEntry("help.add_credit", "5");
        info.addLocalizedEntry("help.show_intro", "Q");
    }
}