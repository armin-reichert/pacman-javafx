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
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameModel;
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
            context.theGame().resetEverything();
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.theGameController().changeGameState(INTRO);
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
                context.theGameController().changeGameState(STARTING_GAME);
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
                context.theGameController().changeGameState(INTRO);
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
            context.theGameEventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameContext context) {
            if (context.theGame().isPlaying()) {
                // resume running game
                if (timer.tickCount() == 1) {
                    context.theGame().initAnimationOfPacManAndGhosts();
                    context.theGameLevel().getReadyToPlay();
                    context.theGameLevel().showPacAndGhosts();
                    context.theGameEventManager().publishEvent(GameEventType.GAME_CONTINUED);
                } else if (timer.tickCount() == TICK_RESUME_GAME) {
                    context.theGameController().changeGameState(GameState.HUNTING);
                }
            }
            else if (context.theGame().canStartNewGame()) {
                if (timer.tickCount() == 1) {
                    context.theGame().startNewGame(context);
                }
                else if (timer.tickCount() == 2) {
                    context.theGame().startLevel();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_SHOW_GUYS) {
                    context.theGameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_HUNTING) {
                    context.theGame().setPlaying(true);
                    context.theGameController().changeGameState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.tickCount() == 1) {
                    context.theGame().buildDemoLevel(context);
                    context.theGameEventManager().publishEvent(GameEventType.LEVEL_CREATED);
                }
                else if (timer.tickCount() == 2) {
                    context.theGame().startLevel();
                }
                else if (timer.tickCount() == 3) {
                    // Now, actor animations are available
                    context.theGameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START_HUNTING) {
                    context.theGameController().changeGameState(GameState.HUNTING);
                }
            }
        }
    },


    HUNTING {
        int delay;

        @Override
        public void onEnter(GameContext context) {
            //TODO reconsider this
            delay = context.theGameController().isSelected("MS_PACMAN_TENGEN") ? Globals.NUM_TICKS_PER_SEC : 0;
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.tickCount() < delay) {
                return;
            }
            if (timer.tickCount() == delay) {
                context.theGame().startHunting();
                if (context.theGameLevel().messageType() == GameLevel.MESSAGE_READY) {
                    context.theGameLevel().clearMessage();
                }
            }
            context.theGame().doHuntingStep(context);
            if (context.theGame().isLevelCompleted()) {
                context.theGameController().changeGameState(LEVEL_COMPLETE);
            } else if (context.theGame().hasPacManBeenKilled()) {
                context.theGameController().changeGameState(PACMAN_DYING);
            } else if (context.theGame().haveGhostsBeenKilled()) {
                context.theGameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameContext context) {
            if (context.theGameLevel().messageType() == GameLevel.MESSAGE_READY) {
                context.theGameLevel().clearMessage();
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
                context.theGame().onLevelCompleted();
            }

            //TODO this is crap. Maybe Tengen Ms. Pac-Man needs its own state machine?
            if (context.theGameController().isSelected("MS_PACMAN_TENGEN") && context.theGameLevel().isDemoLevel()) {
                context.theGameController().changeGameState(SHOWING_CREDITS);
                return;
            }

            if (timer.hasExpired()) {
                if (context.theGameLevel().isDemoLevel()) {
                    // just in case: if demo level is complete, go back to intro scene
                    context.theGameController().changeGameState(INTRO);
                } else if (context.theGame().areCutScenesEnabled()
                    && context.theGame().cutSceneNumber(context.theGameLevel().number()).isPresent()) {
                    context.theGameController().changeGameState(INTERMISSION);
                } else {
                    context.theGameController().changeGameState(LEVEL_TRANSITION);
                }
            }
        }
    },


    LEVEL_TRANSITION {
        @Override
        public void onEnter(GameContext context) {
            timer.restartSeconds(2);
            context.theGame().startNextLevel(context);
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.theGameController().changeGameState(STARTING_GAME);
            }
        }
    },


    GHOST_DYING {
        @Override
        public void onEnter(GameContext context) {
            timer.restartSeconds(1);
            context.theGameLevel().pac().hide();
            context.theGameLevel().ghosts().forEach(Ghost::stopAnimation);
            context.theGameEventManager().publishEvent(GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                context.theGameController().resumePreviousGameState();
            } else {
                context.theGameLevel().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                    .forEach(Ghost::tick);
                context.theGameLevel().blinking().tick();
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.theGameLevel().pac().show();
            context.theGameLevel().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            context.theGameLevel().ghosts().forEach(Ghost::playAnimation);
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
            context.theGame().onPacKilled();
            context.theGameEventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                if (context.theGameLevel().isDemoLevel()) {
                    context.theGameController().changeGameState(GAME_OVER);
                } else {
                    context.theGame().addLives(-1);
                    context.theGameController().changeGameState(context.theGame().lifeCount() == 0
                        ? GAME_OVER : STARTING_GAME);
                }
            }
            else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                context.theGameLevel().ghosts().forEach(Ghost::hide);
                //TODO this does not belong here
                context.theGameLevel().pac().selectAnimation(ANIM_PAC_DYING);
                context.theGameLevel().pac().resetAnimation();
            }
            else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                context.theGameLevel().pac().playAnimation();
                context.theGameEventManager().publishEvent(GameEventType.PAC_DYING, context.theGameLevel().pac().tile());
            }
            else if (timer.tickCount() == TICK_HIDE_PAC) {
                context.theGameLevel().pac().hide();
            }
            else if (timer.tickCount() == TICK_PAC_DEAD) {
                context.theGameEventManager().publishEvent(GameEventType.PAC_DEAD);
            }
            else {
                context.theGameLevel().blinking().tick();
                context.theGameLevel().pac().tick();
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.theGameLevel().bonus().ifPresent(Bonus::setInactive);
        }
    },


    GAME_OVER {
        @Override
        public void onEnter(GameContext context) {
            timer.restartTicks(context.theGameLevel().gameOverStateTicks());
            context.theGame().onGameEnding();
        }

        @Override
        public void onUpdate(GameContext context) {
            if (timer.hasExpired()) {
                //TODO find unified solution
                if (context.theGameController().isSelected("MS_PACMAN_TENGEN")) {
                    if (context.theGameLevel().isDemoLevel()) {
                        context.theGameController().changeGameState(SHOWING_CREDITS);
                    } else {
                        boolean canContinue = context.theGame().continueOnGameOver();
                        context.theGameController().changeGameState(canContinue ? SETTING_OPTIONS_FOR_START : INTRO);
                    }
                } else {
                    context.theGame().prepareForNewGame();
                    if (context.theGame().canStartNewGame()) {
                        context.theGameController().changeGameState(SETTING_OPTIONS_FOR_START);
                    } else {
                        context.theGameController().changeGameState(INTRO);
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
                context.theGameController().changeGameState(context.theGame().isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    // Tests

    TESTING_LEVELS_SHORT {

        private int lastTestedLevelNumber;

        @Override
        public void onEnter(GameContext context) {
            context.theCoinMechanism().setNumCoins(1);
            lastTestedLevelNumber = context.theGame().lastLevelNumber() == Integer.MAX_VALUE ? 25 : context.theGame().lastLevelNumber();
            timer.restartIndefinitely();
            context.theGame().prepareForNewGame();
            context.theGame().buildNormalLevel(context, 1);
            context.theGame().startLevel();
            context.theGameLevel().showPacAndGhosts();
            context.theGameLevel().showMessage(GameLevel.MESSAGE_TEST);
        }

        @Override
        public void onUpdate(GameContext context) {
            final GameLevel gameLevel = context.theGameLevel();
            if (timer().tickCount() > 2 * Globals.NUM_TICKS_PER_SEC) {
                gameLevel.blinking().tick();
                gameLevel.ghosts().forEach(Ghost::tick);
                gameLevel.bonus().ifPresent(Bonus::tick);
            }
            if (timer().atSecond(1.0)) {
                context.theGame().initAnimationOfPacManAndGhosts();
                gameLevel.getReadyToPlay();
                gameLevel.showPacAndGhosts();
            }
            else if (timer().atSecond(2)) {
                gameLevel.blinking().setStartPhase(Pulse.ON);
                gameLevel.blinking().restart();
            }
            else if (timer().atSecond(2.5)) {
                gameLevel.clearMessage();
                context.theGame().activateNextBonus(context);
            }
            else if (timer().atSecond(4.5)) {
                gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                context.theGameEventManager().publishEvent(GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(6.5)) {
                gameLevel.bonus().ifPresent(Bonus::setInactive); // needed?
                context.theGame().activateNextBonus(context);
            }
            else if (timer().atSecond(8.5)) {
                gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                context.theGameEventManager().publishEvent(GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(10.0)) {
                gameLevel.hidePacAndGhosts();
                context.theGame().onLevelCompleted();
            }
            else if (timer().atSecond(11.0)) {
                if (gameLevel.number() == lastTestedLevelNumber) {
                    context.theCoinMechanism().setNumCoins(0);
                    context.theGame().resetEverything();
                    context.theGameController().restart(GameState.BOOT);
                } else {
                    timer().restartIndefinitely();
                    context.theGame().startNextLevel(context);
                    gameLevel.showMessage(GameLevel.MESSAGE_TEST);
                }
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.theCoinMechanism().setNumCoins(0);
            context.theGame().resetEverything();
            context.theGame().theHUD().theLevelCounter().clear();
        }
    },

    /**
     * Runs levels for some fixed time e.g. 10 seconds.
     */
    TESTING_LEVELS_MEDIUM {

        static final int TEST_DURATION_SEC = 10;

        private int lastTestedLevelNumber;

        private void configureLevelForTest(GameContext context) {
            final GameLevel gameLevel = context.theGameLevel();
            gameLevel.pac().usingAutopilotProperty().unbind();
            gameLevel.pac().setUsingAutopilot(true);
            gameLevel.pac().playAnimation();
            gameLevel.ghosts().forEach(Ghost::playAnimation);
            gameLevel.showPacAndGhosts();
            gameLevel.showMessage(GameLevel.MESSAGE_TEST);
            context.theGameEventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onEnter(GameContext context) {
            lastTestedLevelNumber = context.theGame().lastLevelNumber() == Integer.MAX_VALUE ? 25 : context.theGame().lastLevelNumber();
            timer.restartSeconds(TEST_DURATION_SEC);
            context.theGame().prepareForNewGame();
            context.theGame().buildNormalLevel(context, 1);
            context.theGame().startLevel();
            configureLevelForTest(context);
        }

        @Override
        public void onUpdate(GameContext context) {
            context.theGame().doHuntingStep(context);
            if (timer().hasExpired()) {
                if (context.theGameLevel().number() == lastTestedLevelNumber) {
                    context.theGameEventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
                    context.theGameController().changeGameState(INTRO);
                } else {
                    timer().restartSeconds(TEST_DURATION_SEC);
                    context.theGame().startNextLevel(context);
                    configureLevelForTest(context);
                }
            }
            else if (context.theGame().isLevelCompleted()) {
                context.theGameController().changeGameState(INTRO);
            } else if (context.theGame().hasPacManBeenKilled()) {
                timer.expire();
            } else if (context.theGame().haveGhostsBeenKilled()) {
                context.theGameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameContext context) {
            context.theGame().theHUD().theLevelCounter().clear();
        }
    },

    TESTING_CUT_SCENES {
        @Override
        public void onEnter(GameContext context) {
            timer.restartIndefinitely();
            //TODO
            if (context.theGame() instanceof GameModel gameModel) {
                gameModel.setProperty("intermissionTestNumber", 1);
            }
        }

        @Override
        public void onUpdate(GameContext context) {
            if (context.theGame() instanceof GameModel gameModel) {
                if (timer.hasExpired()) {
                    int number = gameModel.<Integer>getProperty("intermissionTestNumber");
                    int lastCutSceneNumber = context.theGameController().isSelected("MS_PACMAN_TENGEN") ? 4 : 3;
                    if (number < lastCutSceneNumber) {
                        gameModel.setProperty("intermissionTestNumber", number + 1);
                        timer.restartIndefinitely();
                        //TODO find another solution and get rid of this event type
                        context.theGameEventManager().publishEvent(GameEventType.UNSPECIFIED_CHANGE);
                    } else {
                        context.theGameController().changeGameState(INTRO);
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