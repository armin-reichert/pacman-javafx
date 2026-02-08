/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevelMessageType;

public class TengenMsPacMan_GameController extends StateMachine<Game> implements GameControl {

    public TengenMsPacMan_GameController() {
        setName("Tengen Ms. Pac-Man Game State Machine");
        addStates(GameState.values());
    }

    @Override
    public StateMachine<Game> stateMachine() {
        return this;
    }

    public enum GameState implements State<Game> {

        /**
         * Corresponds to the screen showing the "TENGEN PRESENTS" text and the red ghost running over the screen.
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
                if (timer.hasExpired()) {
                    game.control().enterState(INTRO);
                }
            }
        },

        /**
         * Corresponds to the screen showing the "TENGEN PRESENTS MS. PAC-MAN" title,
         * the "PRESS START" and copyright text.
         * <p>
         * If no key is pressed for some time, the UI shows to the Ms. Pac-Man intro scene with the
         * ghost presentation. If still no key is pressed, the demo level is shown. After the demo
         * level ends, the credits screens are shown and then again the "PRESS START" scene.
         * </p>
         */
        INTRO {
            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().enterState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        /**
         * Corresponds to the "MS PAC-MAN OPTIONS" screen where difficulty, booster, map category
         * and start level can be set.
         */
        SETTING_OPTIONS_FOR_START {
            @Override
            public void onUpdate(Game game) {
                // wait for user interaction to leave state
            }
        },

        /**
         * Corresponds to the screen showing the people that have contributed to the game. Here, a seconds
         * screen with the contributors to the remake has been added.
         */
        SHOWING_HALL_OF_FAME {
            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().enterState(INTRO);
                }
            }
        },

        STARTING_GAME_OR_LEVEL {
            @Override
            public void onUpdate(Game game) {
                final long tick = timer.tickCount();
                if (game.isPlaying()) {
                    game.continuePlaying(game.level(), tick);
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
                clearReadyMessage(game);
                game.startHunting(game.level());
            }

            @Override
            public void onUpdate(Game game) {
                game.whileHunting(game.level());
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

            @Override
            public void onExit(Game game) {
                //TODO is this needed?
                clearReadyMessage(game);
            }

            private void clearReadyMessage(Game game) {
                game.level().optMessage().filter(message -> message.type() == GameLevelMessageType.READY).ifPresent(message -> {
                    game.clearLevelMessage(); // leave TEST message alone
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
                    game.onLevelCompleted(game.level());
                }

                if (game.level().isDemoLevel()) {
                    game.control().enterState(SHOWING_HALL_OF_FAME);
                    return;
                }

                if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        // Just in case: if demo level is completed, go back to intro scene
                        game.control().enterState(INTRO);
                    }
                    else if (game.cutScenesEnabled() && game.level().cutSceneNumber() != 0) {
                        game.control().enterState(INTERMISSION);
                    }
                    else {
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
                timer.restartTicks(TengenMsPacMan_GameModel.TICK_EATING_GHOST_COMPLETE);
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
                    }
                    else {
                        game.addLives(-1);
                        game.control().enterState(game.lifeCount() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                    }
                } else {
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
                    if (game.level().isDemoLevel()) {
                        game.control().enterState(SHOWING_HALL_OF_FAME);
                    }
                    else {
                        game.control().enterState(game.canContinueOnGameOver() ? SETTING_OPTIONS_FOR_START : INTRO);
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