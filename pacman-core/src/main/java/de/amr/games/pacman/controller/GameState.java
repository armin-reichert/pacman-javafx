/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;

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
            game.clearLevelCounter();
            game.score().reset();
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
            game.setPlaying(false);
            game.removeLevel();
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
        @Override
        public void onEnter(GameModel game) {
            if (!gameController().hasCredit()) { // ready for demo level
                game.reset();
            } else if (game.isPlaying()) { // resume running game
                game.level().ifPresent(level -> level.letsGetReadyToRumble(true));
            } else { // prepare new game
                game.score().reset();
                game.clearLevelCounter();
                game.reset();
            }
        }

        @Override
        public void onUpdate(GameModel game) {
            if (gameController().hasCredit() && !game.isPlaying()) { // new game
                if (timer.tick() == 1) {
                    game.createAndStartLevel(1);
                } else if (timer.tick() == 120) {
                    game.level().ifPresent(level -> level.guys().forEach(Creature::show));
                } else if (timer.tick() == 260) {
                    game.level().ifPresent(level -> {
                        game.setPlaying(true);
                        level.startHuntingPhase(0);
                        gameController().changeState(GameState.HUNTING);
                    });
                }
            } else if (game.isPlaying()) { // resume game play
                if (timer.tick() == 90) {
                    game.level().ifPresent(level -> {
                        level.guys().forEach(Creature::show);
                        level.startHuntingPhase(0);
                        gameController().changeState(GameState.HUNTING);
                    });
                }
            } else { // start demo level
                if (timer.tick() == 1) {
                    game.createAndStartDemoLevel();
                } else if (timer.tick() == 130) {
                    game.level().ifPresent(level -> {
                        level.guys().forEach(Creature::show);
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
                gameController().manualPacSteering().setEnabled(true);
                level.pac().startAnimation();
                level.ghosts().forEach(Ghost::startAnimation);
                level.world().energizerBlinking().restart();
                publishGameEvent(new GameEvent(GameEventType.HUNTING_PHASE_STARTED, game, null));
            });
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> {
                level.simulateOneFrame();
                if (level.thisFrame().levelCompleted) {
                    gameController().changeState(LEVEL_COMPLETE);
                } else if (level.thisFrame().pacKilled) {
                    gameController().changeState(PACMAN_DYING);
                } else if (!level.thisFrame().pacPrey.isEmpty()) {
                    level.killEdibleGhosts();
                    gameController().changeState(GHOST_DYING);
                }
            });
        }
    },

    LEVEL_COMPLETE {
        @Override
        public void onEnter(GameModel game) {
            gameController().manualPacSteering().setEnabled(false);
            timer.restartSeconds(4);
            game.level().ifPresent(GameLevel::end);
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> {
                if (timer.hasExpired()) {
                    if (!gameController().hasCredit()) { // from demo level back to intro scene
                        gameController().changeState(INTRO);
                    } else if (level.data().intermissionNumber() > 0) {
                        gameController().changeState(INTERMISSION); // play intermission scene
                    } else {
                        gameController().changeState(CHANGING_TO_NEXT_LEVEL); // next level
                    }
                } else {
                    level.pac().stopAnimation();
                    level.pac().resetAnimation();
                    var flashing = level.world().mazeFlashing();
                    if (timer.atSecond(1)) {
                        flashing.restart(2 * level.data().numFlashes());
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
            gameController().manualPacSteering().setEnabled(false);
            timer.restartSeconds(1);
            game.level().ifPresent(level -> game.createAndStartLevel(level.number() + 1));
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
                publishGameEvent(game, GameEventType.GHOST_EATEN);
            });
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> {
                if (timer.hasExpired()) {
                    gameController().resumePreviousState();
                } else {
                    level.pac().steering().orElse(gameController().pacSteering()).steer(level, level.pac());
                    level.ghosts(GhostState.EATEN, GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
                        .forEach(ghost -> ghost.updateState(level.pac()));
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
        @Override
        public void onEnter(GameModel game) {
            game.level().ifPresent(level -> {
                gameController().manualPacSteering().setEnabled(false);
                timer.restartSeconds(4);
                level.onPacKilled();
            });
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> {
                if (timer.atSecond(1)) {
                    level.pac().selectAnimation(Pac.ANIM_DYING);
                    level.pac().resetAnimation();
                    level.ghosts().forEach(Ghost::hide);
                } else if (timer.atSecond(1.4)) {
                    level.pac().startAnimation();
                    publishGameEvent(game, GameEventType.PAC_DIED);
                } else if (timer.atSecond(3.0)) {
                    level.pac().hide();
                    game.loseLife();
                    if (game.lives() == 0) {
                        level.world().energizerBlinking().stop();
                    }
                } else if (timer.hasExpired()) {
                    if (!gameController().hasCredit()) {
                        // end of demo level
                        gameController().changeState(INTRO);
                    } else {
                        gameController().changeState(game.lives() == 0 ? GAME_OVER : READY);
                    }
                } else {
                    level.world().energizerBlinking().tick();
                    level.pac().update(level);
                }
            });
        }

        @Override
        public void onExit(GameModel context) {
            context.level().ifPresent(GameLevel::deactivateBonus);
        }
    },

    GAME_OVER {
        @Override
        public void onEnter(GameModel game) {
            timer.restartSeconds(1.2); //TODO not sure about exact duration
            game.updateHighScore();
            gameController().manualPacSteering().setEnabled(false);
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
            game.removeLevel();
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
                gameController().changeState(gameController().hasCredit() && game.isPlaying() ? CHANGING_TO_NEXT_LEVEL : INTRO);
            }
        }
    },

    LEVEL_TEST {
        private int lastTestedLevel;

        @Override
        public void onEnter(GameModel game) {
            lastTestedLevel = switch (game.variant()) {
                case MS_PACMAN -> 18;
                case PACMAN -> 20;
            };
            timer.restartIndefinitely();
            game.reset();
            game.createAndStartLevel(1);
        }

        @Override
        public void onUpdate(GameModel game) {
            game.level().ifPresent(level -> level.simulateOneTestFrame(timer, lastTestedLevel));
        }

        @Override
        public void onExit(GameModel game) {
            game.clearLevelCounter();
        }
    },

    INTERMISSION_TEST {
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely();
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                if (gameController().intermissionTestNumber < 3) {
                    ++gameController().intermissionTestNumber;
                    timer.restartIndefinitely();
                    publishGameEvent(game, GameEventType.UNSPECIFIED_CHANGE);
                } else {
                    gameController().intermissionTestNumber = 1;
                    gameController().changeState(INTRO);
                }
            }
        }
    };

    final TickTimer timer = new TickTimer("Timer-" + name());

    GameController gameController() {
        return GameController.it();
    }

    @Override
    public TickTimer timer() {
        return timer;
    }
}