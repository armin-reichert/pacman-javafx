/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

public class TestActions {

    private final GameAction actionTestCutScenes;
    private final GameAction actionTestLevelShort;
    private final GameAction actionTestLevelMedium;

    private final Set<ActionKeyBinding> bindings;

    public TestActions(Game game) {

        actionTestCutScenes = new GameAction(game, "test_cut_scenes") {
            @Override
            public void doAction() {
                game.currentGameContext().flow().enterState(CutScenesTestState.class.getSimpleName());
                game.ui().shortMessage("Cut scenes test"); //TODO localize
            }

            @Override
            public boolean isEnabled() {
                return game.currentGameContext().flow().optState(CutScenesTestState.class.getSimpleName()).isPresent();
            }
        };

        actionTestLevelShort = new GameAction(game, "short_level_test") {
            @Override
            public void doAction() {
                game.currentGameContext().flow().restartState(LevelShortTestState.class.getSimpleName());
                game.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
            }

            @Override
            public boolean isEnabled() {
                return game.currentGameContext().flow().optState(LevelShortTestState.class.getSimpleName()).isPresent();
            }
        };

        actionTestLevelMedium = new GameAction(game, "medium_level_test") {
            @Override
            public void doAction() {
                game.currentGameContext().flow().restartState(LevelMediumTestState.class.getSimpleName());
                game.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
            }

            @Override
            public boolean isEnabled() {
                return game.currentGameContext().flow().optState(LevelMediumTestState.class.getSimpleName()).isPresent();
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionTestCutScenes,   combine().alt().key(KeyCode.C)),
            new ActionKeyBinding(actionTestLevelShort,  combine().alt().key(KeyCode.T)),
            new ActionKeyBinding(actionTestLevelMedium, combine().alt().shift().key(KeyCode.T))
        );
    }

    public GameAction actionTestCutScenes() {
        return actionTestCutScenes;
    }

    public GameAction actionTestLevelShort() {
        return actionTestLevelShort;
    }

    public GameAction actionTestLevelMedium() {
        return actionTestLevelMedium;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}