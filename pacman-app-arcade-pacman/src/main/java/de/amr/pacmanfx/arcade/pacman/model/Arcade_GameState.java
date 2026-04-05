/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessageType;
import org.tinylog.Logger;

public enum Arcade_GameState implements State<Game> {

    /**
     * Corresponds to the screen showing all these random symbols from the Arcade video memory.
     */
    BOOT { // "Das muss das Boot abkönnen! Jawohl Herr Kaleu!"
        @Override
        public void onEnter(Game game) {
            lock(); // UI triggers timer expiration
            game.init();
            game.hud().hide();        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
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
            lock();
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
        public void onEnter(Game game) {
            lock();
        }

        @Override
        public void onUpdate(Game game) {
            // wait for user interaction (e.g. key press)
        }
    },

    STARTING_GAME_OR_LEVEL {
        @Override
        public void onEnter(Game game) {
            game.hud().credit(false).score(true).levelCounter(true).livesCounter(true).show();
        }

        @Override
        public void onUpdate(Game game) {
            final long tick = timer.tickCount();
            if (game.isPlaying()) {
                final GameLevel level = game.optGameLevel().orElseThrow();
                game.continuePlaying(level, tick);
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
            final GameLevel level = game.optGameLevel().orElseThrow();
            // "GAME_OVER" (demo level) and  "TEST LEVEL XX" messages are not cleared
            level.optMessage()
                .filter(message -> message.type() == GameLevelMessageType.READY)
                .ifPresent(_ -> game.clearLevelMessage());
            game.startHunting(level);
        }

        @Override
        public void onUpdate(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            game.whileHunting(level);
            if (game.isLevelCompleted(level)) {
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
            lock(); // UI triggers timeout
            game.onLevelCompleted(game.optGameLevel().orElseThrow());
        }

        @Override
        public void onUpdate(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer.hasExpired()) {
                if (level.isDemoLevel()) {
                    // just in case: if demo level was completed, go back to intro scene
                    game.control().enterState(INTRO);
                }
                else if (game.cutScenesEnabled() && level.cutSceneNumber() != 0) {
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
            timer.restartTicks(TICK_EATING_GHOST_DURATION);
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                game.control().resumePreviousState();
            } else {
                final GameLevel level = game.optGameLevel().orElseThrow();
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
            game.cheating().clearFlag();
        }
    },

    INTERMISSION {
        @Override
        public void onEnter(Game game) {
            lock();
            game.hud().credit(false).score(false).levelCounter(true).livesCounter(false).show();
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                game.control().enterState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }

        @Override
        public void onExit(Game game) {
            game.hud().credit(false).score(true).levelCounter(true).livesCounter(true).show();
        }
    };

    public static final short TICK_EATING_GHOST_DURATION = 60;
    public static final short TICK_NEW_GAME_SHOW_GUYS = 60;
    public static final short TICK_NEW_GAME_START_HUNTING = 240;
    public static final short TICK_RESUME_HUNTING = 120;
    public static final short TICK_PACMAN_DYING_HIDE_GHOSTS = 60;
    public static final short TICK_PACMAN_DYING_START_ANIMATION = 90;
    public static final short TICK_PACMAN_DYING_HIDE_PAC = 190;
    public static final short TICK_PACMAN_DYING_PAC_DEAD = 210;

    final TickTimer timer;

    Arcade_GameState() {
        timer = new TickTimer("Timer-" + name());
        Logger.info("Game state {} created", name());
    }

    @Override
    public TickTimer timer() {
        return timer;
    }
}
