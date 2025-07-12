/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.ui.PacManGames_GameActions;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_IMMUNITY;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_USING_AUTOPILOT;

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

    public void init() {
        spinnerCredit            = addIntSpinner("Credit", 0, CoinMechanism.MAX_COINS, theGameContext().theCoinMechanism().numCoinsProperty());
        choiceBoxInitialLives    = addChoiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = addButtonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = addButtonList("Cut Scenes Test", List.of("Start", "Quit"));
        cbAutopilot              = addCheckBox("Autopilot", PY_USING_AUTOPILOT);
        cbImmunity               = addCheckBox("Pac-Man Immune", PY_IMMUNITY);

        setAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_START], PacManGames_GameActions.ACTION_TEST_CUT_SCENES);
        setAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT], PacManGames_GameActions.ACTION_RESTART_INTRO);
        setAction(buttonGroupLevelActions[GAME_LEVEL_START], PacManGames_GameActions.ACTION_ARCADE_START_GAME); //TODO Tengen?
        setAction(buttonGroupLevelActions[GAME_LEVEL_QUIT], PacManGames_GameActions.ACTION_RESTART_INTRO);
        setAction(buttonGroupLevelActions[GAME_LEVEL_NEXT], PacManGames_GameActions.ACTION_CHEAT_ENTER_NEXT_LEVEL);
        setAction(choiceBoxInitialLives, () -> theGameContext().theGame().setInitialLifeCount(choiceBoxInitialLives.getValue()));
    }

    @Override
    public void update() {
        super.update();

        //TODO use binding
        choiceBoxInitialLives.setValue(theGameContext().theGame().initialLifeCount());

        spinnerCredit.setDisable(!(isOneOf(theGameContext().theGameState(), GameState.INTRO, GameState.SETTING_OPTIONS)));
        choiceBoxInitialLives.setDisable(theGameContext().theGameState() != GameState.INTRO);

        buttonGroupLevelActions[GAME_LEVEL_START].setDisable(isBooting() || !canStartLevel());
        buttonGroupLevelActions[GAME_LEVEL_QUIT].setDisable(isBooting() || theGameContext().optGameLevel().isEmpty());
        buttonGroupLevelActions[GAME_LEVEL_NEXT].setDisable(isBooting() || !canEnterNextLevel());

        buttonGroupCutScenesTest[CUT_SCENES_TEST_START].setDisable(isBooting() || theGameContext().theGameState() != GameState.INTRO);
        buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT].setDisable(isBooting() || theGameContext().theGameState() != GameState.TESTING_CUT_SCENES);

        cbAutopilot.setDisable(isBooting());
        cbImmunity.setDisable(isBooting());
    }

    private boolean isBooting() {
        return theGameContext().theGameState() == GameState.BOOT;
    }

    private boolean canStartLevel() {
        return theGameContext().theGame().canStartNewGame() && isOneOf(theGameContext().theGameState(), GameState.INTRO, GameState.SETTING_OPTIONS);
    }

    private boolean canEnterNextLevel() {
        return theGameContext().theGame().isPlaying() && isOneOf(theGameContext().theGameState(), GameState.HUNTING);
    }
}