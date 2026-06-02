/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.flow.GameState;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HeadsUpDisplay;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public enum TengenMsPacMan_GameState {

    /**
     * Corresponds to the screen showing the "TENGEN PRESENTS" text and the red ghost running over the screen.
     */
    BOOT (new GameState("BOOT") { 
        
        // "Das muss das Boot abkönnen!"
        
        @Override
        public void onEnter(GameModel game) {
            lock();
            game.init();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().hasExpired()) {
                game.flow().enterState(GAME_INTRO.state());
            }
        }
    }),

    /**
     * Corresponds to the screen showing the "TENGEN PRESENTS MS. PAC-MAN" title,
     * the "PRESS START" and copyright text.
     * <p>
     * If no key is pressed for some time, the UI shows to the Ms. Pac-Man intro scene with the
     * ghost presentation. If still no key is pressed, the demo level is shown. After the demo
     * level ends, the credits screens are shown and then again the "PRESS START" scene.
     * </p>
     */
    GAME_INTRO(new GameState("GAME_INTRO") {
        @Override
        public void onEnter(GameModel game) {
            lock();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().hasExpired()) {
                game.flow().enterState(GAME_STARTING_NEW_GAME_OR_LEVEL.state);
            }
        }
    }),

    /**
     * Corresponds to the "MS PAC-MAN OPTIONS" screen where difficulty, booster, map category
     * and start level can be set.
     */
    GAME_PREPARATION(new GameState("GAME_PREPARATION") {
        @Override
        public void onEnter(GameModel game) {
            game.prepareNewGame();
        }

        @Override
        public void onUpdate(GameModel game) {
            // wait for user interaction to leave state
        }
    }),

    /**
     * Corresponds to the screen showing the people that have contributed to the game. Here, a seconds
     * screen with the contributors to the remake has been added.
     */
    SHOWING_HALL_OF_FAME (new GameState("SHOWING_HALL_OF_FAME") {
        @Override
        public void onEnter(GameModel game) {
            lock();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().hasExpired()) {
                game.flow().enterState(GAME_INTRO.state());
            }
        }
    }),

    GAME_STARTING_NEW_GAME_OR_LEVEL(new GameState("GAME_STARTING_NEW_GAME_OR_LEVEL") {
        @Override
        public void onEnter(GameModel game) {
            game.hud().credit(false).score(true).levelCounter(true).livesCounter(true).show();
        }

        @Override
        public void onUpdate(GameModel game) {
            final long tick = timer().tickCount();
            if (game.isPlayingLevel()) {
                final GameLevel level = game.optGameLevel().orElseThrow();
                game.continuePlayingLevel(level, tick);
            } else if (game.canStartNewGame()) {
                game.startNewGame(tick);
            } else {
                game.startDemoLevel(tick);
            }
        }
    }),

    GAME_LEVEL_PLAYING(new GameState("GAME_LEVEL_PLAYING") {
        @Override
        public void onEnter(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            game.onStartLevelPlaying(level);
        }

        @Override
        public void onUpdate(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            game.doLevelPlaying(level);
            if (game.isLevelCompleted()) {
                game.flow().enterState(GAME_LEVEL_COMPLETE.state());
            } else if (game.hasPacManBeenKilled()) {
                game.flow().enterState(GAME_LEVEL_PACMAN_DYING.state());
            } else if (game.hasGhostBeenKilled()) {
                game.flow().enterState(GAME_LEVEL_EATING_GHOST.state());
            }
        }
    }),

    GAME_LEVEL_COMPLETE(new GameState("GAME_LEVEL_COMPLETE") {
        @Override
        public void onEnter(GameModel game) {
            lock(); // UI triggers timeout
        }

        @Override
        public void onUpdate(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer().tickCount() == 1) {
                game.onLevelCompleted(level);
            }

            if (level.isDemoLevel()) {
                game.flow().enterState(SHOWING_HALL_OF_FAME.state());
                return;
            }

            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    // Just in case: if demo level is completed, go back to intro scene
                    game.flow().enterState(GAME_INTRO.state());
                } else if (game.flow().cutScenesEnabled() && level.cutSceneNumber() != 0) {
                    game.flow().enterState(GAME_LEVEL_INTERMISSION.state());
                } else {
                    game.flow().enterState(GAME_LEVEL_TRANSITION.state());
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
                game.flow().enterState(GAME_STARTING_NEW_GAME_OR_LEVEL.state());
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
                    game.flow().enterState(GAME_OVER.state());
                } else {
                    game.lives().add(-1);
                    game.flow().enterState(game.lives().count() == 0 ? GAME_OVER.state() : GAME_STARTING_NEW_GAME_OR_LEVEL.state());
                }
            } else {
                game.doPacManDying(level, level.entities().pac(), timer().tickCount());
            }
        }
    }),

    GAME_OVER (new GameState("GAME_OVER") {
        @Override
        public void onEnter(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            timer().restartTicks(level.gameOverStateTicks());
            game.onGameOver(level);
        }

        @Override
        public void onUpdate(GameModel game) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (timer().hasExpired()) {
                if (level.isDemoLevel()) {
                    game.flow().enterState(SHOWING_HALL_OF_FAME.state());
                } else {
                    level.clearMessage();
                    game.clearCheats();
                    game.flow().enterState(game.canContinueOnGameOver() ? GAME_PREPARATION.state() : GAME_INTRO.state());
                }
            }
        }
    }),

    GAME_LEVEL_INTERMISSION(new GameState("GAME_LEVEL_INTERMISSION") {
        @Override
        public void onEnter(GameModel game) {
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
        public void onUpdate(GameModel game) {
            if (timer().hasExpired()) {
                game.flow().enterState(game.isPlayingLevel() ? GAME_LEVEL_TRANSITION.state() : GAME_INTRO.state());
            }
        }

        @Override
        public void onExit(GameModel game) {
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
}
