/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

public class TestActions {

    private final GameAction actionTestCutScenes;
    private final GameAction actionTestLevelShort;
    private final GameAction actionTestLevelMedium;

    private final Set<ActionKeyBinding> bindings;

    public TestActions(GameActionContext game) {

        actionTestCutScenes = new GameAction(game, "test_cut_scenes") {
            @Override
            public void doAction() {
                actionContext.gameContext().flow().enterState(TestStateID.CUT_SCENE_TEST);
                actionContext.ui().shortMessage("Cut scenes test"); //TODO localize
            }

            @Override
            public boolean isEnabled() {
                return actionContext.gameContext().flow().optState(TestStateID.CUT_SCENE_TEST).isPresent();
            }
        };

        actionTestLevelShort = new GameAction(game, "short_level_test") {
            @Override
            public void doAction() {
                actionContext.gameContext().flow().restartState(TestStateID.LEVEL_TEST_S);
                actionContext.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
            }

            @Override
            public boolean isEnabled() {
                return actionContext.gameContext().flow().optState(TestStateID.LEVEL_TEST_S).isPresent();
            }
        };

        actionTestLevelMedium = new GameAction(game, "medium_level_test") {
            @Override
            public void doAction() {
                actionContext.gameContext().flow().restartState(TestStateID.LEVEL_TEST_M);
                actionContext.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
            }

            @Override
            public boolean isEnabled() {
                return actionContext.gameContext().flow().optState(TestStateID.LEVEL_TEST_M).isPresent();
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