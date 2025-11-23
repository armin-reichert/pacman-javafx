/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.test.CutScenesTestState;
import de.amr.pacmanfx.controller.test.LevelMediumTestState;
import de.amr.pacmanfx.controller.test.LevelShortTestState;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.util.Duration;

public class TestActions {
    public static final GameAction ACTION_CUT_SCENES_TEST = new GameAction("TEST_CUT_SCENES") {
        @Override
        public void execute(GameUI ui) {
            FsmState<GameContext> testState = ui.gameContext().game().stateMachine().stateByName(CutScenesTestState.class.getSimpleName());
            ui.gameContext().game().stateMachine().changeGameState(testState);
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }
    };

    public static final GameAction ACTION_SHORT_LEVEL_TEST = new GameAction("TEST_LEVELS_SHORT") {
        @Override
        public void execute(GameUI ui) {
            FsmState<GameContext> testState = ui.gameContext().game().stateMachine().stateByName(LevelShortTestState.class.getSimpleName());
            ui.gameContext().game().stateMachine().restart(testState);
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }
    };

    public static final GameAction ACTION_MEDIUM_LEVEL_TEST = new GameAction("TEST_LEVELS_MEDIUM") {
        @Override
        public void execute(GameUI ui) {
            FsmState<GameContext> testState = ui.gameContext().game().stateMachine().stateByName(LevelMediumTestState.class.getSimpleName());
            ui.gameContext().game().stateMachine().restart(testState);
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }
    };
}