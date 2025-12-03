/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl.StateName;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.action.TestActions;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

import java.util.List;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_ENTER_NEXT_LEVEL;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_RESTART_INTRO;

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

    public InfoBoxGameControl(GameUI ui) {
        super(ui);
    }

    @Override
    public void init(GameUI ui) {
        spinnerCredit            = addIntSpinner("Credit", 0, CoinMechanism.MAX_COINS, THE_GAME_BOX.numCoinsProperty());
        choiceBoxInitialLives    = addChoiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = addButtonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = addButtonList("Cut Scenes Test", List.of("Start", "Quit"));

        setAction(choiceBoxInitialLives, () -> ui.context().currentGame().setInitialLifeCount(choiceBoxInitialLives.getValue()));

        //setAction(buttonGroupLevelActions[GAME_LEVEL_START], ArcadeActions.ACTION_START_GAME); //TODO FIXME!
        setAction(buttonGroupLevelActions[GAME_LEVEL_QUIT], ACTION_RESTART_INTRO);
        setAction(buttonGroupLevelActions[GAME_LEVEL_NEXT], ACTION_ENTER_NEXT_LEVEL);

        setAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_START], TestActions.ACTION_CUT_SCENES_TEST);
        setAction(buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT], ACTION_RESTART_INTRO);
    }

    @Override
    public void update() {
        super.update();

        final Game game = ui.context().currentGame();
        final StateMachine.State<?> state = game.control().state();

        //TODO use binding
        choiceBoxInitialLives.setValue(game.initialLifeCount());

        boolean creditDisabled = !state.matches(StateName.INTRO, StateName.SETTING_OPTIONS_FOR_START);
        spinnerCredit.setDisable(creditDisabled);
        choiceBoxInitialLives.setDisable(!state.matches(StateName.INTRO));

        boolean booting = isBooting(state);
        buttonGroupLevelActions[GAME_LEVEL_START].setDisable(booting || !canStartLevel(state));
        buttonGroupLevelActions[GAME_LEVEL_QUIT] .setDisable(booting || ui.context().currentGame().optGameLevel().isEmpty());
        buttonGroupLevelActions[GAME_LEVEL_NEXT] .setDisable(booting || !canEnterNextLevel(state));

        buttonGroupCutScenesTest[CUT_SCENES_TEST_START].setDisable(booting || !state.matches(StateName.INTRO));
        buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT] .setDisable(booting || !(state instanceof CutScenesTestState));
    }

    private boolean isBooting(StateMachine.State<?> state) {
        return state.matches(StateName.BOOT);
    }

    private boolean canStartLevel(StateMachine.State<?> state) {
        return ui.context().currentGame().canStartNewGame()
            && state.matches(StateName.INTRO, StateName.SETTING_OPTIONS_FOR_START);
    }

    private boolean canEnterNextLevel(StateMachine.State<?> state) {
        return ui.context().currentGame().isPlaying() && state.matches(StateName.HUNTING);
    }
}