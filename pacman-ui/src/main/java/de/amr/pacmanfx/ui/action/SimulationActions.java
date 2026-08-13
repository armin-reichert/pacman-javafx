/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

public class SimulationActions {

    private final GameAction actionFaster;
    private final GameAction actionFastest;
    private final GameAction actionSlower;
    private final GameAction actionSlowest;
    private final GameAction actionOneStep;
    private final GameAction actionTenSteps;
    private final GameAction actionReset;
    private final GameAction actionTogglePaused;
    private final GameAction actionToggleMuted;

    private final Set<ActionKeyBinding> bindings;

    public SimulationActions() {

        actionFaster = new GameAction("simulation_faster") {
            @Override
            public void execute(GameAppContext app) {
                final GameClock clock = app.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() + GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MAX ? "At maximum speed: %d Hz" : "%d Hz";
                app.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionFastest = new GameAction("simulation_fastest") {
            @Override
            public void execute(GameAppContext app) {
                app.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MAX);
                final String msg = "At maximum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MAX);
                app.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionSlower = new GameAction("simulation_slower") {
            @Override
            public void execute(GameAppContext app) {
                final GameClock clock = app.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() - GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MIN ? "At minimum speed: %d Hz" : "%d Hz";
                app.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionSlowest = new GameAction("simulation_slowest") {
            @Override
            public void execute(GameAppContext app) {
                app.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MIN);
                final String msg = "At minimum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MIN);
                app.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionOneStep = new GameAction("simulation_one_step") {
            @Override
            public void execute(GameAppContext app) {
                final boolean failure = !app.clock().makeOneStep(true);
                if (failure) {
                    app.ui().shortMessage("Simulation step error!");
                }
            }

            @Override
            public boolean isEnabled(GameAppContext app) { return app.clock().getUpdatesDisabled(); }
        };

        actionTenSteps = new GameAction("simulation_ten_steps") {
            @Override
            public void execute(GameAppContext app) {
                final boolean failure = !app.clock().makeSteps(10, true);
                if (failure) {
                    app.ui().shortMessage("Simulation steps error!");
                }
            }

            @Override
            public boolean isEnabled(GameAppContext app) { return app.clock().getUpdatesDisabled(); }
        };

        actionReset = new GameAction("simulation_reset") {
            @Override
            public void execute(GameAppContext app) {
                final GameClock gameClock = app.clock();
                gameClock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
                app.ui().shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), gameClock.targetFrameRate() + "Hz");
            }
        };

        actionTogglePaused = new GameAction("toggle_paused") {
            @Override
            public void execute(GameAppContext app) {
                final GameClock gameClock = app.clock();
                toggleBooleanProperty(gameClock.updatesDisabledProperty());
                final boolean paused = gameClock.getUpdatesDisabled();
                if (paused) {
                    app.ui().sounds().stopAll();
                    app.gameVariants().currentGameVariant().config().optSoundEffects().ifPresent(GameSoundEffects::stopAll);
                }
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                return app.ui().views().isSelected(GameViewID.GAMEPLAY);
            }
        };

        actionToggleMuted = new GameAction("toggle_muted") {
            @Override
            public void execute(GameAppContext app) {
                toggleBooleanProperty(app.ui().viewModel().mutedProperty);
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

    public GameAction actionFaster() {
        return actionFaster;
    }

    public GameAction actionFastest() {
        return actionFastest;
    }

    public GameAction actionSlower() {
        return actionSlower;
    }

    public GameAction actionSlowest() {
        return actionSlowest;
    }

    public GameAction actionOneStep() {
        return actionOneStep;
    }

    public GameAction actionTenSteps() {
        return actionTenSteps;
    }

    public GameAction actionReset() {
        return actionReset;
    }

    public GameAction actionTogglePaused() {
        return actionTogglePaused;
    }

    public GameAction actionToggleMuted() {
        return actionToggleMuted;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
