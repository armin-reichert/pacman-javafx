/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import org.tinylog.Logger;

public enum TengenMsPacMan_GameState implements State<Game> {

    /**
     * Corresponds to the screen showing the "TENGEN PRESENTS" text and the red ghost running over the screen.
     */
    BOOT { // "Das muss das Boot abkönnen!"
        @Override
        public void onEnter(Game game) {
            lock();
            game.init();
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                game.flow().enterState(INTRO);
            }
        }
    },

    /**
     * Corresponds to the screen showing the "TENGEN PRESENTS MS. PAC-MAN" title,
     * the "PRESS START" and copyright text.
     * <p>
     * If no key is pressed for some time, the UI shows to the Ms. Pac-Man intro scene with the
     * ghost presentation. If still no key is pressed, the demo level is shown. After the demo
     * level ends, the credits screens are shown and then again the "PRESS START" scene.
     * </p>
     */
    INTRO {
        @Override
        public void onEnter(Game game) {
            lock();
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                game.flow().enterState(STARTING_GAME_OR_LEVEL);
            }
        }
    },

    /**
     * Corresponds to the "MS PAC-MAN OPTIONS" screen where difficulty, booster, map category
     * and start level can be set.
     */
    PREPARING_GAME_START {
        @Override
        public void onEnter(Game game) {
            game.prepareNewGame();
        }

        @Override
        public void onUpdate(Game game) {
            // wait for user interaction to leave state
        }
    },

    /**
     * Corresponds to the screen showing the people that have contributed to the game. Here, a seconds
     * screen with the contributors to the remake has been added.
     */
    SHOWING_HALL_OF_FAME {
        @Override
        public void onEnter(Game game) {
            lock();
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                game.flow().enterState(INTRO);
            }
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
            if (game.isPlayingLevel()) {
                game.continuePlayingLevel(tick);
            } else if (game.canStartNewGame()) {
                game.startNewGame(tick);
            } else {
                game.startDemoLevel(tick);
            }
        }
    },

    LEVEL_PLAYING {
        @Override
        public void onEnter(Game game) {
            game.onStartLevelPlaying();
        }

        @Override
        public void onUpdate(Game game) {
            game.doLevelPlaying();
            if (game.isLevelCompleted()) {
                game.flow().enterState(LEVEL_COMPLETE);
            } else if (game.hasPacManBeenKilled()) {
                game.flow().enterState(PACMAN_DYING);
            } else if (game.hasGhostBeenKilled()) {
                game.flow().enterState(EATING_GHOST);
            }
        }
    },

    LEVEL_COMPLETE {
        @Override
        public void onEnter(Game game) {
            lock(); // UI triggers timeout
        }

        @Override
        public void onUpdate(Game game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer.tickCount() == 1) {
                game.onLevelCompleted();
            }

            if (level.isDemoLevel()) {
                game.flow().enterState(SHOWING_HALL_OF_FAME);
                return;
            }

            if (timer.hasExpired()) {
                if (level.isDemoLevel()) {
                    // Just in case: if demo level is completed, go back to intro scene
                    game.flow().enterState(INTRO);
                } else if (game.flow().cutScenesEnabled() && level.cutSceneNumber() != 0) {
                    game.flow().enterState(INTERMISSION);
                } else {
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
            timer.restartTicks(TengenMsPacMan_GameModel.TICK_EATING_GHOST_COMPLETE);
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                game.flow().resumePreviousState();
            } else {
                game.doEatingGhost(timer.tickCount());
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
                game.doPacManDying(level.pac(), timer.tickCount());
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
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer.hasExpired()) {
                if (level.isDemoLevel()) {
                    game.flow().enterState(SHOWING_HALL_OF_FAME);
                } else {
                    level.clearMessage();
                    game.cheats().clearCheatUsedFlag();
                    game.flow().enterState(game.canContinueOnGameOver() ? PREPARING_GAME_START : INTRO);
                }
            }
        }
    },

    INTERMISSION {
        @Override
        public void onEnter(Game game) {
            lock();
            final var tengenHUD = (TengenMsPacMan_HeadsUpDisplay) game.hud();
            final TengenMsPacMan_GameModel tengenGame = tengenGame(game);
            final GameLevel level = game.optGameLevel().orElseThrow();
            final boolean lastCutSceneReached =  level.cutSceneNumber() == tengenGame.lastIntermissionNumber();
            if (tengenGame.mapCategory() == MapCategory.ARCADE || lastCutSceneReached) {
                tengenHUD.hide();
            } else {
                tengenHUD.show();
                tengenHUD.gameOptions(false).score(false).levelCounter(true).livesCounter(false).show();
            }
        }

        @Override
        public void onUpdate(Game game) {
            if (timer.hasExpired()) {
                game.flow().enterState(game.isPlayingLevel() ? LEVEL_TRANSITION : INTRO);
            }
        }

        @Override
        public void onExit(Game game) {
            final var tengenHUD = (TengenMsPacMan_HeadsUpDisplay) game.hud();
            final TengenMsPacMan_GameModel tengenGame = tengenGame(game);
            if (tengenGame.mapCategory() == MapCategory.ARCADE) {
                tengenHUD.hide();
            } else {
                tengenHUD.show();
                tengenHUD.all(true).gameOptions(true).score(true).levelCounter(true).livesCounter(false).show();
            }
        }

    };

    final TickTimer timer;

    TengenMsPacMan_GameState() {
        timer = new TickTimer("Timer-" + name());
        Logger.info("Game state {} created", name());
    }

    TengenMsPacMan_GameModel tengenGame(Game game) {
        return (TengenMsPacMan_GameModel) game;
    }

    @Override
    public TickTimer timer() {
        return timer;
    }
}
