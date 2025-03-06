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
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameActionProvider;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.GameScene;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import static de.amr.games.pacman.controller.GameController.TICKS_PER_SECOND;
import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
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
        public void execute(GameContext context) {
            if (context.gameController().credit < GameController.MAX_COINS) {
                context.gameController().credit += 1;
                context.sound().enabledProperty().set(true);
                context.game().publishGameEvent(GameEventType.CREDIT_ADDED);
            }
            if (context.gameState() != GameState.SETTING_OPTIONS) {
                context.gameController().changeState(GameState.SETTING_OPTIONS);
            }
        }

        @Override
        public boolean isEnabled(GameContext context) {
            if (context.game().isPlaying()) {
                return false;
            }
            return context.gameState() == GameState.SETTING_OPTIONS ||
                context.gameState() == INTRO ||
                context.game().isDemoLevel() ||
                context.gameController().credit == 0;
        }
    },

    BOOT {
        @Override
        public void execute(GameContext context) {
            context.gameController().restart(GameState.BOOT);
            context.gameClock().setTargetFrameRate(TICKS_PER_SECOND);
            context.gameClock().pausedPy.set(false);
            context.gameClock().start();
        }
    },

    CHEAT_ADD_LIVES {
        @Override
        public void execute(GameContext context) {
            context.game().addLives(3);
            context.showFlashMessage(context.locText("cheat_add_lives", context.game().lives()));
        }
    },

    CHEAT_EAT_ALL {
        @Override
        public void execute(GameContext context) {
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                GameWorld world = context.level().world();
                world.map().tiles()
                    .filter(not(world::isEnergizerPosition))
                    .filter(world::hasFoodAt)
                    .forEach(world::registerFoodEatenAt);
                context.game().publishGameEvent(GameEventType.PAC_FOUND_FOOD);
                context.sound().stopMunchingSound();
            }
        }
    },

    CHEAT_KILL_GHOSTS {
        @Override
        public void execute(GameContext context) {
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                context.level().victims().clear();
                context.level().ghosts(FRIGHTENED, HUNTING_PAC).forEach(context.game()::killGhost);
                context.gameController().changeState(GameState.GHOST_DYING);
            }
        }
    },

    CHEAT_NEXT_LEVEL {
        @Override
        public void execute(GameContext context) {
            context.gameController().changeState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled(GameContext context) {
            return context.game().isPlaying()
                && context.gameState() == GameState.HUNTING
                && context.level().number < context.game().lastLevelNumber();
        }
    },

    PLAYER_UP {
        @Override
        public void execute(GameContext context) {
            if (!context.level().pac().isUsingAutopilot()) {
                Logger.debug("Player UP");
                context.level().pac().setWishDir(Direction.UP);
            }
        }
    },

    PLAYER_DOWN {
        @Override
        public void execute(GameContext context) {
            if (!context.level().pac().isUsingAutopilot()) {
                Logger.debug("Player DOWN");
                context.level().pac().setWishDir(Direction.DOWN);
            }
        }
    },

    PLAYER_LEFT {
        @Override
        public void execute(GameContext context) {
            if (!context.level().pac().isUsingAutopilot()) {
                Logger.debug("Player LEFT");
                context.level().pac().setWishDir(Direction.LEFT);
            }
        }
    },

    PLAYER_RIGHT {
        @Override
        public void execute(GameContext context) {
            if (!context.level().pac().isUsingAutopilot()) {
                Logger.debug("Player RIGHT");
                context.level().pac().setWishDir(Direction.RIGHT);
            }
        }
    },

    SHOW_START_PAGE {
        @Override
        public void execute(GameContext context) {
            context.sound().stopAll();
            context.currentGameScene().ifPresent(GameScene::end);
            context.game().endGame();
            context.showStartView();
        }
    },

    RESTART_INTRO {
        @Override
        public void execute(GameContext context) {
            context.sound().stopAll();
            context.currentGameScene().ifPresent(GameScene::end);
            if (context.gameState() == GameState.TESTING_LEVELS) {
                context.gameState().onExit(context.game()); //TODO exit other states too?
            }
            context.gameClock().setTargetFrameRate(TICKS_PER_SECOND);
            context.gameController().restart(INTRO);
        }
    },

    START_GAME {
        @Override
        public void execute(GameContext context) {
            if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                context.gameController().changeState(GameState.SETTING_OPTIONS);
            } else if (context.game().canStartNewGame()) {
                context.sound().stopVoice();
                if (context.gameState() == GameState.INTRO || context.gameState() == GameState.SETTING_OPTIONS) {
                    context.gameController().changeState(GameState.STARTING_GAME);
                } else {
                    Logger.error("Cannot start game play in game state {}", context.gameState());
                }
            }
        }
    },

    TEST_CUT_SCENES {
        @Override
        public void execute(GameContext context) {
            context.gameController().changeState(GameState.TESTING_CUT_SCENES);
            context.showFlashMessage("Cut scenes test"); //TODO localize
        }
    },

    TEST_LEVELS_BONI {
        @Override
        public void execute(GameContext context) {
            context.gameController().restart(GameState.TESTING_LEVELS);
            context.showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TEST_LEVELS_TEASERS {
        @Override
        public void execute(GameContext context) {
            context.gameController().restart(GameState.TESTING_LEVEL_TEASERS);
            context.showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TOGGLE_PAUSED {
        @Override
        public void execute(GameContext context) {
            toggle(context.gameClock().pausedPy);
            if (context.gameClock().isPaused()) {
                context.sound().stopAll();
            }
            Logger.info("Game ({}) {}", context.gameVariant(), context.gameClock().isPaused() ? "paused" : "resumed");
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