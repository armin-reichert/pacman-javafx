/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.runtime.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.GameActionContext;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class TestActions {

    private final GameAction actionTestCutScenes;
    private final GameAction actionTestLevelShort;
    private final GameAction actionTestLevelMedium;

    private final Set<ActionKeyBinding> bindings;

    public TestActions() {

        actionTestCutScenes = new GameAction("test_cut_scenes") {
            @Override
            public void execute(GameActionContext context) {
                context.currentGame().playConfig().gameFlow().enterGameState(context.currentGame(), TestStateID.CUT_SCENE_TEST);
                context.ui().shortMessage("Cut scenes test"); //TODO localize
            }

            @Override
            public boolean isEnabled(GameActionContext context) {
                return context.currentGame().playConfig().gameFlow().optGameState(TestStateID.CUT_SCENE_TEST).isPresent();
            }
        };

        actionTestLevelShort = new GameAction("short_level_test") {
            @Override
            public void execute(GameActionContext context) {
                context.currentGame().playConfig().gameFlow().restartGameState(context.currentGame(), TestStateID.LEVEL_TEST_S);
                context.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
            }

            @Override
            public boolean isEnabled(GameActionContext context) {
                return context.currentGame().playConfig().gameFlow().optGameState(TestStateID.LEVEL_TEST_S).isPresent();
            }
        };

        actionTestLevelMedium = new GameAction("medium_level_test") {
            @Override
            public void execute(GameActionContext context) {
                context.currentGame().playConfig().gameFlow().restartGameState(context.currentGame(), TestStateID.LEVEL_TEST_M);
                context.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
            }

            @Override
            public boolean isEnabled(GameActionContext context) {
                return context.currentGame().playConfig().gameFlow().optGameState(TestStateID.LEVEL_TEST_M).isPresent();
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