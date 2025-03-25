/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.GameActions2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.oneOf;
import static de.amr.games.pacman.ui.UIGlobals.THE_GAME_CONTEXT;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_AUTOPILOT;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_IMMUNITY;

/**
 * Game related settings.
 *
 * @author Armin Reichert
 */
public class InfoBoxGameControl extends InfoBox {

    private static final int GAME_LEVEL_START = 0;
    private static final int GAME_LEVEL_QUIT = 1;
    private static final int GAME_LEVEL_NEXT = 2;

    private static final int CUT_SCENES_TEST_START = 0;
    private static final int CUT_SCENES_TEST_QUIT = 1;

    private Spinner<Integer> spinnerCredit;
    private ComboBox<GameVariant> comboGameVariant;
    private ComboBox<Integer> comboInitialLives;
    private Button[] bgLevelActions;
    private Button[] bgCutScenesTest;
    private CheckBox cbAutopilot;
    private CheckBox cbImmunity;

    public void init() {
        spinnerCredit      = addIntSpinner("Credit", 0, GameController.MAX_COINS, 0);
        comboGameVariant   = addComboBox("Variant", GameVariant.values());
        comboInitialLives  = addComboBox("Initial Lives", new Integer[] {3, 5});
        bgLevelActions     = addButtonList("Game Level", "Start", "Quit", "Next");
        bgCutScenesTest    = addButtonList("Cut Scenes Test", "Start", "Quit");
        cbAutopilot        = addCheckBox("Autopilot");
        cbImmunity         = addCheckBox("Pac-Man Immune");

        spinnerCredit.valueProperty().addListener((py, ov, number) -> THE_GAME_CONTROLLER.credit = number);

        comboGameVariant.setOnAction(e -> {
            if (comboGameVariant.getValue() != THE_GAME_CONTROLLER.selectedGameVariant()) {
                THE_GAME_CONTROLLER.selectGameVariant(comboGameVariant.getValue());
                THE_GAME_CONTROLLER.restart(GameState.BOOT);
            }
        });

        setAction(bgCutScenesTest[CUT_SCENES_TEST_START], GameActions2D.TEST_CUT_SCENES::execute);
        setAction(bgCutScenesTest[CUT_SCENES_TEST_QUIT], GameActions2D.RESTART_INTRO::execute);
        setAction(bgLevelActions[GAME_LEVEL_START], GameActions2D.START_GAME::execute);
        setAction(bgLevelActions[GAME_LEVEL_QUIT], GameActions2D.RESTART_INTRO::execute);
        setAction(bgLevelActions[GAME_LEVEL_NEXT], GameActions2D.CHEAT_NEXT_LEVEL::execute);
        setAction(comboInitialLives, () -> THE_GAME_CONTROLLER.game().setInitialLives(comboInitialLives.getValue()));

        setEditor(cbAutopilot, PY_AUTOPILOT);
        setEditor(cbImmunity, PY_IMMUNITY);
    }

    @Override
    public void update() {
        super.update();

        GameModel game = THE_GAME_CONTROLLER.game();
        GameState state = THE_GAME_CONTROLLER.state();

        spinnerCredit.getValueFactory().setValue(THE_GAME_CONTROLLER.credit);
        comboGameVariant.setValue(THE_GAME_CONTROLLER.selectedGameVariant());
        comboInitialLives.setValue(game.initialLives());

        spinnerCredit.setDisable(!(oneOf(state, GameState.INTRO, GameState.SETTING_OPTIONS)));
        comboGameVariant.setDisable(state != GameState.INTRO);
        comboInitialLives.setDisable(state != GameState.INTRO);

        bgLevelActions[GAME_LEVEL_START].setDisable(isBooting() || !canStartLevel());
        bgLevelActions[GAME_LEVEL_QUIT].setDisable(isBooting() || game.level().isEmpty());
        bgLevelActions[GAME_LEVEL_NEXT].setDisable(isBooting() || !canEnterNextLevel());

        bgCutScenesTest[CUT_SCENES_TEST_START].setDisable(isBooting() || state != GameState.INTRO);
        bgCutScenesTest[CUT_SCENES_TEST_QUIT].setDisable(isBooting() || state != GameState.TESTING_CUT_SCENES);

        cbAutopilot.setDisable(isBooting());
        cbImmunity.setDisable(isBooting());
    }

    private boolean isBooting() {
        return THE_GAME_CONTROLLER.state() == GameState.BOOT;
    }

    private boolean canStartLevel() {
        return THE_GAME_CONTROLLER.game().canStartNewGame() && oneOf(THE_GAME_CONTROLLER.state(), GameState.INTRO, GameState.SETTING_OPTIONS);
    }

    private boolean canEnterNextLevel() {
        return THE_GAME_CONTROLLER.game().isPlaying() && oneOf(THE_GAME_CONTROLLER.state(), GameState.HUNTING);
    }
}