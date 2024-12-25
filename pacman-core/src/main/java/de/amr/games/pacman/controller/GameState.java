/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.CustomMapsHandler;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;

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
            game.resetEverything();
            if (game instanceof CustomMapsHandler customMapsHandler) {
                customMapsHandler.updateCustomMaps();
            }
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
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                gameController().changeState(STARTING_GAME);
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
                gameController().changeState(INTRO);
            }
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
            if (game.isPlaying()) {
                // resume running game
                if (timer.tickCount() == 1) {
                    game.level().ifPresent(GameLevel::showReadyMessage);
                    game.letsGetReadyToRumble();
                    game.showGuys();
                } else if (timer.tickCount() == TICK_RESUME_GAME) {
                    gameController().changeState(GameState.HUNTING);
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
                    game.showGuys();
                }
                else if (timer.tickCount() == TICK_NEW_GAME_START_PLAYING) {
                    game.setPlaying(true);
                    gameController().changeState(GameState.HUNTING);
                }
            }
            else { // start demo level
                if (timer.tickCount() == 1) {
                    game.createDemoLevel();
                    game.startLevel();
                    game.showGuys();
                    game.level().ifPresent(GameLevel::showGameOverMessage);
                }
                else if (timer.tickCount() == TICK_DEMO_LEVEL_START_PLAYING) {
                    gameController().changeState(GameState.HUNTING);
                }
            }
        }
    },


    HUNTING {
        int delay;

        @Override
        public void onEnter(GameModel game) {
            delay = gameController().currentGameVariant() == GameVariant.MS_PACMAN_TENGEN ? 60 : 0;
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.tickCount() < delay) {
                return;
            }
            if (timer.tickCount() == delay) {
                game.startHunting();
                if (level.message() != null && level.message().type() == GameLevel.MessageType.READY) {
                    level.clearMessage();
                }
            }
            game.doHuntingStep();
            if (game.isLevelComplete()) {
                gameController().changeState(LEVEL_COMPLETE);
            } else if (game.isPacManKilled()) {
                gameController().changeState(PACMAN_DYING);
            } else if (game.areGhostsKilled()) {
                gameController().changeState(GHOST_DYING);
            }
        }

        @Override
        public void onExit(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (level.message() != null && level.message().type() == GameLevel.MessageType.READY) {
                level.clearMessage();
            }
        }
    },


    LEVEL_COMPLETE {
        @Override
        public void onEnter(GameModel game) {
            timer.restartIndefinitely(); // UI expires timer e.g. when animation finishes
            game.onLevelCompleted();
            game.publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (gameController().currentGameVariant() == GameVariant.MS_PACMAN_TENGEN && game.isDemoLevel()) {
                gameController().changeState(SHOWING_CREDITS);
                return;
            }
            if (timer.hasExpired()) {
                if (game.isDemoLevel()) { // just in case: if demo level is completed, go back to intro scene
                    gameController().changeState(INTRO);
                } else if (level.intermissionNumber() != 0) {
                    gameController().changeState(INTERMISSION);
                } else {
                    gameController().changeState(LEVEL_TRANSITION);
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
                gameController().changeState(STARTING_GAME);
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
        static final int TICK_HIDE_GHOSTS = 60;
        static final int TICK_START_PAC_ANIMATION = 90;
        static final int TICK_HIDE_PAC = 210;

        @Override
        public void onEnter(GameModel game) {
            //TODO find a better solution
            timer.reset(gameController().currentGameVariant() == GameVariant.MS_PACMAN_TENGEN ? 300 : 240);
            timer.start();
            game.onPacKilled();
            game.publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer.hasExpired()) {
                if (game.isDemoLevel()) {
                    gameController().changeState(GAME_OVER);
                } else {
                    game.loseLife();
                    gameController().changeState(game.isOver() ? GAME_OVER : STARTING_GAME);
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
                game.publishGameEvent(GameEventType.PAC_DYING, level.pac().tile());
            }
            else if (timer.tickCount() == TICK_HIDE_PAC) {
                level.pac().hide();
                game.publishGameEvent(GameEventType.PAC_DEAD);
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
            game.level().ifPresent(GameLevel::showGameOverMessage);
        }

        @Override
        public void onUpdate(GameModel game) {
            if (timer.hasExpired()) {
                //TODO find unified solution
                if (gameController().currentGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                    if (game.isDemoLevel()) {
                        gameController().changeState(SHOWING_CREDITS);
                    } else {
                        boolean canContinue = game.continueOnGameOver();
                        gameController().changeState(canContinue ? SETTING_OPTIONS : INTRO);
                    }
                } else {
                    game.resetForStartingNewGame();
                    if (game.canStartNewGame()) {
                        gameController().changeState(SETTING_OPTIONS);
                    } else {
                        gameController().changeState(INTRO);
                    }
                }
            }
        }

        @Override
        public void onExit(GameModel game) {
            game.setPlaying(false);
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
                gameController().changeState(game.isPlaying() ? LEVEL_TRANSITION : INTRO);
            }
        }
    },

    // Tests

    TESTING_LEVELS {

        private int lastLevelNumber;

        @Override
        public void onEnter(GameModel game) {
            GameVariant gameVariant = GameController.it().currentGameVariant();
            lastLevelNumber = switch (gameVariant) {
                case MS_PACMAN -> 25;
                case MS_PACMAN_TENGEN -> 32;
                case PACMAN -> 21;
                case PACMAN_XXL -> 16;
            };
            timer.restartIndefinitely();
            game.resetForStartingNewGame();
            game.createNormalLevel(1);
            game.startLevel();
            game.showGuys();
        }

        @Override
        public void onUpdate(GameModel game) {
            GameLevel level = game.level().orElseThrow();
            if (timer().tickCount() > 2 * TICKS_PER_SECOND) {
                level.blinking().tick();
                level.ghosts().forEach(ghost -> ghost.update(game));
                level.bonus().ifPresent(bonus -> bonus.update(game));
            }
            if (timer().atSecond(1.0)) {
                game.letsGetReadyToRumble();
                game.showGuys();
            }
            else if (timer().atSecond(2)) {
                level.blinking().setStartPhase(Pulse.ON);
                level.blinking().restart();
            }
            else if (timer().atSecond(2.5)) {
                game.activateNextBonus();
            }
            else if (timer().atSecond(4.5)) {
                level.bonus().ifPresent(bonus -> bonus.setEaten(TICKS_PER_SECOND));
                game.publishGameEvent(GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(6.5)) {
                level.bonus().ifPresent(Bonus::setInactive); // needed?
                game.activateNextBonus();
            }
            else if (timer().atSecond(7.5)) {
                level.bonus().ifPresent(bonus -> bonus.setEaten(TICKS_PER_SECOND));
                game.publishGameEvent(GameEventType.BONUS_EATEN);
            }
            else if (timer().atSecond(8.5)) {
                game.hideGuys();
                level.blinking().stop();
                level.blinking().setStartPhase(Pulse.ON);
                level.blinking().reset();
            }
            else if (timer().atSecond(9.5)) {
                setProperty("mazeFlashing", true);
                level.blinking().setStartPhase(Pulse.OFF);
                level.blinking().restart(2 * level.numFlashes());
            }
            else if (timer().atSecond(12.0)) {
                setProperty("mazeFlashing", false);
                level.blinking().reset();
                level.pac().freeze();
                level.bonus().ifPresent(Bonus::setInactive);
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
                case PACMAN_XXL -> 16;
            };
            timer.restartSeconds(TEASER_TIME_SECONDS);
            game.resetForStartingNewGame();
            game.createNormalLevel(1);
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
                    gameController().changeState(INTRO);
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
                gameController().changeState(INTRO);
            } else if (game.isPacManKilled()) {
                timer.expire();
            } else if (game.areGhostsKilled()) {
                gameController().changeState(GHOST_DYING);
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
                    gameController().changeState(INTRO);
                }
            }
        }
    };

    final TickTimer timer = new TickTimer("GameState-Timer-" + name());
    private Map<String, Object> propertyMap;

    @Override
    public TickTimer timer() {
        return timer;
    }

    GameController gameController() { return GameController.it(); }

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