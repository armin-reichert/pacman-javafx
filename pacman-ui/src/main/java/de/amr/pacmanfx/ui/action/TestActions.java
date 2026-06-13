/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.game.Game;
import javafx.util.Duration;

public class TestActions {

    abstract class AbstractGameAction extends GameAction {

        protected AbstractGameAction(String key) {
            super(TestActions.this.game, key);
        }
    }

    private final Game game;

    public TestActions(Game game) {
        this.game = game;
    }

    public final GameAction ACTION_CUT_SCENES_TEST = new AbstractGameAction("test_cut_scenes") {
        @Override
        public void doAction() {
            game.currentGameContext().flow().enterState(CutScenesTestState.class.getSimpleName());
            game.shortMessage("Cut scenes test"); //TODO localize
        }

        @Override
        public boolean isEnabled() {
            return game.currentGameContext().flow().optState(CutScenesTestState.class.getSimpleName()).isPresent();
        }
    };

    public final GameAction ACTION_SHORT_LEVEL_TEST = new AbstractGameAction("short_level_test") {
        @Override
        public void doAction() {
            game.currentGameContext().flow().restartState(LevelShortTestState.class.getSimpleName());
            game.shortMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
        }

        @Override
        public boolean isEnabled() {
            return game.currentGameContext().flow().optState(LevelShortTestState.class.getSimpleName()).isPresent();
        }
    };

    public final GameAction ACTION_MEDIUM_LEVEL_TEST = new AbstractGameAction("medium_level_test") {
        @Override
        public void doAction() {
            game.currentGameContext().flow().restartState(LevelMediumTestState.class.getSimpleName());
            game.shortMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
        }

        @Override
        public boolean isEnabled() {
            return game.currentGameContext().flow().optState(LevelMediumTestState.class.getSimpleName()).isPresent();
        }
    };
}