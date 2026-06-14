package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameConstants;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.pacmanfx.model.GameRules.NUM_TICKS_PER_SEC;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

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

    public SimulationActions(Game game) {

        actionFaster = new GameAction(game, "simulation_faster") {
            @Override
            protected void doAction() {
                final GameClock clock = game.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() + GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MAX ? "At maximum speed: %d Hz" : "%d Hz";
                game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionFastest = new GameAction(game, "simulation_fastest") {
            @Override
            protected void doAction() {
                game.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MAX);
                final String msg = "At maximum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MAX);
                game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionSlower = new GameAction(game, "simulation_slower") {
            @Override
            protected void doAction() {
                final GameClock clock = game.clock();
                final int newRate = Math.clamp(clock.targetFrameRate() - GameConstants.SIM_SPEED_DELTA,
                    GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                clock.setTargetFrameRate(newRate);

                final String msg = newRate == GameConstants.SIM_SPEED_MIN ? "At minimum speed: %d Hz" : "%d Hz";
                game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
            }
        };

        actionSlowest = new GameAction(game, "simulation_slowest") {
            @Override
            protected void doAction() {
                game.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MIN);
                final String msg = "At minimum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MIN);
                game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
            }
        };

        actionOneStep = new GameAction(game, "simulation_one_step") {
            @Override
            protected void doAction() {
                final boolean failure = !game.clock().makeOneStep(true);
                if (failure) {
                    game.shortMessage("Simulation step error!");
                }
            }

            @Override
            public boolean isEnabled() { return game.clock().getUpdatesDisabled(); }
        };

        actionTenSteps = new GameAction(game, "simulation_ten_steps") {
            @Override
            protected void doAction() {
                final boolean failure = !game.clock().makeSteps(10, true);
                if (failure) {
                    game.shortMessage("Simulation steps error!");
                }
            }

            @Override
            public boolean isEnabled() { return game.clock().getUpdatesDisabled(); }
        };

        actionReset = new GameAction(game, "simulation_reset") {
            @Override
            protected void doAction() {
                game.clock().setTargetFrameRate(NUM_TICKS_PER_SEC);
                game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), game.clock().targetFrameRate() + "Hz");
            }
        };

        actionTogglePaused = new GameAction(game, "toggle_paused") {
            @Override
            protected void doAction() {
                final GameClock gameClock = game.clock();
                toggleBooleanProperty(gameClock.updatesDisabledProperty());
                final boolean paused = gameClock.getUpdatesDisabled();
                if (paused) {
                    game.ui().sounds().stopAll();
                    game.currentUIConfig().optSoundEffects().ifPresent(GameSoundEffects::stopAll);
                }
            }

            @Override
            public boolean isEnabled() {
                final SubViewManager subViews = game.ui().subViews();
                return subViews.isSelected(subViews.gamePlayView());
            }
        };

        actionToggleMuted = new GameAction(game, "toggle_muted") {
            @Override
            protected void doAction() {
                toggleBooleanProperty(game.ui().settings().mutedProperty);
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
