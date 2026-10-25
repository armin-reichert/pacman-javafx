/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.ui.EngineContext;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class SimulationActions {

    private final GameAction<EngineContext> actionFaster;
    private final GameAction<EngineContext> actionFastest;
    private final GameAction<EngineContext> actionSlower;
    private final GameAction<EngineContext> actionSlowest;
    private final GameAction<EngineContext> actionOneStep;
    private final GameAction<EngineContext> actionTenSteps;
    private final GameAction<EngineContext> actionReset;
    private final GameAction<EngineContext> actionTogglePaused;
    private final GameAction<EngineContext> actionToggleMuted;

    private final Set<ActionKeyBinding<EngineContext>> bindings;

    public SimulationActions() {

        actionFaster = new GameAction<>("simulation_faster") {
            @Override
            public void execute(EngineContext engine) {
                final GameClock clock = engine.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() + GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MAX ? "At maximum speed: %d Hz" : "%d Hz";
                engine.ui().showMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionFastest = new GameAction<>("simulation_fastest") {
            @Override
            public void execute(EngineContext engine) {
                engine.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MAX);
                final String msg = "At maximum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MAX);
                engine.ui().showMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionSlower = new GameAction<>("simulation_slower") {
            @Override
            public void execute(EngineContext engine) {
                final GameClock clock = engine.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() - GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MIN ? "At minimum speed: %d Hz" : "%d Hz";
                engine.ui().showMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionSlowest = new GameAction<>("simulation_slowest") {
            @Override
            public void execute(EngineContext engine) {
                engine.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MIN);
                final String msg = "At minimum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MIN);
                engine.ui().showMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionOneStep = new GameAction<>("simulation_one_step") {
            @Override
            public void execute(EngineContext engine) {
                final boolean failure = !engine.clock().makeStep(true);
                if (failure) {
                    engine.ui().showMessage("Simulation step error!");
                }
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                return engine.clock().areUpdatesDisabled();
            }
        };

        actionTenSteps = new GameAction<>("simulation_ten_steps") {
            @Override
            public void execute(EngineContext engine) {
                final boolean failure = !engine.clock().makeSteps(10, true);
                if (failure) {
                    engine.ui().showMessage("Simulation steps error!");
                }
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                return engine.clock().areUpdatesDisabled();
            }
        };

        actionReset = new GameAction<>("simulation_reset") {
            @Override
            public void execute(EngineContext engine) {
                final GameClock gameClock = engine.clock();
                gameClock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
                engine.ui().showMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), gameClock.targetFrameRate() + "Hz");
            }
        };

        actionTogglePaused = new GameAction<>("toggle_paused") {
            @Override
            public void execute(EngineContext engine) {
                final GameClock gameClock = engine.clock();
                toggleBooleanProperty(gameClock.updatesDisabledProperty());
                final boolean paused = gameClock.areUpdatesDisabled();
                if (paused) {
                    engine.soundManager().stopAll();
                    //engine.gameVariantManager().currentRuntime().uiConfig().optSoundEffects().ifPresent(PacManGameSoundEffects::stopAll);
                }
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                return engine.viewManager().isSelected(GameViewID.GAMEPLAY);
            }
        };

        actionToggleMuted = new GameAction<>("toggle_muted") {
            @Override
            public void execute(EngineContext engine) {
                toggleBooleanProperty(engine.ui().viewModel().muteProperty());
            }
        };

        bindings = Set.of(
            new ActionKeyBinding<>(actionSlower(),       combine().alt().key(KeyCode.MINUS)),
            new ActionKeyBinding<>(actionSlowest(),      combine().alt().shift().key(KeyCode.MINUS)),
            new ActionKeyBinding<>(actionFaster(),       combine().alt().key(KeyCode.PLUS)),
            new ActionKeyBinding<>(actionFastest(),      combine().alt().shift().key(KeyCode.PLUS)),
            new ActionKeyBinding<>(actionReset(),        combine().alt().key(KeyCode.DIGIT0)),
            new ActionKeyBinding<>(actionOneStep(),      combine().shift().key(KeyCode.P), combine().shift().key(KeyCode.F5)),
            new ActionKeyBinding<>(actionTenSteps(),     combine().shift().key(KeyCode.SPACE)),
            new ActionKeyBinding<>(actionTogglePaused(), bareKey(KeyCode.P), bareKey(KeyCode.F5)),
            new ActionKeyBinding<>(actionToggleMuted(),  combine().alt().key(KeyCode.M))
        );
    }

    public GameAction<EngineContext> actionFaster() {
        return actionFaster;
    }

    public GameAction<EngineContext> actionFastest() {
        return actionFastest;
    }

    public GameAction<EngineContext> actionSlower() {
        return actionSlower;
    }

    public GameAction<EngineContext> actionSlowest() {
        return actionSlowest;
    }

    public GameAction<EngineContext> actionOneStep() {
        return actionOneStep;
    }

    public GameAction<EngineContext> actionTenSteps() {
        return actionTenSteps;
    }

    public GameAction<EngineContext> actionReset() {
        return actionReset;
    }

    public GameAction<EngineContext> actionTogglePaused() {
        return actionTogglePaused;
    }

    public GameAction<EngineContext> actionToggleMuted() {
        return actionToggleMuted;
    }

    public Set<ActionKeyBinding<EngineContext>> bindings() {
        return bindings;
    }
}
