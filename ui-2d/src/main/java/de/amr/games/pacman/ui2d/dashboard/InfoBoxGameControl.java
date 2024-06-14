/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_AUTOPILOT;

/**
 * Game related settings.
 *
 * @author Armin Reichert
 */
public class InfoBoxGameControl extends InfoBox {

    private static final int GAME_LEVEL_START = 0;
    private static final int GAME_LEVEL_QUIT = 1;
    private static final int GAME_LEVEL_NEXT = 2;

    private static final int INTERMISSION_TEST_START = 0;
    private static final int INTERMISSION_TEST_QUIT = 1;

    private Spinner<Integer> spinnerCredit;
    private ComboBox<GameVariant> comboGameVariant;
    private ComboBox<Integer> comboInitialLives;
    private Button[] buttonsLevelActions;
    private Button[] buttonsIntermissionTest;
    private CheckBox cbAutopilot;
    private CheckBox cbImmunity;

    public void init(GameContext context) {
        this.context = context;

        spinnerCredit = integerSpinner("Credit", 0, GameController.MAX_CREDIT, 0);
        comboGameVariant = comboBox("Variant", GameController.it().supportedVariants().toArray(GameVariant[]::new));
        comboInitialLives = comboBox("Initial Lives", new Integer[]{3, 5});
        buttonsLevelActions = buttonList("Game Level", "Start", "Quit", "Next");
        buttonsIntermissionTest = buttonList("Cut Scenes Test", "Start", "Quit");
        cbAutopilot = checkBox("Autopilot");
        cbImmunity = checkBox("Pac-Man Immune");

        comboGameVariant.setOnAction(e -> {
            var selectedVariant = comboGameVariant.getValue();
            if (selectedVariant != context.game().variant()) {
                context.gameController().selectGameVariant(selectedVariant);
                context.gameController().restart(GameState.BOOT);
            }
        });
        buttonsIntermissionTest[INTERMISSION_TEST_START].setOnAction(e -> context.actionHandler().startCutscenesTest());
        buttonsIntermissionTest[INTERMISSION_TEST_QUIT].setOnAction(e -> context.actionHandler().restartIntro());
        comboInitialLives.setOnAction(e -> context.game().setInitialLives(comboInitialLives.getValue()));
        buttonsLevelActions[GAME_LEVEL_START].setOnAction(e -> context.actionHandler().startGame());
        buttonsLevelActions[GAME_LEVEL_QUIT].setOnAction(e -> context.actionHandler().restartIntro());
        buttonsLevelActions[GAME_LEVEL_NEXT].setOnAction(e -> context.actionHandler().cheatEnterNextLevel());
        spinnerCredit.valueProperty().addListener((py, ov, nv) -> context.gameController().setCredit(nv));
        spinnerCredit.getValueFactory().setValue(context.gameController().credit());
        cbAutopilot.setOnAction(e -> context.actionHandler().toggleAutopilot());
        cbImmunity.setOnAction(e -> context.actionHandler().toggleImmunity());
    }

    @Override
    public void update() {
        super.update();

        comboGameVariant.setValue(context.game().variant());
        comboGameVariant.setDisable(context.gameState() != GameState.INTRO);
        comboInitialLives.setValue(context.game().initialLives());
        cbAutopilot.setSelected(PY_AUTOPILOT.get());
        cbImmunity.setSelected(context.gameController().isPacImmune());
        buttonsLevelActions[GAME_LEVEL_START].setDisable(!canStartLevel());
        buttonsLevelActions[GAME_LEVEL_QUIT].setDisable(context.game().level().isEmpty());
        buttonsLevelActions[GAME_LEVEL_NEXT].setDisable(!canEnterNextLevel());
        buttonsIntermissionTest[INTERMISSION_TEST_START].setDisable(
            context.gameState() == GameState.INTERMISSION_TEST || context.gameState() != GameState.INTRO);
        buttonsIntermissionTest[INTERMISSION_TEST_QUIT].setDisable(context.gameState() != GameState.INTERMISSION_TEST);
        spinnerCredit.getValueFactory().setValue(context.gameController().credit());
    }

    private boolean canStartLevel() {
        return context.gameController().hasCredit()
            && Globals.oneOf(context.gameState(), GameState.INTRO, GameState.CREDIT);
    }

    private boolean canEnterNextLevel() {
        return context.game().isPlaying()
            && Globals.oneOf(context.gameState(), GameState.HUNTING);
    }
}