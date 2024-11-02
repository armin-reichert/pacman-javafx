/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.util.ArcadeController;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui2d.util.KeyInput.*;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public enum GameActions2D implements GameAction {
    /**
     * Adds credit (simulates insertion of a coin) and switches to the credit scene.
     */
    ADD_CREDIT {
        @Override
        public void execute(GameContext context) {
            boolean enabled =
                context.gameState() == GameState.WAITING_FOR_START ||
                    context.gameState() == INTRO ||
                    context.game().isDemoLevel() ||
                    !context.gameController().coinControl().hasCredit();
            if (!enabled) {
                Logger.info("Action ADD_CREDIT is disabled");
                return;
            }
            if (!context.game().isPlaying()) {
                boolean coinInserted = context.gameController().coinControl().insertCoin();
                if (coinInserted) {
                    context.sound().enabledProperty().set(true); // in demo mode, sound is disabled
                    context.sound().playCreditSound();
                    context.game().publishGameEvent(GameEventType.CREDIT_ADDED);
                }
                if (context.gameState() != GameState.WAITING_FOR_START) {
                    context.gameController().changeState(GameState.WAITING_FOR_START);
                }
            }
        }
    },

    BOOT {
        @Override
        public void execute(GameContext context) {
            context.sound().stopAll();
            context.game().removeWorld();
            context.gameClock().setTargetFrameRate(GameModel.TICKS_PER_SECOND);
            context.gameController().restart(GameState.BOOT);
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
                GameWorld world = context.game().world();
                world.map().food().tiles().filter(not(world::isEnergizerPosition)).forEach(world::registerFoodEatenAt);
                context.game().publishGameEvent(GameEventType.PAC_FOUND_FOOD);
                context.sound().stopMunchingSound();
            }
        }
    },

    CHEAT_KILL_GHOSTS {
        @Override
        public void execute(GameContext context) {
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                context.game().victims().clear();
                context.game().ghosts(FRIGHTENED, HUNTING_PAC).forEach(context.game()::killGhost);
                context.gameController().changeState(GameState.GHOST_DYING);
            }
        }
    },

    CHEAT_NEXT_LEVEL {
        @Override
        public void execute(GameContext context) {
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                context.gameController().changeState(GameState.LEVEL_COMPLETE);
            }
        }
    },

    PLAYER_UP {
        @Override
        public void execute(GameContext context) {
            if (!context.game().pac().isUsingAutopilot()) {
                Logger.info("Player UP");
                context.game().pac().setWishDir(Direction.UP);
            }
        }
    },

    PLAYER_DOWN {
        @Override
        public void execute(GameContext context) {
            if (!context.game().pac().isUsingAutopilot()) {
                Logger.info("Player DOWN");
                context.game().pac().setWishDir(Direction.DOWN);
            }
        }
    },

    PLAYER_LEFT {
        @Override
        public void execute(GameContext context) {
            if (!context.game().pac().isUsingAutopilot()) {
                Logger.info("Player LEFT");
                context.game().pac().setWishDir(Direction.LEFT);
            }
        }
    },

    PLAYER_RIGHT {
        @Override
        public void execute(GameContext context) {
            if (!context.game().pac().isUsingAutopilot()) {
                Logger.info("Player RIGHT");
                context.game().pac().setWishDir(Direction.RIGHT);
            }
        }
    },

    OPEN_EDITOR {
        @Override
        public void execute(GameContext context) {
            if (context.game().world() == null) {
                Logger.error("Map editor cannot be opened because no world is available");
                return;
            }
            context.currentGameScene().ifPresent(GameScene::end);
            context.sound().stopAll();
            context.gameClock().stop();
            EditorPage editorPage = context.getOrCreateEditorPage();
            editorPage.startEditor(context.game().world().map());
            context.selectPage(editorPage);
        }

        @Override
        public boolean isEnabled(GameContext context) {
            return context.gameVariant() == GameVariant.PACMAN_XXL;
        }
    },

    SHOW_START_PAGE {
        @Override
        public void execute(GameContext context) {
            context.sound().stopAll();
            context.currentGameScene().ifPresent(GameScene::end);
            context.game().onGameEnded();
            context.selectStartPage();
        }
    },

    RESTART_INTRO {
        @Override
        public void execute(GameContext context) {
            context.sound().stopAll();
            context.currentGameScene().ifPresent(GameScene::end);
            if (context.gameState() == GameState.TESTING_LEVEL_BONI) {
                context.gameState().onExit(context.game()); //TODO exit other states too?
            }
            context.gameClock().setTargetFrameRate(GameModel.TICKS_PER_SECOND);
            context.gameController().restart(INTRO);
        }
    },

    START_GAME {
        @Override
        public void execute(GameContext context) {
            if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                context.gameController().changeState(GameState.WAITING_FOR_START);
            } else if (context.game().canStartNewGame()) {
                context.sound().stopVoice();
                if (context.gameState() == GameState.INTRO || context.gameState() == GameState.WAITING_FOR_START) {
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
            context.gameController().restart(GameState.TESTING_LEVEL_BONI);
            context.showFlashMessageSeconds(3, "Level TEST MODE");
        }
    },

    TEST_LEVELS_TEASERS {
        @Override
        public void execute(GameContext context) {
            context.gameController().restart(GameState.TESTING_LEVEL_TEASERS);
            context.showFlashMessageSeconds(3, "Level TEST MODE");
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

    public static void bindDefaultArcadeControllerActions(GameActionProvider actionProvider, ArcadeController arcadeController) {
        actionProvider.bind(GameActions2D.ADD_CREDIT,   arcadeController.coin());
        actionProvider.bind(GameActions2D.START_GAME,   arcadeController.start());
        actionProvider.bind(GameActions2D.PLAYER_UP,    arcadeController.up());
        actionProvider.bind(GameActions2D.PLAYER_DOWN,  arcadeController.down());
        actionProvider.bind(GameActions2D.PLAYER_LEFT,  arcadeController.left());
        actionProvider.bind(GameActions2D.PLAYER_RIGHT, arcadeController.right());
    }

    public static void bindFallbackPlayerControlActions(GameActionProvider actionProvider) {
        actionProvider.bind(GameActions2D.PLAYER_UP, control(KeyCode.UP));
        actionProvider.bind(GameActions2D.PLAYER_DOWN, control(KeyCode.DOWN));
        actionProvider.bind(GameActions2D.PLAYER_LEFT, control(KeyCode.LEFT));
        actionProvider.bind(GameActions2D.PLAYER_RIGHT, control(KeyCode.RIGHT));
    }

    public static void bindCheatActions(GameActionProvider actionProvider) {
        actionProvider.bind(GameActions2D.CHEAT_EAT_ALL, alt(KeyCode.E));
        actionProvider.bind(GameActions2D.CHEAT_ADD_LIVES, alt(KeyCode.L));
        actionProvider.bind(GameActions2D.CHEAT_NEXT_LEVEL, alt(KeyCode.N));
        actionProvider.bind(GameActions2D.CHEAT_KILL_GHOSTS, alt(KeyCode.X));
    }

    public static void bindTestActions(GameActionProvider actionProvider) {
        actionProvider.bind(GameActions2D.TEST_CUT_SCENES,     alt(KeyCode.C));
        actionProvider.bind(GameActions2D.TEST_LEVELS_BONI,    alt(KeyCode.T));
        actionProvider.bind(GameActions2D.TEST_LEVELS_TEASERS, shift_alt(KeyCode.T));
    }
}