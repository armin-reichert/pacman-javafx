/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl.StateName;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.TestActions;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

import java.util.List;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_ENTER_NEXT_LEVEL;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_RESTART_INTRO;

public class DashboardSectionGameControl extends DashboardSection {

    private static final int GAME_LEVEL_START = 0;
    private static final int GAME_LEVEL_QUIT = 1;
    private static final int GAME_LEVEL_NEXT = 2;

    private static final int CUT_SCENES_TEST_START = 0;
    private static final int CUT_SCENES_TEST_QUIT = 1;

    private Spinner<Integer> spinnerCredit;
    private ChoiceBox<Integer> choiceBoxInitialLives;
    private Button[] buttonGroupLevelActions;
    private Button[] buttonGroupCutScenesTest;
    private CheckBox cbCollisionCheckedTwice;

    public DashboardSectionGameControl(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void init(GameUI ui) {
        final CoinMechanism coinMechanism = ui.gameContext().coinMechanism();
        spinnerCredit            = addIntSpinner("Credit", 0, coinMechanism.maxCoins(), coinMechanism.numCoinsProperty());
        choiceBoxInitialLives    = addChoiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = addButtonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = addButtonList("Cut Scenes Test", List.of("Start", "Quit"));
        cbCollisionCheckedTwice  = addCheckBox("Collision Check 2x");

        setAction(choiceBoxInitialLives, () -> ui.gameContext().currentGame().setInitialLifeCount(choiceBoxInitialLives.getValue()));

        //setAction(buttonGroupLevelActions[GAME_LEVEL_START], ArcadeActions.ACTION_START_GAME); //TODO FIXME!
        setAction(ui, buttonGroupLevelActions[GAME_LEVEL_QUIT], ACTION_RESTART_INTRO);
        setAction(ui, buttonGroupLevelActions[GAME_LEVEL_NEXT], ACTION_ENTER_NEXT_LEVEL);

        setAction(ui, buttonGroupCutScenesTest[CUT_SCENES_TEST_START], TestActions.ACTION_CUT_SCENES_TEST);
        setAction(ui, buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT], ACTION_RESTART_INTRO);

        cbCollisionCheckedTwice.setOnAction(_ -> {
            final AbstractGameModel game = ui.gameContext().currentGame();
            game.setCollisionDoubleChecked(cbCollisionCheckedTwice.isSelected());
        });
    }

    @Override
    public void update(GameUI ui) {
        super.update(ui);

        final AbstractGameModel game = ui.gameContext().currentGame();
        final StateMachine.State<?> state = game.control().state();

        //TODO use binding
        choiceBoxInitialLives.setValue(game.initialLifeCount());

        boolean creditDisabled = !state.nameMatches(StateName.INTRO.name(), StateName.SETTING_OPTIONS_FOR_START.name());
        spinnerCredit.setDisable(creditDisabled);
        choiceBoxInitialLives.setDisable(!state.nameMatches(StateName.INTRO.name()));

        boolean booting = isBooting(state);
        buttonGroupLevelActions[GAME_LEVEL_START].setDisable(booting || !canStartLevel(game, state));
        buttonGroupLevelActions[GAME_LEVEL_QUIT] .setDisable(booting || ui.gameContext().currentGame().optGameLevel().isEmpty());
        buttonGroupLevelActions[GAME_LEVEL_NEXT] .setDisable(booting || !canEnterNextLevel(game, state));

        buttonGroupCutScenesTest[CUT_SCENES_TEST_START].setDisable(booting || !state.nameMatches(StateName.INTRO.name()));
        buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT] .setDisable(booting || !(state instanceof CutScenesTestState));

        cbCollisionCheckedTwice.setSelected(game.isCollisionDoubleChecked());
    }

    private boolean isBooting(StateMachine.State<?> state) {
        return state.nameMatches(StateName.BOOT.name());
    }

    private boolean canStartLevel(Game game, StateMachine.State<?> state) {
        return game.canStartNewGame()
            && state.nameMatches(StateName.INTRO.name(), StateName.SETTING_OPTIONS_FOR_START.name());
    }

    private boolean canEnterNextLevel(Game game, StateMachine.State<?> state) {
        return game.isPlaying() && state.nameMatches(StateName.HUNTING.name());
    }
}