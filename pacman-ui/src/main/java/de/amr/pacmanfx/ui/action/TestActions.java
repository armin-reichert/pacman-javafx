/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngineImpl;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.runtime.action.GameAction;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;
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
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                engine.currentGame().playConfig().gameFlow().enterGameState(engine.currentGame(), TestStateID.CUT_SCENE_TEST);
                engineImpl.ui().shortMessage("Cut scenes test"); //TODO localize
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return engine.currentGame().playConfig().gameFlow().optGameState(TestStateID.CUT_SCENE_TEST).isPresent();
            }
        };

        actionTestLevelShort = new GameAction("short_level_test") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                engine.currentGame().playConfig().gameFlow().restartGameState(engine.currentGame(), TestStateID.LEVEL_TEST_S);
                engineImpl.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return engine.currentGame().playConfig().gameFlow().optGameState(TestStateID.LEVEL_TEST_S).isPresent();
            }
        };

        actionTestLevelMedium = new GameAction("medium_level_test") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                engine.currentGame().playConfig().gameFlow().restartGameState(engine.currentGame(), TestStateID.LEVEL_TEST_M);
                engineImpl.ui().shortMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return engine.currentGame().playConfig().gameFlow().optGameState(TestStateID.LEVEL_TEST_M).isPresent();
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