/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;

public class Arcade_GameController extends StateMachine<FsmState<Game>, Game> implements GameControl {

    public Arcade_GameController() {
        setName("Arcade Pac-Man Games State Machine");
        addStates(GameState.values());
    }

    @Override
    public StateMachine<FsmState<Game>, Game> stateMachine() {
        return this;
    }

    public enum GameState implements FsmState<Game> {

        /**
         * Corresponds to the screen showing all these random symbols from the Arcade video memory.
         */
        BOOT {
            // "Das muss das Boot abkönnen!"
            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely();
                game.resetEverything();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) { // timer is set to expired by UI
                    game.control().changeState(INTRO);
                }
            }
        },

        /**
         * Corresponds to the intro screen with the Pac-Man and ghost animations.
         */
        INTRO {
            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    // start demo level (attract mode)
                    game.control().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        /**
         * Corresponds to the start screen of the Arcade Pac-Man games.
         */
        SETTING_OPTIONS_FOR_START {
            @Override
            public void onUpdate(Game game) {
                // wait for user interaction to start playing
            }
        },

        STARTING_GAME_OR_LEVEL {
            @Override
            public void onEnter(Game game) {
                game.publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
            }

            @Override
            public void onUpdate(Game game) {
                long tick = timer.tickCount();
                if (game.isPlaying()) {
                    game.continuePlaying(tick);
                }
                else if (game.canStartNewGame()) {
                    game.startNewGame(tick);
                }
                else {
                    game.startDemoLevel(tick);
                }
            }
        },

        HUNTING {
            @Override
            public void onEnter(Game game) {
                game.level().optMessage().filter(message -> message.type() == MessageType.READY).ifPresent(message -> {
                    game.level().clearMessage(); // leave TEST message alone
                });
                game.startHunting();
            }

            @Override
            public void onUpdate(Game game) {
                game.updateHunting();
                if (game.isLevelCompleted()) {
                    game.control().changeState(LEVEL_COMPLETE);
                }
                else if (game.hasPacManBeenKilled()) {
                    game.control().changeState(PACMAN_DYING);
                }
                else if (game.hasGhostBeenKilled()) {
                    game.control().changeState(EATING_GHOST);
                }
            }

            @Override
            public void onExit(Game game) {
                game.level().optMessage().ifPresent(message -> {
                    if (message.type() == MessageType.READY) {
                        game.level().clearMessage();
                    }
                });
            }
        },

        LEVEL_COMPLETE {
            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely(); // UI triggers timeout
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.tickCount() == 1) {
                    game.onLevelCompleted();
                }
                else if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        // just in case: if demo level was completed, go back to intro scene
                        game.control().changeState(INTRO);
                    } else if (game.cutScenesEnabled() && game.level().cutSceneNumber() != 0) {
                        game.control().changeState(INTERMISSION);
                    } else {
                        game.control().changeState(LEVEL_TRANSITION);
                    }
                }
            }
        },

        LEVEL_TRANSITION {
            @Override
            public void onEnter(Game game) {
                timer.restartSeconds(2);
                game.startNextLevel();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        EATING_GHOST {
            @Override
            public void onEnter(Game game) {
                timer.restartTicks(Arcade_GameModel.TICK_EATING_GHOST_COMPLETE);
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().resumePreviousState();
                } else {
                    game.updateEatingGhost(timer.tickCount());
                }
            }
        },

        PACMAN_DYING {
            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely(); // UI triggers time-out
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        game.control().changeState(GAME_OVER);
                    } else {
                        game.addLives(-1);
                        game.control().changeState(game.lifeCount() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                    }
                }
                else {
                    game.updatePacManDying(timer.tickCount());
                }
            }
        },

        GAME_OVER {
            @Override
            public void onEnter(Game game) {
                timer.restartTicks(game.level().gameOverStateTicks());
                game.onGameOver();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.prepareForNewGame();
                    if (game.canStartNewGame()) {
                        game.control().changeState(SETTING_OPTIONS_FOR_START);
                    } else {
                        game.control().changeState(INTRO);
                    }
                }
            }

            @Override
            public void onExit(Game game) {
                game.optGameLevel().ifPresent(GameLevel::clearMessage);
                game.cheatUsedProperty().set(false);
            }
        },

        INTERMISSION {
            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().changeState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
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