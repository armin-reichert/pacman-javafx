/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
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
            game.hud().hide();
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                game.flow().enterState(INTRO);
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
            game.hud().credit(true).livesCounter(false).levelCounter(true).score(true).show();
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                // Start demo level (attract mode)
                game.flow().enterState(STARTING_GAME_OR_LEVEL);
            }
        }
    },

    /**
     * Corresponds to the start screen of the Arcade Pac-Man games.
     */
    PREPARING_GAME_START {
        @Override
        public void onEnter(Game game) {
            lock();
            game.hud().credit(true).score(true).levelCounter(true).livesCounter(false).show();
            game.prepareNewGame();
        }

        @Override
        public void onUpdate(Game game) {
            // Wait for user interaction (e.g. key press) to start playing
        }
    },

    STARTING_GAME_OR_LEVEL {
        @Override
        public void onEnter(Game game) {
            game.hud().score(true).levelCounter(true).show();
        }

        @Override
        public void onUpdate(Game game) {
            final long tick = timer.tickCount();
            if (game.isPlayingLevel()) {
                final GameLevel level = game.optGameLevel().orElseThrow();
                game.continuePlayingLevel(level, tick);
                game.hud().credit(false).livesCounter(true);
            }
            else if (game.canStartNewGame()) {
                game.startNewGame(tick);
                game.hud().credit(false).livesCounter(true);
            }
            else {
                game.startDemoLevel(tick);
                game.hud().credit(true).livesCounter(false);
            }
        }
    },

    LEVEL_PLAYING {
        @Override
        public void onEnter(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            game.onLevelPlayingStart(level);
        }

        @Override
        public void onUpdate(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            game.doPlayLevel(level);
            if (game.isLevelCompleted(level)) {
                game.flow().enterState(LEVEL_COMPLETE);
            }
            else if (game.hasPacManBeenKilled()) {
                game.flow().enterState(PACMAN_DYING);
            }
            else if (game.hasGhostBeenKilled()) {
                game.flow().enterState(EATING_GHOST);
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
                    game.flow().enterState(INTRO);
                }
                else if (game.flow().cutScenesEnabled() && level.cutSceneNumber() != 0) {
                    game.flow().enterState(INTERMISSION);
                }
                else {
                    game.flow().enterState(LEVEL_TRANSITION);
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
                game.flow().enterState(STARTING_GAME_OR_LEVEL);
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
                game.flow().resumePreviousState();
            } else {
                final GameLevel level = game.optGameLevel().orElseThrow();
                game.doEatingGhost(level, timer.tickCount());
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
                    game.flow().enterState(GAME_OVER);
                } else {
                    game.addLives(-1);
                    game.flow().enterState(game.lifeCount() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                }
            } else {
                game.doPacManDying(level, level.pac(), timer.tickCount());
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
                final GameLevel level = game.optGameLevel().orElseThrow();
                level.clearMessage();
                game.cheats().clearFlag();
                if (game.canStartNewGame()) {
                    game.flow().enterState(PREPARING_GAME_START);
                } else {
                    game.flow().enterState(INTRO);
                }
            }
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
                game.flow().enterState(game.isPlayingLevel() ? LEVEL_TRANSITION : INTRO);
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
