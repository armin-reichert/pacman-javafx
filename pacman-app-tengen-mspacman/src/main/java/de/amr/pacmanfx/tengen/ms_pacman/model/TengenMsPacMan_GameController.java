/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.*;

public class TengenMsPacMan_GameController extends StateMachine<FsmState<GameContext>, GameContext> implements GameControl {

    public TengenMsPacMan_GameController() {
        setName("Tengen Ms. Pac-Man Game State Machine");
        addStates(GameState.values());
    }

    @Override
    public StateMachine<FsmState<GameContext>, GameContext> stateMachine() {
        return this;
    }

    public enum GameState implements FsmState<GameContext> {

        /**
         * Corresponds to the screen showing the "TENGEN PRESENTS" text and the red ghost running over the screen.
         */
        BOOT {
            // "Das muss das Boot abkönnen!"
            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
                context.currentGame().resetEverything();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().control().changeState(INTRO);
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
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().control().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        /**
         * Corresponds to the "MS PAC-MAN OPTIONS" screen where difficulty, booster, map category
         * and start level can be set.
         */
        SETTING_OPTIONS_FOR_START {
            @Override
            public void onUpdate(GameContext context) {
                // wait for user interaction to leave state
            }
        },

        /**
         * Corresponds to the screen showing the people that have contributed to the game. Here, a seconds
         * screen with the contributors to the remake has been added.
         */
        SHOWING_HALL_OF_FAME {
            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().control().changeState(INTRO);
                }
            }
        },

        STARTING_GAME_OR_LEVEL {

            private static final short TICK_SHOW_READY = 10;
            private static final short TICK_NEW_GAME_SHOW_GUYS = 70;
            private static final short TICK_NEW_GAME_START_HUNTING = 250;

            private static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
            private static final short TICK_RESUME_HUNTING = 240;

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
                if (game.isPlaying()) {
                    continueGame(game);
                }
                else if (game.canStartNewGame()) {
                    startNewGame(game);
                }
                else {
                    startDemoLevel(game);
                }
            }

            private void startNewGame(Game game) {
                if (timer.tickCount() == 1) {
                    game.startNewGame();
                }
                else if (timer.tickCount() == TICK_SHOW_READY) {
                    if (!game.level().isDemoLevel()) {
                        game.level().pac().immuneProperty().bind(game.immunityProperty());
                        game.level().pac().usingAutopilotProperty().bind(game.usingAutopilotProperty());
                        boolean cheating = game.immunity() || game.usingAutopilot();
                        game.cheatUsedProperty().set(cheating);
                    }
                    game.startLevel();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_SHOW_GUYS) {
                    game.level().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_HUNTING) {
                    game.setPlaying(true);
                    game.control().changeState(HUNTING);
                }
            }

            private void continueGame(Game game) {
                if (timer.tickCount() == 1) {
                    game.continueGame();
                } else if (timer.tickCount() == TICK_RESUME_HUNTING) {
                    game.control().changeState(HUNTING);
                }
            }

            private void startDemoLevel(Game game) {
                if (timer.tickCount() == 1) {
                    game.buildDemoLevel();
                }
                else if (timer.tickCount() == 2) {
                    game.startLevel();
                }
                else if (timer.tickCount() == 3) {
                    // Now, actor animations are available
                    game.level().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START_HUNTING) {
                    game.control().changeState(HUNTING);
                }
            }
        },

        HUNTING {
            @Override
            public void onEnter(GameContext context) {
                final Game game = context.currentGame();
                clearReadyMessage(game.level());
                game.startHunting();
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();

                game.level().pac().tick(context);
                game.level().ghosts().forEach(ghost -> ghost.tick(context));
                game.level().optBonus().ifPresent(bonus -> bonus.tick(context));

                game.updateHunting();

                if (game.isLevelCompleted()) {
                    game.control().changeState(LEVEL_COMPLETE);
                }
                else if (game.hasPacManBeenKilled()) {
                    game.control().changeState(PACMAN_DYING);
                }
                else if (game.hasGhostBeenKilled()) {
                    game.control().changeState(GHOST_DYING);
                }
            }

            @Override
            public void onExit(GameContext context) {
                //TODO is this needed?
                clearReadyMessage(context.currentGame().level());
            }

            private void clearReadyMessage(GameLevel gameLevel) {
                gameLevel.optMessage().filter(message -> message.type() == MessageType.READY).ifPresent(message -> {
                    gameLevel.clearMessage(); // leave TEST message alone
                });
            }
        },

        LEVEL_COMPLETE {

            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely(); // UI triggers timeout
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();

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
            public void onEnter(GameContext context) {
                timer.restartSeconds(2);
                context.currentGame().startNextLevel();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().control().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        GHOST_DYING {

            @Override
            public void onEnter(GameContext context) {
                final Game game = context.currentGame();
                timer.restartSeconds(1);
                game.level().pac().hide();
                game.level().ghosts().forEach(Ghost::stopAnimation);
                game.publishGameEvent(GameEvent.Type.GHOST_EATEN);
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
                if (timer.hasExpired()) {
                    game.control().resumePreviousState();
                } else {
                    game.level().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                        .forEach(ghost -> ghost.tick(context));
                    game.level().blinking().tick();
                }
            }

            @Override
            public void onExit(GameContext context) {
                context.currentGame().level().pac().show();
                context.currentGame().level().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
                context.currentGame().level().ghosts()
                    .forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
            }
        },

        PACMAN_DYING {
            private static final int TICK_HIDE_GHOSTS = 60;
            private static final int TICK_START_PAC_ANIMATION = 90;
            private static final int TICK_HIDE_PAC = 190;
            private static final int TICK_PAC_DEAD = 240;

            @Override
            public void onEnter(GameContext context) {
                final Game game = context.currentGame();
                timer.restartIndefinitely();
                game.onPacKilled();
                game.publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
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
                    pac.tick(context);
                }
            }

            @Override
            public void onExit(GameContext context) {
                //TODO clarify in MAME
                context.currentGame().level().optBonus().ifPresent(Bonus::setInactive);
            }
        },

        GAME_OVER {

            @Override
            public void onEnter(GameContext context) {
                final Game game = context.currentGame();
                timer.restartTicks(game.level().gameOverStateTicks());
                game.onGameEnding();
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
                if (timer.hasExpired()) {
                    if (context.currentGame().level().isDemoLevel()) {
                        game.control().changeState(SHOWING_HALL_OF_FAME);
                    }
                    else {
                        game.control().changeState(game.canContinueOnGameOver() ? SETTING_OPTIONS_FOR_START : INTRO);
                    }
                }
            }

            @Override
            public void onExit(GameContext context) {
                final Game game = context.currentGame();
                game.optGameLevel().ifPresent(GameLevel::clearMessage);
                game.cheatUsedProperty().set(false);
            }
        },

        INTERMISSION {

            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
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