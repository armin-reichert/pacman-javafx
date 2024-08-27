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
import de.amr.games.pacman.model.actors.*;

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
                GameController.it().changeState(INTRO);
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
                GameController.it().changeState(READY);
            }
        }
    },

    CREDIT {
        @Override
        public void onUpdate(GameModel game) {
            // wait for user interaction
        }
    },

    READY {
        static final short TICK_CREATE_LEVEL             = 1;
        static final short TICK_START_LEVEL              = 2;
        static final short TICK_NEW_GAME_SHOW_GUYS       = 120;
        static final short TICK_NEW_GAME_START_PLAYING   = 240;
        static final short TICK_DEMO_LEVEL_START_PLAYING = 120;
        static final short TICK_RESUME_GAME              = 120;

        @Override
        public void onUpdate(GameModel game) {
            if (game.isPlaying()) { // resume running game
                if (timer.currentTick() == 1) {
                    game.letsGetReadyToRumble();
                    game.showGuys();
                } else if (timer.currentTick() == TICK_RESUME_GAME) {
                    game.startHuntingPhase(0);
                    GameController.it().changeState(GameState.HUNTING);
                }
            }
            else if (GameController.it().hasCredit()) { // start new game
                if (timer.currentTick() == TICK_CREATE_LEVEL) {
                    game.reset();
                    game.createLevel(1);
                }
                else if (timer.currentTick() == TICK_START_LEVEL) {
                    game.startLevel();
                }
                else if (timer.currentTick() == TICK_NEW_GAME_SHOW_GUYS) {
                    game.showGuys();
                }
                else if (timer.currentTick() == TICK_NEW_GAME_START_PLAYING) {
                    game.setPlaying(true);
                    game.startHuntingPhase(0);
                    GameController.it().changeState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.currentTick() == TICK_CREATE_LEVEL) {
                    game.createDemoLevel();
                    game.startLevel();
                    game.showGuys();
                }
                else if (timer.currentTick() == TICK_DEMO_LEVEL_START_PLAYING) {
                    game.startHuntingPhase(0);
                    GameController.it().changeState(GameState.HUNTING);
                }
            }
        }
    },

    HUNTING {
        @Override
        public void onEnter(GameModel game) {
            game.pac().animations().ifPresent(Animations::startSelected);
            game.ghosts().forEach(Ghost::startAnimation);
            game.blinking().setStartPhase(Pulse.ON);
            game.blinking().restart(Integer.MAX_VALUE);
            game.publishGameEvent(GameEventType.HUNTING_PHASE_STARTED);
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doHuntingStep();
            if (game.isLevelComplete()) {
                GameController.it().changeState(LEVEL_COMPLETE);
            } else if (game.isPacManKilled()) {
                GameController.it().changeState(PACMAN_DYING);
            } else if (game.areGhostsKilled()) {
                GameController.it().changeState(GHOST_DYING);
            }
        }
    },

    LEVEL_COMPLETE {
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
                    GameController.it().changeState(INTRO);
                } else if (game.intermissionNumber(game.levelNumber()) != 0) {
                    GameController.it().changeState(INTERMISSION);
                } else {
                    GameController.it().changeState(LEVEL_TRANSITION);
                }
            } else if (timer.atSecond(1)) {
                game.ghosts().forEach(Ghost::hide);
            } else if (timer.atSecond(2)) {
                setProperty("mazeFlashing", true);
                game.blinking().setStartPhase(Pulse.OFF);
                game.blinking().restart(2 * game.level().orElseThrow().numFlashes());
            } else {
                game.blinking().tick();
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
                GameController.it().changeState(READY);
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
                game.ghosts(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
                    .forEach(ghost -> ghost.update(game));
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

        static final int TICK_HIDE_GHOSTS = 60;
        static final int TICK_START_PAC_ANIMATION = 90;
        static final int TICK_HIDE_PAC = 180;

        @Override
        public void onEnter(GameModel game) {
            timer.reset(220);
            timer.start();
            game.onPacDying();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                game.loseLife();
                if (game.isDemoLevel()) {
                    GameController.it().changeState(INTRO);
                } else {
                    GameController.it().changeState(game.lives() == 0 ? GAME_OVER : READY);
                }
            }
            else if (timer.currentTick() == TICK_HIDE_GHOSTS) {
                game.ghosts().forEach(Ghost::hide);
                game.pac().selectAnimation(Pac.ANIM_DYING);
                game.pac().animations().ifPresent(Animations::resetSelected);
            }
            else if (timer.currentTick() == TICK_START_PAC_ANIMATION) {
                game.pac().animations().ifPresent(Animations::startSelected);
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

        static final int TICKS_STATE_DURATION = 90; // not sure

        @Override
        public void onEnter(GameModel game) {
            timer.reset(TICKS_STATE_DURATION);
            timer.start();
            game.updateHighScore();
            GameController.it().consumeCoin();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                game.removeWorld();
                GameController.it().changeState(GameController.it().hasCredit() ? CREDIT : INTRO);
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
                GameController.it().changeState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    LEVEL_TEST {

        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
            GameController.it().clock().setTargetFrameRate(2 * GameModel.FPS);
            game.reset();
            game.createLevel(1);
            game.startLevel();
            game.showGuys();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (game.level().isEmpty()) {
                return;
            }
            if (game.levelNumber() == 25) {
                GameController.it().clock().setTargetFrameRate(GameModel.FPS);
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
                game.blinking().restart(2 * game.level().get().numFlashes());
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
            GameController.it().clock().setTargetFrameRate(GameModel.FPS);
        }
    },

    INTERMISSION_TEST {
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
                    GameController.it().changeState(INTRO);
                }
            }
        }
    };

    final TickTimer timer = new TickTimer("GameState-Timer-" + name());

    @Override
    public TickTimer timer() {
        return timer;
    }

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
}