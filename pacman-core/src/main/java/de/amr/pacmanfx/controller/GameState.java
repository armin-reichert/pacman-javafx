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
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.LevelMessage;
import de.amr.pacmanfx.model.actors.*;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.Globals.theGameEventManager;

/**
 * Game states of the Pac-Man game variants.
 * <p>
 * Rule of thumb: Specify what should happen when, not how exactly.
 * </p>
 *
 * @author Armin Reichert
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
                theGameController().changeState(INTRO);
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
                theGameController().changeState(STARTING_GAME);
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
                theGameController().changeState(INTRO);
            }
        }
    },


    STARTING_GAME {
        static final short TICK_NEW_GAME_SHOW_GUYS       = 120;
        static final short TICK_NEW_GAME_START_PLAYING   = 240;
        static final short TICK_DEMO_LEVEL_START         = 120;
        static final short TICK_RESUME_GAME              =  90;

        @Override
        public void onEnter(GameModel game) {
            theGameEventManager().publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (game.isPlaying()) {
                GameLevel level = game.level().orElseThrow();
                // resume running game
                if (timer.tickCount() == 1) {
                    game.initAnimationOfPacManAndGhosts();
                    level.makeReadyForPlaying();
                    level.showPacAndGhosts();
                    theGameEventManager().publishEvent(game, GameEventType.GAME_CONTINUED);
                } else if (timer.tickCount() == TICK_RESUME_GAME) {
                    theGameController().changeState(GameState.HUNTING);
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
                    GameLevel level = game.level().orElseThrow();
                    level.showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_PLAYING) {
                    game.playingProperty().set(true);
                    theGameController().changeState(GameState.HUNTING);
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
                    GameLevel level = game.level().orElseThrow();
                    level.showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START) {
                    theGameController().changeState(GameState.HUNTING);
                }
            }
        }
    },


    HUNTING {
        int delay;

        @Override
        public void onEnter(GameModel game) {
            //TODO reconsider this
            delay = theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN) ? 60 : 0;
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.tickCount() < delay) {
                return;
            }
            if (timer.tickCount() == delay) {
                game.startHunting();
                if (level.message() != null && level.message() == LevelMessage.READY) {
                    level.clearMessage();
                }
            }
            game.doHuntingStep();
            if (game.isLevelCompleted()) {
                theGameController().changeState(LEVEL_COMPLETE);
            } else if (game.hasPacManBeenKilled()) {
                theGameController().changeState(PACMAN_DYING);
            } else if (game.haveGhostsBeenKilled()) {
                theGameController().changeState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (level.message() != null && level.message() == LevelMessage.READY) {
                level.clearMessage();
            }
        }
    },


    LEVEL_COMPLETE {
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely(); // UI triggers timeout e.g. when animation finishes
            GameLevel level = game.level().orElseThrow();
            level.onCompleted();
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            //TODO ugly
            if (theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN) && level.isDemoLevel()) {
                theGameController().changeState(SHOWING_CREDITS);
                return;
            }
            if (timer.hasExpired()) {
                if (level.isDemoLevel()) { // just in case: if demo level is completed, go back to intro scene
                    theGameController().changeState(INTRO);
                } else if (game.cutScenesEnabledProperty().get() && level.cutSceneNumber() != 0) {
                    theGameController().changeState(INTERMISSION);
                } else {
                    theGameController().changeState(LEVEL_TRANSITION);
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
                theGameController().changeState(STARTING_GAME);
            }
        }
    },


    GHOST_DYING {
        @Override
        public void onEnter(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            timer.restartSeconds(1);
            level.pac().hide();
            level.ghosts().forEach(Ghost::stopAnimation);
            theGameEventManager().publishEvent(game, GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.hasExpired()) {
                theGameController().resumePreviousState();
            } else {
                level.ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).forEach(Ghost::update);
                level.blinking().tick();
            }
        }

        @Override
        public void onExit(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            level.pac().show();
            level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            level.ghosts().forEach(Ghost::startAnimation);
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
            GameLevel level = game.level().orElseThrow();
            if (timer.hasExpired()) {
                if (level.isDemoLevel()) {
                    theGameController().changeState(GAME_OVER);
                } else {
                    game.addLives(-1);
                    theGameController().changeState(game.isOver() ? GAME_OVER : STARTING_GAME);
                }
            }
            else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                level.ghosts().forEach(Ghost::hide);
                //TODO this does not belong here
                level.pac().selectAnimation(CommonAnimationID.ANY_PAC_DYING);
                level.pac().resetAnimation();
            }
            else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                level.pac().startAnimation();
                theGameEventManager().publishEvent(game, GameEventType.PAC_DYING, level.pac().tile());
            }
            else if (timer.tickCount() == TICK_HIDE_PAC) {
                level.pac().hide();
            }
            else if (timer.tickCount() == TICK_PAC_DEAD) {
                theGameEventManager().publishEvent(game, GameEventType.PAC_DEAD);
            }
            else {
                level.blinking().tick();
                level.pac().update();
            }
        }

        @Override
        public void onExit(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            level.bonus().ifPresent(Bonus::setInactive);
        }
    },


    GAME_OVER {
        @Override
        public void onEnter(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            timer.restartTicks(level.gameOverStateTicks());
            game.onGameEnding();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                GameLevel level = game.level().orElseThrow();
                //TODO find unified solution
                if (theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN)) {
                    if (level.isDemoLevel()) {
                        theGameController().changeState(SHOWING_CREDITS);
                    } else {
                        boolean canContinue = game.continueOnGameOver();
                        theGameController().changeState(canContinue ? SETTING_OPTIONS : INTRO);
                    }
                } else {
                    game.prepareForNewGame();
                    if (game.canStartNewGame()) {
                        theGameController().changeState(SETTING_OPTIONS);
                    } else {
                        theGameController().changeState(INTRO);
                    }
                }
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.level().ifPresent(GameLevel::clearMessage);
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
                theGameController().changeState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    // Tests

    TESTING_LEVELS {

        private int lastTestedLevelNumber;

        @Override
        public void onEnter(GameModel game) {
            lastTestedLevelNumber = 25;
            if (theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN)) {
                lastTestedLevelNumber = 32;
            }
            timer.restartIndefinitely();
            game.prepareForNewGame();
            game.buildNormalLevel(1);
            game.startLevel();
            GameLevel level = game.level().orElseThrow();
            level.showPacAndGhosts();
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer().tickCount() > 2 * Globals.NUM_TICKS_PER_SEC) {
                level.blinking().tick();
                level.ghosts().forEach(Ghost::update);
                level.bonus().ifPresent(bonus -> bonus.update(game));
            }
            if (timer().atSecond(1.0)) {
                game.initAnimationOfPacManAndGhosts();
                level.makeReadyForPlaying();
                level.showPacAndGhosts();
            }
            else if (timer().atSecond(2)) {
                level.blinking().setStartPhase(Pulse.ON);
                level.blinking().restart();
            }
            else if (timer().atSecond(2.5)) {
                level.clearMessage();
                game.activateNextBonus();
            }
            else if (timer().atSecond(4.5)) {
                level.bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                theGameEventManager().publishEvent(game, GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(6.5)) {
                level.bonus().ifPresent(Bonus::setInactive); // needed?
                game.activateNextBonus();
            }
            else if (timer().atSecond(7.5)) {
                level.bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
                theGameEventManager().publishEvent(game, GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(8.5)) {
                level.hidePacAndGhosts();
                level.blinking().stop();
                level.blinking().setStartPhase(Pulse.ON);
                level.blinking().reset();
            }
            else if (timer().atSecond(9.5)) {
                setProperty("mazeFlashing", true); //TODO fix
                level.blinking().setStartPhase(Pulse.OFF);
                level.blinking().restart(2 * level.data().numFlashes());
            }
            else if (timer().atSecond(12.0)) {
                setProperty("mazeFlashing", false); //TODO fix
                level.blinking().reset();
                level.pac().stopAndShowInFullBeauty();
                level.bonus().ifPresent(Bonus::setInactive);
                if (level.number() == lastTestedLevelNumber) {
                    theGameController().restart(GameState.BOOT);
                } else {
                    timer().restartIndefinitely();
                    game.startNextLevel();
                }
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.levelCounter().clear();
        }
    },

    /**
     * Runs levels for some fixed time e.g. 10 seconds.
     */
    TESTING_LEVEL_TEASERS {

        static final int TEASER_TIME_SECONDS = 10;

        private int lastTestedLevelNumber;

        @Override
        public void onEnter(GameModel game) {
            lastTestedLevelNumber = 25;
            if (theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN)) {
                lastTestedLevelNumber = 32;
            }
            timer.restartSeconds(TEASER_TIME_SECONDS);
            game.prepareForNewGame();
            game.buildNormalLevel(1);
            game.startLevel();
            GameLevel level = game.level().orElseThrow();
            level.showPacAndGhosts();
            level.pac().startAnimation();
            level.ghosts().forEach(Ghost::startAnimation);
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doHuntingStep();
            GameLevel level = game.level().orElseThrow();
            if (timer().hasExpired()) {
                if (level.number() == lastTestedLevelNumber) {
                    theGameEventManager().publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
                    theGameController().changeState(INTRO);
                } else {
                    level.pac().stopAndShowInFullBeauty();
                    level.bonus().ifPresent(Bonus::setInactive);
                    setProperty("mazeFlashing", false);
                    level.blinking().reset();
                    game.startNextLevel();
                    timer().restartSeconds(TEASER_TIME_SECONDS);
                }
            }
            else if (game.isLevelCompleted()) {
                theGameController().changeState(INTRO);
            } else if (game.hasPacManBeenKilled()) {
                timer.expire();
            } else if (game.haveGhostsBeenKilled()) {
                theGameController().changeState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.levelCounter().clear();
        }
    },

    TESTING_CUT_SCENES {
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
            setProperty("intermissionTestNumber", 1);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                int number = this.<Integer>getProperty("intermissionTestNumber");
                int lastCutSceneNumber = theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN) ? 4 : 3;
                if (number < lastCutSceneNumber) {
                    setProperty("intermissionTestNumber", number + 1);
                    timer.restartIndefinitely();
                    //TODO find another solution and get rid of this event type
                    theGameEventManager().publishEvent(game, GameEventType.UNSPECIFIED_CHANGE);
                } else {
                    theGameController().changeState(INTRO);
                }
            }
        }
    };

    @Override
    public TickTimer timer() {
        return timer;
    }

    final TickTimer timer = new TickTimer("GameState-Timer-" + name());

    //TODO replace property map by something leaner

    private Map<String, Object> propertyMap;

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) propertyMap().get(key);
    }

    public void setProperty(String key, Object value) {
        propertyMap().put(key, value);
    }

    private Map<String, Object> propertyMap() {
        if (propertyMap == null) {
            propertyMap = new HashMap<>(4);
        }
        return propertyMap;
    }
}