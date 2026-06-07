/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameContinuedEvent;
import de.amr.pacmanfx.event.GameStartedEvent;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HUDState;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public enum TengenMsPacMan_GameState {

    /**
     * Corresponds to the screen showing the "TENGEN PRESENTS" text and the red ghost running over the screen.
     */
    BOOT (new GameBootState()),

    /**
     * Corresponds to the screen showing the "TENGEN PRESENTS MS. PAC-MAN" title,
     * the "PRESS START" and copyright text.
     * <p>
     * If no key is pressed for some time, the UI shows to the Ms. Pac-Man intro scene with the
     * ghost presentation. If still no key is pressed, the demo level is shown. After the demo
     * level ends, the credits screens are shown and then again the "PRESS START" scene.
     * </p>
     */
    GAME_INTRO (new GameIntroState()),

    /**
     * Corresponds to the "MS PAC-MAN OPTIONS" screen where difficulty, booster, map category
     * and start level can be set.
     */
    GAME_PREPARATION (new GamePreparationState()),

    /**
     * Corresponds to the screen showing the people that have contributed to the game. Here, a seconds
     * screen with the contributors to the remake has been added.
     */
    SHOWING_HALL_OF_FAME (new GameState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME) {
        
        @Override
        public void onEnter(GameContext gameContext) {
            lock();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer().hasExpired()) {
                gameContext.flow().enterState(GameStateID.GAME_INTRO);
            }
        }
    }),

    GAME_OR_LEVEL_STARTING( new GameState(GameStateID.GAME_OR_LEVEL_STARTING) {
        
        @Override
        public void onEnter(GameContext gameContext) {
            final TengenMsPacMan_GameModel gameModel = (TengenMsPacMan_GameModel) gameContext.model();
            final TengenMsPacMan_HUDState hud = gameModel.hud();
        
            hud.creditOff().scoreOn().levelCounterOn().livesCounterOn().show();

            // The rules vary between map categories so update the rules here:
            final TengenMsPacMan_GameRules gameRules = (TengenMsPacMan_GameRules) gameContext.rules();
            gameRules.setCurrentMapCategory(gameModel.mapCategory());
        
            Logger.info("Using game rules for map category {}", gameRules.currentMapCategory());
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();
            final long tick = timer().tickCount();
        
            if (gameModel.isPlaying()) {
                gameContext.flow().enterState(GameStateID.GAME_LEVEL_CONTINUE);
            }
            else if (gameModel.canStartNewGame(gameContext)) {
                gameContext.flow().enterState(GameStateID.GAME_STARTING);
            } 
            else {
                gameModel.startDemoLevel(gameContext, tick);
            }
        }
    }),

    GAME_STARTING( new GameState(GameStateID.GAME_STARTING) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();
            
            gameModel.prepareNewGame();
            gameModel.buildNormalLevel(gameContext, tengenMsPacman(gameModel).startLevelNumber());

            gameContext.flow().publishGameEvent(new GameStartedEvent(gameContext));
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();
            final long tick = timer().tickCount();

            if (tick == TengenMsPacMan_GameState.Timing.TICK_SHOW_READY) {
                gameModel.startLevel(gameContext);
            }
            else if (tick == TengenMsPacMan_GameState.Timing.TICK_NEW_GAME_SHOW_GUYS) {
                final GameLevel level = gameContext.requireLevel();
                level.entities().pac().show();
                level.entities().ghosts().forEach(Ghost::show);
            }
            else if (tick == TengenMsPacMan_GameState.Timing.TICK_NEW_GAME_START_HUNTING) {
                gameModel.setPlaying(true);
                gameContext.flow().enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
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

            gameContext.flow().publishGameEvent(new GameContinuedEvent(gameContext));
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final long tick = timer().tickCount();

            if (tick == TengenMsPacMan_GameState.Timing.TICK_RESUME_HUNTING) {
                gameContext.flow().enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_PLAYING( new GameLevelPlayingState()),

    GAME_LEVEL_COMPLETE( new GameState(GameStateID.GAME_LEVEL_COMPLETE) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();

            lock(); // Waits for UI to trigger timeout
            gameModel.onLevelCompleted(gameContext.requireLevel());
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.flow();
            final GameLevel level = gameContext.requireLevel();

            if (level.isDemoLevel()) {
                flow.enterState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
                return;
            }

            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    // Just in case: if demo level is completed, go back to intro scene
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

    GAME_LEVEL_TRANSITION( new GameLevelTransitionState() ),

    GAME_LEVEL_EATING_GHOST( new GameLevelEatingGhostState() ),

    GAME_LEVEL_PACMAN_DYING(
        new GameLevelPacManDyingState(new GameLevelPacManDyingState.Timing(60, 90, 190, 240))
    ),

    GAME_OVER ( new GameState(GameStateID.GAME_OVER) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();
            final GameLevel level = gameContext.requireLevel();

            timer().restartTicks(level.gameOverStateTicks());
            gameModel.onGameOver(gameContext, level);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameFlow flow = gameContext.flow();
            final GameModel gameModel = gameContext.model();
            final GameLevel level = gameContext.requireLevel();

            if (timer().hasExpired()) {
                gameModel.cheats().clear();
                level.clearMessage();

                if (level.isDemoLevel()) {
                    flow.enterState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
                }
                else {
                    flow.enterState(gameModel.canContinueOnGameOver() ? GameStateID.GAME_PREPARATION : GameStateID.GAME_INTRO);
                }
            }
        }
    }),

    GAME_LEVEL_INTERMISSION( new GameState(GameStateID.GAME_LEVEL_INTERMISSION) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.model();
            final GameLevel level = gameContext.requireLevel();
            final var hud = (TengenMsPacMan_HUDState) gameModel.hud();
            final boolean isLastCutScene = level.cutSceneNumber() == gameContext.rules().lastCutSceneNumber();

            lock();

            if (tengenMsPacman(gameModel).mapCategory() == MapCategory.ARCADE || isLastCutScene) {
                hud.hide();
            }
            else {
                hud.gameOptionsOff().scoreOff().levelCounterOn().livesCounterOff().show();
            }
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
            final var hud = (TengenMsPacMan_HUDState) gameModel.hud();

            if (tengenMsPacman(gameModel).mapCategory() == MapCategory.ARCADE) {
                hud.hide();
            }
            else {
                hud.gameOptionsOn().scoreOn().levelCounterOn().livesCounterOff().show();
            }
        }
    });

    final GameState state;

    TengenMsPacMan_GameState(GameState state) {
        this.state = requireNonNull(state);
    }

    public GameState state() {
        return state;
    }

    static TengenMsPacMan_GameModel tengenMsPacman(GameModel game) {
        return (TengenMsPacMan_GameModel) game;
    }

    //TODO make package-private again
    public static class Timing {
        public static final short TICK_SHOW_READY = 10;
        public static final short TICK_NEW_GAME_SHOW_GUYS = 70;
        public static final short TICK_NEW_GAME_START_HUNTING = 250;
        public static final short TICK_RESUME_HUNTING = 240;
        public static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
    }
}
