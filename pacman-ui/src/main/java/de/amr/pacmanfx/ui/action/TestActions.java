/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.GameUI;
import javafx.util.Duration;

public class TestActions {

    public static final GameAction ACTION_CUT_SCENES_TEST = new GameAction("TEST_CUT_SCENES") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().game().flow().enterStateWithName(CutScenesTestState.class.getSimpleName());
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().game().flow().optState(CutScenesTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_SHORT_LEVEL_TEST = new GameAction("SHORT_LEVEL_TEST") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().game().flow().restartStateWithName(LevelShortTestState.class.getSimpleName());
            ui.showFlashMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().game().flow().optState(LevelShortTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_MEDIUM_LEVEL_TEST = new GameAction("MEDIUM_LEVEL_TEST") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().game().flow().restartStateWithName(LevelMediumTestState.class.getSimpleName());
            ui.showFlashMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().game().flow().optState(LevelMediumTestState.class.getSimpleName()).isPresent();
        }
    };
}