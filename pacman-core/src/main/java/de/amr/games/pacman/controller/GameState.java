/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.model.pacman_xxl.PacManXXLGame;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.model.GameModel.TICKS_PER_SECOND;

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
            game.reset();
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
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                enterState(STARTING_GAME);
            }
        }
    },

    WAITING_FOR_START {
        @Override
        public void onUpdate(GameModel game) {
            // wait for user interaction to leave state
        }
    },

    SHOWING_CREDITS {
        @Override
        public void onUpdate(GameModel context) {

        }
    },

    STARTING_GAME {
        static final short TICK_NEW_GAME_SHOW_GUYS       = 120;
        static final short TICK_NEW_GAME_START_PLAYING   = 240;
        static final short TICK_DEMO_LEVEL_START_PLAYING = 120;
        static final short TICK_RESUME_GAME              =  90;

        @Override
        public void onEnter(GameModel game) {
            game.publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (game.isPlaying()) { // resume running game
                if (timer.currentTick() == 1) {
                    game.letsGetReadyToRumble();
                    game.showGuys();
                } else if (timer.currentTick() == TICK_RESUME_GAME) {
                    enterState(GameState.HUNTING);
                }
            }
            else if (game.canStartNewGame()) { // start new game
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
                    enterState(GameState.HUNTING);
                }
            }
        }
    },

    HUNTING {
        @Override
        public void onEnter(GameModel game) {
            game.startHunting();
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
            timer.restartSeconds(5); // TODO let game variant decide
            game.onLevelCompleted();
            game.publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.hasExpired()) {
                setProperty("mazeFlashing", false);
                if (game.isDemoLevel()) { // just in case: if demo level is completed, go back to intro scene
                    enterState(INTRO);
                } else if (game.intermissionNumberAfterLevel() != 0) {
                    enterState(INTERMISSION);
                } else {
                    enterState(LEVEL_TRANSITION);
                }
            } else if (timer.atSecond(1.5)) {
                level.ghosts().forEach(Ghost::hide);
            } else if (timer.atSecond(2)) {
                flashCount = 2 * game.numFlashes();
                setProperty("mazeFlashing", true);
                level.blinking().setStartPhase(Pulse.OFF);
                level.blinking().restart(flashCount);
            } else {
                level.blinking().tick();
                if (level.blinking().getNumPhasesCompleted() == flashCount) {
                    setProperty("mazeFlashing", false); // maze will be rendered normally again
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
                enterState(STARTING_GAME);
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
            game.publishGameEvent(GameEventType.GHOST_EATEN);
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.hasExpired()) {
                GameController.it().resumePreviousState();
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

        //TODO this is crap, should depend on sprite animation time
        static final int TICK_HIDE_GHOSTS = 60;
        static final int TICK_START_PAC_ANIMATION = 90;
        static final int TICK_HIDE_PAC = 210;

        @Override
        public void onEnter(GameModel game) {
            timer.reset(240);
            timer.start();
            game.onPacKilled();
            game.publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.hasExpired()) {
                if (game.isDemoLevel()) {
                    enterState(GAME_OVER);
                } else {
                    game.loseLife();
                    enterState(game.isOver() ? GAME_OVER : STARTING_GAME);
                }
            }
            else if (timer.currentTick() == TICK_HIDE_GHOSTS) {
                level.ghosts().forEach(Ghost::hide);
                level.pac().selectAnimation(GameModel.ANIM_PAC_DYING);
                level.pac().resetAnimation();
            }
            else if (timer.currentTick() == TICK_START_PAC_ANIMATION) {
                level.pac().startAnimation();
                game.publishGameEvent(GameEventType.PAC_DYING, level.pac().tile());
            }
            else if (timer.currentTick() == TICK_HIDE_PAC) {
                level.pac().hide();
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
            timer.restartTicks(game.gameOverStateTicks());
            game.endGame();
            game.publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                game.reset();
                if (GameController.it().currentGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                    if (game.isDemoLevel()) {
                        enterState(SHOWING_CREDITS);
                    } else {
                        TengenMsPacManGame tengenGame = (TengenMsPacManGame) game;
                        if (tengenGame.startLevelNumber() >= 10 && tengenGame.numContinues() > 0) {
                            tengenGame.setNumContinues(tengenGame.numContinues() - 1);
                            enterState(WAITING_FOR_START);
                        } else {
                            enterState(INTRO);
                        }
                    }
                } else {
                    if (game.canStartNewGame()) {
                        enterState(WAITING_FOR_START);
                    } else {
                        enterState(INTRO);
                    }
                }
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
            GameVariant gameVariant = GameController.it().currentGameVariant();
            int numCustomMaps = 0;
            if (gameVariant == GameVariant.PACMAN_XXL) {
                PacManXXLGame xxlGame = (PacManXXLGame) game;
                numCustomMaps = xxlGame.customMapsSortedByFile().size();
            }
            lastLevelNumber = switch (gameVariant) {
                case MS_PACMAN -> 25;
                case MS_PACMAN_TENGEN -> TengenMsPacManGame.MAX_LEVEL_NUMBER;
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
            GameLevel level = game.level().orElseThrow();
            if (timer().currentTick() > 2 * TICKS_PER_SECOND) {
                level.blinking().tick();
                level.ghosts().forEach(ghost -> ghost.update(game));
                level.bonus().ifPresent(bonus -> bonus.update(game));
            }
            if (timer().atSecond(1.0)) {
                game.letsGetReadyToRumble();
                game.showGuys();
            } else if (timer().atSecond(2)) {
                level.blinking().setStartPhase(Pulse.ON);
                level.blinking().restart();
            } else if (timer().atSecond(2.5)) {
                game.activateNextBonus();
            } else if (timer().atSecond(4.5)) {
                level.bonus().ifPresent(bonus -> bonus.setEaten(TICKS_PER_SECOND));
                game.publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer().atSecond(6.5)) {
                level.bonus().ifPresent(Bonus::setInactive); // needed?
                game.activateNextBonus();
            } else if (timer().atSecond(7.5)) {
                level.bonus().ifPresent(bonus -> bonus.setEaten(TICKS_PER_SECOND));
                game.publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer().atSecond(8.5)) {
                game.hideGuys();
                level.blinking().stop();
                level.blinking().setStartPhase(Pulse.ON);
                level.blinking().reset();
            } else if (timer().atSecond(9.5)) {
                setProperty("mazeFlashing", true);
                level.blinking().setStartPhase(Pulse.OFF);
                level.blinking().restart(2 * game.numFlashes());
            } else if (timer().atSecond(12.0)) {
                level.pac().freeze();
                level.bonus().ifPresent(Bonus::setInactive);
                setProperty("mazeFlashing", false);
                level.blinking().reset();
                if (level.number == lastLevelNumber) {
                    GameController.it().restart(GameState.BOOT);
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

        private int lastLevelNumber;

        @Override
        public void onEnter(GameModel game) {
            GameVariant gameVariant = GameController.it().currentGameVariant();
            lastLevelNumber = switch (gameVariant) {
                case MS_PACMAN -> 17;
                case MS_PACMAN_TENGEN -> 32;
                case PACMAN -> 21;
                case PACMAN_XXL -> {
                    PacManXXLGame xxlGame = (PacManXXLGame) game;
                    yield 8 + xxlGame.customMapsSortedByFile().size();
                }
            };
            timer.restartSeconds(TEASER_TIME_SECONDS);
            game.reset();
            game.createLevel(1);
            game.startLevel();
            game.showGuys();
            GameLevel level = game.level().orElseThrow();
            level.pac().startAnimation();
            level.ghosts().forEach(Ghost::startAnimation);
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doHuntingStep();
            GameLevel level = game.level().orElseThrow();
            if (timer().hasExpired()) {
                if (level.number == lastLevelNumber) {
                    game.publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
                    enterState(INTRO);
                } else {
                    level.pac().freeze();
                    level.bonus().ifPresent(Bonus::setInactive);
                    setProperty("mazeFlashing", false);
                    level.blinking().reset();
                    game.startNextLevel();
                    timer().restartSeconds(TEASER_TIME_SECONDS);
                }
            }
            else if (game.isLevelComplete()) {
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
            GameVariant gameVariant = GameController.it().currentGameVariant();
            if (timer.hasExpired()) {
                int number = this.<Integer>getProperty("intermissionTestNumber");
                int last = gameVariant == GameVariant.MS_PACMAN_TENGEN ? 4 : 3;
                if (number < last) {
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