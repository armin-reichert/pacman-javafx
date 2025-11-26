package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;

import java.util.ArrayList;
import java.util.List;

public class Arcade_GameStateMachine extends StateMachine<FsmState<GameContext>, GameContext> {

    public Arcade_GameStateMachine() {
        List<FsmState<GameContext>> states = new ArrayList<>(List.of(GameState.values()));
        states.add(new LevelShortTestState());
        states.add(new LevelMediumTestState());
        states.add(new CutScenesTestState());
        setName("Arcade Game State Machine");
        setStates(states);
    }

    /**
     * States of the common state machine for all Pac-Man games.
     * <p>
     * TODO: Provide different FSM for Tengen Ms. Pac-Man than for Arcade Pac-Man & Ms. Pac-Man?
     */
    public enum GameState implements FsmState<GameContext> {

        // "Das muss das Boot abkönnen!"
        BOOT {
            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
                context.cheatUsedProperty().set(false);
                context.immunityProperty().set(false);
                context.usingAutopilotProperty().set(false);
                context.currentGame().resetEverything();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().changeState(INTRO);
                }
            }
        },

        INTRO {
            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        SETTING_OPTIONS_FOR_START {
            @Override
            public void onUpdate(GameContext context) {
                // wait for user interaction to leave state
            }
        },

        /**
         * In Tengen Ms. Pac-Man, the credited people are shown.
         */
        SHOWING_HALL_OF_FAME {
            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().changeState(INTRO);
                }
            }
        },

        STARTING_GAME_OR_LEVEL {
            static final short TICK_NEW_GAME_SHOW_GUYS = 120;
            static final short TICK_NEW_GAME_START_HUNTING = 240;
            static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
            static final short TICK_RESUME_HUNTING =  90;

            @Override
            public void onEnter(GameContext context) {
                context.currentGame().publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
            }

            private void startNewGame(GameContext context) {
                if (timer.tickCount() == 1) {
                    context.currentGame().startNewGame();
                }
                else if (timer.tickCount() == 2) {
                    final GameLevel gameLevel = context.gameLevel();
                    if (!gameLevel.isDemoLevel()) {
                        gameLevel.pac().immuneProperty().bind(context.immunityProperty());
                        gameLevel.pac().usingAutopilotProperty().bind(context.usingAutopilotProperty());
                        boolean cheating = context.immunityProperty().get() || context.usingAutopilotProperty().get();
                        context.cheatUsedProperty().set(cheating);
                    }
                    context.currentGame().startLevel(gameLevel);
                }
                else if (timer.tickCount() == TICK_NEW_GAME_SHOW_GUYS) {
                    context.gameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_HUNTING) {
                    context.currentGame().setPlaying(true);
                    context.currentGame().changeState(GameState.HUNTING);
                }
            }

            private void continueGame(GameContext context) {
                if (timer.tickCount() == 1) {
                    context.currentGame().continueGame(context.gameLevel());
                } else if (timer.tickCount() == TICK_RESUME_HUNTING) {
                    context.currentGame().changeState(GameState.HUNTING);
                }
            }

            private void startDemoLevel(GameContext context) {
                if (timer.tickCount() == 1) {
                    context.currentGame().buildDemoLevel();
                    context.currentGame().publishGameEvent(GameEvent.Type.LEVEL_CREATED);
                }
                else if (timer.tickCount() == 2) {
                    context.currentGame().startLevel(context.gameLevel());
                }
                else if (timer.tickCount() == 3) {
                    // Now, actor animations are available
                    context.gameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START_HUNTING) {
                    context.currentGame().changeState(GameState.HUNTING);
                }
            }

            @Override
            public void onUpdate(GameContext context) {
                if (context.currentGame().isPlaying()) {
                    continueGame(context);
                }
                else if (context.currentGame().canStartNewGame()) {
                    startNewGame(context);
                }
                else {
                    startDemoLevel(context);
                }
            }
        },

        HUNTING {
            int delay;

            @Override
            public void onEnter(GameContext context) {
                //TODO reconsider this
                delay = context.isCurrentGameVariant("MS_PACMAN_TENGEN") ? Globals.NUM_TICKS_PER_SEC : 0;
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
                final GameLevel gameLevel = context.gameLevel();

                if (timer.tickCount() < delay) {
                    return;
                }

                if (timer.tickCount() == delay) {
                    gameLevel.optMessage().filter(message -> message.type() == MessageType.READY).ifPresent(message -> {
                        gameLevel.clearMessage(); // leave TEST message alone
                    });
                    game.startHunting(gameLevel);
                }

                gameLevel.pac().tick(context);
                gameLevel.ghosts().forEach(ghost -> ghost.tick(context));
                gameLevel.bonus().ifPresent(bonus -> bonus.tick(context));
                game.updateHunting(gameLevel);

                // What next?
                if (game.isLevelCompleted(gameLevel)) {
                    game.changeState(LEVEL_COMPLETE);
                }
                else if (game.hasPacManBeenKilled()) {
                    game.changeState(PACMAN_DYING);
                }
                else if (game.hasGhostBeenKilled()) {
                    game.changeState(GHOST_DYING);
                }
            }

            @Override
            public void onExit(GameContext context) {
                context.gameLevel().optMessage().ifPresent(message -> {
                    if (message.type() == MessageType.READY) {
                        context.gameLevel().clearMessage();
                    }
                });
            }
        },

        LEVEL_COMPLETE {

            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely(); // UI triggers timeout
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();

                if (timer.tickCount() == 1) {
                    game.onLevelCompleted(context.gameLevel());
                }

                //TODO this is crap. Maybe Tengen Ms. Pac-Man needs its own state machine?
                if (context.isCurrentGameVariant(StandardGameVariant.MS_PACMAN_TENGEN.name())
                    && context.gameLevel().isDemoLevel()) {
                    game.changeState(SHOWING_HALL_OF_FAME);
                    return;
                }

                if (timer.hasExpired()) {
                    if (context.gameLevel().isDemoLevel()) {
                        // just in case: if demo level was completed, go back to intro scene
                        game.changeState(INTRO);
                    } else if (game.cutScenesEnabled()
                        && game.optCutSceneNumber(context.gameLevel().number()).isPresent()) {
                        game.changeState(INTERMISSION);
                    } else {
                        game.changeState(LEVEL_TRANSITION);
                    }
                }
            }
        },

        LEVEL_TRANSITION {
            @Override
            public void onEnter(GameContext context) {
                timer.restartSeconds(2);
                context.currentGame().startNextLevel();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        GHOST_DYING {
            @Override
            public void onEnter(GameContext context) {
                timer.restartSeconds(1);
                context.gameLevel().pac().hide();
                context.gameLevel().ghosts().forEach(Ghost::stopAnimation);
                context.currentGame().publishGameEvent(GameEvent.Type.GHOST_EATEN);
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().stateMachine().resumePreviousState();
                } else {
                    context.gameLevel().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                        .forEach(ghost -> ghost.tick(context));
                    context.gameLevel().blinking().tick();
                }
            }

            @Override
            public void onExit(GameContext context) {
                context.gameLevel().pac().show();
                context.gameLevel().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
                context.gameLevel().ghosts()
                    .forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
            }
        },

        PACMAN_DYING {
            static final int TICK_HIDE_GHOSTS = 60;
            static final int TICK_START_PAC_ANIMATION = 90;
            static final int TICK_HIDE_PAC = 190;
            static final int TICK_PAC_DEAD = 240;

            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
                context.currentGame().onPacKilled(context.gameLevel());
                context.currentGame().publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();

                if (timer.hasExpired()) {
                    if (context.gameLevel().isDemoLevel()) {
                        game.changeState(GAME_OVER);
                    } else {
                        game.addLives(-1);
                        game.changeState(game.lifeCount() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                    }
                }
                else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                    context.gameLevel().ghosts().forEach(Ghost::hide);
                    //TODO this does not belong here
                    context.gameLevel().pac().optAnimationManager().ifPresent(am -> {
                        am.select(CommonAnimationID.ANIM_PAC_DYING);
                        am.reset();
                    });
                }
                else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                    context.gameLevel().pac().optAnimationManager().ifPresent(AnimationManager::play);
                    context.currentGame().publishGameEvent(GameEvent.Type.PAC_DYING, context.gameLevel().pac().tile());
                }
                else if (timer.tickCount() == TICK_HIDE_PAC) {
                    context.gameLevel().pac().hide();
                }
                else if (timer.tickCount() == TICK_PAC_DEAD) {
                    context.currentGame().publishGameEvent(GameEvent.Type.PAC_DEAD);
                }
                else {
                    context.gameLevel().blinking().tick();
                    context.gameLevel().pac().tick(context);
                }
            }

            @Override
            public void onExit(GameContext context) {
                context.gameLevel().bonus().ifPresent(Bonus::setInactive);
            }
        },

        GAME_OVER {
            @Override
            public void onEnter(GameContext context) {
                timer.restartTicks(context.gameLevel().gameOverStateTicks());
                context.currentGame().onGameEnding(context.gameLevel());
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();

                if (timer.hasExpired()) {
                    //TODO find unified solution
                    if (context.isCurrentGameVariant(StandardGameVariant.MS_PACMAN_TENGEN.name())) {
                        if (context.gameLevel().isDemoLevel()) {
                            game.changeState(SHOWING_HALL_OF_FAME);
                        } else {
                            boolean canContinue = game.canContinueOnGameOver();
                            game.changeState(canContinue ? SETTING_OPTIONS_FOR_START : INTRO);
                        }
                    } else {
                        game.prepareForNewGame();
                        if (game.canStartNewGame()) {
                            game.changeState(SETTING_OPTIONS_FOR_START);
                        } else {
                            game.changeState(INTRO);
                        }
                    }
                }
            }

            @Override
            public void onExit(GameContext context) {
                context.optGameLevel().ifPresent(GameLevel::clearMessage);
                context.cheatUsedProperty().set(false);
            }
        },

        INTERMISSION {
            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().changeState(
                        context.currentGame().isPlaying() ? LEVEL_TRANSITION : INTRO);
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }
}
