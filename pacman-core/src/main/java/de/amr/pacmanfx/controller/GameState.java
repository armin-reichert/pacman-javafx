/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

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

import static de.amr.pacmanfx.Globals.theGameContext;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;

/**
 * States of the Pac-Man games state machine.
 */
public enum GameState implements FsmState<GameModel> {

    BOOT { // "Das muss das Boot abkönnen!"
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
            game.resetEverything();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                theGameContext().theGameController().changeGameState(INTRO);
            }
        }
    },


    INTRO {
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                theGameContext().theGameController().changeGameState(STARTING_GAME);
            }
        }
    },


    SETTING_OPTIONS {
        @Override
        public void onUpdate(GameModel game) {
            // wait for user interaction to leave state
        }
    },


    SHOWING_CREDITS {
        @Override
        public void onEnter(GameModel context) {
            timer.restartIndefinitely();
        }

        @Override
        public void onUpdate(GameModel context) {
            if (timer.hasExpired()) {
                theGameContext().theGameController().changeGameState(INTRO);
            }
        }
    },


    STARTING_GAME {
        static final short TICK_NEW_GAME_SHOW_GUYS = 120;
        static final short TICK_NEW_GAME_START_HUNTING = 240;
        static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
        static final short TICK_RESUME_GAME =  90;

        @Override
        public void onEnter(GameModel game) {
            theGameContext().theGameEventManager().publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (game.isPlaying()) {
                // resume running game
                if (timer.tickCount() == 1) {
                    game.initAnimationOfPacManAndGhosts();
                    theGameContext().theGameLevel().getReadyToPlay();
                    theGameContext().theGameLevel().showPacAndGhosts();
                    theGameContext().theGameEventManager().publishEvent(game, GameEventType.GAME_CONTINUED);
                } else if (timer.tickCount() == TICK_RESUME_GAME) {
                    theGameContext().theGameController().changeGameState(GameState.HUNTING);
                }
            }
            else if (game.canStartNewGame()) {
                if (timer.tickCount() == 1) {
                    game.startNewGame();
                }
                else if (timer.tickCount() == 2) {
                    game.startLevel();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_SHOW_GUYS) {
                    theGameContext().theGameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_HUNTING) {
                    game.playingProperty().set(true);
                    theGameContext().theGameController().changeGameState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.tickCount() == 1) {
                    game.buildDemoLevel();
                    theGameContext().theGameEventManager().publishEvent(game, GameEventType.LEVEL_CREATED);
                }
                else if (timer.tickCount() == 2) {
                    game.startLevel();
                }
                else if (timer.tickCount() == 3) {
                    // Now, actor animations are available
                    theGameContext().theGameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START_HUNTING) {
                    theGameContext().theGameController().changeGameState(GameState.HUNTING);
                }
            }
        }
    },


    HUNTING {
        int delay;

        @Override
        public void onEnter(GameModel game) {
            //TODO reconsider this
            delay = theGameContext().theGameController().isSelected("MS_PACMAN_TENGEN") ? 60 : 0;
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.tickCount() < delay) {
                return;
            }
            if (timer.tickCount() == delay) {
                game.startHunting();
                if (theGameContext().theGameLevel().messageType() == GameLevel.MESSAGE_READY) {
                    theGameContext().theGameLevel().clearMessage();
                }
            }
            game.doHuntingStep();
            if (game.isLevelCompleted()) {
                theGameContext().theGameController().changeGameState(LEVEL_COMPLETE);
            } else if (game.hasPacManBeenKilled()) {
                theGameContext().theGameController().changeGameState(PACMAN_DYING);
            } else if (game.haveGhostsBeenKilled()) {
                theGameContext().theGameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameModel game) {
            if (theGameContext().theGameLevel().messageType() == GameLevel.MESSAGE_READY) {
                theGameContext().theGameLevel().clearMessage();
            }
        }
    },


    LEVEL_COMPLETE {

        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely(); // UI triggers timeout
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.tickCount() == 1) {
                game.onLevelCompleted(theGameContext().theGameLevel());
            }

            //TODO this is crap. Maybe Tengen Ms. Pac-Man needs its own state machine?
            if (theGameContext().theGameController().isSelected("MS_PACMAN_TENGEN") && theGameContext().theGameLevel().isDemoLevel()) {
                theGameContext().theGameController().changeGameState(SHOWING_CREDITS);
                return;
            }

            if (timer.hasExpired()) {
                if (theGameContext().theGameLevel().isDemoLevel()) {
                    // just in case: if demo level is complete, go back to intro scene
                    theGameContext().theGameController().changeGameState(INTRO);
                } else if (game.cutScenesEnabled() && game.cutSceneNumber(theGameContext().theGameLevel().number()).isPresent()) {
                    theGameContext().theGameController().changeGameState(INTERMISSION);
                } else {
                    theGameContext().theGameController().changeGameState(LEVEL_TRANSITION);
                }
            }
        }
    },


    LEVEL_TRANSITION {
        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(1);
            game.startNextLevel();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                theGameContext().theGameController().changeGameState(STARTING_GAME);
            }
        }
    },


    GHOST_DYING {
        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(1);
            theGameContext().theGameLevel().pac().hide();
            theGameContext().theGameLevel().ghosts().forEach(Ghost::stopAnimation);
            theGameContext().theGameEventManager().publishEvent(game, GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                theGameContext().theGameController().resumePreviousGameState();
            } else {
                theGameContext().theGameLevel().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                    .forEach(ghost -> ghost.update(theGameContext().theGameLevel()));
                theGameContext().theGameLevel().blinking().tick();
            }
        }

        @Override
        public void onExit(GameModel game) {
            theGameContext().theGameLevel().pac().show();
            theGameContext().theGameLevel().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            theGameContext().theGameLevel().ghosts().forEach(Ghost::playAnimation);
        }
    },


    PACMAN_DYING {
        static final int TICK_HIDE_GHOSTS = 60;
        static final int TICK_START_PAC_ANIMATION = 90;
        static final int TICK_HIDE_PAC = 190;
        static final int TICK_PAC_DEAD = 240;

        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
            game.onPacKilled();
            theGameContext().theGameEventManager().publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                if (theGameContext().theGameLevel().isDemoLevel()) {
                    theGameContext().theGameController().changeGameState(GAME_OVER);
                } else {
                    game.addLives(-1);
                    theGameContext().theGameController().changeGameState(game.isOver() ? GAME_OVER : STARTING_GAME);
                }
            }
            else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                theGameContext().theGameLevel().ghosts().forEach(Ghost::hide);
                //TODO this does not belong here
                theGameContext().theGameLevel().pac().selectAnimation(ANIM_PAC_DYING);
                theGameContext().theGameLevel().pac().resetAnimation();
            }
            else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                theGameContext().theGameLevel().pac().playAnimation();
                theGameContext().theGameEventManager().publishEvent(game, GameEventType.PAC_DYING, theGameContext().theGameLevel().pac().tile());
            }
            else if (timer.tickCount() == TICK_HIDE_PAC) {
                theGameContext().theGameLevel().pac().hide();
            }
            else if (timer.tickCount() == TICK_PAC_DEAD) {
                theGameContext().theGameEventManager().publishEvent(game, GameEventType.PAC_DEAD);
            }
            else {
                theGameContext().theGameLevel().blinking().tick();
                theGameContext().theGameLevel().pac().update(theGameContext().theGameLevel());
            }
        }

        @Override
        public void onExit(GameModel game) {
            theGameContext().theGameLevel().bonus().ifPresent(Bonus::setInactive);
        }
    },


    GAME_OVER {
        @Override
        public void onEnter(GameModel game) {
            timer.restartTicks(theGameContext().theGameLevel().gameOverStateTicks());
            game.onGameEnding();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                //TODO find unified solution
                if (theGameContext().theGameController().isSelected("MS_PACMAN_TENGEN")) {
                    if (theGameContext().theGameLevel().isDemoLevel()) {
                        theGameContext().theGameController().changeGameState(SHOWING_CREDITS);
                    } else {
                        boolean canContinue = game.continueOnGameOver();
                        theGameContext().theGameController().changeGameState(canContinue ? SETTING_OPTIONS : INTRO);
                    }
                } else {
                    game.prepareForNewGame();
                    if (game.canStartNewGame()) {
                        theGameContext().theGameController().changeGameState(SETTING_OPTIONS);
                    } else {
                        theGameContext().theGameController().changeGameState(INTRO);
                    }
                }
            }
        }

        @Override
        public void onExit(GameModel game) {
            theGameContext().optGameLevel().ifPresent(GameLevel::clearMessage);
        }
    },


    INTERMISSION {
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                theGameContext().theGameController().changeGameState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    // Tests

    TESTING_LEVELS_SHORT {

        private int lastTestedLevelNumber;

        @Override
        public void onEnter(GameModel game) {
            theGameContext().theCoinMechanism().setNumCoins(1);
            lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
            timer.restartIndefinitely();
            game.prepareForNewGame();
            game.buildNormalLevel(1);
            game.startLevel();
            theGameContext().theGameLevel().showPacAndGhosts();
            theGameContext().theGameLevel().showMessage(GameLevel.MESSAGE_TEST);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().tickCount() > 2 * Globals.NUM_TICKS_PER_SEC) {
                theGameContext().theGameLevel().blinking().tick();
                theGameContext().theGameLevel().ghosts().forEach(ghost -> ghost.update(theGameContext().theGameLevel()));
                theGameContext().theGameLevel().bonus().ifPresent(bonus -> bonus.update(game));
            }
            if (timer().atSecond(1.0)) {
                game.initAnimationOfPacManAndGhosts();
                theGameContext().theGameLevel().getReadyToPlay();
                theGameContext().theGameLevel().showPacAndGhosts();
            }
            else if (timer().atSecond(2)) {
                theGameContext().theGameLevel().blinking().setStartPhase(Pulse.ON);
                theGameContext().theGameLevel().blinking().restart();
            }
            else if (timer().atSecond(2.5)) {
                theGameContext().theGameLevel().clearMessage();
                game.activateNextBonus();
            }
            else if (timer().atSecond(4.5)) {
                theGameContext().theGameLevel().bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                theGameContext().theGameEventManager().publishEvent(game, GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(6.5)) {
                theGameContext().theGameLevel().bonus().ifPresent(Bonus::setInactive); // needed?
                game.activateNextBonus();
            }
            else if (timer().atSecond(8.5)) {
                theGameContext().theGameLevel().bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                theGameContext().theGameEventManager().publishEvent(game, GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(10.0)) {
                theGameContext().theGameLevel().hidePacAndGhosts();
                theGameContext().theGame().onLevelCompleted(theGameContext().theGameLevel());
            }
            else if (timer().atSecond(11.0)) {
                if (theGameContext().theGameLevel().number() == lastTestedLevelNumber) {
                    theGameContext().theCoinMechanism().setNumCoins(0);
                    theGameContext().theGame().resetEverything();
                    theGameContext().theGameController().restart(GameState.BOOT);
                } else {
                    timer().restartIndefinitely();
                    game.startNextLevel();
                    theGameContext().theGameLevel().showMessage(GameLevel.MESSAGE_TEST);
                }
            }
        }

        @Override
        public void onExit(GameModel game) {
            theGameContext().theCoinMechanism().setNumCoins(0);
            theGameContext().theGame().resetEverything();
            game.hud().levelCounter().clear();
        }
    },

    /**
     * Runs levels for some fixed time e.g. 10 seconds.
     */
    TESTING_LEVELS_MEDIUM {

        static final int TEST_DURATION_SEC = 10;

        private int lastTestedLevelNumber;

        private void configureLevelForTest() {
            theGameContext().theGameLevel().pac().usingAutopilotProperty().unbind();
            theGameContext().theGameLevel().pac().setUsingAutopilot(true);
            theGameContext().theGameLevel().pac().playAnimation();
            theGameContext().theGameLevel().ghosts().forEach(Ghost::playAnimation);
            theGameContext().theGameLevel().showPacAndGhosts();
            theGameContext().theGameLevel().showMessage(GameLevel.MESSAGE_TEST);
            theGameContext().theGameEventManager().publishEvent(theGameContext().theGame(), GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onEnter(GameModel game) {
            lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
            timer.restartSeconds(TEST_DURATION_SEC);
            game.prepareForNewGame();
            game.buildNormalLevel(1);
            game.startLevel();
            configureLevelForTest();
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doHuntingStep();
            if (timer().hasExpired()) {
                if (theGameContext().theGameLevel().number() == lastTestedLevelNumber) {
                    theGameContext().theGameEventManager().publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
                    theGameContext().theGameController().changeGameState(INTRO);
                } else {
                    timer().restartSeconds(TEST_DURATION_SEC);
                    game.startNextLevel();
                    configureLevelForTest();
                }
            }
            else if (game.isLevelCompleted()) {
                theGameContext().theGameController().changeGameState(INTRO);
            } else if (game.hasPacManBeenKilled()) {
                timer.expire();
            } else if (game.haveGhostsBeenKilled()) {
                theGameContext().theGameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.hud().levelCounter().clear();
        }
    },

    TESTING_CUT_SCENES {
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
            game.setProperty("intermissionTestNumber", 1);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                int number = game.<Integer>getProperty("intermissionTestNumber");
                int lastCutSceneNumber = theGameContext().theGameController().isSelected("MS_PACMAN_TENGEN") ? 4 : 3;
                if (number < lastCutSceneNumber) {
                    game.setProperty("intermissionTestNumber", number + 1);
                    timer.restartIndefinitely();
                    //TODO find another solution and get rid of this event type
                    theGameContext().theGameEventManager().publishEvent(game, GameEventType.UNSPECIFIED_CHANGE);
                } else {
                    theGameContext().theGameController().changeGameState(INTRO);
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