/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.ui.GameEngineUIContext;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class SimulationActions {

    private final GameAction<GameEngineUIContext> actionFaster;
    private final GameAction<GameEngineUIContext> actionFastest;
    private final GameAction<GameEngineUIContext> actionSlower;
    private final GameAction<GameEngineUIContext> actionSlowest;
    private final GameAction<GameEngineUIContext> actionOneStep;
    private final GameAction<GameEngineUIContext> actionTenSteps;
    private final GameAction<GameEngineUIContext> actionReset;
    private final GameAction<GameEngineUIContext> actionTogglePaused;
    private final GameAction<GameEngineUIContext> actionToggleMuted;

    private final Set<ActionKeyBinding> bindings;

    public SimulationActions() {

        actionFaster = new GameAction<>("simulation_faster") {
            @Override
            public void execute(GameEngineUIContext context) {
                final GameClock clock = context.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() + GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MAX ? "At maximum speed: %d Hz" : "%d Hz";
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionFastest = new GameAction<>("simulation_fastest") {
            @Override
            public void execute(GameEngineUIContext context) {
                context.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MAX);
                final String msg = "At maximum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MAX);
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionSlower = new GameAction<>("simulation_slower") {
            @Override
            public void execute(GameEngineUIContext context) {
                final GameClock clock = context.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() - GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MIN ? "At minimum speed: %d Hz" : "%d Hz";
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionSlowest = new GameAction<>("simulation_slowest") {
            @Override
            public void execute(GameEngineUIContext context) {
                context.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MIN);
                final String msg = "At minimum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MIN);
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionOneStep = new GameAction<>("simulation_one_step") {
            @Override
            public void execute(GameEngineUIContext context) {
                final boolean failure = !context.clock().makeOneStep(true);
                if (failure) {
                    context.ui().shortMessage("Simulation step error!");
                }
            }

            @Override
            public boolean isEnabled(GameEngineUIContext context) {
                return context.clock().getUpdatesDisabled();
            }
        };

        actionTenSteps = new GameAction<>("simulation_ten_steps") {
            @Override
            public void execute(GameEngineUIContext context) {
                final boolean failure = !context.clock().makeSteps(10, true);
                if (failure) {
                    context.ui().shortMessage("Simulation steps error!");
                }
            }

            @Override
            public boolean isEnabled(GameEngineUIContext context) {
                return context.clock().getUpdatesDisabled();
            }
        };

        actionReset = new GameAction<>("simulation_reset") {
            @Override
            public void execute(GameEngineUIContext context) {
                final GameClock gameClock = context.clock();
                gameClock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
                context.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), gameClock.targetFrameRate() + "Hz");
            }
        };

        actionTogglePaused = new GameAction<>("toggle_paused") {
            @Override
            public void execute(GameEngineUIContext engineContext) {
                final GameClock gameClock = engineContext.clock();
                toggleBooleanProperty(gameClock.updatesDisabledProperty());
                final boolean paused = gameClock.getUpdatesDisabled();
                if (paused) {
                    engineContext.soundManager().stopAll();
                    //engineContext.gameVariantManager().currentRuntime().uiConfig().optSoundEffects().ifPresent(PacManGameSoundEffects::stopAll);
                }
            }

            @Override
            public boolean isEnabled(GameEngineUIContext context) {
                return context.ui().viewManager().isSelected(GameViewID.GAMEPLAY);
            }
        };

        actionToggleMuted = new GameAction<>("toggle_muted") {
            @Override
            public void execute(GameEngineUIContext context) {
                toggleBooleanProperty(context.ui().viewModel().muteProperty());
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionSlower(),       combine().alt().key(KeyCode.MINUS)),
            new ActionKeyBinding(actionSlowest(),      combine().alt().shift().key(KeyCode.MINUS)),
            new ActionKeyBinding(actionFaster(),       combine().alt().key(KeyCode.PLUS)),
            new ActionKeyBinding(actionFastest(),      combine().alt().shift().key(KeyCode.PLUS)),
            new ActionKeyBinding(actionReset(),        combine().alt().key(KeyCode.DIGIT0)),
            new ActionKeyBinding(actionOneStep(),      combine().shift().key(KeyCode.P), combine().shift().key(KeyCode.F5)),
            new ActionKeyBinding(actionTenSteps(),     combine().shift().key(KeyCode.SPACE)),
            new ActionKeyBinding(actionTogglePaused(), bareKey(KeyCode.P), bareKey(KeyCode.F5)),
            new ActionKeyBinding(actionToggleMuted(),  combine().alt().key(KeyCode.M))
        );
    }

    public GameAction<GameEngineUIContext> actionFaster() {
        return actionFaster;
    }

    public GameAction<GameEngineUIContext> actionFastest() {
        return actionFastest;
    }

    public GameAction<GameEngineUIContext> actionSlower() {
        return actionSlower;
    }

    public GameAction<GameEngineUIContext> actionSlowest() {
        return actionSlowest;
    }

    public GameAction<GameEngineUIContext> actionOneStep() {
        return actionOneStep;
    }

    public GameAction<GameEngineUIContext> actionTenSteps() {
        return actionTenSteps;
    }

    public GameAction<GameEngineUIContext> actionReset() {
        return actionReset;
    }

    public GameAction<GameEngineUIContext> actionTogglePaused() {
        return actionTogglePaused;
    }

    public GameAction<GameEngineUIContext> actionToggleMuted() {
        return actionToggleMuted;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
