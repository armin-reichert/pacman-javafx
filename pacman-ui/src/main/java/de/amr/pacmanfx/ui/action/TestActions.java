/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.app.Game;
import javafx.util.Duration;

public class TestActions {

    public static final GameAction ACTION_CUT_SCENES_TEST = new GameAction("test_cut_scenes") {
        @Override
        public void doAction(Game appContext) {
            appContext.currentGameContext().flow().enterState(CutScenesTestState.class.getSimpleName());
            appContext.shortMessage("Cut scenes test"); //TODO localize
        }

        @Override
        public boolean isEnabled(Game appContext) {
            return appContext.currentGameContext().flow().optState(CutScenesTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_SHORT_LEVEL_TEST = new GameAction("short_level_test") {
        @Override
        public void doAction(Game appContext) {
            appContext.currentGameContext().flow().restartState(LevelShortTestState.class.getSimpleName());
            appContext.shortMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
        }

        @Override
        public boolean isEnabled(Game appContext) {
            return appContext.currentGameContext().flow().optState(LevelShortTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_MEDIUM_LEVEL_TEST = new GameAction("medium_level_test") {
        @Override
        public void doAction(Game appContext) {
            appContext.currentGameContext().flow().restartState(LevelMediumTestState.class.getSimpleName());
            appContext.shortMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
        }

        @Override
        public boolean isEnabled(Game appContext) {
            return appContext.currentGameContext().flow().optState(LevelMediumTestState.class.getSimpleName()).isPresent();
        }
    };
}