/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.controller.teststates.CutScenesTestState;
import de.amr.pacmanfx.ui.CommonGameActions;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

import java.util.List;

import static de.amr.pacmanfx.Validations.isOneOf;

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
    private ChoiceBox<Integer> choiceBoxInitialLives;
    private Button[] buttonGroupLevelActions;
    private Button[] buttonGroupCutScenesTest;
    private CheckBox cbAutopilot;
    private CheckBox cbImmunity;

    public InfoBoxGameControl(GameUI ui) {
        super(ui);
    }

    @Override
    public void init(GameUI ui) {
        spinnerCredit            = addIntSpinner("Credit", 0, CoinMechanism.MAX_COINS, ui.gameContext().coinMechanism().numCoinsProperty());
        choiceBoxInitialLives    = addChoiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = addButtonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = addButtonList("Cut Scenes Test", List.of("Start", "Quit"));
        cbAutopilot              = addCheckBox("Autopilot", ui.gameContext().gameController().propertyUsingAutopilot());
        cbImmunity               = addCheckBox("Pac-Man Immune", ui.gameContext().gameController().propertyImmunity());

        setAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_START], CommonGameActions.ACTION_TEST_CUT_SCENES);
        setAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT], CommonGameActions.ACTION_RESTART_INTRO);
        setAction(buttonGroupLevelActions[GAME_LEVEL_START], CommonGameActions.ACTION_ARCADE_START_GAME); //TODO Tengen?
        setAction(buttonGroupLevelActions[GAME_LEVEL_QUIT], CommonGameActions.ACTION_RESTART_INTRO);
        setAction(buttonGroupLevelActions[GAME_LEVEL_NEXT], CommonGameActions.ACTION_CHEAT_ENTER_NEXT_LEVEL);
        setAction(choiceBoxInitialLives, () -> ui.gameContext().game().setInitialLifeCount(choiceBoxInitialLives.getValue()));
    }

    @Override
    public void update() {
        super.update();

        //TODO use binding
        choiceBoxInitialLives.setValue(ui.gameContext().game().initialLifeCount());

        GameState state = ui.gameContext().gameState();

        spinnerCredit.setDisable(!(isOneOf(state, GamePlayState.INTRO, GamePlayState.SETTING_OPTIONS_FOR_START)));
        choiceBoxInitialLives.setDisable(state != GamePlayState.INTRO);

        buttonGroupLevelActions[GAME_LEVEL_START].setDisable(isBooting() || !canStartLevel());
        buttonGroupLevelActions[GAME_LEVEL_QUIT].setDisable(isBooting() || ui.gameContext().optGameLevel().isEmpty());
        buttonGroupLevelActions[GAME_LEVEL_NEXT].setDisable(isBooting() || !canEnterNextLevel());

        buttonGroupCutScenesTest[CUT_SCENES_TEST_START].setDisable(isBooting() || state != GamePlayState.INTRO);
        buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT].setDisable(isBooting() || !state.is(CutScenesTestState.class));

        cbAutopilot.setDisable(isBooting());
        cbImmunity.setDisable(isBooting());
    }

    private boolean isBooting() {
        return ui.gameContext().gameState() == GamePlayState.BOOT;
    }

    private boolean canStartLevel() {
        return ui.gameContext().game().canStartNewGame() && isOneOf(ui.gameContext().gameState(), GamePlayState.INTRO, GamePlayState.SETTING_OPTIONS_FOR_START);
    }

    private boolean canEnterNextLevel() {
        return ui.gameContext().game().isPlaying() && isOneOf(ui.gameContext().gameState(), GamePlayState.HUNTING);
    }
}