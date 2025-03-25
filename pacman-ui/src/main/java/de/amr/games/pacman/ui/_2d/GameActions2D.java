/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameActionProvider;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.Keyboard;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.controller.GameController.TICKS_PER_SECOND;
import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui.UIGlobals.*;
import static de.amr.games.pacman.uilib.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public enum GameActions2D implements GameAction {
    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    INSERT_COIN {
        @Override
        public void execute() {
            if (THE_GAME_CONTROLLER.credit < GameController.MAX_COINS) {
                THE_GAME_CONTROLLER.credit += 1;
                THE_SOUND.enabledProperty().set(true);
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
            THE_CLOCK.setTargetFrameRate(TICKS_PER_SECOND);
            THE_CLOCK.pausedProperty().set(false);
            THE_CLOCK.start();
        }
    },

    CHEAT_ADD_LIVES {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.game().addLives(3);
            THE_GAME_CONTEXT.showFlashMessage(THE_ASSETS.localizedText("cheat_add_lives", THE_GAME_CONTROLLER.game().lives()));
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
                    THE_SOUND.stopMunchingSound();
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
            THE_SOUND.stopAll();
            THE_GAME_CONTEXT.currentGameScene().ifPresent(GameScene::end);
            THE_GAME_CONTROLLER.game().endGame();
            THE_GAME_CONTEXT.showStartView();
        }
    },

    RESTART_INTRO {
        @Override
        public void execute() {
            THE_SOUND.stopAll();
            THE_GAME_CONTEXT.currentGameScene().ifPresent(GameScene::end);
            if (THE_GAME_CONTROLLER.state() == GameState.TESTING_LEVELS) {
                THE_GAME_CONTROLLER.state().onExit(THE_GAME_CONTROLLER.game()); //TODO exit other states too?
            }
            THE_CLOCK.setTargetFrameRate(TICKS_PER_SECOND);
            THE_GAME_CONTROLLER.restart(INTRO);
        }
    },

    START_GAME {
        @Override
        public void execute() {
            if (THE_GAME_CONTEXT.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
            } else if (THE_GAME_CONTROLLER.game().canStartNewGame()) {
                THE_SOUND.stopVoice();
                if (THE_GAME_CONTROLLER.state() == GameState.INTRO || THE_GAME_CONTROLLER.state() == GameState.SETTING_OPTIONS) {
                    THE_GAME_CONTROLLER.changeState(GameState.STARTING_GAME);
                } else {
                    Logger.error("Cannot start game play in game state {}", THE_GAME_CONTROLLER.state());
                }
            }
        }
    },

    TEST_CUT_SCENES {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.changeState(GameState.TESTING_CUT_SCENES);
            THE_GAME_CONTEXT.showFlashMessage("Cut scenes test"); //TODO localize
        }
    },

    TEST_LEVELS_BONI {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.restart(GameState.TESTING_LEVELS);
            THE_GAME_CONTEXT.showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TEST_LEVELS_TEASERS {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.restart(GameState.TESTING_LEVEL_TEASERS);
            THE_GAME_CONTEXT.showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TOGGLE_PAUSED {
        @Override
        public void execute() {
            toggle(THE_CLOCK.pausedProperty());
            if (THE_CLOCK.isPaused()) {
                THE_SOUND.stopAll();
            }
            Logger.info("Game ({}) {}", THE_GAME_CONTEXT.gameVariant(), THE_CLOCK.isPaused() ? "paused" : "resumed");
        }
    };

    public static void bindDefaultArcadeControllerActions(GameActionProvider actionProvider, ArcadeKeyBinding arcadeKeys) {
        actionProvider.bind(GameActions2D.INSERT_COIN,  arcadeKeys.key(Arcade.Button.COIN));
        actionProvider.bind(GameActions2D.START_GAME,   arcadeKeys.key(Arcade.Button.START));
        actionProvider.bind(GameActions2D.PLAYER_UP,    arcadeKeys.key(Arcade.Button.UP));
        actionProvider.bind(GameActions2D.PLAYER_DOWN,  arcadeKeys.key(Arcade.Button.DOWN));
        actionProvider.bind(GameActions2D.PLAYER_LEFT,  arcadeKeys.key(Arcade.Button.LEFT));
        actionProvider.bind(GameActions2D.PLAYER_RIGHT, arcadeKeys.key(Arcade.Button.RIGHT));
    }

    public static void bindFallbackPlayerControlActions(GameActionProvider actionProvider) {
        actionProvider.bind(GameActions2D.PLAYER_UP,    Keyboard.control(KeyCode.UP));
        actionProvider.bind(GameActions2D.PLAYER_DOWN,  Keyboard.control(KeyCode.DOWN));
        actionProvider.bind(GameActions2D.PLAYER_LEFT,  Keyboard.control(KeyCode.LEFT));
        actionProvider.bind(GameActions2D.PLAYER_RIGHT, Keyboard.control(KeyCode.RIGHT));
    }

    public static void bindCheatActions(GameActionProvider actionProvider) {
        actionProvider.bind(GameActions2D.CHEAT_EAT_ALL,     Keyboard.alt(KeyCode.E));
        actionProvider.bind(GameActions2D.CHEAT_ADD_LIVES,   Keyboard.alt(KeyCode.L));
        actionProvider.bind(GameActions2D.CHEAT_NEXT_LEVEL,  Keyboard.alt(KeyCode.N));
        actionProvider.bind(GameActions2D.CHEAT_KILL_GHOSTS, Keyboard.alt(KeyCode.X));
    }

    public static void bindTestActions(GameActionProvider actionProvider) {
        actionProvider.bind(GameActions2D.TEST_CUT_SCENES,     Keyboard.alt(KeyCode.C));
        actionProvider.bind(GameActions2D.TEST_LEVELS_BONI,    Keyboard.alt(KeyCode.T));
        actionProvider.bind(GameActions2D.TEST_LEVELS_TEASERS, Keyboard.shift_alt(KeyCode.T));
    }
}