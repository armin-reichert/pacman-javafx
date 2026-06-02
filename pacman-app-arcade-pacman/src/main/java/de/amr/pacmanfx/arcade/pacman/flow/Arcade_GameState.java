/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.flow.GameState;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public enum Arcade_GameState {

    /**
     * Corresponds to the screen showing all these random symbols from the Arcade video memory.
     */
    BOOT (new GameState("BOOT") {

        // "Das muss das Boot abkönnen! Jawohl, Herr Kaleu!"

        @Override
        public void onEnter(GameModel game) {
            lock(); // UI triggers timer expiration
            game.init();
            game.hud().hide();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().hasExpired()) {
                game.flow().enterState(GAME_INTRO.state);
            }
        }
    }),

    /**
     * Corresponds to the intro screen with the Pac-Man and ghost animations.
     */
    GAME_INTRO(new GameState("GAME_INTRO") {
        @Override
        public void onEnter(GameModel game) {
            lock();
            game.hud().credit(true).livesCounter(false).levelCounter(true).score(true).show();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().hasExpired()) {
                // Start demo level (attract mode)
                game.flow().enterState(GAME_STARTING_NEW_GAME_OR_LEVEL.state);
            }
        }
    }),

    /**
     * Corresponds to the start screen of the Arcade Pac-Man games.
     */
    GAME_PREPARATION(new GameState("GAME_PREPARATION") {
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
    }),

    GAME_STARTING_NEW_GAME_OR_LEVEL(new GameState("GAME_STARTING_NEW_GAME_OR_LEVEL") {
        @Override
        public void onEnter(GameModel game) {
            game.hud().score(true).levelCounter(true).show();
        }

        @Override
        public void onUpdate(GameModel game) {
            final long tick = timer().tickCount();
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
    }),

    GAME_LEVEL_PLAYING(new GameState("GAME_LEVEL_PLAYING") {
        @Override
        public void onEnter(GameModel game) {
            game.onStartLevelPlaying();
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doLevelPlaying();
            if (game.isLevelCompleted()) {
                game.flow().enterState(GAME_LEVEL_COMPLETE.state);
            }
            else if (game.hasPacManBeenKilled()) {
                game.flow().enterState(GAME_LEVEL_PACMAN_DYING.state);
            }
            else if (game.hasGhostBeenKilled()) {
                game.flow().enterState(GAME_LEVEL_EATING_GHOST.state);
            }
        }
    }),

    GAME_LEVEL_COMPLETE (new GameState("GAME_LEVEL_COMPLETE") {
        @Override
        public void onEnter(GameModel game) {
            lock(); // UI triggers timeout
            game.onLevelCompleted();
        }

        @Override
        public void onUpdate(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    // just in case: if demo level was completed, go back to intro scene
                    game.flow().enterState(GAME_INTRO.state);
                }
                else if (game.flow().cutScenesEnabled() && level.cutSceneNumber() != 0) {
                    game.flow().enterState(GAME_LEVEL_INTERMISSION.state);
                }
                else {
                    game.flow().enterState(GAME_LEVEL_TRANSITION.state);
                }
            }
        }
    }),

    GAME_LEVEL_TRANSITION(new GameState("GAME_LEVEL_TRANSITION") {
        @Override
        public void onEnter(GameModel game) {
            timer().restartSeconds(2);
            game.startNextLevel();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().hasExpired()) {
                game.flow().enterState(GAME_STARTING_NEW_GAME_OR_LEVEL.state);
            }
        }
    }),

    GAME_LEVEL_EATING_GHOST(new GameState("GAME_LEVEL_EATING_GHOST") {
        @Override
        public void onEnter(GameModel game) {
            timer().restartTicks(60);
        }

        @Override
        public void onUpdate(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer().hasExpired()) {
                level.entities().pac().show();
                level.ghostsInState(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
                level.entities().ghosts().forEach(ghost -> ghost.animations().playSelected());
                game.flow().resumePreviousState();
            } else {
                if (timer().tickCount() < 60) {
                    level.ghostsInAnyOfStates(Set.of(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE))
                        .forEach(ghost -> ghost.update(level));
                    level.blinking().doTick();
                }
            }
        }
    }),

    GAME_LEVEL_PACMAN_DYING(new GameState("GAME_LEVEL_PACMAN_DYING") {
        @Override
        public void onEnter(GameModel game) {
            lock(); // UI triggers time-out
        }

        @Override
        public void onUpdate(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    game.flow().enterState(GAME_OVER.state);
                } else {
                    game.lives().add(-1);
                    game.flow().enterState(game.lives().count() == 0 ? GAME_OVER.state : GAME_STARTING_NEW_GAME_OR_LEVEL.state);
                }
            } else {
                game.doPacManDying(level.entities().pac(), timer().tickCount());
            }
        }
    }),

    GAME_OVER (new GameState("GAME_OVER") {
        @Override
        public void onEnter(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            timer().restartTicks(level.gameOverStateTicks());
            game.onGameOver();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().hasExpired()) {
                final GameLevel level = game.optGameLevel().orElseThrow();
                level.clearMessage();
                game.clearCheats();
                if (game.canStartNewGame()) {
                    game.flow().enterState(GAME_PREPARATION.state);
                } else {
                    game.flow().enterState(GAME_INTRO.state);
                }
            }
        }
    }),

    GAME_LEVEL_INTERMISSION(new GameState("GAME_LEVEL_INTERMISSION") {
        @Override
        public void onEnter(GameModel game) {
            lock();
            game.hud().credit(false).score(false).levelCounter(true).livesCounter(false).show();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().hasExpired()) {
                game.flow().enterState(game.isPlayingLevel() ? GAME_LEVEL_TRANSITION.state : GAME_INTRO.state);
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.hud().credit(false).score(true).levelCounter(true).livesCounter(true).show();
        }
    });

    final GameState state;

    Arcade_GameState(GameState state) {
        this.state = requireNonNull(state);
    }

    public GameState state() {
        return state;
    }

    public static final short TICK_NEW_GAME_SHOW_GUYS = 60;
    public static final short TICK_NEW_GAME_START_HUNTING = 240;
    public static final short TICK_RESUME_HUNTING = 120;
    public static final short TICK_PACMAN_DYING_HIDE_GHOSTS = 60;
    public static final short TICK_PACMAN_DYING_START_ANIMATION = 90;
    public static final short TICK_PACMAN_DYING_HIDE_PAC = 190;
    public static final short TICK_PACMAN_DYING_PAC_DEAD = 210;

}
