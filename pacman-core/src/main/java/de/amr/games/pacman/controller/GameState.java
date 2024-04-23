/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Pulse;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import org.tinylog.Logger;

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
        public void onUpdate(GameModel game) {}
    },

    READY {
        static final int TICK_NEW_GAME_CREATE_LEVEL    = 1;
        static final int TICK_NEW_GAME_SHOW_GUYS       = 120;
        static final int TICK_NEW_GAME_START_PLAYING   = 240;
        static final int TICK_DEMO_LEVEL_CREATE_LEVEL  = 1;
        static final int TICK_DEMO_LEVEL_START_PLAYING = 120;
        static final int TICK_RESUME_GAME              = 120;

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
                switch ((int) timer.tick()) {
                    case TICK_NEW_GAME_CREATE_LEVEL -> {
                        game.createLevel(1, false);
                        game.startLevel();
                    }
                    case TICK_NEW_GAME_SHOW_GUYS -> {
                        game.makeGuysVisible(true);
                    }
                    case TICK_NEW_GAME_START_PLAYING -> {
                        game.setPlaying(true);
                        game.startHuntingPhase(0);
                        gameController().changeState(GameState.HUNTING);
                    }
                }
            }
            else { // start demo level
                switch ((int) timer.tick()) {
                    case TICK_DEMO_LEVEL_CREATE_LEVEL -> {
                        game.createLevel(1, true);
                        game.startLevel();
                    }
                    case TICK_DEMO_LEVEL_START_PLAYING -> {
                        game.startHuntingPhase(0);
                        gameController().changeState(GameState.HUNTING);
                    }
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
            game.blinking().restart();
            game.publishGameEvent(GameEventType.HUNTING_PHASE_STARTED);
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doHuntingStep();
            if (game.isLevelComplete()) {
                gameController().changeState(LEVEL_COMPLETE);
            } else if (game.isPacManKilled()) {
                if (game.isDemoLevel()) {
                    long runningTime = System.currentTimeMillis() - game.demoLevelStartTime();
                    Logger.info("Pac-Man killed, demo level running for {} seconds", runningTime / 1000);
                    if (runningTime >= GameModel.DEMO_LEVEL_MIN_DURATION_SEC * 1000) {
                        gameController().changeState(PACMAN_DYING);
                    }
                } else {
                    gameController().changeState(PACMAN_DYING);
                }
            } else if (game.areGhostsKilled()) {
                gameController().changeState(GHOST_DYING);
            }
        }
    },

    LEVEL_COMPLETE {
        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(4);
            game.onLevelCompleted();
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElse(null);
            if (timer.hasExpired()) {
                setProperty("mazeFlashing", false);
                if (game.isDemoLevel()) { // just in case demo level is completed: back to intro scene
                    gameController().changeState(INTRO);
                } else if (level.intermissionNumber() > 0) {
                    gameController().changeState(INTERMISSION);
                } else {
                    gameController().changeState(LEVEL_TRANSITION);
                }
            } else if (timer.atSecond(1)) {
                setProperty("mazeFlashing", true);
                game.blinking().setStartPhase(Pulse.OFF);
                game.blinking().restart(2 * level.numFlashes());
            } else {
                game.blinking().tick();
            }
        }
    },

    LEVEL_TRANSITION {
        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(1);
            game.createLevel(game.levelNumber() + 1, false);
            game.startLevel();
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
                return;
            }
            switch ((int) timer.tick()) {
                case TICK_HIDE_GHOSTS -> {
                    game.ghosts().forEach(Ghost::hide);
                    game.pac().selectAnimation(Pac.ANIM_DYING);
                    game.pac().resetAnimation();
                }
                case TICK_START_PAC_ANIMATION -> {
                    game.pac().startAnimation();
                    game.publishGameEvent(GameEventType.PAC_DYING);
                }
                case TICK_HIDE_PAC -> game.pac().hide();
                default -> {
                    game.blinking().tick();
                    game.pac().update(game);
                }
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
            game.createLevel(1, false);
            game.startLevel();
        }

        @Override
        public void onUpdate(GameModel game) {
            game.doLevelTestStep(this);
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