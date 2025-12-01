/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.*;

public class TengenMsPacMan_GameController extends StateMachine<FsmState<Game>, Game> implements GameControl {

    public TengenMsPacMan_GameController() {
        setName("Tengen Ms. Pac-Man Game State Machine");
        addStates(GameState.values());
    }

    @Override
    public StateMachine<FsmState<Game>, Game> stateMachine() {
        return this;
    }

    public enum GameState implements FsmState<Game> {

        /**
         * Corresponds to the screen showing the "TENGEN PRESENTS" text and the red ghost running over the screen.
         */
        BOOT {
            // "Das muss das Boot abkönnen!"
            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely();
                game.resetEverything();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().changeState(INTRO);
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
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        /**
         * Corresponds to the "MS PAC-MAN OPTIONS" screen where difficulty, booster, map category
         * and start level can be set.
         */
        SETTING_OPTIONS_FOR_START {
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
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().changeState(INTRO);
                }
            }
        },

        STARTING_GAME_OR_LEVEL {
            @Override
            public void onUpdate(Game game) {
                final long tick = timer.tickCount();
                if (game.isPlaying()) {
                    game.continuePlaying(tick);
                }
                else if (game.canStartNewGame()) {
                    game.startNewGame(tick);
                }
                else {
                    game.startDemoLevel(tick);
                }
            }
        },

        HUNTING {
            @Override
            public void onEnter(Game game) {
                clearReadyMessage(game.level());
                game.startHunting();
            }

            @Override
            public void onUpdate(Game game) {
                game.updateHunting();
                if (game.isLevelCompleted()) {
                    game.control().changeState(LEVEL_COMPLETE);
                }
                else if (game.hasPacManBeenKilled()) {
                    game.control().changeState(PACMAN_DYING);
                }
                else if (game.hasGhostBeenKilled()) {
                    game.control().changeState(EATING_GHOST);
                }
            }

            @Override
            public void onExit(Game game) {
                //TODO is this needed?
                clearReadyMessage(game.level());
            }

            private void clearReadyMessage(GameLevel gameLevel) {
                gameLevel.optMessage().filter(message -> message.type() == MessageType.READY).ifPresent(message -> {
                    gameLevel.clearMessage(); // leave TEST message alone
                });
            }
        },

        LEVEL_COMPLETE {

            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely(); // UI triggers timeout
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.tickCount() == 1) {
                    game.onLevelCompleted();
                }

                if (game.level().isDemoLevel()) {
                    game.control().changeState(SHOWING_HALL_OF_FAME);
                    return;
                }

                if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        // Just in case: if demo level is completed, go back to intro scene
                        game.control().changeState(INTRO);
                    }
                    else if (game.cutScenesEnabled() && game.level().cutSceneNumber() != 0) {
                        game.control().changeState(INTERMISSION);
                    }
                    else {
                        game.control().changeState(LEVEL_TRANSITION);
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
                    game.control().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        EATING_GHOST {

            @Override
            public void onEnter(Game game) {
                timer.restartSeconds(1);
                game.level().pac().hide();
                game.level().ghosts().forEach(Ghost::stopAnimation);
                game.publishGameEvent(GameEvent.Type.GHOST_EATEN);
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().resumePreviousState();
                } else {
                    game.level().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                        .forEach(ghost -> ghost.tick(game));
                    game.level().blinking().tick();
                }
            }

            @Override
            public void onExit(Game game) {
                game.level().pac().show();
                game.level().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
                game.level().ghosts()
                    .forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
            }
        },

        PACMAN_DYING {
            private static final int TICK_HIDE_GHOSTS = 60;
            private static final int TICK_START_PAC_ANIMATION = 90;
            private static final int TICK_HIDE_PAC = 190;
            private static final int TICK_PAC_DEAD = 240;

            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely();
                game.onPacKilled();
                game.publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
            }

            @Override
            public void onUpdate(Game game) {
                final Pac pac = game.level().pac();

                if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        game.control().changeState(GAME_OVER);
                    }
                    else {
                        game.addLives(-1);
                        game.control().changeState(game.lifeCount() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                    }
                }
                else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                    game.level().ghosts().forEach(Ghost::hide);
                    //TODO this does not belong here
                    pac.optAnimationManager().ifPresent(animations -> {
                        animations.select(CommonAnimationID.ANIM_PAC_DYING);
                        animations.reset();
                    });
                }
                else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                    //TODO this does not belong here
                    pac.optAnimationManager().ifPresent(AnimationManager::play);
                    game.publishGameEvent(GameEvent.Type.PAC_DYING, pac.tile());
                }
                else if (timer.tickCount() == TICK_HIDE_PAC) {
                    pac.hide();
                }
                else if (timer.tickCount() == TICK_PAC_DEAD) {
                    game.publishGameEvent(GameEvent.Type.PAC_DEAD);
                }
                else {
                    game.level().blinking().tick();
                    pac.tick(game);
                }
            }

            @Override
            public void onExit(Game game) {
                //TODO clarify in MAME
                game.level().optBonus().ifPresent(Bonus::setInactive);
            }
        },

        GAME_OVER {
            @Override
            public void onEnter(Game game) {
                timer.restartTicks(game.level().gameOverStateTicks());
                game.onGameOver();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        game.control().changeState(SHOWING_HALL_OF_FAME);
                    }
                    else {
                        game.control().changeState(game.canContinueOnGameOver() ? SETTING_OPTIONS_FOR_START : INTRO);
                    }
                }
            }

            @Override
            public void onExit(Game game) {
                game.optGameLevel().ifPresent(GameLevel::clearMessage);
                game.cheatUsedProperty().set(false);
            }
        },

        INTERMISSION {

            @Override
            public void onEnter(Game game) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(Game game) {
                if (timer.hasExpired()) {
                    game.control().changeState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }
}