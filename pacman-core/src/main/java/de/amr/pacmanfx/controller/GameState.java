/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;

/**
 * States of the Pac-Man games state machine.
 */
public enum GameState implements FsmState<GameContext> {

    BOOT { // "Das muss das Boot abkönnen!"
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
                context.gameController().changeGameState(STARTING_GAME);
            }
        }
    },


    SETTING_OPTIONS_FOR_START {
        @Override
        public void onUpdate(GameContext context) {
            // wait for user interaction to leave state
        }
    },


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


    STARTING_GAME {
        static final short TICK_NEW_GAME_SHOW_GUYS = 120;
        static final short TICK_NEW_GAME_START_HUNTING = 240;
        static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
        static final short TICK_RESUME_GAME =  90;

        @Override
        public void onEnter(GameContext context) {
            context.eventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameContext context) {
            if (context.game().isPlaying()) {
                // resume running game
                if (timer.tickCount() == 1) {
                    context.game().resetPacManAndGhostAnimations();
                    context.gameLevel().getReadyToPlay();
                    context.gameLevel().showPacAndGhosts();
                    context.eventManager().publishEvent(GameEventType.GAME_CONTINUED);
                } else if (timer.tickCount() == TICK_RESUME_GAME) {
                    context.gameController().changeGameState(GameState.HUNTING);
                }
            }
            else if (context.game().canStartNewGame()) {
                if (timer.tickCount() == 1) {
                    context.game().startNewGame(context);
                }
                else if (timer.tickCount() == 2) {
                    context.game().startLevel();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_SHOW_GUYS) {
                    context.gameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_HUNTING) {
                    context.game().setPlaying(true);
                    context.gameController().changeGameState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.tickCount() == 1) {
                    context.game().buildDemoLevel(context);
                    context.eventManager().publishEvent(GameEventType.LEVEL_CREATED);
                }
                else if (timer.tickCount() == 2) {
                    context.game().startLevel();
                }
                else if (timer.tickCount() == 3) {
                    // Now, actor animations are available
                    context.gameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START_HUNTING) {
                    context.gameController().changeGameState(GameState.HUNTING);
                }
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
            if (timer.tickCount() == delay) {
                context.game().startHunting();
                if (context.gameLevel().messageType() == GameLevel.MESSAGE_READY) {
                    context.gameLevel().clearMessage();
                }
            }
            context.game().doHuntingStep();
            if (context.game().isLevelCompleted()) {
                context.gameController().changeGameState(LEVEL_COMPLETE);
            } else if (context.game().hasPacManBeenKilled()) {
                context.gameController().changeGameState(PACMAN_DYING);
            } else if (context.game().haveGhostsBeenKilled()) {
                context.gameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameContext context) {
            if (context.gameLevel().messageType() == GameLevel.MESSAGE_READY) {
                context.gameLevel().clearMessage();
            }
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
                context.game().onLevelCompleted();
            }

            //TODO this is crap. Maybe Tengen Ms. Pac-Man needs its own state machine?
            if (context.gameController().isSelected("MS_PACMAN_TENGEN") && context.gameLevel().isDemoLevel()) {
                context.gameController().changeGameState(SHOWING_CREDITS);
                return;
            }

            if (timer.hasExpired()) {
                if (context.gameLevel().isDemoLevel()) {
                    // just in case: if demo level is complete, go back to intro scene
                    context.gameController().changeGameState(INTRO);
                } else if (context.game().areCutScenesEnabled()
                    && context.game().cutSceneNumber(context.gameLevel().number()).isPresent()) {
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
            context.game().startNextLevel(context);
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.gameController().changeGameState(STARTING_GAME);
            }
        }
    },


    GHOST_DYING {
        @Override
        public void onEnter(GameContext context) {
            timer.restartSeconds(1);
            context.gameLevel().pac().hide();
            context.gameLevel().ghosts().forEach(Ghost::stopAnimation);
            context.eventManager().publishEvent(GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.gameController().resumePreviousGameState();
            } else {
                context.gameLevel().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                    .forEach(Ghost::tick);
                context.gameLevel().blinking().tick();
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.gameLevel().pac().show();
            context.gameLevel().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            context.gameLevel().ghosts().forEach(Ghost::playAnimation);
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
                        ? GAME_OVER : STARTING_GAME);
                }
            }
            else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                context.gameLevel().ghosts().forEach(Ghost::hide);
                //TODO this does not belong here
                context.gameLevel().pac().selectAnimation(ANIM_PAC_DYING);
                context.gameLevel().pac().resetAnimation();
            }
            else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                context.gameLevel().pac().playAnimation();
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
                context.gameLevel().pac().tick();
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
                        boolean canContinue = context.game().continueOnGameOver();
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
    },

    // Tests

    TESTING_LEVELS_SHORT {

        private int lastTestedLevelNumber;

        @Override
        public void onEnter(GameContext context) {
            context.coinMechanism().setNumCoins(1);
            lastTestedLevelNumber = context.game().lastLevelNumber() == Integer.MAX_VALUE ? 25 : context.game().lastLevelNumber();
            timer.restartIndefinitely();
            context.game().prepareForNewGame();
            context.game().buildNormalLevel(context, 1);
            context.game().startLevel();
            context.gameLevel().showPacAndGhosts();
            context.gameLevel().showMessage(GameLevel.MESSAGE_TEST);
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameLevel gameLevel = context.gameLevel();
            if (timer().tickCount() > 2 * Globals.NUM_TICKS_PER_SEC) {
                gameLevel.blinking().tick();
                gameLevel.ghosts().forEach(Ghost::tick);
                gameLevel.bonus().ifPresent(Bonus::tick);
            }
            if (timer().atSecond(1.0)) {
                context.game().resetPacManAndGhostAnimations();
                gameLevel.getReadyToPlay();
                gameLevel.showPacAndGhosts();
            }
            else if (timer().atSecond(2)) {
                gameLevel.blinking().setStartPhase(Pulse.ON);
                gameLevel.blinking().restart();
            }
            else if (timer().atSecond(2.5)) {
                gameLevel.clearMessage();
                context.game().activateNextBonus(context);
            }
            else if (timer().atSecond(4.5)) {
                gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                context.eventManager().publishEvent(GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(6.5)) {
                gameLevel.bonus().ifPresent(Bonus::setInactive); // needed?
                context.game().activateNextBonus(context);
            }
            else if (timer().atSecond(8.5)) {
                gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                context.eventManager().publishEvent(GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(10.0)) {
                gameLevel.hidePacAndGhosts();
                context.game().onLevelCompleted();
            }
            else if (timer().atSecond(11.0)) {
                if (gameLevel.number() == lastTestedLevelNumber) {
                    context.coinMechanism().setNumCoins(0);
                    context.game().resetEverything();
                    context.gameController().restart(GameState.BOOT);
                } else {
                    timer().restartIndefinitely();
                    context.game().startNextLevel(context);
                    gameLevel.showMessage(GameLevel.MESSAGE_TEST);
                }
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.coinMechanism().setNumCoins(0);
            context.game().resetEverything();
            context.game().theHUD().theLevelCounter().clear();
        }
    },

    /**
     * Runs levels for some fixed time e.g. 10 seconds.
     */
    TESTING_LEVELS_MEDIUM {

        static final int TEST_DURATION_SEC = 10;

        private int lastTestedLevelNumber;

        private void configureLevelForTest(GameContext context) {
            final GameLevel gameLevel = context.gameLevel();
            gameLevel.pac().usingAutopilotProperty().unbind();
            gameLevel.pac().setUsingAutopilot(true);
            gameLevel.pac().playAnimation();
            gameLevel.ghosts().forEach(Ghost::playAnimation);
            gameLevel.showPacAndGhosts();
            gameLevel.showMessage(GameLevel.MESSAGE_TEST);
            context.eventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onEnter(GameContext context) {
            lastTestedLevelNumber = context.game().lastLevelNumber() == Integer.MAX_VALUE ? 25 : context.game().lastLevelNumber();
            timer.restartSeconds(TEST_DURATION_SEC);
            context.game().prepareForNewGame();
            context.game().buildNormalLevel(context, 1);
            context.game().startLevel();
            configureLevelForTest(context);
        }

        @Override
        public void onUpdate(GameContext context) {
            context.game().doHuntingStep();
            if (timer().hasExpired()) {
                if (context.gameLevel().number() == lastTestedLevelNumber) {
                    context.eventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
                    context.gameController().changeGameState(INTRO);
                } else {
                    timer().restartSeconds(TEST_DURATION_SEC);
                    context.game().startNextLevel(context);
                    configureLevelForTest(context);
                }
            }
            else if (context.game().isLevelCompleted()) {
                context.gameController().changeGameState(INTRO);
            } else if (context.game().hasPacManBeenKilled()) {
                timer.expire();
            } else if (context.game().haveGhostsBeenKilled()) {
                context.gameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.game().theHUD().theLevelCounter().clear();
        }
    },

    TESTING_CUT_SCENES {
        @Override
        public void onEnter(GameContext context) {
            timer.restartIndefinitely();
            if (context.game() instanceof AbstractGameModel gameModel) {
                gameModel.testedCutSceneNumber = 1;
            }
        }

        @Override
        public void onUpdate(GameContext context) {
            if (context.game() instanceof AbstractGameModel gameModel) {
                if (timer.hasExpired()) {
                    int lastCutSceneNumber = context.gameController().isSelected("MS_PACMAN_TENGEN") ? 4 : 3;
                    if (gameModel.testedCutSceneNumber < lastCutSceneNumber) {
                        gameModel.testedCutSceneNumber += 1;
                        timer.restartIndefinitely();
                        //TODO find another solution and get rid of this event type
                        context.eventManager().publishEvent(GameEventType.UNSPECIFIED_CHANGE);
                    } else {
                        context.gameController().changeGameState(INTRO);
                    }
                }
            }
        }
    };

    @Override
    public TickTimer timer() {
        return timer;
    }

    final TickTimer timer = new TickTimer("Timer_" + name());
}