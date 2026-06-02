/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.GhostState;

public enum Arcade_GameState implements State<GameModel> {

    /**
     * Corresponds to the screen showing all these random symbols from the Arcade video memory.
     */
    BOOT { // "Das muss das Boot abkönnen! Jawohl Herr Kaleu!"
        @Override
        public void onEnter(GameModel game) {
            lock(); // UI triggers timer expiration
            game.init();
            game.hud().hide();
        }

        @Override
        public void onUpdate(GameModel game) {
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
        public void onEnter(GameModel game) {
            lock();
            game.hud().credit(true).livesCounter(false).levelCounter(true).score(true).show();
        }

        @Override
        public void onUpdate(GameModel game) {
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
        public void onEnter(GameModel game) {
            lock();
            game.hud().credit(true).score(true).levelCounter(true).livesCounter(false).show();
            game.prepareNewGame();
        }

        @Override
        public void onUpdate(GameModel game) {
            // Wait for user interaction (e.g. key press) to start playing
        }
    },

    STARTING_GAME_OR_LEVEL {
        @Override
        public void onEnter(GameModel game) {
            game.hud().score(true).levelCounter(true).show();
        }

        @Override
        public void onUpdate(GameModel game) {
            final long tick = timer.tickCount();
            if (game.isPlayingLevel()) {
                game.continuePlayingLevel(tick);
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
        public void onEnter(GameModel game) {
            game.onStartLevelPlaying();
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doLevelPlaying();
            if (game.isLevelCompleted()) {
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
        public void onEnter(GameModel game) {
            lock(); // UI triggers timeout
            game.onLevelCompleted();
        }

        @Override
        public void onUpdate(GameModel game) {
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
        public void onEnter(GameModel game) {
            timer.restartSeconds(2);
            game.startNextLevel();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                game.flow().enterState(STARTING_GAME_OR_LEVEL);
            }
        }
    },

    EATING_GHOST {
        @Override
        public void onEnter(GameModel game) {
            timer.restartTicks(60);
        }

        @Override
        public void onUpdate(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer.hasExpired()) {
                level.entities().pac().show();
                level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
                level.ghosts().forEach(ghost -> ghost.animations().playSelected());
                game.flow().resumePreviousState();
            } else {
                if (timer.tickCount() < 60) {
                    level.ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                        .forEach(ghost -> ghost.update(level));
                    level.blinking().doTick();
                }
            }
        }
    },

    PACMAN_DYING {
        @Override
        public void onEnter(GameModel game) {
            lock(); // UI triggers time-out
        }

        @Override
        public void onUpdate(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer.hasExpired()) {
                if (level.isDemoLevel()) {
                    game.flow().enterState(GAME_OVER);
                } else {
                    game.lives().add(-1);
                    game.flow().enterState(game.lives().count() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                }
            } else {
                game.doPacManDying(level.entities().pac(), timer.tickCount());
            }
        }
    },

    GAME_OVER {
        @Override
        public void onEnter(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            timer.restartTicks(level.gameOverStateTicks());
            game.onGameOver();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                final GameLevel level = game.optGameLevel().orElseThrow();
                level.clearMessage();
                game.cheats().clear();
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
        public void onEnter(GameModel game) {
            lock();
            game.hud().credit(false).score(false).levelCounter(true).livesCounter(false).show();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                game.flow().enterState(game.isPlayingLevel() ? LEVEL_TRANSITION : INTRO);
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.hud().credit(false).score(true).levelCounter(true).livesCounter(true).show();
        }
    };

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
    }

    @Override
    public TickTimer timer() {
        return timer;
    }
}
