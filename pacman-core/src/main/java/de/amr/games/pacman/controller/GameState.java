/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.pacman_xxl.PacManXXLGame;

import java.util.HashMap;
import java.util.Map;

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
            game.levelCounter().clear();
            game.loadHighScore();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                enterState(INTRO);
            }
        }
    },

    INTRO {
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
            game.reset();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                enterState(READY);
            }
        }
    },

    STARTING {
        @Override
        public void onUpdate(GameModel game) {
            // wait for user interaction to leave state
        }
    },

    READY {
        static final short TICK_NEW_GAME_SHOW_GUYS       = 120;
        static final short TICK_NEW_GAME_START_PLAYING   = 240;
        static final short TICK_DEMO_LEVEL_START_PLAYING = 120;
        static final short TICK_RESUME_GAME              = 105;

        @Override
        public void onUpdate(GameModel game) {
            if (game.isPlaying()) { // resume running game
                if (timer.currentTick() == 1) {
                    game.letsGetReadyToRumble();
                    game.showGuys();
                } else if (timer.currentTick() == TICK_RESUME_GAME) {
                    game.startHuntingPhase(0);
                    enterState(GameState.HUNTING);
                }
            }
            else if (game.hasCredit()) { // start new game
                if (timer.currentTick() == 1) {
                    game.startNewGame();
                }
                else if (timer.currentTick() == 2) {
                    game.startLevel();
                }
                else if (timer.currentTick() == TICK_NEW_GAME_SHOW_GUYS) {
                    game.showGuys();
                }
                else if (timer.currentTick() == TICK_NEW_GAME_START_PLAYING) {
                    game.setPlaying(true);
                    game.startHuntingPhase(0);
                    enterState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.currentTick() == 1) {
                    game.createDemoLevel();
                    game.startLevel();
                    game.showGuys();
                }
                else if (timer.currentTick() == TICK_DEMO_LEVEL_START_PLAYING) {
                    game.startHuntingPhase(0);
                    enterState(GameState.HUNTING);
                }
            }
        }
    },

    HUNTING {
        @Override
        public void onEnter(GameModel game) {
            game.pac().startAnimation();
            game.ghosts().forEach(Ghost::startAnimation);
            game.blinking().setStartPhase(Pulse.ON);
            game.blinking().restart(Integer.MAX_VALUE);
            game.publishGameEvent(GameEventType.HUNTING_PHASE_STARTED);
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doHuntingStep();
            if (game.isLevelComplete()) {
                enterState(LEVEL_COMPLETE);
            } else if (game.isPacManKilled()) {
                enterState(PACMAN_DYING);
            } else if (game.areGhostsKilled()) {
                enterState(GHOST_DYING);
            }
        }
    },

    LEVEL_COMPLETE {
        private int flashCount = 0;

        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(5);
            game.onLevelCompleted();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                setProperty("mazeFlashing", false);
                if (game.isDemoLevel()) { // just in case demo level is completed: back to intro scene
                    enterState(INTRO);
                } else if (game.intermissionNumber(game.levelNumber()) != 0) {
                    enterState(INTERMISSION);
                } else {
                    enterState(LEVEL_TRANSITION);
                }
            } else if (timer.atSecond(1.5)) {
                game.ghosts().forEach(Ghost::hide);
            } else if (timer.atSecond(2)) {
                flashCount = 2 * game.currentLevelData().orElseThrow().numFlashes();
                setProperty("mazeFlashing", true);
                game.blinking().setStartPhase(Pulse.OFF);
                game.blinking().restart(flashCount);
            } else {
                game.blinking().tick();
                if (game.blinking().getNumPhasesCompleted() == flashCount) {
                    setProperty("mazeFlashing", false); // maze will be rendered normally again
                }
            }
        }
    },

    LEVEL_TRANSITION {
        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(1);
            game.createLevel(game.levelNumber() + 1);
            game.startLevel();
            game.showGuys();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                enterState(READY);
            }
        }
    },

    GHOST_DYING {
        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(1);
            game.pac().hide();
            game.ghosts().forEach(Ghost::stopAnimation);
            game.publishGameEvent(GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                GameController.it().resumePreviousState();
            } else {
                game.ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).forEach(ghost -> ghost.update(game));
                game.blinking().tick();
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.pac().show();
            game.ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            game.ghosts().forEach(Ghost::startAnimation);
        }
    },

    PACMAN_DYING {

        //TODO this is crap, should depend on sprite animation time
        static final int TICK_HIDE_GHOSTS = 60;
        static final int TICK_START_PAC_ANIMATION = 90;
        static final int TICK_HIDE_PAC = 210;

        @Override
        public void onEnter(GameModel game) {
            timer.reset(240);
            timer.start();
            game.onPacDying();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                game.loseLife();
                if (game.isDemoLevel()) {
                    enterState(INTRO);
                } else {
                    enterState(game.lives() == 0 ? GAME_OVER : READY);
                }
            }
            else if (timer.currentTick() == TICK_HIDE_GHOSTS) {
                game.ghosts().forEach(Ghost::hide);
                game.pac().selectAnimation(GameModel.ANIM_PAC_DYING);
                game.pac().resetAnimation();
            }
            else if (timer.currentTick() == TICK_START_PAC_ANIMATION) {
                game.pac().startAnimation();
                game.publishGameEvent(GameEventType.PAC_DYING, game.pac().tile());
            }
            else if (timer.currentTick() == TICK_HIDE_PAC) {
                game.pac().hide();
            }
            else {
                game.blinking().tick();
                game.pac().update(game);
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.bonus().ifPresent(Bonus::setInactive);
        }
    },

    GAME_OVER {

        static final int TICKS_STATE_DURATION = 120; // not sure

        @Override
        public void onEnter(GameModel game) {
            timer.reset(TICKS_STATE_DURATION);
            timer.start();
            game.updateHighScore();
            game.consumeCoin();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                game.removeWorld();
                enterState(game.hasCredit() ? STARTING : INTRO);
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.setPlaying(false);
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
                enterState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    // Test states

    TESTING_LEVEL_BONI {

        private int lastLevelNumber;

        @Override
        public void onEnter(GameModel game) {
            int numCustomMaps = 0;
            if (game.variant() == GameVariant.PACMAN_XXL) {
                PacManXXLGame xxlGame = (PacManXXLGame) game;
                numCustomMaps = xxlGame.customMapsSortedByFile().size();
            }
            lastLevelNumber = switch (game.variant()) {
                case MS_PACMAN -> 17;
                case MS_PACMAN_TENGEN -> 32;
                case PACMAN -> 21;
                case PACMAN_XXL -> 8 + numCustomMaps;
            };
            timer.restartIndefinitely();
            game.reset();
            game.createLevel(1);
            game.startLevel();
            game.showGuys();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (game.currentLevelData().isEmpty()) {
                return;
            }
            if (game.levelNumber() > lastLevelNumber) {
                GameController.it().restart(GameState.BOOT);
                return;
            }
            if (timer().currentTick() > 2 * GameModel.FPS) {
                game.blinking().tick();
                game.ghosts().forEach(ghost -> ghost.update(game));
                game.bonus().ifPresent(bonus -> bonus.update(game));
            }
            if (timer().atSecond(1.0)) {
                game.letsGetReadyToRumble();
                game.showGuys();
            } else if (timer().atSecond(2)) {
                game.blinking().setStartPhase(Pulse.ON);
                game.blinking().restart();
            } else if (timer().atSecond(2.5)) {
                game.activateNextBonus();
            } else if (timer().atSecond(4.5)) {
                game.bonus().ifPresent(bonus -> bonus.setEaten(60));
                game.publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer().atSecond(6.5)) {
                game.bonus().ifPresent(Bonus::setInactive); // needed?
                game.activateNextBonus();
            } else if (timer().atSecond(7.5)) {
                game.bonus().ifPresent(bonus -> bonus.setEaten(60));
                game.publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer().atSecond(8.5)) {
                game.hideGuys();
                game.blinking().stop();
                game.blinking().setStartPhase(Pulse.ON);
                game.blinking().reset();
            } else if (timer().atSecond(9.5)) {
                setProperty("mazeFlashing", true);
                game.blinking().setStartPhase(Pulse.OFF);
                game.blinking().restart(2 * game.currentLevelData().get().numFlashes());
            } else if (timer().atSecond(12.0)) {
                timer().restartIndefinitely();
                game.pac().freeze();
                game.bonus().ifPresent(Bonus::setInactive);
                setProperty("mazeFlashing", false);
                game.blinking().reset();
                game.createLevel(game.levelNumber() + 1);
                game.startLevel();
                game.showGuys();
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.levelCounter().clear();
        }
    },

    /**
     * Play levels for some defined time e.g. 20 seconds.
     */
    TESTING_LEVEL_TEASERS {

        static final int TEASER_TIME_SECONDS = 10;

        private int lastLevelNumber;

        @Override
        public void onEnter(GameModel game) {
            int numCustomMaps = 0;
            if (game.variant() == GameVariant.PACMAN_XXL) {
                PacManXXLGame xxlGame = (PacManXXLGame) game;
                numCustomMaps = xxlGame.customMapsSortedByFile().size();
            }
            lastLevelNumber = switch (game.variant()) {
                case MS_PACMAN -> 17;
                case MS_PACMAN_TENGEN -> 32;
                case PACMAN -> 21;
                case PACMAN_XXL -> 8 + numCustomMaps;
            };
            timer.restartSeconds(TEASER_TIME_SECONDS);
            game.reset();
            game.createLevel(1);
            game.startLevel();
            game.showGuys();
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doHuntingStep();
            if (timer().hasExpired()) {
                if (game.levelNumber() == lastLevelNumber) {
                    game.publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
                    enterState(INTRO);
                } else {
                    timer().restartSeconds(TEASER_TIME_SECONDS);
                    game.pac().freeze();
                    game.bonus().ifPresent(Bonus::setInactive);
                    setProperty("mazeFlashing", false);
                    game.blinking().reset();
                    game.createLevel(game.levelNumber() + 1);
                    game.startLevel();
                    game.showGuys();
                }
            }
            else if (game.isLevelComplete()) {
                //enterState(LEVEL_COMPLETE);
                enterState(INTRO);
            } else if (game.isPacManKilled()) {
                timer.expire();
            } else if (game.areGhostsKilled()) {
                enterState(GHOST_DYING);
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
                if (number < 3) {
                    setProperty("intermissionTestNumber", number + 1);
                    timer.restartIndefinitely();
                    game.publishGameEvent(GameEventType.UNSPECIFIED_CHANGE);
                } else {
                    enterState(INTRO);
                }
            }
        }
    };

    void enterState(GameState state) {
        GameController.it().changeState(state);
    }

    final TickTimer timer = new TickTimer("GameState-Timer-" + name());
    private Map<String, Object> propertyMap;

    private Map<String, Object> propertyMap() {
        if (propertyMap == null) {
            propertyMap = new HashMap<>(4);
        }
        return propertyMap;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) propertyMap().get(key);
    }

    public void setProperty(String key, Object value) {
        propertyMap().put(key, value);
    }

    @Override
    public TickTimer timer() {
        return timer;
    }
}