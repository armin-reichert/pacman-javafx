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
            ui.gameContext().currentGame().control().enterStateNamed(CutScenesTestState.class.getSimpleName());
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().currentGame().control().optState(CutScenesTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_SHORT_LEVEL_TEST = new GameAction("TEST_LEVELS_SHORT") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().currentGame().control().restart(LevelShortTestState.class.getSimpleName());
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().currentGame().control().optState(LevelShortTestState.class.getSimpleName()).isPresent();
        }
    };

    public static final GameAction ACTION_MEDIUM_LEVEL_TEST = new GameAction("TEST_LEVELS_MEDIUM") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().currentGame().control().restart(LevelMediumTestState.class.getSimpleName());
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().currentGame().control().optState(LevelMediumTestState.class.getSimpleName()).isPresent();
        }
    };
}