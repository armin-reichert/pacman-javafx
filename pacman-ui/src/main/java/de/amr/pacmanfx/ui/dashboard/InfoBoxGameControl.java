/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.PacManGames_GameActions;
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
        spinnerCredit            = addIntSpinner("Credit", 0, CoinMechanism.MAX_COINS, ui.theGameContext().theCoinMechanism().numCoinsProperty());
        choiceBoxInitialLives    = addChoiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = addButtonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = addButtonList("Cut Scenes Test", List.of("Start", "Quit"));
        cbAutopilot              = addCheckBox("Autopilot", ui.theGameContext().theGameController().propertyUsingAutopilot());
        cbImmunity               = addCheckBox("Pac-Man Immune", ui.theGameContext().theGameController().propertyImmunity());

        setAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_START], PacManGames_GameActions.ACTION_TEST_CUT_SCENES);
        setAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT], PacManGames_GameActions.ACTION_RESTART_INTRO);
        setAction(buttonGroupLevelActions[GAME_LEVEL_START], PacManGames_GameActions.ACTION_ARCADE_START_GAME); //TODO Tengen?
        setAction(buttonGroupLevelActions[GAME_LEVEL_QUIT], PacManGames_GameActions.ACTION_RESTART_INTRO);
        setAction(buttonGroupLevelActions[GAME_LEVEL_NEXT], PacManGames_GameActions.ACTION_CHEAT_ENTER_NEXT_LEVEL);
        setAction(choiceBoxInitialLives, () -> ui.theGameContext().theGame().setInitialLifeCount(choiceBoxInitialLives.getValue()));
    }

    @Override
    public void update() {
        super.update();

        //TODO use binding
        choiceBoxInitialLives.setValue(ui.theGameContext().theGame().initialLifeCount());

        spinnerCredit.setDisable(!(isOneOf(ui.theGameContext().theGameState(), GameState.INTRO, GameState.SETTING_OPTIONS_FOR_START)));
        choiceBoxInitialLives.setDisable(ui.theGameContext().theGameState() != GameState.INTRO);

        buttonGroupLevelActions[GAME_LEVEL_START].setDisable(isBooting() || !canStartLevel());
        buttonGroupLevelActions[GAME_LEVEL_QUIT].setDisable(isBooting() || ui.theGameContext().optGameLevel().isEmpty());
        buttonGroupLevelActions[GAME_LEVEL_NEXT].setDisable(isBooting() || !canEnterNextLevel());

        buttonGroupCutScenesTest[CUT_SCENES_TEST_START].setDisable(isBooting() || ui.theGameContext().theGameState() != GameState.INTRO);
        buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT].setDisable(isBooting() || ui.theGameContext().theGameState() != GameState.TESTING_CUT_SCENES);

        cbAutopilot.setDisable(isBooting());
        cbImmunity.setDisable(isBooting());
    }

    private boolean isBooting() {
        return ui.theGameContext().theGameState() == GameState.BOOT;
    }

    private boolean canStartLevel() {
        return ui.theGameContext().theGame().canStartNewGame() && isOneOf(ui.theGameContext().theGameState(), GameState.INTRO, GameState.SETTING_OPTIONS_FOR_START);
    }

    private boolean canEnterNextLevel() {
        return ui.theGameContext().theGame().isPlaying() && isOneOf(ui.theGameContext().theGameState(), GameState.HUNTING);
    }
}