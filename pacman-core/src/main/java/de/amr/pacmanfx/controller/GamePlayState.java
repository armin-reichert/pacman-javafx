/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.DefaultGameVariants;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.*;

/**
 * States of the Pac-Man game play state machine. Game controller FSM also contains some additional test states.
 */
public enum GamePlayState implements GameState {

    // "Das muss das Boot abkönnen!"
    BOOT {
        @Override
        public void onEnter(GameContext context) {
            timer.restartIndefinitely();
            context.game().resetEverything();
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.gameController().changeGameState(INTRO);
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
                context.gameController().changeGameState(STARTING_GAME_OR_LEVEL);
            }
        }
    },

    SETTING_OPTIONS_FOR_START {
        @Override
        public void onUpdate(GameContext context) {
            // wait for user interaction to leave state
        }
    },

    /**
     * In Tengen Ms. Pac-Man, the credited people are shown.
     */
    SHOWING_CREDITS {
        @Override
        public void onEnter(GameContext context) {
            timer.restartIndefinitely();
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.gameController().changeGameState(INTRO);
            }
        }
    },

    STARTING_GAME_OR_LEVEL {
        static final short TICK_NEW_GAME_SHOW_GUYS = 120;
        static final short TICK_NEW_GAME_START_HUNTING = 240;
        static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
        static final short TICK_RESUME_HUNTING =  90;

        @Override
        public void onEnter(GameContext context) {
            context.eventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        private void startNewGame(GameContext context) {
            if (timer.tickCount() == 1) {
                context.game().startNewGame();
            }
            else if (timer.tickCount() == 2) {
                context.game().startLevel(context.gameLevel());
            }
            else if (timer.tickCount() == TICK_NEW_GAME_SHOW_GUYS) {
                context.gameLevel().showPacAndGhosts();
            }
            else if (timer.tickCount() == TICK_NEW_GAME_START_HUNTING) {
                context.game().setPlaying(true);
                context.gameController().changeGameState(GamePlayState.HUNTING);
            }
        }

        private void continueGame(GameContext context) {
            if (timer.tickCount() == 1) {
                context.game().continueGame(context.gameLevel());
            } else if (timer.tickCount() == TICK_RESUME_HUNTING) {
                context.gameController().changeGameState(GamePlayState.HUNTING);
            }
        }

        private void startDemoLevel(GameContext context) {
            if (timer.tickCount() == 1) {
                context.game().buildDemoLevel();
                context.eventManager().publishEvent(GameEventType.LEVEL_CREATED);
            }
            else if (timer.tickCount() == 2) {
                context.game().startLevel(context.gameLevel());
            }
            else if (timer.tickCount() == 3) {
                // Now, actor animations are available
                context.gameLevel().showPacAndGhosts();
            }
            else if (timer.tickCount() == TICK_DEMO_LEVEL_START_HUNTING) {
                context.gameController().changeGameState(GamePlayState.HUNTING);
            }
        }

        @Override
        public void onUpdate(GameContext context) {
            if (context.game().isPlaying()) {
                continueGame(context);
            }
            else if (context.game().canStartNewGame()) {
                startNewGame(context);
            }
            else {
                startDemoLevel(context);
            }
        }
    },

    HUNTING {
        int delay;

        @Override
        public void onEnter(GameContext context) {
            //TODO reconsider this
            delay = context.gameController().isSelected("MS_PACMAN_TENGEN") ? Globals.NUM_TICKS_PER_SEC : 0;
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.tickCount() < delay) {
                return;
            }
            final Game game = context.game();
            final GameLevel gameLevel = context.gameLevel();
            if (timer.tickCount() == delay) {
                gameLevel.optMessage().filter(message -> message.type() == MessageType.READY).ifPresent(message -> {
                    gameLevel.clearMessage(); // leave TEST message alone
                });
                game.startHunting(gameLevel);
            }
            gameLevel.pac().tick(context);
            gameLevel.ghosts().forEach(ghost -> ghost.tick(context));
            gameLevel.bonus().ifPresent(bonus -> bonus.tick(context));
            game.doHuntingStep(gameLevel);
            if (game.isLevelCompleted(gameLevel)) {
                context.gameController().changeGameState(LEVEL_COMPLETE);
            } else if (game.hasPacManBeenKilled()) {
                context.gameController().changeGameState(PACMAN_DYING);
            } else if (game.haveGhostsBeenKilled()) {
                context.gameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.gameLevel().optMessage().ifPresent(message -> {
                if (message.type() == MessageType.READY) {
                    context.gameLevel().clearMessage();
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
            if (timer.tickCount() == 1) {
                context.game().onLevelCompleted(context.gameLevel());
            }

            //TODO this is crap. Maybe Tengen Ms. Pac-Man needs its own state machine?
            if (context.gameController().isSelected(DefaultGameVariants.MS_PACMAN_TENGEN.name())
                && context.gameLevel().isDemoLevel()) {
                context.gameController().changeGameState(SHOWING_CREDITS);
                return;
            }

            if (timer.hasExpired()) {
                if (context.gameLevel().isDemoLevel()) {
                    // just in case: if demo level was completed, go back to intro scene
                    context.gameController().changeGameState(INTRO);
                } else if (context.game().cutScenesEnabled()
                    && context.game().optCutSceneNumber(context.gameLevel().number()).isPresent()) {
                    context.gameController().changeGameState(INTERMISSION);
                } else {
                    context.gameController().changeGameState(LEVEL_TRANSITION);
                }
            }
        }
    },

    LEVEL_TRANSITION {
        @Override
        public void onEnter(GameContext context) {
            timer.restartSeconds(2);
            context.game().startNextLevel();
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.gameController().changeGameState(STARTING_GAME_OR_LEVEL);
            }
        }
    },

    GHOST_DYING {
        @Override
        public void onEnter(GameContext context) {
            timer.restartSeconds(1);
            context.gameLevel().pac().hide();
            context.gameLevel().ghosts().forEach(AnimationSupport::stopAnimation);
            context.eventManager().publishEvent(GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.gameController().resumePreviousGameState();
            } else {
                context.gameLevel().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                    .forEach(ghost -> ghost.tick(context));
                context.gameLevel().blinking().tick();
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.gameLevel().pac().show();
            context.gameLevel().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            context.gameLevel().ghosts()
                .forEach(ghost -> ghost.animationManager().ifPresent(AnimationManager::play));
        }
    },

    PACMAN_DYING {
        static final int TICK_HIDE_GHOSTS = 60;
        static final int TICK_START_PAC_ANIMATION = 90;
        static final int TICK_HIDE_PAC = 190;
        static final int TICK_PAC_DEAD = 240;

        @Override
        public void onEnter(GameContext context) {
            timer.restartIndefinitely();
            context.game().onPacKilled();
            context.eventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                if (context.gameLevel().isDemoLevel()) {
                    context.gameController().changeGameState(GAME_OVER);
                } else {
                    context.game().addLives(-1);
                    context.gameController().changeGameState(context.game().lifeCount() == 0
                        ? GAME_OVER : STARTING_GAME_OR_LEVEL);
                }
            }
            else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                context.gameLevel().ghosts().forEach(Ghost::hide);
                //TODO this does not belong here
                context.gameLevel().pac().animationManager().ifPresent(am -> {
                    am.select(AnimationSupport.ANIM_PAC_DYING);
                    am.reset();
                });
            }
            else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                context.gameLevel().pac().animationManager().ifPresent(AnimationManager::play);
                context.eventManager().publishEvent(GameEventType.PAC_DYING, context.gameLevel().pac().tile());
            }
            else if (timer.tickCount() == TICK_HIDE_PAC) {
                context.gameLevel().pac().hide();
            }
            else if (timer.tickCount() == TICK_PAC_DEAD) {
                context.eventManager().publishEvent(GameEventType.PAC_DEAD);
            }
            else {
                context.gameLevel().blinking().tick();
                context.gameLevel().pac().tick(context);
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.gameLevel().bonus().ifPresent(Bonus::setInactive);
        }
    },

    GAME_OVER {
        @Override
        public void onEnter(GameContext context) {
            timer.restartTicks(context.gameLevel().gameOverStateTicks());
            context.game().onGameEnding();
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                //TODO find unified solution
                if (context.gameController().isSelected("MS_PACMAN_TENGEN")) {
                    if (context.gameLevel().isDemoLevel()) {
                        context.gameController().changeGameState(SHOWING_CREDITS);
                    } else {
                        boolean canContinue = context.game().canContinueOnGameOver();
                        context.gameController().changeGameState(canContinue ? SETTING_OPTIONS_FOR_START : INTRO);
                    }
                } else {
                    context.game().prepareForNewGame();
                    if (context.game().canStartNewGame()) {
                        context.gameController().changeGameState(SETTING_OPTIONS_FOR_START);
                    } else {
                        context.gameController().changeGameState(INTRO);
                    }
                }
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.optGameLevel().ifPresent(GameLevel::clearMessage);
        }
    },

    INTERMISSION {
        @Override
        public void onEnter(GameContext context) {
            timer.restartIndefinitely();
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.gameController().changeGameState(context.game().isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    };

    @Override
    public TickTimer timer() {
        return timer;
    }

    final TickTimer timer = new TickTimer("Timer_" + name());
}