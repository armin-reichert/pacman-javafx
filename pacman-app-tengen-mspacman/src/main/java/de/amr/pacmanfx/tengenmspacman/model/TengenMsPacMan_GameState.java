/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessageType;
import org.tinylog.Logger;

public enum TengenMsPacMan_GameState implements State<Game> {

    /**
     * Corresponds to the screen showing the "TENGEN PRESENTS" text and the red ghost running over the screen.
     */
    BOOT { // "Das muss das Boot abkönnen!"
        @Override
        public void onEnter(Game game) {
            lock();
            game.init();
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
            lock();
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
            lock();
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
                final GameLevel level = game.optGameLevel().orElseThrow();
                game.continuePlaying(level, tick);
            } else if (game.canStartNewGame()) {
                game.startNewGame(tick);
            } else {
                game.startDemoLevel(tick);
            }
        }
    },

    HUNTING {
        @Override
        public void onEnter(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            clearReadyMessage(game);
            game.startHunting(level);
        }

        @Override
        public void onUpdate(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            game.whileHunting(level);
            if (game.isLevelCompleted(level)) {
                game.control().enterState(LEVEL_COMPLETE);
            } else if (game.hasPacManBeenKilled()) {
                game.control().enterState(PACMAN_DYING);
            } else if (game.hasGhostBeenKilled()) {
                game.control().enterState(EATING_GHOST);
            }
        }

        @Override
        public void onExit(Game game) {
            //TODO is this needed?
            clearReadyMessage(game);
        }

        private void clearReadyMessage(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            level.optMessage().filter(message -> message.type() == GameLevelMessageType.READY).ifPresent(_ -> {
                game.clearLevelMessage(); // leave TEST message alone
            });
        }
    },

    LEVEL_COMPLETE {
        @Override
        public void onEnter(Game game) {
            lock(); // UI triggers timeout
        }

        @Override
        public void onUpdate(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer.tickCount() == 1) {
                game.onLevelCompleted(level);
            }

            if (level.isDemoLevel()) {
                game.control().enterState(SHOWING_HALL_OF_FAME);
                return;
            }

            if (timer.hasExpired()) {
                if (level.isDemoLevel()) {
                    // Just in case: if demo level is completed, go back to intro scene
                    game.control().enterState(INTRO);
                } else if (game.cutScenesEnabled() && level.cutSceneNumber() != 0) {
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
            timer.restartTicks(TengenMsPacMan_GameModel.TICK_EATING_GHOST_COMPLETE);
        }

        @Override
        public void onUpdate(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer.hasExpired()) {
                game.control().resumePreviousState();
            } else {
                game.whileEatingGhost(level, timer.tickCount());
            }
        }
    },


    PACMAN_DYING {
        @Override
        public void onEnter(Game game) {
            lock(); // UI triggers time-out
        }

        @Override
        public void onUpdate(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer.hasExpired()) {
                if (level.isDemoLevel()) {
                    game.control().enterState(GAME_OVER);
                } else {
                    game.addLives(-1);
                    game.control().enterState(game.lifeCount() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                }
            } else {
                game.whilePacManDying(level, level.pac(), timer.tickCount());
            }
        }
    },

    GAME_OVER {
        @Override
        public void onEnter(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            timer.restartTicks(level.gameOverStateTicks());
            game.onGameOver();
        }

        @Override
        public void onUpdate(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer.hasExpired()) {
                if (level.isDemoLevel()) {
                    game.control().enterState(SHOWING_HALL_OF_FAME);
                } else {
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
            lock();
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                game.control().enterState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    };

    final TickTimer timer;

    TengenMsPacMan_GameState() {
        timer = new TickTimer("Timer-" + name());
        Logger.info("Game state {} created", name());
    }

    @Override
    public TickTimer timer() {
        return timer;
    }
}
