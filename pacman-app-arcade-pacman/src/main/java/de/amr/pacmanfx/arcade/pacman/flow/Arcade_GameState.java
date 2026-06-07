/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameContinuedEvent;
import de.amr.pacmanfx.event.GameStartedEvent;
import de.amr.pacmanfx.flow.GameFlow;
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
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            gameModel.hud().score(true).levelCounter(true).show();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.gameFlow();
            final GameModel gameModel = gameContext.gameModel();
            final long tick = timer().tickCount();

            if (gameModel.isPlaying()) {
                flow.enterState(GameStateID.GAME_LEVEL_CONTINUE);
            }
            else if (gameModel.canStartNewGame(gameContext)) {
                flow.enterState(GameStateID.GAME_STARTING);
            }
            else {
                gameModel.startDemoLevel(gameContext, tick);
                gameModel.hud().credit(true).livesCounter(false);
            }
        }
    }),


    GAME_STARTING( new GameState(GameStateID.GAME_STARTING) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameFlow flow = gameContext.gameFlow();
            final GameModel gameModel = gameContext.gameModel();

            gameModel.hud().credit(false).livesCounter(true);
            gameModel.prepareNewGame();
            gameModel.buildNormalLevel(gameContext, 1);

            flow.publishGameEvent(new GameStartedEvent(gameContext));
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.gameFlow();
            final GameModel gameModel = gameContext.gameModel();
            final long tick = timer().tickCount();
            
            if (tick == Timing.TICK_NEW_GAME_START_LEVEL) {
                gameModel.startLevel(gameContext);
            }
            else if (tick == Timing.TICK_NEW_GAME_SHOW_GUYS) {
                final GameLevel level = gameContext.requireGameLevel();
                level.entities().pac().show();
                level.entities().ghosts().forEach(Ghost::show);
            }
            else if (tick == Timing.TICK_NEW_GAME_START_HUNTING) {
                gameModel.setPlaying(true);
                flow.enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_CONTINUE( new GameState(GameStateID.GAME_LEVEL_CONTINUE) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel game = gameContext.gameModel();
            final GameLevel level = gameContext.requireGameLevel();

            game.prepareLevelForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);

            game.showLevelMessage(level, GameLevelMessageType.READY);
            game.hud().credit(false).livesCounter(true);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.gameFlow();
            final long tick = timer().tickCount();

            if (tick == Timing.TICK_CONTINUE_LEVEL) {
                flow.publishGameEvent(new GameContinuedEvent(gameContext));
            }
            else if (tick == Timing.TICK_RESUME_HUNTING) {
                flow.enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_PLAYING( new GameLevelPlayingState()),

    GAME_LEVEL_COMPLETE ( new GameState(GameStateID.GAME_LEVEL_COMPLETE) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel game = gameContext.gameModel();
            lock(); // UI triggers timeout
            game.onLevelCompleted(gameContext.requireGameLevel());
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.gameFlow();
            final GameModel gameModel = gameContext.gameModel();
            final GameLevel level = gameContext.requireGameLevel();

            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    // just in case: if demo level was completed, go back to intro scene
                    flow.enterState(GameStateID.GAME_INTRO);
                }
                else if (flow.cutScenesEnabled() && level.cutSceneNumber() != 0) {
                    flow.enterState(GameStateID.GAME_LEVEL_INTERMISSION);
                }
                else {
                    flow.enterState(GameStateID.GAME_LEVEL_TRANSITION);
                }
            }
        }
    }),

    GAME_LEVEL_TRANSITION(new GameLevelTransitionState()),

    GAME_LEVEL_EATING_GHOST(new GameLevelEatingGhostState()),

    GAME_LEVEL_PACMAN_DYING(new GameLevelPacManDyingState()),

    GAME_OVER (new GameState(GameStateID.GAME_OVER) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel game = gameContext.gameModel();
            final GameLevel level = gameContext.requireGameLevel();
            timer().restartTicks(level.gameOverStateTicks());
            game.onGameOver(gameContext, level);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.gameFlow();
            final GameModel game = gameContext.gameModel();

            if (timer().hasExpired()) {
                final GameLevel level = gameContext.requireGameLevel();
                level.clearMessage();
                game.cheats().clear();
                if (game.canStartNewGame(gameContext)) {
                    flow.enterState(GameStateID.GAME_PREPARATION);
                } else {
                    flow.enterState(GameStateID.GAME_INTRO);
                }
            }
        }
    }),

    GAME_LEVEL_INTERMISSION(new GameState(GameStateID.GAME_LEVEL_INTERMISSION) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            lock();
            gameModel.hud().credit(false).score(false).levelCounter(true).livesCounter(false).show();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.gameFlow();
            final GameModel gameModel = gameContext.gameModel();

            if (timer().hasExpired()) {
                flow.enterState(gameModel.isPlaying() ? GameStateID.GAME_LEVEL_TRANSITION : GameStateID.GAME_INTRO);
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            gameModel.hud().credit(false).score(true).levelCounter(true).livesCounter(true).show();
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
        public static final int TICK_NEW_GAME_START_LEVEL = 2;
        public static final int TICK_NEW_GAME_SHOW_GUYS = 60;
        public static final int TICK_NEW_GAME_START_HUNTING = 240;
        public static final int TICK_RESUME_HUNTING = 120;
        public static final int TICK_CONTINUE_LEVEL = 60;
    }
}
