/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class SimulationActions {

    private final GameAction<GameEngineContext> actionFaster;
    private final GameAction<GameEngineContext> actionFastest;
    private final GameAction<GameEngineContext> actionSlower;
    private final GameAction<GameEngineContext> actionSlowest;
    private final GameAction<GameEngineContext> actionOneStep;
    private final GameAction<GameEngineContext> actionTenSteps;
    private final GameAction<GameEngineContext> actionReset;
    private final GameAction<GameEngineContext> actionTogglePaused;
    private final GameAction<GameEngineContext> actionToggleMuted;

    private final Set<ActionKeyBinding> bindings;

    public SimulationActions() {

        actionFaster = new GameAction<GameEngineContext>("simulation_faster") {
            @Override
            public void execute(GameEngineContext context) {
                final GameClock clock = context.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() + GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MAX ? "At maximum speed: %d Hz" : "%d Hz";
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionFastest = new GameAction<GameEngineContext>("simulation_fastest") {
            @Override
            public void execute(GameEngineContext context) {
                context.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MAX);
                final String msg = "At maximum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MAX);
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionSlower = new GameAction<GameEngineContext>("simulation_slower") {
            @Override
            public void execute(GameEngineContext context) {
                final GameClock clock = context.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() - GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MIN ? "At minimum speed: %d Hz" : "%d Hz";
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionSlowest = new GameAction<GameEngineContext>("simulation_slowest") {
            @Override
            public void execute(GameEngineContext context) {
                context.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MIN);
                final String msg = "At minimum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MIN);
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionOneStep = new GameAction<GameEngineContext>("simulation_one_step") {
            @Override
            public void execute(GameEngineContext context) {
                final boolean failure = !context.clock().makeOneStep(true);
                if (failure) {
                    context.ui().shortMessage("Simulation step error!");
                }
            }

            @Override
            public boolean isEnabled(GameEngineContext context) { return context.clock().getUpdatesDisabled(); }
        };

        actionTenSteps = new GameAction<GameEngineContext>("simulation_ten_steps") {
            @Override
            public void execute(GameEngineContext context) {
                final boolean failure = !context.clock().makeSteps(10, true);
                if (failure) {
                    context.ui().shortMessage("Simulation steps error!");
                }
            }

            @Override
            public boolean isEnabled(GameEngineContext context) { return context.clock().getUpdatesDisabled(); }
        };

        actionReset = new GameAction<GameEngineContext>("simulation_reset") {
            @Override
            public void execute(GameEngineContext context) {
                final GameClock gameClock = context.clock();
                gameClock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), gameClock.targetFrameRate() + "Hz");
            }
        };

        actionTogglePaused = new GameAction<GameEngineContext>("toggle_paused") {
            @Override
            public void execute(GameEngineContext context) {
                final GameClock gameClock = context.clock();
                toggleBooleanProperty(gameClock.updatesDisabledProperty());
                final boolean paused = gameClock.getUpdatesDisabled();
                if (paused) {
                    context.soundManager().stopAll();
                    context.gameVariantManager().currentRuntime().uiConfig().optSoundEffects().ifPresent(PacManGameSoundEffects::stopAll);
                }
            }

            @Override
            public boolean isEnabled(GameEngineContext context) {
                return context.ui().viewManager().isSelected(GameViewID.GAMEPLAY);
            }
        };

        actionToggleMuted = new GameAction<GameEngineContext>("toggle_muted") {
            @Override
            public void execute(GameEngineContext context) {
                toggleBooleanProperty(context.ui().viewModel().muteProperty());
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionSlower,       combine().alt().key(KeyCode.MINUS)),
            new ActionKeyBinding(actionSlowest,      combine().alt().shift().key(KeyCode.MINUS)),
            new ActionKeyBinding(actionFaster,       combine().alt().key(KeyCode.PLUS)),
            new ActionKeyBinding(actionFastest,      combine().alt().shift().key(KeyCode.PLUS)),
            new ActionKeyBinding(actionReset,        combine().alt().key(KeyCode.DIGIT0)),
            new ActionKeyBinding(actionOneStep,      combine().shift().key(KeyCode.P), combine().shift().key(KeyCode.F5)),
            new ActionKeyBinding(actionTenSteps,     combine().shift().key(KeyCode.SPACE)),
            new ActionKeyBinding(actionTogglePaused, bareKey(KeyCode.P), bareKey(KeyCode.F5)),
            new ActionKeyBinding(actionToggleMuted,  combine().alt().key(KeyCode.M))
        );
    }

    public GameAction<GameEngineContext> actionFaster() {
        return actionFaster;
    }

    public GameAction<GameEngineContext> actionFastest() {
        return actionFastest;
    }

    public GameAction<GameEngineContext> actionSlower() {
        return actionSlower;
    }

    public GameAction<GameEngineContext> actionSlowest() {
        return actionSlowest;
    }

    public GameAction<GameEngineContext> actionOneStep() {
        return actionOneStep;
    }

    public GameAction<GameEngineContext> actionTenSteps() {
        return actionTenSteps;
    }

    public GameAction<GameEngineContext> actionReset() {
        return actionReset;
    }

    public GameAction<GameEngineContext> actionTogglePaused() {
        return actionTogglePaused;
    }

    public GameAction<GameEngineContext> actionToggleMuted() {
        return actionToggleMuted;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
