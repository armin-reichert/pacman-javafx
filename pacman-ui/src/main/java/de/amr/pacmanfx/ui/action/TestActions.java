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

    public static final GameAction ACTION_CUT_SCENES_TEST = new GameAction("test_cut_scenes") {
        @Override
        public void doAction(GameUI ui) {
            ui.access().currentGame().flow().enterStateWithName(CutScenesTestState.class.getSimpleName());
            ui.access().flashMessage("Cut scenes test"); //TODO localize
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.access().currentGame().flow().optState(CutScenesTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_SHORT_LEVEL_TEST = new GameAction("short_level_test") {
        @Override
        public void doAction(GameUI ui) {
            ui.access().currentGame().flow().restartStateWithName(LevelShortTestState.class.getSimpleName());
            ui.access().flashMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.access().currentGame().flow().optState(LevelShortTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_MEDIUM_LEVEL_TEST = new GameAction("medium_level_test") {
        @Override
        public void doAction(GameUI ui) {
            ui.access().currentGame().flow().restartStateWithName(LevelMediumTestState.class.getSimpleName());
            ui.access().flashMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.access().currentGame().flow().optState(LevelMediumTestState.class.getSimpleName()).isPresent();
        }
    };
}