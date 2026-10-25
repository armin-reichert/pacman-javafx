/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.ui.EngineContext;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class TestActions {

    private final GameAction<EngineContext> actionTestCutScenes;
    private final GameAction<EngineContext> actionTestLevelShort;
    private final GameAction<EngineContext> actionTestLevelMedium;

    private final Set<ActionKeyBinding<EngineContext>> bindings;

    public TestActions() {

        actionTestCutScenes = new GameAction<>("test_cut_scenes") {
            @Override
            public void execute(EngineContext engine) {
                engine.optCurrentGame().ifPresent(game -> {
                    game.runtime().gameFlow().enterGameState(game, TestStateID.CUT_SCENE_TEST);
                    engine.ui().showMessage("Cut scenes test"); //TODO localize
                });
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                final GameContext game = engine.optCurrentGame().orElse(null);
                return game != null && game.runtime().gameFlow().optGameState(TestStateID.CUT_SCENE_TEST).isPresent();
            }
        };

        actionTestLevelShort = new GameAction<>("short_level_test") {
            @Override
            public void execute(EngineContext engine) {
                engine.optCurrentGame().ifPresent(game -> {
                    game.runtime().gameFlow().restartGameState(game, TestStateID.LEVEL_TEST_S);
                    engine.ui().showMessage(Duration.seconds(3), "Level Test Mode (Short tests)");
                });
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                final GameContext game = engine.optCurrentGame().orElse(null);
                return game != null && game.runtime().gameFlow().optGameState(TestStateID.LEVEL_TEST_S).isPresent();
            }
        };

        actionTestLevelMedium = new GameAction<>("medium_level_test") {
            @Override
            public void execute(EngineContext engine) {
                engine.optCurrentGame().ifPresent(game -> {
                    game.runtime().gameFlow().restartGameState(game, TestStateID.LEVEL_TEST_M);
                    engine.ui().showMessage(Duration.seconds(3), "Level Test Mode (Medium tests)");
                });
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                final GameContext game = engine.optCurrentGame().orElse(null);
                return game != null && game.runtime().gameFlow().optGameState(TestStateID.LEVEL_TEST_M).isPresent();
            }
        };

        bindings = Set.of(
            new ActionKeyBinding<>(actionTestCutScenes(),   combine().alt().key(KeyCode.C)),
            new ActionKeyBinding<>(actionTestLevelShort(),  combine().alt().key(KeyCode.T)),
            new ActionKeyBinding<>(actionTestLevelMedium(), combine().alt().shift().key(KeyCode.T))
        );
    }

    public GameAction<EngineContext> actionTestCutScenes() {
        return actionTestCutScenes;
    }

    public GameAction<EngineContext> actionTestLevelShort() {
        return actionTestLevelShort;
    }

    public GameAction<EngineContext> actionTestLevelMedium() {
        return actionTestLevelMedium;
    }

    public Set<ActionKeyBinding<EngineContext>> bindings() {
        return bindings;
    }
}