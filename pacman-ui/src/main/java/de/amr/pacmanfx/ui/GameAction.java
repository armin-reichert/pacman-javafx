/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.uilib.Action;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.controller.GameState.INTRO;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.uilib.Ufx.toggle;
import static java.util.function.Predicate.not;

public enum GameAction implements Action {

    CHEAT_ADD_LIVES {
        @Override
        public void execute() {
            game().addLives(3);
            PacManGamesEnv.theUI().showFlashMessage(PacManGamesEnv.theAssets().text("cheat_add_lives", game().lifeCount()));
        }

        @Override
        public boolean isEnabled() { return gameLevel().isPresent(); }
    },

    CHEAT_EAT_ALL_PELLETS {
        @Override
        public void execute() {
            gameLevel().ifPresent(level -> {
                List<Vector2i> pelletTiles = level.worldMap().tiles()
                    .filter(not(level::isEnergizerPosition))
                    .filter(level::hasFoodAt)
                    .toList();
                if (!pelletTiles.isEmpty()) {
                    pelletTiles.forEach(level::registerFoodEatenAt);
                    PacManGamesEnv.theSound().stopMunchingSound();
                    THE_GAME_EVENT_MANAGER.publishEvent(game(), GameEventType.PAC_FOUND_FOOD);
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return gameLevel().isPresent() && !gameLevel().get().isDemoLevel() && gameState() == GameState.HUNTING;
        }
    },

    CHEAT_KILL_GHOSTS {
        @Override
        public void execute() {
            gameLevel().ifPresent(level -> {
                List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
                if (!vulnerableGhosts.isEmpty()) {
                    level.victims().clear(); // resets value of next killed ghost to 200
                    vulnerableGhosts.forEach(game()::onGhostKilled);
                    THE_GAME_CONTROLLER.changeState(GameState.GHOST_DYING);
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return gameState() == GameState.HUNTING && gameLevel().isPresent() && !gameLevel().get().isDemoLevel();
        }
    },

    CHEAT_ENTER_NEXT_LEVEL {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.changeState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled() {
            return game().isPlaying() && gameState() == GameState.HUNTING && gameLevel().isPresent()
                && gameLevel().get().number() < game().lastLevelNumber();
        }
    },

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    INSERT_COIN {
        @Override
        public void execute() {
            if (THE_COIN_MECHANISM.numCoins() < CoinMechanism.MAX_COINS) {
                THE_COIN_MECHANISM.insertCoin();
                PacManGamesEnv.theSound().enabledProperty().set(true);
                THE_GAME_EVENT_MANAGER.publishEvent(game(), GameEventType.CREDIT_ADDED);
            }
            if (gameState() != GameState.SETTING_OPTIONS) {
                THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
            }
        }

        @Override
        public boolean isEnabled() {
            if (game().isPlaying()) {
                return false;
            }
            return gameState() == GameState.SETTING_OPTIONS
                || gameState() == INTRO
                || gameLevel().isPresent() && gameLevel().get().isDemoLevel()
                || THE_COIN_MECHANISM.isEmpty();
        }
    },

    PLAYER_UP {
        @Override
        public void execute() { pac().get().setWishDir(Direction.UP); }

        @Override
        public boolean isEnabled() { return pac().isPresent() && !pac().get().isUsingAutopilot(); }
    },

    PLAYER_DOWN {
        @Override
        public void execute() { pac().get().setWishDir(Direction.DOWN); }

        @Override
        public boolean isEnabled() { return pac().isPresent() && !pac().get().isUsingAutopilot(); }
    },

    PLAYER_LEFT {
        @Override
        public void execute() { pac().get().setWishDir(Direction.LEFT); }

        @Override
        public boolean isEnabled() { return pac().isPresent() && !pac().get().isUsingAutopilot(); }
    },

    PLAYER_RIGHT {
        @Override
        public void execute() { pac().get().setWishDir(Direction.RIGHT); }

        @Override
        public boolean isEnabled() { return pac().isPresent() && !pac().get().isUsingAutopilot(); }
    },

    QUIT_GAME_SCENE {
        @Override
        public void execute() {
            PacManGamesEnv.theUI().currentGameScene().ifPresent(GameScene::end);
            game().resetEverything();
            if (!THE_COIN_MECHANISM.isEmpty()) THE_COIN_MECHANISM.consumeCoin();
            PacManGamesEnv.theUI().showStartView();
        }
    },

    RESTART_INTRO {
        @Override
        public void execute() {
            PacManGamesEnv.theSound().stopAll();
            PacManGamesEnv.theUI().currentGameScene().ifPresent(GameScene::end);
            if (gameState() == GameState.TESTING_LEVELS) {
                gameState().onExit(game()); //TODO exit other states too?
            }
            PacManGamesEnv.theClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            THE_GAME_CONTROLLER.restart(INTRO);
        }
    },

    SIMULATION_SLOWER {
        @Override
        public void execute() {
            double newRate = PacManGamesEnv.theClock().getTargetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            PacManGamesEnv.theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            PacManGamesEnv.theUI().showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    },

    SIMULATION_FASTER {
        @Override
        public void execute() {
            double newRate = PacManGamesEnv.theClock().getTargetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            PacManGamesEnv.theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            PacManGamesEnv.theUI().showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    },

    SIMULATION_ONE_STEP {
        @Override
        public void execute() {
            boolean success = PacManGamesEnv.theClock().makeOneStep(true);
            if (!success) {
                PacManGamesEnv.theUI().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return PacManGamesEnv.theClock().isPaused(); }
    },

    SIMULATION_TEN_STEPS {
        @Override
        public void execute() {
            boolean success = PacManGamesEnv.theClock().makeSteps(10, true);
            if (!success) {
                PacManGamesEnv.theUI().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return PacManGamesEnv.theClock().isPaused(); }
    },

    SIMULATION_RESET {
        @Override
        public void execute() {
            PacManGamesEnv.theClock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            PacManGamesEnv.theUI().showFlashMessageSec(0.75, PacManGamesEnv.theClock().getTargetFrameRate() + "Hz");
        }
    },

    START_ARCADE_GAME {
        @Override
        public void execute() {
            PacManGamesEnv.theSound().stopVoice();
            THE_GAME_CONTROLLER.changeState(GameState.STARTING_GAME);
        }

        @Override
        public boolean isEnabled() {
            return gameVariant() != GameVariant.MS_PACMAN_TENGEN
                && !THE_COIN_MECHANISM.isEmpty()
                && (gameState() == GameState.INTRO || gameState() == GameState.SETTING_OPTIONS)
                && game().canStartNewGame();
        }
    },

    TEST_CUT_SCENES {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.changeState(GameState.TESTING_CUT_SCENES);
            PacManGamesEnv.theUI().showFlashMessage("Cut scenes test"); //TODO localize
        }
    },

    TEST_LEVELS_BONI {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.restart(GameState.TESTING_LEVELS);
            PacManGamesEnv.theUI().showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TEST_LEVELS_TEASERS {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.restart(GameState.TESTING_LEVEL_TEASERS);
            PacManGamesEnv.theUI().showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TOGGLE_AUTOPILOT {
        @Override
        public void execute() {
            toggle(PacManGamesEnv.PY_AUTOPILOT);
            boolean auto = PacManGamesEnv.PY_AUTOPILOT.get();
            PacManGamesEnv.theUI().showFlashMessage(PacManGamesEnv.theAssets().text(auto ? "autopilot_on" : "autopilot_off"));
            PacManGamesEnv.theSound().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
        }
    },

    TOGGLE_DEBUG_INFO {
        @Override
        public void execute() {
            toggle(PacManGamesEnv.PY_DEBUG_INFO_VISIBLE);
        }
    },

    TOGGLE_IMMUNITY {
        @Override
        public void execute() {
            toggle(PacManGamesEnv.PY_IMMUNITY);
            PacManGamesEnv.theUI().showFlashMessage(PacManGamesEnv.theAssets().text(PacManGamesEnv.PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
            PacManGamesEnv.theSound().playVoice(PacManGamesEnv.PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
        }
    },

    TOGGLE_PAUSED {
        @Override
        public void execute() {
            toggle(PacManGamesEnv.theClock().pausedProperty());
            if (PacManGamesEnv.theClock().isPaused()) {
                PacManGamesEnv.theSound().stopAll();
            }
            Logger.info("Game ({}) {}", gameVariant(), PacManGamesEnv.theClock().isPaused() ? "paused" : "resumed");
        }
    },

    PERSPECTIVE_NEXT {
        @Override
        public void execute() {
            PerspectiveID id = PacManGamesEnv.PY_3D_PERSPECTIVE.get().next();
            PacManGamesEnv.PY_3D_PERSPECTIVE.set(id);
            String msgKey = PacManGamesEnv.theAssets().text("camera_perspective", PacManGamesEnv.theAssets().text("perspective_id_" + id.name()));
            PacManGamesEnv.theUI().showFlashMessage(msgKey);
        }
    },

    PERSPECTIVE_PREVIOUS {
        @Override
        public void execute() {
            PerspectiveID id = PacManGamesEnv.PY_3D_PERSPECTIVE.get().prev();
            PacManGamesEnv.PY_3D_PERSPECTIVE.set(id);
            String msgKey = PacManGamesEnv.theAssets().text("camera_perspective", PacManGamesEnv.theAssets().text("perspective_id_" + id.name()));
            PacManGamesEnv.theUI().showFlashMessage(msgKey);
        }
    },

    TOGGLE_DRAW_MODE {
        @Override
        public void execute() {
            PacManGamesEnv.PY_3D_DRAW_MODE.set(PacManGamesEnv.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    },

    TOGGLE_PLAY_SCENE_2D_3D {
        @Override
        public void execute() {
            PacManGamesEnv.theUI().currentGameScene().ifPresent(gameScene -> {
                toggle(PacManGamesEnv.PY_3D_ENABLED);
                if (PacManGamesEnv.theUIConfig().currentGameSceneIsPlayScene2D()
                    || PacManGamesEnv.theUIConfig().currentGameSceneIsPlayScene3D()) {
                    PacManGamesEnv.theUI().updateGameScene(true);
                    THE_GAME_CONTROLLER.update(); //TODO needed?
                }
                if (!game().isPlaying()) {
                    PacManGamesEnv.theUI().showFlashMessage(PacManGamesEnv.theAssets().text(PacManGamesEnv.PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return Validations.isOneOf(gameState(),
                GameState.BOOT, GameState.INTRO, GameState.SETTING_OPTIONS, GameState.HUNTING,
                GameState.TESTING_LEVEL_TEASERS, GameState.TESTING_LEVELS
            );
        }
    },

    TOGGLE_PIP_VISIBILITY {
        @Override
        public void execute() {
            toggle(PacManGamesEnv.PY_PIP_ON);
            if (!PacManGamesEnv.theUIConfig().currentGameSceneIsPlayScene3D()) {
                PacManGamesEnv.theUI().showFlashMessage(PacManGamesEnv.theAssets().text(PacManGamesEnv.PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    };

    static final int SIMULATION_SPEED_DELTA = 2;
    static final int SIMULATION_SPEED_MIN   = 10;
    static final int SIMULATION_SPEED_MAX   = 240;
}