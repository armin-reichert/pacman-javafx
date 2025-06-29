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

import static de.amr.pacmanfx.Globals.*;
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
                theGameController().changeGameState(INTRO);
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
                theGameController().changeGameState(STARTING_GAME);
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
                theGameController().changeGameState(INTRO);
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
            theGameEventManager().publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (game.isPlaying()) {
                // resume running game
                if (timer.tickCount() == 1) {
                    game.initAnimationOfPacManAndGhosts();
                    theGameLevel().getReadyToPlay();
                    theGameLevel().showPacAndGhosts();
                    theGameEventManager().publishEvent(game, GameEventType.GAME_CONTINUED);
                } else if (timer.tickCount() == TICK_RESUME_GAME) {
                    theGameController().changeGameState(GameState.HUNTING);
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
                    theGameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_HUNTING) {
                    game.playingProperty().set(true);
                    theGameController().changeGameState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.tickCount() == 1) {
                    game.buildDemoLevel();
                    theGameEventManager().publishEvent(game, GameEventType.LEVEL_CREATED);
                }
                else if (timer.tickCount() == 2) {
                    game.startLevel();
                }
                else if (timer.tickCount() == 3) {
                    // Now, actor animations are available
                    theGameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START_HUNTING) {
                    theGameController().changeGameState(GameState.HUNTING);
                }
            }
        }
    },


    HUNTING {
        int delay;

        @Override
        public void onEnter(GameModel game) {
            //TODO reconsider this
            delay = theGameController().isSelected("MS_PACMAN_TENGEN") ? 60 : 0;
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.tickCount() < delay) {
                return;
            }
            if (timer.tickCount() == delay) {
                game.startHunting();
                if (theGameLevel().message() == GameLevel.MESSAGE_READY) {
                    theGameLevel().clearMessage();
                }
            }
            game.doHuntingStep();
            if (game.isLevelCompleted()) {
                theGameController().changeGameState(LEVEL_COMPLETE);
            } else if (game.hasPacManBeenKilled()) {
                theGameController().changeGameState(PACMAN_DYING);
            } else if (game.haveGhostsBeenKilled()) {
                theGameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameModel game) {
            if (theGameLevel().message() == GameLevel.MESSAGE_READY) {
                theGameLevel().clearMessage();
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
                game.onLevelCompleted(theGameLevel());
            }

            //TODO this is crap. Maybe Tengen Ms. Pac-Man needs its own state machine?
            if (theGameController().isSelected("MS_PACMAN_TENGEN") && theGameLevel().isDemoLevel()) {
                theGameController().changeGameState(SHOWING_CREDITS);
                return;
            }

            if (timer.hasExpired()) {
                if (theGameLevel().isDemoLevel()) {
                    // just in case: if demo level is complete, go back to intro scene
                    theGameController().changeGameState(INTRO);
                } else if (game.cutScenesEnabled() && game.cutSceneNumber(theGameLevel().number()).isPresent()) {
                    theGameController().changeGameState(INTERMISSION);
                } else {
                    theGameController().changeGameState(LEVEL_TRANSITION);
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
                theGameController().changeGameState(STARTING_GAME);
            }
        }
    },


    GHOST_DYING {
        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(1);
            theGameLevel().pac().hide();
            theGameLevel().ghosts().forEach(Ghost::stopAnimation);
            theGameEventManager().publishEvent(game, GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                theGameController().resumePreviousGameState();
            } else {
                theGameLevel().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                    .forEach(ghost -> ghost.update(theGameLevel()));
                theGameLevel().blinking().tick();
            }
        }

        @Override
        public void onExit(GameModel game) {
            theGameLevel().pac().show();
            theGameLevel().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            theGameLevel().ghosts().forEach(Ghost::playAnimation);
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
            theGameEventManager().publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                if (theGameLevel().isDemoLevel()) {
                    theGameController().changeGameState(GAME_OVER);
                } else {
                    game.addLives(-1);
                    theGameController().changeGameState(game.isOver() ? GAME_OVER : STARTING_GAME);
                }
            }
            else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                theGameLevel().ghosts().forEach(Ghost::hide);
                //TODO this does not belong here
                theGameLevel().pac().selectAnimation(ANIM_PAC_DYING);
                theGameLevel().pac().resetAnimation();
            }
            else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                theGameLevel().pac().playAnimation();
                theGameEventManager().publishEvent(game, GameEventType.PAC_DYING, theGameLevel().pac().tile());
            }
            else if (timer.tickCount() == TICK_HIDE_PAC) {
                theGameLevel().pac().hide();
            }
            else if (timer.tickCount() == TICK_PAC_DEAD) {
                theGameEventManager().publishEvent(game, GameEventType.PAC_DEAD);
            }
            else {
                theGameLevel().blinking().tick();
                theGameLevel().pac().update(theGameLevel());
            }
        }

        @Override
        public void onExit(GameModel game) {
            theGameLevel().bonus().ifPresent(Bonus::setInactive);
        }
    },


    GAME_OVER {
        @Override
        public void onEnter(GameModel game) {
            timer.restartTicks(theGameLevel().gameOverStateTicks());
            game.onGameEnding();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                //TODO find unified solution
                if (theGameController().isSelected("MS_PACMAN_TENGEN")) {
                    if (theGameLevel().isDemoLevel()) {
                        theGameController().changeGameState(SHOWING_CREDITS);
                    } else {
                        boolean canContinue = game.continueOnGameOver();
                        theGameController().changeGameState(canContinue ? SETTING_OPTIONS : INTRO);
                    }
                } else {
                    game.prepareForNewGame();
                    if (game.canStartNewGame()) {
                        theGameController().changeGameState(SETTING_OPTIONS);
                    } else {
                        theGameController().changeGameState(INTRO);
                    }
                }
            }
        }

        @Override
        public void onExit(GameModel game) {
            optGameLevel().ifPresent(GameLevel::clearMessage);
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
                theGameController().changeGameState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    // Tests

    TESTING_LEVELS_SHORT {

        private int lastTestedLevelNumber;

        @Override
        public void onEnter(GameModel game) {
            theCoinMechanism().setNumCoins(1);
            lastTestedLevelNumber = 25;
            if (theGameController().isSelected("MS_PACMAN_TENGEN")) {
                lastTestedLevelNumber = 32;
            }
            timer.restartIndefinitely();
            game.prepareForNewGame();
            game.buildNormalLevel(1);
            game.startLevel();
            theGameLevel().showPacAndGhosts();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer().tickCount() > 2 * Globals.NUM_TICKS_PER_SEC) {
                theGameLevel().blinking().tick();
                theGameLevel().ghosts().forEach(ghost -> ghost.update(theGameLevel()));
                theGameLevel().bonus().ifPresent(bonus -> bonus.update(game));
            }
            if (timer().atSecond(1.0)) {
                game.initAnimationOfPacManAndGhosts();
                theGameLevel().getReadyToPlay();
                theGameLevel().showPacAndGhosts();
            }
            else if (timer().atSecond(2)) {
                theGameLevel().blinking().setStartPhase(Pulse.ON);
                theGameLevel().blinking().restart();
            }
            else if (timer().atSecond(2.5)) {
                theGameLevel().clearMessage();
                game.activateNextBonus();
            }
            else if (timer().atSecond(4.5)) {
                theGameLevel().bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                theGameEventManager().publishEvent(game, GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(6.5)) {
                theGameLevel().bonus().ifPresent(Bonus::setInactive); // needed?
                game.activateNextBonus();
            }
            else if (timer().atSecond(8.5)) {
                theGameLevel().bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                theGameEventManager().publishEvent(game, GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(10.0)) {
                theGameLevel().hidePacAndGhosts();
                theGame().onLevelCompleted(theGameLevel());
            }
            else if (timer().atSecond(11.0)) {
                if (theGameLevel().number() == lastTestedLevelNumber) {
                    theGameController().restart(GameState.BOOT);
                } else {
                    timer().restartIndefinitely();
                    game.startNextLevel();
                }
            }
        }

        @Override
        public void onExit(GameModel game) {
            theCoinMechanism().consumeCoin();
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
            theGameLevel().pac().usingAutopilotProperty().unbind();
            theGameLevel().pac().setUsingAutopilot(true);
            theGameLevel().pac().playAnimation();
            theGameLevel().ghosts().forEach(Ghost::playAnimation);
            theGameLevel().showPacAndGhosts();
            theGameLevel().showMessage(GameLevel.MESSAGE_TEST);
            theGameEventManager().publishEvent(theGame(), GameEventType.STOP_ALL_SOUNDS);
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
                if (theGameLevel().number() == lastTestedLevelNumber) {
                    theGameEventManager().publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
                    theGameController().changeGameState(INTRO);
                } else {
                    timer().restartSeconds(TEST_DURATION_SEC);
                    game.startNextLevel();
                    configureLevelForTest();
                }
            }
            else if (game.isLevelCompleted()) {
                theGameController().changeGameState(INTRO);
            } else if (game.hasPacManBeenKilled()) {
                timer.expire();
            } else if (game.haveGhostsBeenKilled()) {
                theGameController().changeGameState(GHOST_DYING);
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
                int lastCutSceneNumber = theGameController().isSelected("MS_PACMAN_TENGEN") ? 4 : 3;
                if (number < lastCutSceneNumber) {
                    game.setProperty("intermissionTestNumber", number + 1);
                    timer.restartIndefinitely();
                    //TODO find another solution and get rid of this event type
                    theGameEventManager().publishEvent(game, GameEventType.UNSPECIFIED_CHANGE);
                } else {
                    theGameController().changeGameState(INTRO);
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