/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.util.Duration;

public class TestActions {
    public static final GameAction ACTION_CUT_SCENES_TEST = new GameAction("TEST_CUT_SCENES") {
        @Override
        public void execute(GameUI ui) {
            ui.context().currentGame().changeState(CutScenesTestState.class.getSimpleName());
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }
    };

    public static final GameAction ACTION_SHORT_LEVEL_TEST = new GameAction("TEST_LEVELS_SHORT") {
        @Override
        public void execute(GameUI ui) {
            ui.context().currentGame().restart(LevelShortTestState.class.getSimpleName());
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }
    };

    public static final GameAction ACTION_MEDIUM_LEVEL_TEST = new GameAction("TEST_LEVELS_MEDIUM") {
        @Override
        public void execute(GameUI ui) {
            ui.context().currentGame().restart(LevelMediumTestState.class.getSimpleName());
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }
    };
}