/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameContinuedEvent;
import de.amr.pacmanfx.event.GameStartedEvent;
import de.amr.pacmanfx.gamestate.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HeadsUpDisplay;
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
    GAME_INTRO(new GameIntroState()),

    /**
     * Corresponds to the "MS PAC-MAN OPTIONS" screen where difficulty, booster, map category
     * and start level can be set.
     */
    GAME_PREPARATION(new GamePreparationState()),

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
                gameContext.gameFlow().enterState(GameStateID.GAME_INTRO);
            }
        }
    }),

    GAME_OR_LEVEL_STARTING(new GameState(GameStateID.GAME_OR_LEVEL_STARTING) {
        @Override
        public void onEnter(GameContext gameContext) {
            final TengenMsPacMan_GameModel gameModel = (TengenMsPacMan_GameModel) gameContext.gameModel();
            final TengenMsPacMan_HeadsUpDisplay hud = gameModel.hud();
            hud.credit(false).score(true).levelCounter(true).livesCounter(true).show();

            // The rules vary between map categories so update the rules here:
            final TengenMsPacMan_GameRules gameRules = (TengenMsPacMan_GameRules) gameContext.gameRules();
            gameRules.setCurrentMapCategory(gameModel.mapCategory());
            Logger.info("Using game rules for map category {}", gameRules.currentMapCategory());
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            final long tick = timer().tickCount();
            if (gameModel.isPlaying()) {
                gameContext.gameFlow().enterState(GameStateID.GAME_LEVEL_CONTINUE);
            }
            else if (gameModel.canStartNewGame(gameContext)) {
                gameContext.gameFlow().enterState(GameStateID.GAME_STARTING);
            } else {
                gameModel.startDemoLevel(gameContext, tick);
            }
        }
    }),

    GAME_STARTING(new GameState(GameStateID.GAME_STARTING) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            gameModel.prepareNewGame();
            gameModel.buildNormalLevel(gameContext, tengenGameModel(gameModel).startLevelNumber());
            gameContext.gameFlow().publishGameEvent(new GameStartedEvent(gameContext));
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            final long tick = timer().tickCount();
            if (tick == Timing.TICK_SHOW_READY) {
                gameModel.startLevel(gameContext);
            }
            else if (tick == Timing.TICK_NEW_GAME_SHOW_GUYS) {
                final GameLevel level = gameModel.optGameLevel().orElseThrow();
                level.entities().pac().show();
                level.entities().ghosts().forEach(Ghost::show);
            }
            else if (tick == Timing.TICK_NEW_GAME_START_HUNTING) {
                gameModel.setPlaying(true);
                gameContext.gameFlow().enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_CONTINUE(new GameState(GameStateID.GAME_LEVEL_CONTINUE) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            final GameLevel level = gameModel.optGameLevel().orElseThrow();

            gameModel.prepareLevelForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);

            gameContext.gameFlow().publishGameEvent(new GameContinuedEvent(gameContext));
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final long tick = timer().tickCount();
            if (tick == Timing.TICK_RESUME_HUNTING) {
                gameContext.gameFlow().enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_PLAYING(new GameLevelPlayingState()),

    GAME_LEVEL_COMPLETE(new GameState(GameStateID.GAME_LEVEL_COMPLETE) {
        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            lock(); // UI triggers timeout
            gameModel.onLevelCompleted(gameModel.optGameLevel().orElseThrow());
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            final GameLevel level = gameModel.optGameLevel().orElseThrow();

            if (level.isDemoLevel()) {
                gameContext.gameFlow().enterState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
                return;
            }

            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    // Just in case: if demo level is completed, go back to intro scene
                    gameContext.gameFlow().enterState(GameStateID.GAME_INTRO);
                }
                else if (gameContext.gameFlow().cutScenesEnabled() && level.cutSceneNumber() != 0) {
                    gameContext.gameFlow().enterState(GameStateID.GAME_LEVEL_INTERMISSION);
                }
                else {
                    gameContext.gameFlow().enterState(GameStateID.GAME_LEVEL_TRANSITION);
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
            final GameModel gameModel = gameContext.gameModel();
            final GameLevel level = gameModel.optGameLevel().orElseThrow();
            timer().restartTicks(level.gameOverStateTicks());
            gameModel.onGameOver(gameContext, level);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            if (timer().hasExpired()) {
                final GameLevel level = gameModel.optGameLevel().orElseThrow();
                gameModel.cheats().clear();
                if (level.isDemoLevel()) {
                    gameContext.gameFlow().enterState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
                } else {
                    level.clearMessage();
                    gameContext.gameFlow().enterState(gameModel.canContinueOnGameOver()
                        ? GameStateID.GAME_PREPARATION
                        : GameStateID.GAME_INTRO);
                }
            }
        }
    }),

    GAME_LEVEL_INTERMISSION(new GameState(GameStateID.GAME_LEVEL_INTERMISSION) {

        @Override
        public void onEnter(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            final GameLevel level = gameModel.optGameLevel().orElseThrow();
            final var hud = (TengenMsPacMan_HeadsUpDisplay) gameModel.hud();
            final boolean isLastCutScene = level.cutSceneNumber() == gameContext.gameRules().lastCutSceneNumber();

            lock();

            if (tengenGameModel(gameModel).mapCategory() == MapCategory.ARCADE || isLastCutScene) {
                hud.hide();
            } else {
                hud.show();
                hud.gameOptions(false).score(false).levelCounter(true).livesCounter(false).show();
            }
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            if (timer().hasExpired()) {
                gameContext.gameFlow().enterState(gameModel.isPlaying()
                    ? GameStateID.GAME_LEVEL_TRANSITION
                    : GameStateID.GAME_INTRO);
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            final GameModel gameModel = gameContext.gameModel();
            final var hud = (TengenMsPacMan_HeadsUpDisplay) gameModel.hud();
            if (tengenGameModel(gameModel).mapCategory() == MapCategory.ARCADE) {
                hud.hide();
            } else {
                hud.show();
                hud.all(true).gameOptions(true).score(true).levelCounter(true).livesCounter(false).show();
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

    static TengenMsPacMan_GameModel tengenGameModel(GameModel game) {
        return (TengenMsPacMan_GameModel) game;
    }

    //TODO make package-private again
    public static class Timing {
        public static final short TICK_SHOW_READY = 10;
        public static final short TICK_NEW_GAME_SHOW_GUYS = 70;
        public static final short TICK_NEW_GAME_START_HUNTING = 250;
        public static final short TICK_RESUME_HUNTING = 240;
        public static final short TICK_DEMO_LEVEL_START_HUNTING = 120;

        //TODO check times in common game state
        public static final short TICK_PACMAN_DYING_HIDE_GHOSTS = 60;
        public static final short TICK_PACMAN_DYING_START_PAC_ANIMATION = 90;
        public static final short TICK_PACMAN_DYING_HIDE_PAC = 190;
        public static final short TICK_PACMAN_DYING_PAC_DEAD = 240;
    }
}
