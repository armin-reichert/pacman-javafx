/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameScene;
import org.tinylog.Logger;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.uilib.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public enum GameActions2D implements GameAction {
    /**
     * Adds credit (simulates insertion of aButtonKey coin) and switches the game state accordingly.
     */
    INSERT_COIN {
        @Override
        public void execute() {
            if (THE_GAME_CONTROLLER.credit < Globals.MAX_COINS) {
                THE_GAME_CONTROLLER.credit += 1;
                THE_UI.sound().enabledProperty().set(true);
                THE_GAME_CONTROLLER.game().publishGameEvent(GameEventType.CREDIT_ADDED);
            }
            if (THE_GAME_CONTROLLER.state() != GameState.SETTING_OPTIONS) {
                THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
            }
        }

        @Override
        public boolean isEnabled() {
            if (THE_GAME_CONTROLLER.game().isPlaying()) {
                return false;
            }
            return THE_GAME_CONTROLLER.state() == GameState.SETTING_OPTIONS ||
                THE_GAME_CONTROLLER.state() == INTRO ||
                THE_GAME_CONTROLLER.game().isDemoLevel() ||
                THE_GAME_CONTROLLER.credit == 0;
        }
    },

    BOOT {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.restart(GameState.BOOT);
            THE_UI.clock().setTargetFrameRate(Globals.TICKS_PER_SECOND);
            THE_UI.clock().pausedProperty().set(false);
            THE_UI.clock().start();
        }
    },

    CHEAT_ADD_LIVES {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.game().addLives(3);
            THE_UI.showFlashMessage(THE_UI.assets().text("cheat_add_lives", THE_GAME_CONTROLLER.game().lives()));
        }
    },

    CHEAT_EAT_ALL {
        @Override
        public void execute() {
            if (THE_GAME_CONTROLLER.game().isPlaying() && THE_GAME_CONTROLLER.state() == GameState.HUNTING) {
                THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                    level.worldMap().tiles()
                        .filter(not(level::isEnergizerPosition))
                        .filter(level::hasFoodAt)
                        .forEach(level::registerFoodEatenAt);
                    THE_GAME_CONTROLLER.game().publishGameEvent(GameEventType.PAC_FOUND_FOOD);
                    THE_UI.sound().stopMunchingSound();
                });
            }
        }
    },

    CHEAT_KILL_GHOSTS {
        @Override
        public void execute() {
            if (THE_GAME_CONTROLLER.game().isPlaying() && THE_GAME_CONTROLLER.state() == GameState.HUNTING) {
                THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                    level.victims().clear();
                    level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(THE_GAME_CONTROLLER.game()::killGhost);
                    THE_GAME_CONTROLLER.changeState(GameState.GHOST_DYING);
                });
            }
        }
    },

    CHEAT_NEXT_LEVEL {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.changeState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled() {
            return THE_GAME_CONTROLLER.game().isPlaying()
                    && THE_GAME_CONTROLLER.state() == GameState.HUNTING
                    && THE_GAME_CONTROLLER.game().level().isPresent()
                    && THE_GAME_CONTROLLER.game().level().get().number() < THE_GAME_CONTROLLER.game().lastLevelNumber();
        }
    },

    PLAYER_UP {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                if (!level.pac().isUsingAutopilot()) {
                    level.pac().setWishDir(Direction.UP);
                }
            });
        }
    },

    PLAYER_DOWN {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                if (!level.pac().isUsingAutopilot()) {
                    level.pac().setWishDir(Direction.DOWN);
                }
            });
        }
    },

    PLAYER_LEFT {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                if (!level.pac().isUsingAutopilot()) {
                    level.pac().setWishDir(Direction.LEFT);
                }
            });
        }
    },

    PLAYER_RIGHT {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                if (!level.pac().isUsingAutopilot()) {
                    level.pac().setWishDir(Direction.RIGHT);
                }
            });
        }
    },

    SHOW_START_PAGE {
        @Override
        public void execute() {
            THE_UI.sound().stopAll();
            THE_UI.currentGameScene().ifPresent(GameScene::end);
            THE_GAME_CONTROLLER.game().endGame();
            THE_UI.showStartView();
        }
    },

    RESTART_INTRO {
        @Override
        public void execute() {
            THE_UI.sound().stopAll();
            THE_UI.currentGameScene().ifPresent(GameScene::end);
            if (THE_GAME_CONTROLLER.state() == GameState.TESTING_LEVELS) {
                THE_GAME_CONTROLLER.state().onExit(THE_GAME_CONTROLLER.game()); //TODO exit other states too?
            }
            THE_UI.clock().setTargetFrameRate(Globals.TICKS_PER_SECOND);
            THE_GAME_CONTROLLER.restart(INTRO);
        }
    },

    START_GAME {
        @Override
        public void execute() {
            if (THE_GAME_CONTROLLER.selectedGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
            } else if (THE_GAME_CONTROLLER.game().canStartNewGame()) {
                THE_UI.sound().stopVoice();
                if (THE_GAME_CONTROLLER.state() == GameState.INTRO || THE_GAME_CONTROLLER.state() == GameState.SETTING_OPTIONS) {
                    THE_GAME_CONTROLLER.changeState(GameState.STARTING_GAME);
                } else {
                    Logger.error("Cannot startButtonKey game play in game state {}", THE_GAME_CONTROLLER.state());
                }
            }
        }
    },

    TEST_CUT_SCENES {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.changeState(GameState.TESTING_CUT_SCENES);
            THE_UI.showFlashMessage("Cut scenes test"); //TODO localize
        }
    },

    TEST_LEVELS_BONI {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.restart(GameState.TESTING_LEVELS);
            THE_UI.showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TEST_LEVELS_TEASERS {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.restart(GameState.TESTING_LEVEL_TEASERS);
            THE_UI.showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TOGGLE_PAUSED {
        @Override
        public void execute() {
            toggle(THE_UI.clock().pausedProperty());
            if (THE_UI.clock().isPaused()) {
                THE_UI.sound().stopAll();
            }
            Logger.info("Game ({}) {}", THE_GAME_CONTROLLER.selectedGameVariant(), THE_UI.clock().isPaused() ? "paused" : "resumed");
        }
    }
}