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
            final GameModel gameModel = gameContext.model();
            gameModel.hudState().scoreOn().levelCounterOn().show();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.flow();
            final GameModel gameModel = gameContext.model();

            if (gameModel.isPlaying()) {
                flow.enterState(GameStateID.GAME_LEVEL_CONTINUE);
            }
            else if (gameModel.canStartNewGame(gameContext)) {
                flow.enterState(GameStateID.GAME_STARTING);
            }
            else {
                flow.enterState(GameStateID.DEMO_LEVEL_PLAYING);
            }
        }
    }),

    DEMO_LEVEL_PLAYING(new DemoLevelPlayingState(Timing.TICK_DEMO_LEVEL_HUNTING_START)),

    GAME_STARTING( new GameState(GameStateID.GAME_STARTING) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameFlow flow = gameContext.flow();
            final GameModel gameModel = gameContext.model();

            gameModel.hudState().creditOff().livesCounterOn();
            gameModel.resetForNewGame();
            gameModel.buildNormalLevel(gameContext, 1);

            flow.publishGameEvent(new GameStartedEvent(gameContext));
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.flow();
            final GameModel gameModel = gameContext.model();
            final long tick = timer().tickCount();
            
            if (tick == Arcade_GameState.Timing.TICK_NEW_GAME_START_LEVEL) {
                gameModel.startLevel(gameContext);
            }
            else if (tick == Arcade_GameState.Timing.TICK_NEW_GAME_SHOW_GUYS) {
                final GameLevel level = gameContext.requireLevel();
                level.entities().pac().show();
                level.entities().ghosts().forEach(Ghost::show);
            }
            else if (tick == Arcade_GameState.Timing.TICK_NEW_GAME_START_HUNTING) {
                gameModel.setPlaying(true);
                flow.enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            gameContext.coinMechanism().consumeCoin();
        }
    }),

    GAME_LEVEL_CONTINUE( new GameState(GameStateID.GAME_LEVEL_CONTINUE) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();
            final GameLevel level = gameContext.requireLevel();

            gameModel.prepareLevelForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);

            gameModel.showLevelMessage(level, GameLevelMessageType.READY);
            gameModel.hudState().creditOff().livesCounterOn();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.flow();
            final long tick = timer().tickCount();

            if (tick == Arcade_GameState.Timing.TICK_CONTINUE_LEVEL) {
                flow.publishGameEvent(new GameContinuedEvent(gameContext));
            }
            else if (tick == Arcade_GameState.Timing.TICK_RESUME_HUNTING) {
                flow.enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_PLAYING( new GameLevelPlayingState()),

    GAME_LEVEL_COMPLETE ( new GameState(GameStateID.GAME_LEVEL_COMPLETE) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();
            lock(); // UI triggers timeout
            gameModel.onLevelCompleted(gameContext.requireLevel());
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.flow();
            final GameLevel level = gameContext.requireLevel();

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

    GAME_LEVEL_PACMAN_DYING(new GameLevelPacManDyingState(
        new GameLevelPacManDyingState.Timing(60, 90, 190, 210)
    )),

    GAME_OVER (new GameState(GameStateID.GAME_OVER) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();
            final GameLevel level = gameContext.requireLevel();

            timer().restartTicks(level.gameOverStateTicks());
            //TODO check if this is needed
            gameContext.model().lives().setCount(0);
            gameModel.onGameOver(gameContext, level);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.flow();
            final GameModel gameModel = gameContext.model();

            if (timer().hasExpired()) {
                final GameLevel level = gameContext.requireLevel();
                level.clearMessage();
                gameContext.cheats().clear();
                if (gameModel.canStartNewGame(gameContext)) {
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
            final GameModel gameModel = gameContext.model();
            lock();
            gameModel.hudState().creditOff().scoreOff().levelCounterOn().livesCounterOff().show();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.flow();
            final GameModel gameModel = gameContext.model();

            if (timer().hasExpired()) {
                flow.enterState(gameModel.isPlaying() ? GameStateID.GAME_LEVEL_TRANSITION : GameStateID.GAME_INTRO);
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();
            gameModel.hudState().creditOff().scoreOn().levelCounterOn().livesCounterOn().show();
        }
    });

    final GameState state;

    Arcade_GameState(GameState state) {
        this.state = requireNonNull(state);
    }

    public GameState state() {
        return state;
    }

    public interface Timing {
        int TICK_NEW_GAME_START_LEVEL = 2;
        int TICK_NEW_GAME_SHOW_GUYS = 60;
        int TICK_NEW_GAME_START_HUNTING = 240;
        int TICK_RESUME_HUNTING = 120;
        int TICK_CONTINUE_LEVEL = 60;

        int TICK_DEMO_LEVEL_HUNTING_START = 120;
    }
}
