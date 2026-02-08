/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevelMessageType;

public class Arcade_GameController extends StateMachine<Game> implements GameControl {

    public static final short TICK_NEW_GAME_SHOW_GUYS = 60;
    public static final short TICK_NEW_GAME_START_HUNTING = 240;
    public static final short TICK_RESUME_HUNTING = 120;
    public static final short TICK_EATING_GHOST_COMPLETE = 60;
    public static final short TICK_PACMAN_DYING_HIDE_GHOSTS = 60;
    public static final short TICK_PACMAN_DYING_START_ANIMATION = 90;
    public static final short TICK_PACMAN_DYING_HIDE_PAC = 190;
    public static final short TICK_PACMAN_DYING_PAC_DEAD = 210;

    public Arcade_GameController() {
        setName("Arcade Pac-Man Games State Machine");
        addStates(GameState.values());
    }

    @Override
    public StateMachine<Game> stateMachine() {
        return this;
    }

    public enum GameState implements State<Game> {

        /**
         * Corresponds to the screen showing all these random symbols from the Arcade video memory.
         */
        BOOT {
            // "Das muss das Boot abkönnen!"
            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely();
                game.boot();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) { // timer is set to expired by UI
                    game.control().enterState(INTRO);
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
                    game.control().enterState(STARTING_GAME_OR_LEVEL);
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
                // "GAME_OVER" (demo level) and  "TEST LEVEL XX" messages are not cleared
                game.level().optMessage()
                    .filter(message -> message.type() == GameLevelMessageType.READY)
                    .ifPresent(_ -> game.clearLevelMessage());
                game.startHunting(game.level());
            }

            @Override
            public void onUpdate(Game game) {
                game.updateHunting(game.level());
                if (game.isLevelCompleted()) {
                    game.control().enterState(LEVEL_COMPLETE);
                }
                else if (game.hasPacManBeenKilled()) {
                    game.control().enterState(PACMAN_DYING);
                }
                else if (game.hasGhostBeenKilled()) {
                    game.control().enterState(EATING_GHOST);
                }
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
                    game.onLevelCompleted(game.level());
                }
                else if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        // just in case: if demo level was completed, go back to intro scene
                        game.control().enterState(INTRO);
                    } else if (game.cutScenesEnabled() && game.level().cutSceneNumber() != 0) {
                        game.control().enterState(INTERMISSION);
                    } else {
                        game.control().enterState(LEVEL_TRANSITION);
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
                    game.control().enterState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        EATING_GHOST {
            @Override
            public void onEnter(Game game) {
                timer.restartTicks(TICK_EATING_GHOST_COMPLETE);
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().resumePreviousState();
                } else {
                    game.whileEatingGhost(game.level(), timer.tickCount());
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
                        game.control().enterState(GAME_OVER);
                    } else {
                        game.addLives(-1);
                        game.control().enterState(game.lifeCount() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                    }
                }
                else {
                    game.whilePacManDying(game.level(), timer.tickCount());
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
                    game.prepareNewGame();
                    if (game.canStartNewGame()) {
                        game.control().enterState(SETTING_OPTIONS_FOR_START);
                    } else {
                        game.control().enterState(INTRO);
                    }
                }
            }

            @Override
            public void onExit(Game game) {
                game.clearLevelMessage();
                game.clearCheatFlag();
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
                    game.control().enterState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
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