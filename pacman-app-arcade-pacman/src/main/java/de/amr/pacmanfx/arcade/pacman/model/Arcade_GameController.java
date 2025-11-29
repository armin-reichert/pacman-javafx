/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

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

public class Arcade_GameController extends StateMachine<FsmState<GameContext>, GameContext> implements GameControl {

    public Arcade_GameController() {
        setName("Arcade Pac-Man Games State Machine");
        addStates(GameState.values());
    }

    @Override
    public StateMachine<FsmState<GameContext>, GameContext> stateMachine() {
        return this;
    }

    public enum GameState implements FsmState<GameContext> {

        // "Das muss das Boot abkönnen!"
        BOOT {
            @Override
            public void onEnter(GameContext context) {
                final Game game = context.currentGame();
                timer.restartIndefinitely();
                context.cheatUsedProperty().set(false);
                context.immunityProperty().set(false);
                context.usingAutopilotProperty().set(false);
                game.resetEverything();
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
                if (timer.hasExpired()) {
                    game.control().changeState(INTRO);
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
                final Game game = context.currentGame();
                if (timer.hasExpired()) {
                    // start demo level (attract mode)
                    game.control().changeState(STARTING_GAME_OR_LEVEL);
                }
            }
        },

        SETTING_OPTIONS_FOR_START {
            @Override
            public void onUpdate(GameContext context) {
                // wait for user interaction to start playing
            }
        },

        STARTING_GAME_OR_LEVEL {
            static final short TICK_NEW_GAME_SHOW_GUYS = 120;
            static final short TICK_NEW_GAME_START_HUNTING = 240;
            static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
            static final short TICK_RESUME_HUNTING =  90;

            @Override
            public void onEnter(GameContext context) {
                final Game game = context.currentGame();
                game.publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
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
                    startDemoLevel(context);
                }
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
                    game.control().changeState(GameState.HUNTING);
                }
            }

            private void continueGame(GameContext context) {
                final Game game = context.currentGame();
                if (timer.tickCount() == 1) {
                    game.continueGame();
                } else if (timer.tickCount() == TICK_RESUME_HUNTING) {
                    game.control().changeState(GameState.HUNTING);
                }
            }

            private void startDemoLevel(GameContext context) {
                final Game game = context.currentGame();
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
                    game.control().changeState(GameState.HUNTING);
                }
            }
        },

        HUNTING {
            @Override
            public void onEnter(GameContext context) {
                final Game game = context.currentGame();
                game.level().optMessage().filter(message -> message.type() == MessageType.READY).ifPresent(message -> {
                    game.level().clearMessage(); // leave TEST message alone
                });
                game.startHunting();
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();

                game.level().pac().tick(context);
                game.level().ghosts().forEach(ghost -> ghost.tick(context));
                game.level().optBonus().ifPresent(bonus -> bonus.tick(context));
                game.updateHunting();

                // What next?
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
                final Game game = context.currentGame();
                game.level().optMessage().ifPresent(message -> {
                    if (message.type() == MessageType.READY) {
                        game.level().clearMessage();
                    }
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

                if (timer.hasExpired()) {
                    if (game.level().isDemoLevel()) {
                        // just in case: if demo level was completed, go back to intro scene
                        game.control().changeState(INTRO);
                    } else if (game.cutScenesEnabled() && game.level().cutSceneNumber() != 0) {
                        game.control().changeState(INTERMISSION);
                    } else {
                        game.control().changeState(LEVEL_TRANSITION);
                    }
                }
            }
        },

        LEVEL_TRANSITION {
            @Override
            public void onEnter(GameContext context) {
                final Game game = context.currentGame();
                timer.restartSeconds(2);
                game.startNextLevel();
            }

            @Override
            public void onUpdate(GameContext context) {
                final Game game = context.currentGame();
                if (timer.hasExpired()) {
                    game.control().changeState(STARTING_GAME_OR_LEVEL);
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
                final Game game = context.currentGame();
                game.level().pac().show();
                game.level().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
                game.level().ghosts()
                    .forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
            }
        },

        PACMAN_DYING {
            static final int TICK_HIDE_GHOSTS = 60;
            static final int TICK_START_PAC_ANIMATION = 90;
            static final int TICK_HIDE_PAC = 190;
            static final int TICK_PAC_DEAD = 240;

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
                    } else {
                        game.addLives(-1);
                        game.control().changeState(game.lifeCount() == 0 ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                    }
                }
                else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                    game.level().ghosts().forEach(Ghost::hide);
                    //TODO this does not belong here
                    pac.optAnimationManager().ifPresent(am -> {
                        am.select(CommonAnimationID.ANIM_PAC_DYING);
                        am.reset();
                    });
                }
                else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
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
                final Game game = context.currentGame();
                game.level().optBonus().ifPresent(Bonus::setInactive);
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
                    game.prepareForNewGame();
                    if (game.canStartNewGame()) {
                        game.control().changeState(SETTING_OPTIONS_FOR_START);
                    } else {
                        game.control().changeState(INTRO);
                    }
                }
            }

            @Override
            public void onExit(GameContext context) {
                final Game game = context.currentGame();
                game.optGameLevel().ifPresent(GameLevel::clearMessage);
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