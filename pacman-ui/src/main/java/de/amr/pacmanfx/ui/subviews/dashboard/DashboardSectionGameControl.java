/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.action.TestActions;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

import java.util.List;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_ENTER_NEXT_LEVEL;
import static de.amr.pacmanfx.ui.action.CommonActions.ACTION_RESTART_INTRO;

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
    public void connect(AppContext context) {
        final CoinMechanism coinMechanism = context.gameContext().coinMechanism();
        spinnerCredit            = addIntSpinner("Credit", 0, coinMechanism.maxCoins(), coinMechanism.numCoinsProperty());
        choiceBoxInitialLives    = addChoiceBox("Initial Lives", new Integer[] {3, 5});
        buttonGroupLevelActions  = addButtonList("Game Level", List.of("Start", "Quit", "Next"));
        buttonGroupCutScenesTest = addButtonList("Cut Scenes Test", List.of("Start", "Quit"));
        cbCollisionCheckedTwice  = addCheckBox("Collision Check 2x");

        setAction(choiceBoxInitialLives, () -> context.currentGame().lives().setInitialCount(choiceBoxInitialLives.getValue()));

        //setAction(buttonGroupLevelActions[GAME_LEVEL_START], ArcadeActions.ACTION_START_GAME); //TODO FIXME!
        setAction(context, buttonGroupLevelActions[GAME_LEVEL_QUIT], ACTION_RESTART_INTRO);
        setAction(context, buttonGroupLevelActions[GAME_LEVEL_NEXT], ACTION_ENTER_NEXT_LEVEL);

        setAction(context, buttonGroupCutScenesTest[CUT_SCENES_TEST_START], TestActions.ACTION_CUT_SCENES_TEST);
        setAction(context, buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT], ACTION_RESTART_INTRO);

        cbCollisionCheckedTwice.setOnAction(_ -> {
            final AbstractGameModel game = context.currentGame();
            game.setCollisionDoubleChecked(cbCollisionCheckedTwice.isSelected());
        });
    }

    @Override
    public void update() {
        super.update();

        if (dashboard.context() != null) {
            final AbstractGameModel game = dashboard.context().currentGame();
            final State<GameContext> state = dashboard.context().currentGameState();

            choiceBoxInitialLives.setValue(game.lives().initialCount());
            choiceBoxInitialLives.setDisable(!state.nameIsOneOf(GameStateID.GAME_INTRO.name()));

            final boolean creditDisabled = !state.nameIsOneOf(
                GameStateID.GAME_INTRO.name(),
                GameStateID.GAME_PREPARATION.name()
            );
            spinnerCredit.setDisable(creditDisabled);

            final boolean booting = isBooting(state);
            buttonGroupLevelActions[GAME_LEVEL_START].setDisable(booting || !canStartLevel(game, state));
            buttonGroupLevelActions[GAME_LEVEL_QUIT].setDisable(booting || game.optGameLevel().isEmpty());
            buttonGroupLevelActions[GAME_LEVEL_NEXT].setDisable(booting || !canEnterNextLevel(game, state));

            buttonGroupCutScenesTest[CUT_SCENES_TEST_START].setDisable(booting || !state.nameIsOneOf(GameStateID.GAME_INTRO.name()));
            buttonGroupCutScenesTest[CUT_SCENES_TEST_QUIT].setDisable(booting || !(state instanceof CutScenesTestState));

            cbCollisionCheckedTwice.setSelected(game.isCollisionDoubleChecked());
        }
    }

    private boolean isBooting(State<?> state) {
        return state.name().equals(GameStateID.BOOT.name());
    }

    private boolean canStartLevel(GameModel game, State<GameContext> state) {
        return game.canStartNewGame()
            && state.nameIsOneOf(
                GameStateID.GAME_INTRO.name(),
                GameStateID.GAME_PREPARATION.name()
        );
    }

    private boolean canEnterNextLevel(GameModel game, State<GameContext> state) {
        return game.isPlaying() && state.nameIsOneOf(GameStateID.GAME_LEVEL_PLAYING.name());
    }
}