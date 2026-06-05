/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.AppContext;
import javafx.util.Duration;

public class TestActions {

    public static final GameAction ACTION_CUT_SCENES_TEST = new GameAction("test_cut_scenes") {
        @Override
        public void doAction(AppContext context) {
            context.gameContext().gameFlow().enterState(CutScenesTestState.class.getSimpleName());
            context.shortMessage("Cut scenes test"); //TODO localize
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return context.gameContext().gameFlow().optState(CutScenesTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_SHORT_LEVEL_TEST = new GameAction("short_level_test") {
        @Override
        public void doAction(AppContext context) {
            context.gameContext().gameFlow().restartState(LevelShortTestState.class.getSimpleName());
            context.shortMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return context.gameContext().gameFlow().optState(LevelShortTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_MEDIUM_LEVEL_TEST = new GameAction("medium_level_test") {
        @Override
        public void doAction(AppContext context) {
            context.gameContext().gameFlow().restartState(LevelMediumTestState.class.getSimpleName());
            context.shortMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return context.gameContext().gameFlow().optState(LevelMediumTestState.class.getSimpleName()).isPresent();
        }
    };
}