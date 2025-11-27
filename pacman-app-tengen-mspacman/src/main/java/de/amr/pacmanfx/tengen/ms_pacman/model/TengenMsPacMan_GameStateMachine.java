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
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;

public class TengenMsPacMan_GameStateMachine extends StateMachine<FsmState<GameContext>, GameContext> {

    public TengenMsPacMan_GameStateMachine() {
        setName("Tengen Ms. Pac-Man Game State Machine");
        addStates(GameState.values());
        addState(new LevelShortTestState());
        addState(new LevelMediumTestState());
        addState(new CutScenesTestState());
    }

    public enum GameState implements FsmState<GameContext> {

        // "Das muss das Boot abkönnen!"
        BOOT {
            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
                context.cheatUsedProperty().set(false);
                context.immunityProperty().set(false);
                context.usingAutopilotProperty().set(false);
                context.currentGame().resetEverything();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().changeState(INTRO);
                }
            }
        },

        INTRO {
            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        SETTING_OPTIONS_FOR_START {
            @Override
            public void onUpdate(GameContext context) {
                // wait for user interaction to leave state
            }
        },

        SHOWING_HALL_OF_FAME {
            @Override
            public void onEnter(GameContext context) {
                timer.restartIndefinitely();
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().changeState(INTRO);
                }
            }
        },

        STARTING_GAME_OR_LEVEL {

            private static final short TICK_NEW_GAME_SHOW_GUYS = 120;
            private static final short TICK_NEW_GAME_START_HUNTING = 240;
            private static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
            private static final short TICK_RESUME_HUNTING =  90;

            @Override
            public void onEnter(GameContext context) {
                context.currentGame().publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
            }

            private void startNewGame(GameContext context) {
                final Game game = context.currentGame();
                if (timer.tickCount() == 1) {
                    game.startNewGame();
                }
                else if (timer.tickCount() == 2) {
                    if (!game.level().isDemoLevel()) {
                        game.level().pac().immuneProperty().bind(context.immunityProperty());
                        game.level().pac().usingAutopilotProperty().bind(context.usingAutopilotProperty());
                        boolean cheating = context.immunityProperty().get() || context.usingAutopilotProperty().get();
                        context.cheatUsedProperty().set(cheating);
                    }
                    game.startLevel();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_SHOW_GUYS) {
                    game.level().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_HUNTING) {
                    game.setPlaying(true);
                    game.changeState(HUNTING);
                }
            }

            private void continueGame(GameContext context) {
                final Game game = context.currentGame();
                if (timer.tickCount() == 1) {
                    game.continueGame(game.level());
                } else if (timer.tickCount() == TICK_RESUME_HUNTING) {
                    game.changeState(HUNTING);
                }
            }

            private void startDemoLevel(Game game) {
                if (timer.tickCount() == 1) {
                    game.buildDemoLevel();
                    game.publishGameEvent(GameEvent.Type.LEVEL_CREATED);
                }
                else if (timer.tickCount() == 2) {
                    game.startLevel();
                }
                else if (timer.tickCount() == 3) {
                    // Now, actor animations are available
                    game.level().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START_HUNTING) {
                    game.changeState(HUNTING);
                }
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
                if (game.isPlaying()) {
                    continueGame(context);
                }
                else if (game.canStartNewGame()) {
                    startNewGame(context);
                }
                else {
                    startDemoLevel(game);
                }
            }
        },

        HUNTING {
            private int delayTicks;

            private void clearReadyMessage(GameLevel gameLevel) {
                gameLevel.optMessage().filter(message -> message.type() == MessageType.READY).ifPresent(message -> {
                    gameLevel.clearMessage(); // leave TEST message alone
                });
            }

            @Override
            public void onEnter(GameContext context) {
                delayTicks = 60;
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.tickCount() < delayTicks) {
                    return;
                }

                final Game game = context.currentGame();

                if (timer.tickCount() == delayTicks) {
                    clearReadyMessage(game.level());
                    game.startHunting(game.level());
                }

                game.level().pac().tick(context);
                game.level().ghosts().forEach(ghost -> ghost.tick(context));
                game.level().bonus().ifPresent(bonus -> bonus.tick(context));

                game.updateHunting(game.level());

                if (game.isLevelCompleted(game.level())) {
                    game.changeState(LEVEL_COMPLETE);
                }
                else if (game.hasPacManBeenKilled()) {
                    game.changeState(PACMAN_DYING);
                }
                else if (game.hasGhostBeenKilled()) {
                    game.changeState(GHOST_DYING);
                }
            }

            @Override
            public void onExit(GameContext context) {
                //TODO is this needed?
                clearReadyMessage(context.currentGame().level());
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
                    game.onLevelCompleted(game.level());
                }

                if (game.level().isDemoLevel()) {
                    game.changeState(SHOWING_HALL_OF_FAME);
                    return;
                }

                if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        // Just in case: if demo level is completed, go back to intro scene
                        game.changeState(INTRO);
                    }
                    else if (game.cutScenesEnabled() && game.optCutSceneNumber(game.level().number()).isPresent()) {
                        game.changeState(INTERMISSION);
                    }
                    else {
                        game.changeState(LEVEL_TRANSITION);
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
                    context.currentGame().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        GHOST_DYING {

            @Override
            public void onEnter(GameContext context) {
                timer.restartSeconds(1);
                context.currentGame().level().pac().hide();
                context.currentGame().level().ghosts().forEach(Ghost::stopAnimation);
                context.currentGame().publishGameEvent(GameEvent.Type.GHOST_EATEN);
            }

            @Override
            public void onUpdate(GameContext context) {
                if (timer.hasExpired()) {
                    context.currentGame().resumePreviousState();
                } else {
                    context.currentGame().level().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                        .forEach(ghost -> ghost.tick(context));
                    context.currentGame().level().blinking().tick();
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
                timer.restartIndefinitely();
                context.currentGame().onPacKilled(context.currentGame().level());
                context.currentGame().publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
                final Pac pac = game.level().pac();

                if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        game.changeState(GAME_OVER);
                    }
                    else {
                        game.addLives(-1);
                        game.changeState(game.lifeCount() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
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
                context.currentGame().level().bonus().ifPresent(Bonus::setInactive);
            }
        },

        GAME_OVER {

            @Override
            public void onEnter(GameContext context) {
                timer.restartTicks(context.currentGame().level().gameOverStateTicks());
                context.currentGame().onGameEnding(context.currentGame().level());
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
                if (timer.hasExpired()) {
                    if (context.currentGame().level().isDemoLevel()) {
                        game.changeState(SHOWING_HALL_OF_FAME);
                    }
                    else {
                        game.changeState(game.canContinueOnGameOver() ? SETTING_OPTIONS_FOR_START : INTRO);
                    }
                }
            }

            @Override
            public void onExit(GameContext context) {
                context.currentGame().optGameLevel().ifPresent(GameLevel::clearMessage);
                context.cheatUsedProperty().set(false);
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
                    game.changeState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
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