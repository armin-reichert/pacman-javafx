/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameContinuedEvent;
import de.amr.pacmanfx.event.GameStartedEvent;
import de.amr.pacmanfx.gamestate.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

import static java.util.Objects.requireNonNull;

public enum Arcade_GameState {

    /**
     * Corresponds to the screen showing all these random symbols from the Arcade video memory.
     */
    BOOT (new GameBootState()),

    /**
     * Corresponds to the intro screen with the Pac-Man and ghost animations.
     */
    GAME_INTRO(new GameIntroState()),

    /**
     * Corresponds to the start screen of the Arcade Pac-Man games.
     */
    GAME_PREPARATION(new GamePreparationState()),

    GAME_OR_LEVEL_STARTING(new GameState(GameStateID.GAME_OR_LEVEL_STARTING) {

        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.game();
            game.hud().score(true).levelCounter(true).show();
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.game();
            final long tick = timer().tickCount();
            if (game.isPlaying()) {
                context.flow().enterState(GameStateID.GAME_LEVEL_CONTINUE);
            }
            else if (game.canStartNewGame()) {
                context.flow().enterState(GameStateID.GAME_STARTING);
            }
            else {
                game.startDemoLevel(tick);
                game.hud().credit(true).livesCounter(false);
            }
        }
    }),


    GAME_STARTING( new GameState(GameStateID.GAME_STARTING) {

        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.game();
            game.hud().credit(false).livesCounter(true);
            game.prepareNewGame();
            game.buildNormalLevel(1);
            context.flow().publishGameEvent(new GameStartedEvent(context));
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.game();
            final long tick = timer().tickCount();
            if (tick == Timing.TICK_NEW_GAME_START_LEVEL) {
                game.startLevel();
            }
            else if (tick == Timing.TICK_NEW_GAME_SHOW_GUYS) {
                final GameLevel level = game.optGameLevel().orElseThrow();
                level.entities().pac().show();
                level.entities().ghosts().forEach(Ghost::show);
            }
            else if (tick == Timing.TICK_NEW_GAME_START_HUNTING) {
                game.setPlaying(true);
                context.flow().enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_CONTINUE(new GameState(GameStateID.GAME_LEVEL_CONTINUE) {

        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.game();
            final GameLevel level = game.optGameLevel().orElseThrow();

            game.prepareLevelForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);

            game.showLevelMessage(level, GameLevelMessageType.READY);
            game.hud().credit(false).livesCounter(true);

            context.flow().publishGameEvent(new GameContinuedEvent(context));
        }

        @Override
        public void onUpdate(GameContext context) {
            final long tick = timer().tickCount();
            if (tick == 60) {
                context.flow().publishGameEvent(new GameContinuedEvent(context));
            }
            else if (tick == Timing.TICK_RESUME_HUNTING) {
                context.flow().enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_PLAYING(new GameLevelPlayingState()),

    GAME_LEVEL_COMPLETE (new GameState(GameStateID.GAME_LEVEL_COMPLETE) {

        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.game();
            lock(); // UI triggers timeout
            game.onLevelCompleted(game.optGameLevel().orElseThrow());
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.game();
            final GameLevel level = game.optGameLevel().orElseThrow();

            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    // just in case: if demo level was completed, go back to intro scene
                    context.flow().enterState(GameStateID.GAME_INTRO);
                }
                else if (context.flow().cutScenesEnabled() && level.cutSceneNumber() != 0) {
                    context.flow().enterState(GameStateID.GAME_LEVEL_INTERMISSION);
                }
                else {
                    context.flow().enterState(GameStateID.GAME_LEVEL_TRANSITION);
                }
            }
        }
    }),

    GAME_LEVEL_TRANSITION(new GameLevelTransitionState()),

    GAME_LEVEL_EATING_GHOST(new GameLevelEatingGhostState()),

    GAME_LEVEL_PACMAN_DYING(new GameLevelPacManDyingState()),

    GAME_OVER (new GameState(GameStateID.GAME_OVER) {

        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.game();
            final GameLevel level = game.optGameLevel().orElseThrow();
            timer().restartTicks(level.gameOverStateTicks());
            game.onGameOver(level);
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.game();
            if (timer().hasExpired()) {
                final GameLevel level = game.optGameLevel().orElseThrow();
                level.clearMessage();
                game.cheats().clear();
                if (game.canStartNewGame()) {
                    context.flow().enterState(GameStateID.GAME_PREPARATION);
                } else {
                    context.flow().enterState(GameStateID.GAME_INTRO);
                }
            }
        }
    }),

    GAME_LEVEL_INTERMISSION(new GameState(GameStateID.GAME_LEVEL_INTERMISSION) {

        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.game();
            lock();
            game.hud().credit(false).score(false).levelCounter(true).livesCounter(false).show();
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.game();
            if (timer().hasExpired()) {
                context.flow().enterState(game.isPlaying()
                    ? GameStateID.GAME_LEVEL_TRANSITION
                    : GameStateID.GAME_INTRO
                );
            }
        }

        @Override
        public void onExit(GameContext context) {
            final GameModel game = context.game();
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

    public static class Timing {
        public static final short TICK_NEW_GAME_START_LEVEL = 2;
        public static final short TICK_NEW_GAME_SHOW_GUYS = 60;
        public static final short TICK_NEW_GAME_START_HUNTING = 240;
        public static final short TICK_RESUME_HUNTING = 120;
    }
}
