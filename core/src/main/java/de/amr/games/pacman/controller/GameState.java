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
                gameController().changeState(INTRO);
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
                gameController().changeState(READY);
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
                if (timer.tick() == 1) {
                    game.letsGetReadyToRumble();
                    game.makeGuysVisible(true);
                } else if (timer.tick() == TICK_RESUME_GAME) {
                    game.startHuntingPhase(0);
                    gameController().changeState(GameState.HUNTING);
                }
            }
            else if (gameController().hasCredit()) { // start new game
                if (timer.tick() == TICK_CREATE_LEVEL) {
                    game.reset();
                    game.createLevel(1);
                }
                else if (timer.tick() == TICK_START_LEVEL) {
                    game.startLevel();
                }
                else if (timer.tick() == TICK_NEW_GAME_SHOW_GUYS) {
                    game.makeGuysVisible(true);
                }
                else if (timer.tick() == TICK_NEW_GAME_START_PLAYING) {
                    game.setPlaying(true);
                    game.startHuntingPhase(0);
                    gameController().changeState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.tick() == TICK_CREATE_LEVEL) {
                    game.createDemoLevel();
                    game.startLevel();
                    game.makeGuysVisible(true);
                }
                else if (timer.tick() == TICK_DEMO_LEVEL_START_PLAYING) {
                    game.startHuntingPhase(0);
                    gameController().changeState(GameState.HUNTING);
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
                gameController().changeState(LEVEL_COMPLETE);
            } else if (game.isPacManKilled()) {
                gameController().changeState(PACMAN_DYING);
            } else if (game.areGhostsKilled()) {
                gameController().changeState(GHOST_DYING);
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
                    gameController().changeState(INTRO);
                } else if (game.intermissionNumberAfterLevel(game.levelNumber()) != 0) {
                    gameController().changeState(INTERMISSION);
                } else {
                    gameController().changeState(LEVEL_TRANSITION);
                }
            } else if (timer.atSecond(2)) {
                game.ghosts().forEach(Ghost::hide);
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
            game.makeGuysVisible(true);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                gameController().changeState(READY);
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
                gameController().resumePreviousState();
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
                    gameController().changeState(INTRO);
                } else {
                    gameController().changeState(game.lives() == 0 ? GAME_OVER : READY);
                }
            }
            else if (timer.tick() == TICK_HIDE_GHOSTS) {
                game.ghosts().forEach(Ghost::hide);
                game.pac().selectAnimation(Pac.ANIM_DYING);
                game.pac().animations().ifPresent(Animations::resetSelected);
            }
            else if (timer.tick() == TICK_START_PAC_ANIMATION) {
                game.pac().animations().ifPresent(Animations::startSelected);
                game.publishGameEvent(GameEventType.PAC_DYING, game.pac().tile());
            }
            else if (timer.tick() == TICK_HIDE_PAC) {
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
            gameController().changeCredit(-1);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                game.removeWorld();
                gameController().changeState(gameController().hasCredit() ? CREDIT : INTRO);
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
                gameController().changeState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    LEVEL_TEST {

        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
            gameController().clock().setTargetFrameRate(2 * GameModel.FPS);
            game.reset();
            game.createLevel(1);
            game.startLevel();
            game.makeGuysVisible(true);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (game.level().isEmpty()) {
                return;
            }
            if (game.levelNumber() == 25) {
                gameController().clock().setTargetFrameRate(GameModel.FPS);
                gameController().restart(GameState.BOOT);
                return;
            }
            if (timer().tick() > 2 * GameModel.FPS) {
                game.blinking().tick();
                game.ghosts().forEach(ghost -> ghost.update(game));
                game.bonus().ifPresent(bonus -> bonus.update(game));
            }
            if (timer().atSecond(1.0)) {
                game.letsGetReadyToRumble();
                game.pac().show();
                game.ghosts().forEach(Ghost::show);
            } else if (timer().atSecond(2)) {
                game.blinking().setStartPhase(Pulse.ON);
                game.blinking().restart();
            } else if (timer().atSecond(2.5)) {
                game.createNextBonus();
            } else if (timer().atSecond(4.5)) {
                game.bonus().ifPresent(bonus -> bonus.setEaten(60));
                game.publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer().atSecond(6.5)) {
                game.bonus().ifPresent(Bonus::setInactive); // needed?
                game.createNextBonus();
            } else if (timer().atSecond(7.5)) {
                game.bonus().ifPresent(bonus -> bonus.setEaten(60));
                game.publishGameEvent(GameEventType.BONUS_EATEN);
            } else if (timer().atSecond(8.5)) {
                game.pac().hide();
                game.ghosts().forEach(Ghost::hide);
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
                game.ghosts().forEach(Ghost::hide);
                game.bonus().ifPresent(Bonus::setInactive);
                setProperty("mazeFlashing", false);
                game.blinking().reset();
                game.createLevel(game.levelNumber() + 1);
                game.startLevel();
                game.makeGuysVisible(true);
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.levelCounter().clear();
            gameController().clock().setTargetFrameRate(GameModel.FPS);
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
                    gameController().changeState(INTRO);
                }
            }
        }
    };

    final TickTimer timer = new TickTimer("GameState-Timer-" + name());

    GameController gameController() {
        return GameController.it();
    }

    @Override
    public TickTimer timer() {
        return timer;
    }

    private final Map<String, Object> properties = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}