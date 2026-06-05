/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameContinuedEvent;
import de.amr.pacmanfx.event.GameStartedEvent;
import de.amr.pacmanfx.flow.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HeadsUpDisplay;

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
        public void onEnter(GameContext context) {
            lock();
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer().hasExpired()) {
                context.gameFlow().enterState(GAME_INTRO.state());
            }
        }
    }),

    GAME_OR_LEVEL_STARTING(new GameState(GameStateID.GAME_OR_LEVEL_STARTING) {
        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.gameModel();
            game.hud().credit(false).score(true).levelCounter(true).livesCounter(true).show();
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.gameModel();
            final long tick = timer().tickCount();
            if (game.isPlaying()) {
                context.gameFlow().enterState(GameStateID.GAME_LEVEL_CONTINUE);
            }
            else if (game.canStartNewGame()) {
                context.gameFlow().enterState(GameStateID.GAME_STARTING);
            } else {
                game.startDemoLevel(tick);
            }
        }
    }),

    GAME_STARTING(new GameState(GameStateID.GAME_STARTING) {

        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.gameModel();
            game.prepareNewGame();
            game.buildNormalLevel(tengenGame(game).startLevelNumber());
            context.gameFlow().publishGameEvent(new GameStartedEvent(context));
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.gameModel();
            final long tick = timer().tickCount();
            if (tick == Timing.TICK_SHOW_READY) {
                game.startLevel();
            }
            else if (tick == Timing.TICK_NEW_GAME_SHOW_GUYS) {
                final GameLevel level = game.optGameLevel().orElseThrow();
                level.entities().pac().show();
                level.entities().ghosts().forEach(Ghost::show);
            }
            else if (tick == Timing.TICK_NEW_GAME_START_HUNTING) {
                game.setPlaying(true);
                context.gameFlow().enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_CONTINUE(new GameState(GameStateID.GAME_LEVEL_CONTINUE) {

        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.gameModel();
            final GameLevel level = game.optGameLevel().orElseThrow();

            game.prepareLevelForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);

            context.gameFlow().publishGameEvent(new GameContinuedEvent(context));
        }

        @Override
        public void onUpdate(GameContext context) {
            final long tick = timer().tickCount();
            if (tick == Timing.TICK_RESUME_HUNTING) {
                context.gameFlow().enterState(GameStateID.GAME_LEVEL_PLAYING);
            }
        }
    }),

    GAME_LEVEL_PLAYING(new GameLevelPlayingState()),

    GAME_LEVEL_COMPLETE(new GameState(GameStateID.GAME_LEVEL_COMPLETE) {
        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.gameModel();
            lock(); // UI triggers timeout
            game.onLevelCompleted(game.optGameLevel().orElseThrow());
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.gameModel();
            final GameLevel level = game.optGameLevel().orElseThrow();

            if (level.isDemoLevel()) {
                context.gameFlow().enterState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
                return;
            }

            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    // Just in case: if demo level is completed, go back to intro scene
                    context.gameFlow().enterState(GameStateID.GAME_INTRO);
                }
                else if (context.gameFlow().cutScenesEnabled() && level.cutSceneNumber() != 0) {
                    context.gameFlow().enterState(GameStateID.GAME_LEVEL_INTERMISSION);
                }
                else {
                    context.gameFlow().enterState(GameStateID.GAME_LEVEL_TRANSITION);
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
            final GameModel game = context.gameModel();
            final GameLevel level = game.optGameLevel().orElseThrow();
            timer().restartTicks(level.gameOverStateTicks());
            game.onGameOver(level);
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.gameModel();
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    context.gameFlow().enterState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
                } else {
                    level.clearMessage();
                    game.cheats().clear();
                    context.gameFlow().enterState(game.canContinueOnGameOver()
                        ? GameStateID.GAME_PREPARATION
                        : GameStateID.GAME_INTRO);
                }
            }
        }
    }),

    GAME_LEVEL_INTERMISSION(new GameState("GAME_LEVEL_INTERMISSION") {
        @Override
        public void onEnter(GameContext context) {
            final GameModel game = context.gameModel();
            lock();
            final var tengenHUD = (TengenMsPacMan_HeadsUpDisplay) game.hud();
            final TengenMsPacMan_GameModel tengenGame = tengenGame(game);
            final GameLevel level = game.optGameLevel().orElseThrow();
            final boolean lastCutSceneReached =  level.cutSceneNumber() == tengenGame.rules().lastCutSceneNumber();
            if (tengenGame.mapCategory() == MapCategory.ARCADE || lastCutSceneReached) {
                tengenHUD.hide();
            } else {
                tengenHUD.show();
                tengenHUD.gameOptions(false).score(false).levelCounter(true).livesCounter(false).show();
            }
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameModel game = context.gameModel();
            if (timer().hasExpired()) {
                context.gameFlow().enterState(game.isPlaying()
                    ? GameStateID.GAME_LEVEL_TRANSITION
                    : GameStateID.GAME_INTRO);
            }
        }

        @Override
        public void onExit(GameContext context) {
            final GameModel game = context.gameModel();
            final var tengenHUD = (TengenMsPacMan_HeadsUpDisplay) game.hud();
            final TengenMsPacMan_GameModel tengenGame = tengenGame(game);
            if (tengenGame.mapCategory() == MapCategory.ARCADE) {
                tengenHUD.hide();
            } else {
                tengenHUD.show();
                tengenHUD.all(true).gameOptions(true).score(true).levelCounter(true).livesCounter(false).show();
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

    static TengenMsPacMan_GameModel tengenGame(GameModel game) {
        return (TengenMsPacMan_GameModel) game;
    }

    public static class Timing {
        public static final short TICK_SHOW_READY = 10;
        public static final short TICK_NEW_GAME_SHOW_GUYS = 70;
        public static final short TICK_NEW_GAME_START_HUNTING = 250;
        public static final short TICK_RESUME_HUNTING = 240;
        public static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
    }

}
