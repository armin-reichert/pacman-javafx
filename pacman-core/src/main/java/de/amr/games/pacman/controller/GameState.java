/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;



/**
 * Game states of the Pac-Man/Ms. Pac-Man game.
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
            game.score().reset();
            game.highScore().loadFromFile(game.highScoreFile());
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
            game.setPlaying(false);
            game.setLevel(null);
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

        static final int TICK_NEW_GAME_CREATE_LEVEL  = 1;
        static final int TICK_NEW_GAME_SHOW_GUYS     = 120;
        static final int TICK_NEW_GAME_START_PLAYING = 260;

        static final int TICK_DEMO_LEVEL_CREATE_LEVEL = 1;
        static final int TICK_DEMO_LEVEL_START_PLAYING = 120;

        static final int TICK_RESUME_GAME = 90;

        @Override
        public void onEnter(GameModel game) {
            if (game.isPlaying()) {
                // resume running game
                game.level().ifPresent(level -> level.letsGetReadyToRumble(true));
            }
            else if (gameController().hasCredit()) {
                // prepare new game
                game.reset();
                game.score().reset();
                game.levelCounter().clear();
            }
            else {
                // prepare demo level
                game.reset();
            }
        }

        @Override
        public void onUpdate(GameModel game) {
            if (game.isPlaying()) { // resume running game
                if (timer.tick() == TICK_RESUME_GAME) {
                    game.level().ifPresent(level -> {
                        level.pac().show();
                        level.ghosts().forEach(Ghost::show);
                        level.startHuntingPhase(0);
                        gameController().changeState(GameState.HUNTING);
                    });
                }
            }
            else if (gameController().hasCredit()) { // start new game
                switch ((int) timer.tick()) {
                    case TICK_NEW_GAME_CREATE_LEVEL -> game.createAndStartLevel(1);
                    case TICK_NEW_GAME_SHOW_GUYS -> game.level().ifPresent(level -> {
                        level.pac().show();
                        level.ghosts().forEach(Ghost::show);
                    });
                    case TICK_NEW_GAME_START_PLAYING -> game.level().ifPresent(level -> {
                        game.setPlaying(true);
                        level.startHuntingPhase(0);
                        gameController().changeState(GameState.HUNTING);
                    });
                }
            }
            else { // start demo level
                switch ((int) timer.tick()) {
                    case TICK_DEMO_LEVEL_CREATE_LEVEL -> game.createAndStartDemoLevel();
                    case TICK_DEMO_LEVEL_START_PLAYING -> game.level().ifPresent(level -> {
                        level.startHuntingPhase(0);
                        gameController().changeState(GameState.HUNTING);
                    });
                }
            }
        }
    },

    HUNTING {
        @Override
        public void onEnter(GameModel game) {
            game.level().ifPresent(level -> {
                level.pac().startAnimation();
                level.ghosts().forEach(Ghost::startAnimation);
                level.world().energizerBlinking().restart();
                game.publishGameEvent(GameEventType.HUNTING_PHASE_STARTED);
            });
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> {
                GameState nextState = level.doHuntingStep();
                if (nextState != GameState.HUNTING) {
                    gameController().changeState(nextState);
                }
            });
        }
    },

    LEVEL_COMPLETE {
        @Override
        public void onEnter(GameModel game) {
            game.level().ifPresent(level -> {
                timer.restartSeconds(4);
                level.pac().freeze();
                level.ghosts().forEach(Ghost::hide);
                level.bonus().ifPresent(Bonus::setInactive);
                level.world().mazeFlashing().reset();
                level.stopHuntingPhase();
                Logger.trace("Game level {} ({}) ended.", level.levelNumber, game);
            });
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> {
                if (timer.hasExpired()) {
                    if (level.isDemoLevel()) { // just in case demo level is completed: back to intro scene
                        gameController().changeState(INTRO);
                    } else if (level.intermissionNumber > 0) {
                        gameController().changeState(INTERMISSION);
                    } else {
                        gameController().changeState(CHANGING_TO_NEXT_LEVEL);
                    }
                } else {
                    level.pac().stopAnimation();
                    level.pac().resetAnimation();
                    var flashing = level.world().mazeFlashing();
                    if (timer.atSecond(1)) {
                        flashing.restart(2 * level.numFlashes);
                    } else {
                        flashing.tick();
                    }
                    level.pac().update(level);
                }
            });
        }
    },

    CHANGING_TO_NEXT_LEVEL {
        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(1);
            game.level().ifPresent(level -> game.createAndStartLevel(level.levelNumber + 1));
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
            game.level().ifPresent(level -> {
                timer.restartSeconds(1);
                level.pac().hide();
                level.ghosts().forEach(Ghost::stopAnimation);
                game.publishGameEvent(GameEventType.GHOST_EATEN);
            });
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> {
                if (timer.hasExpired()) {
                    gameController().resumePreviousState();
                } else {
                    level.ghosts(GhostState.EATEN, GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
                        .forEach(ghost -> ghost.update(level));
                    level.world().energizerBlinking().tick();
                }
            });
        }

        @Override
        public void onExit(GameModel game) {
            game.level().ifPresent(level -> {
                level.pac().show();
                level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_TO_HOUSE));
                level.ghosts().forEach(Ghost::startAnimation);
            });
        }
    },

    PACMAN_DYING {

        static final int TICK_HIDE_GHOSTS = 60;
        static final int TICK_START_PAC_ANIMATION = 90;
        static final int TICK_HIDE_PAC = 180;

        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(4);
            game.level().ifPresent(GameLevel::letPacDie);
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> {
                level.world().energizerBlinking().tick();
                level.pac().update(level);
                if (timer.tick() == TICK_HIDE_GHOSTS) {
                    level.ghosts().forEach(Ghost::hide);
                    level.pac().selectAnimation(Pac.ANIM_DYING);
                    level.pac().resetAnimation();
                } else if (timer.tick() == TICK_START_PAC_ANIMATION) {
                    level.pac().startAnimation();
                    game.publishGameEvent(GameEventType.PAC_DIED);
                } else if (timer.tick() == TICK_HIDE_PAC) {
                    level.pac().hide();
                    game.loseLife();
                } else if (timer.hasExpired()) {
                    if (!gameController().hasCredit()) { // end of demo level
                        gameController().changeState(INTRO);
                    } else {
                        gameController().changeState(game.lives() == 0 ? GAME_OVER : READY);
                    }
                }
            });
        }

        @Override
        public void onExit(GameModel context) {
            context.level().flatMap(GameLevel::bonus).ifPresent(Bonus::setInactive);
        }
    },

    GAME_OVER {

        static final int TICKS_STATE_DURATION = 75;

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
            game.setLevel(null);
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
                gameController().changeState(
                    gameController().hasCredit() && game.isPlaying() ? CHANGING_TO_NEXT_LEVEL : INTRO);
            }
        }
    },

    LEVEL_TEST {
        private int lastTestedLevel;

        @Override
        public void onEnter(GameModel game) {
            lastTestedLevel = switch (game) {
                case GameVariants.MS_PACMAN -> 18;
                case GameVariants.PACMAN -> 20;
                default -> throw new IllegalGameVariantException(game);

            };
            timer.restartIndefinitely();
            game.reset();
            game.createAndStartLevel(1);
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> level.doLevelTestStep(timer, lastTestedLevel));
        }

        @Override
        public void onExit(GameModel game) {
            game.levelCounter().clear();
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