/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

public class TestActions {

    private final GameAction actionTestCutScenes;
    private final GameAction actionTestLevelShort;
    private final GameAction actionTestLevelMedium;

    private final Set<ActionKeyBinding> bindings;

    public TestActions(GameAppContext appContext) {

        actionTestCutScenes = new GameAction(appContext, "test_cut_scenes") {
            @Override
            public void doAction() {
                gameFlow().enterState(TestStateID.CUT_SCENE_TEST);
                appContext.ui().shortMessage("Cut scenes test"); //TODO localize
            }

            @Override
            public boolean isEnabled() {
                return gameFlow().optState(TestStateID.CUT_SCENE_TEST).isPresent();
            }
        };

        actionTestLevelShort = new GameAction(appContext, "short_level_test") {
            @Override
            public void doAction() {
                gameFlow().restartState(TestStateID.LEVEL_TEST_S);
                appContext.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
            }

            @Override
            public boolean isEnabled() {
                return gameFlow().optState(TestStateID.LEVEL_TEST_S).isPresent();
            }
        };

        actionTestLevelMedium = new GameAction(appContext, "medium_level_test") {
            @Override
            public void doAction() {
                gameFlow().restartState(TestStateID.LEVEL_TEST_M);
                appContext.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
            }

            @Override
            public boolean isEnabled() {
                return gameFlow().optState(TestStateID.LEVEL_TEST_M).isPresent();
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