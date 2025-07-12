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
        public void onEnter(GameContext gameContext) {
            timer.restartIndefinitely();
            gameContext.theGame().resetEverything();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.hasExpired()) {
                gameContext.theGameController().changeGameState(INTRO);
            }
        }
    },


    INTRO {
        @Override
        public void onEnter(GameContext gameContext) {
            timer.restartIndefinitely();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.hasExpired()) {
                gameContext.theGameController().changeGameState(STARTING_GAME);
            }
        }
    },


    SETTING_OPTIONS {
        @Override
        public void onUpdate(GameContext gameContext) {
            // wait for user interaction to leave state
        }
    },


    SHOWING_CREDITS {
        @Override
        public void onEnter(GameContext gameContext) {
            timer.restartIndefinitely();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.hasExpired()) {
                gameContext.theGameController().changeGameState(INTRO);
            }
        }
    },


    STARTING_GAME {
        static final short TICK_NEW_GAME_SHOW_GUYS = 120;
        static final short TICK_NEW_GAME_START_HUNTING = 240;
        static final short TICK_DEMO_LEVEL_START_HUNTING = 120;
        static final short TICK_RESUME_GAME =  90;

        @Override
        public void onEnter(GameContext gameContext) {
            gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (gameContext.theGame().isPlaying()) {
                // resume running game
                if (timer.tickCount() == 1) {
                    gameContext.theGame().initAnimationOfPacManAndGhosts();
                    gameContext.theGameLevel().getReadyToPlay();
                    gameContext.theGameLevel().showPacAndGhosts();
                    gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.GAME_CONTINUED);
                } else if (timer.tickCount() == TICK_RESUME_GAME) {
                    gameContext.theGameController().changeGameState(GameState.HUNTING);
                }
            }
            else if (gameContext.theGame().canStartNewGame()) {
                if (timer.tickCount() == 1) {
                    gameContext.theGame().startNewGame();
                }
                else if (timer.tickCount() == 2) {
                    gameContext.theGame().startLevel();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_SHOW_GUYS) {
                    gameContext.theGameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_HUNTING) {
                    gameContext.theGame().playingProperty().set(true);
                    gameContext.theGameController().changeGameState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.tickCount() == 1) {
                    gameContext.theGame().buildDemoLevel();
                    gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.LEVEL_CREATED);
                }
                else if (timer.tickCount() == 2) {
                    gameContext.theGame().startLevel();
                }
                else if (timer.tickCount() == 3) {
                    // Now, actor animations are available
                    gameContext.theGameLevel().showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START_HUNTING) {
                    gameContext.theGameController().changeGameState(GameState.HUNTING);
                }
            }
        }
    },


    HUNTING {
        int delay;

        @Override
        public void onEnter(GameContext gameContext) {
            //TODO reconsider this
            delay = gameContext.theGameController().isSelected("MS_PACMAN_TENGEN") ? 60 : 0;
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.tickCount() < delay) {
                return;
            }
            if (timer.tickCount() == delay) {
                gameContext.theGame().startHunting();
                if (gameContext.theGameLevel().messageType() == GameLevel.MESSAGE_READY) {
                    gameContext.theGameLevel().clearMessage();
                }
            }
            gameContext.theGame().doHuntingStep();
            if (gameContext.theGame().isLevelCompleted()) {
                gameContext.theGameController().changeGameState(LEVEL_COMPLETE);
            } else if (gameContext.theGame().hasPacManBeenKilled()) {
                gameContext.theGameController().changeGameState(PACMAN_DYING);
            } else if (gameContext.theGame().haveGhostsBeenKilled()) {
                gameContext.theGameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            if (gameContext.theGameLevel().messageType() == GameLevel.MESSAGE_READY) {
                gameContext.theGameLevel().clearMessage();
            }
        }
    },


    LEVEL_COMPLETE {

        @Override
        public void onEnter(GameContext gameContext) {
            timer.restartIndefinitely(); // UI triggers timeout
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.tickCount() == 1) {
                gameContext.theGame().onLevelCompleted(gameContext.theGameLevel());
            }

            //TODO this is crap. Maybe Tengen Ms. Pac-Man needs its own state machine?
            if (gameContext.theGameController().isSelected("MS_PACMAN_TENGEN") && gameContext.theGameLevel().isDemoLevel()) {
                gameContext.theGameController().changeGameState(SHOWING_CREDITS);
                return;
            }

            if (timer.hasExpired()) {
                if (gameContext.theGameLevel().isDemoLevel()) {
                    // just in case: if demo level is complete, go back to intro scene
                    gameContext.theGameController().changeGameState(INTRO);
                } else if (gameContext.theGame().cutScenesEnabled() && gameContext.theGame().cutSceneNumber(gameContext.theGameLevel().number()).isPresent()) {
                    gameContext.theGameController().changeGameState(INTERMISSION);
                } else {
                    gameContext.theGameController().changeGameState(LEVEL_TRANSITION);
                }
            }
        }
    },


    LEVEL_TRANSITION {
        @Override
        public void onEnter(GameContext gameContext) {
            timer.restartSeconds(1);
            gameContext.theGame().startNextLevel();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.hasExpired()) {
                gameContext.theGameController().changeGameState(STARTING_GAME);
            }
        }
    },


    GHOST_DYING {
        @Override
        public void onEnter(GameContext gameContext) {
            timer.restartSeconds(1);
            gameContext.theGameLevel().pac().hide();
            gameContext.theGameLevel().ghosts().forEach(Ghost::stopAnimation);
            gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.hasExpired()) {
                gameContext.theGameController().resumePreviousGameState();
            } else {
                gameContext.theGameLevel().ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                    .forEach(ghost -> ghost.update(gameContext.theGameLevel()));
                gameContext.theGameLevel().blinking().tick();
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            gameContext.theGameLevel().pac().show();
            gameContext.theGameLevel().ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            gameContext.theGameLevel().ghosts().forEach(Ghost::playAnimation);
        }
    },


    PACMAN_DYING {
        static final int TICK_HIDE_GHOSTS = 60;
        static final int TICK_START_PAC_ANIMATION = 90;
        static final int TICK_HIDE_PAC = 190;
        static final int TICK_PAC_DEAD = 240;

        @Override
        public void onEnter(GameContext gameContext) {
            timer.restartIndefinitely();
            gameContext.theGame().onPacKilled();
            gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.hasExpired()) {
                if (gameContext.theGameLevel().isDemoLevel()) {
                    gameContext.theGameController().changeGameState(GAME_OVER);
                } else {
                    gameContext.theGame().addLives(-1);
                    gameContext.theGameController().changeGameState(gameContext.theGame().isOver() ? GAME_OVER : STARTING_GAME);
                }
            }
            else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                gameContext.theGameLevel().ghosts().forEach(Ghost::hide);
                //TODO this does not belong here
                gameContext.theGameLevel().pac().selectAnimation(ANIM_PAC_DYING);
                gameContext.theGameLevel().pac().resetAnimation();
            }
            else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                gameContext.theGameLevel().pac().playAnimation();
                gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.PAC_DYING, gameContext.theGameLevel().pac().tile());
            }
            else if (timer.tickCount() == TICK_HIDE_PAC) {
                gameContext.theGameLevel().pac().hide();
            }
            else if (timer.tickCount() == TICK_PAC_DEAD) {
                gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.PAC_DEAD);
            }
            else {
                gameContext.theGameLevel().blinking().tick();
                gameContext.theGameLevel().pac().update(gameContext.theGameLevel());
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            gameContext.theGameLevel().bonus().ifPresent(Bonus::setInactive);
        }
    },


    GAME_OVER {
        @Override
        public void onEnter(GameContext gameContext) {
            timer.restartTicks(gameContext.theGameLevel().gameOverStateTicks());
            gameContext.theGame().onGameEnding();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.hasExpired()) {
                //TODO find unified solution
                if (gameContext.theGameController().isSelected("MS_PACMAN_TENGEN")) {
                    if (gameContext.theGameLevel().isDemoLevel()) {
                        gameContext.theGameController().changeGameState(SHOWING_CREDITS);
                    } else {
                        boolean canContinue = gameContext.theGame().continueOnGameOver();
                        gameContext.theGameController().changeGameState(canContinue ? SETTING_OPTIONS : INTRO);
                    }
                } else {
                    gameContext.theGame().prepareForNewGame();
                    if (gameContext.theGame().canStartNewGame()) {
                        gameContext.theGameController().changeGameState(SETTING_OPTIONS);
                    } else {
                        gameContext.theGameController().changeGameState(INTRO);
                    }
                }
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            gameContext.optGameLevel().ifPresent(GameLevel::clearMessage);
        }
    },


    INTERMISSION {
        @Override
        public void onEnter(GameContext gameContext) {
            timer.restartIndefinitely();
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.hasExpired()) {
                gameContext.theGameController().changeGameState(gameContext.theGame().isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    // Tests

    TESTING_LEVELS_SHORT {

        private int lastTestedLevelNumber;

        @Override
        public void onEnter(GameContext gameContext) {
            gameContext.theCoinMechanism().setNumCoins(1);
            lastTestedLevelNumber = gameContext.theGame().lastLevelNumber() == Integer.MAX_VALUE ? 25 : gameContext.theGame().lastLevelNumber();
            timer.restartIndefinitely();
            gameContext.theGame().prepareForNewGame();
            gameContext.theGame().buildNormalLevel(1);
            gameContext.theGame().startLevel();
            gameContext.theGameLevel().showPacAndGhosts();
            gameContext.theGameLevel().showMessage(GameLevel.MESSAGE_TEST);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer().tickCount() > 2 * Globals.NUM_TICKS_PER_SEC) {
                gameContext.theGameLevel().blinking().tick();
                gameContext.theGameLevel().ghosts().forEach(ghost -> ghost.update(gameContext.theGameLevel()));
                gameContext.theGameLevel().bonus().ifPresent(bonus -> bonus.update(gameContext.theGame()));
            }
            if (timer().atSecond(1.0)) {
                gameContext.theGame().initAnimationOfPacManAndGhosts();
                gameContext.theGameLevel().getReadyToPlay();
                gameContext.theGameLevel().showPacAndGhosts();
            }
            else if (timer().atSecond(2)) {
                gameContext.theGameLevel().blinking().setStartPhase(Pulse.ON);
                gameContext.theGameLevel().blinking().restart();
            }
            else if (timer().atSecond(2.5)) {
                gameContext.theGameLevel().clearMessage();
                gameContext.theGame().activateNextBonus();
            }
            else if (timer().atSecond(4.5)) {
                gameContext.theGameLevel().bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(6.5)) {
                gameContext.theGameLevel().bonus().ifPresent(Bonus::setInactive); // needed?
                gameContext.theGame().activateNextBonus();
            }
            else if (timer().atSecond(8.5)) {
                gameContext.theGameLevel().bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(10.0)) {
                gameContext.theGameLevel().hidePacAndGhosts();
                gameContext.theGame().onLevelCompleted(gameContext.theGameLevel());
            }
            else if (timer().atSecond(11.0)) {
                if (gameContext.theGameLevel().number() == lastTestedLevelNumber) {
                    gameContext.theCoinMechanism().setNumCoins(0);
                    gameContext.theGame().resetEverything();
                    gameContext.theGameController().restart(GameState.BOOT);
                } else {
                    timer().restartIndefinitely();
                    gameContext.theGame().startNextLevel();
                    gameContext.theGameLevel().showMessage(GameLevel.MESSAGE_TEST);
                }
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            gameContext.theCoinMechanism().setNumCoins(0);
            gameContext.theGame().resetEverything();
            gameContext.theGame().hud().levelCounter().clear();
        }
    },

    /**
     * Runs levels for some fixed time e.g. 10 seconds.
     */
    TESTING_LEVELS_MEDIUM {

        static final int TEST_DURATION_SEC = 10;

        private int lastTestedLevelNumber;

        private void configureLevelForTest(GameContext gameContext) {
            gameContext.theGameLevel().pac().usingAutopilotProperty().unbind();
            gameContext.theGameLevel().pac().setUsingAutopilot(true);
            gameContext.theGameLevel().pac().playAnimation();
            gameContext.theGameLevel().ghosts().forEach(Ghost::playAnimation);
            gameContext.theGameLevel().showPacAndGhosts();
            gameContext.theGameLevel().showMessage(GameLevel.MESSAGE_TEST);
            gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onEnter(GameContext gameContext) {
            lastTestedLevelNumber = gameContext.theGame().lastLevelNumber() == Integer.MAX_VALUE ? 25 : gameContext.theGame().lastLevelNumber();
            timer.restartSeconds(TEST_DURATION_SEC);
            gameContext.theGame().prepareForNewGame();
            gameContext.theGame().buildNormalLevel(1);
            gameContext.theGame().startLevel();
            configureLevelForTest(gameContext);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            gameContext.theGame().doHuntingStep();
            if (timer().hasExpired()) {
                if (gameContext.theGameLevel().number() == lastTestedLevelNumber) {
                    gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.STOP_ALL_SOUNDS);
                    gameContext.theGameController().changeGameState(INTRO);
                } else {
                    timer().restartSeconds(TEST_DURATION_SEC);
                    gameContext.theGame().startNextLevel();
                    configureLevelForTest(gameContext);
                }
            }
            else if (gameContext.theGame().isLevelCompleted()) {
                gameContext.theGameController().changeGameState(INTRO);
            } else if (gameContext.theGame().hasPacManBeenKilled()) {
                timer.expire();
            } else if (gameContext.theGame().haveGhostsBeenKilled()) {
                gameContext.theGameController().changeGameState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameContext gameContext) {
            gameContext.theGame().hud().levelCounter().clear();
        }
    },

    TESTING_CUT_SCENES {
        @Override
        public void onEnter(GameContext gameContext) {
            timer.restartIndefinitely();
            gameContext.theGame().setProperty("intermissionTestNumber", 1);
        }

        @Override
        public void onUpdate(GameContext gameContext) {
            if (timer.hasExpired()) {
                int number = gameContext.theGame().<Integer>getProperty("intermissionTestNumber");
                int lastCutSceneNumber = gameContext.theGameController().isSelected("MS_PACMAN_TENGEN") ? 4 : 3;
                if (number < lastCutSceneNumber) {
                    gameContext.theGame().setProperty("intermissionTestNumber", number + 1);
                    timer.restartIndefinitely();
                    //TODO find another solution and get rid of this event type
                    gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.UNSPECIFIED_CHANGE);
                } else {
                    gameContext.theGameController().changeGameState(INTRO);
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