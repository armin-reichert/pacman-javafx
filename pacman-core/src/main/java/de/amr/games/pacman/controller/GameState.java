/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.THE_GAME_EVENT_MANAGER;

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
                THE_GAME_CONTROLLER.changeState(INTRO);
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
                THE_GAME_CONTROLLER.changeState(STARTING_GAME);
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
                THE_GAME_CONTROLLER.changeState(INTRO);
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
            THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (game.isPlaying()) {
                GameLevel level = game.level().orElseThrow();
                // resume running game
                if (timer.tickCount() == 1) {
                    game.initActorAnimationState();
                    level.makeReadyForPlaying();
                    level.showPacAndGhosts();
                    THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.GAME_CONTINUED);
                } else if (timer.tickCount() == TICK_RESUME_GAME) {
                    THE_GAME_CONTROLLER.changeState(GameState.HUNTING);
                }
            }
            else if (game.canStartNewGame()) {
                // start new game
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
                    THE_GAME_CONTROLLER.changeState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.tickCount() == 1) {
                    game.buildDemoLevel();
                    THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.LEVEL_CREATED);
                }
                else if (timer.tickCount() == 2) {
                    // This publishes a LEVEL_STARTED event which triggers the actor animation UI creation
                    game.startLevel();
                }
                else if (timer.tickCount() == 3) {
                    // Now, actor animations are available
                    GameLevel level = game.level().orElseThrow();
                    level.showPacAndGhosts();
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START) {
                    THE_GAME_CONTROLLER.changeState(GameState.HUNTING);
                }
            }
        }
    },


    HUNTING {
        int delay;

        @Override
        public void onEnter(GameModel game) {
            delay = THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN) ? 60 : 0;
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.tickCount() < delay) {
                return;
            }
            if (timer.tickCount() == delay) {
                game.startHunting();
                if (level.message() != null && level.message() == GameLevel.Message.READY) {
                    level.clearMessage();
                }
            }
            game.doHuntingStep();
            if (game.isLevelFinished()) {
                THE_GAME_CONTROLLER.changeState(LEVEL_COMPLETE);
            } else if (game.hasPacManBeenKilled()) {
                THE_GAME_CONTROLLER.changeState(PACMAN_DYING);
            } else if (game.haveGhostsBeenKilled()) {
                THE_GAME_CONTROLLER.changeState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (level.message() != null && level.message() == GameLevel.Message.READY) {
                level.clearMessage();
            }
        }
    },


    LEVEL_COMPLETE {
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely(); // UI triggers timeout e.g. when animation finishes
            game.endLevel();
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN) && level.isDemoLevel()) {
                THE_GAME_CONTROLLER.changeState(SHOWING_CREDITS);
                return;
            }
            if (timer.hasExpired()) {
                if (level.isDemoLevel()) { // just in case: if demo level is completed, go back to intro scene
                    THE_GAME_CONTROLLER.changeState(INTRO);
                } else if (game.cutScenesEnabledProperty().get() && level.cutSceneNumber() != 0) {
                    THE_GAME_CONTROLLER.changeState(INTERMISSION);
                } else {
                    THE_GAME_CONTROLLER.changeState(LEVEL_TRANSITION);
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
                THE_GAME_CONTROLLER.changeState(STARTING_GAME);
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
            THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.hasExpired()) {
                THE_GAME_CONTROLLER.resumePreviousState();
            } else {
                level.ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).forEach(ghost -> ghost.update(game));
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
            THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.hasExpired()) {
                if (level.isDemoLevel()) {
                    THE_GAME_CONTROLLER.changeState(GAME_OVER);
                } else {
                    game.loseLife();
                    THE_GAME_CONTROLLER.changeState(game.isOver() ? GAME_OVER : STARTING_GAME);
                }
            }
            else if (timer.tickCount() == TICK_HIDE_GHOSTS) {
                level.ghosts().forEach(Ghost::hide);
                //TODO this does not belong here
                level.pac().selectAnimation(ActorAnimations.ANIM_PAC_DYING);
                level.pac().resetAnimation();
            }
            else if (timer.tickCount() == TICK_START_PAC_ANIMATION) {
                level.pac().startAnimation();
                THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.PAC_DYING, level.pac().tile());
            }
            else if (timer.tickCount() == TICK_HIDE_PAC) {
                level.pac().hide();
            }
            else if (timer.tickCount() == TICK_PAC_DEAD) {
                THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.PAC_DEAD);
            }
            else {
                level.blinking().tick();
                level.pac().update(game);
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
            game.endGame();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                GameLevel level = game.level().orElseThrow();
                //TODO find unified solution
                if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN)) {
                    if (level.isDemoLevel()) {
                        THE_GAME_CONTROLLER.changeState(SHOWING_CREDITS);
                    } else {
                        boolean canContinue = game.continueOnGameOver();
                        THE_GAME_CONTROLLER.changeState(canContinue ? SETTING_OPTIONS : INTRO);
                    }
                } else {
                    game.resetForStartingNewGame();
                    if (game.canStartNewGame()) {
                        THE_GAME_CONTROLLER.changeState(SETTING_OPTIONS);
                    } else {
                        THE_GAME_CONTROLLER.changeState(INTRO);
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
                THE_GAME_CONTROLLER.changeState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    // Tests

    TESTING_LEVELS {

        private int lastTestedLevelNumber;

        @Override
        public void onEnter(GameModel game) {
            lastTestedLevelNumber = 25;
            if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN)) {
                lastTestedLevelNumber = 32;
            }
            timer.restartIndefinitely();
            game.resetForStartingNewGame();
            game.createLevel(1, game.createLevelData(1));
            game.startLevel();
            GameLevel level = game.level().orElseThrow();
            level.showPacAndGhosts();
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer().tickCount() > 2 * Globals.TICKS_PER_SECOND) {
                level.blinking().tick();
                level.ghosts().forEach(ghost -> ghost.update(game));
                level.bonus().ifPresent(bonus -> bonus.update(game));
            }
            if (timer().atSecond(1.0)) {
                game.initActorAnimationState();
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
                level.bonus().ifPresent(bonus -> bonus.setEaten(Globals.TICKS_PER_SECOND));
                THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(6.5)) {
                level.bonus().ifPresent(Bonus::setInactive); // needed?
                game.activateNextBonus();
            }
            else if (timer().atSecond(7.5)) {
                level.bonus().ifPresent(bonus -> bonus.setEaten(Globals.TICKS_PER_SECOND));
                THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.BONUS_EATEN);
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
                level.pac().freeze();
                level.bonus().ifPresent(Bonus::setInactive);
                if (level.number() == lastTestedLevelNumber) {
                    THE_GAME_CONTROLLER.restart(GameState.BOOT);
                } else {
                    timer().restartIndefinitely();
                    game.startNextLevel();
                }
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.levelCounter().reset();
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
            if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN)) {
                lastTestedLevelNumber = 32;
            }
            timer.restartSeconds(TEASER_TIME_SECONDS);
            game.resetForStartingNewGame();
            game.createLevel(1, game.createLevelData(1));
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
                    THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.STOP_ALL_SOUNDS);
                    THE_GAME_CONTROLLER.changeState(INTRO);
                } else {
                    level.pac().freeze();
                    level.bonus().ifPresent(Bonus::setInactive);
                    setProperty("mazeFlashing", false);
                    level.blinking().reset();
                    game.startNextLevel();
                    timer().restartSeconds(TEASER_TIME_SECONDS);
                }
            }
            else if (game.isLevelFinished()) {
                THE_GAME_CONTROLLER.changeState(INTRO);
            } else if (game.hasPacManBeenKilled()) {
                timer.expire();
            } else if (game.haveGhostsBeenKilled()) {
                THE_GAME_CONTROLLER.changeState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.levelCounter().reset();
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
                int lastCutSceneNumber = THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN) ? 4 : 3;
                if (number < lastCutSceneNumber) {
                    setProperty("intermissionTestNumber", number + 1);
                    timer.restartIndefinitely();
                    //TODO find another solution and get rid of this event type
                    THE_GAME_EVENT_MANAGER.publishEvent(game, GameEventType.UNSPECIFIED_CHANGE);
                } else {
                    THE_GAME_CONTROLLER.changeState(INTRO);
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