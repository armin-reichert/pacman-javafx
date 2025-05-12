/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
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
            PacManGamesEnvironment.THE_UI.showFlashMessage(PacManGamesEnvironment.THE_ASSETS.text("cheat_add_lives", game().lifeCount()));
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
                    PacManGamesEnvironment.THE_SOUND.stopMunchingSound();
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
                PacManGamesEnvironment.THE_SOUND.enabledProperty().set(true);
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
            PacManGamesEnvironment.THE_UI.currentGameScene().ifPresent(GameScene::end);
            game().resetEverything();
            if (!THE_COIN_MECHANISM.isEmpty()) THE_COIN_MECHANISM.consumeCoin();
            PacManGamesEnvironment.THE_UI.showStartView();
        }
    },

    RESTART_INTRO {
        @Override
        public void execute() {
            PacManGamesEnvironment.THE_SOUND.stopAll();
            PacManGamesEnvironment.THE_UI.currentGameScene().ifPresent(GameScene::end);
            if (gameState() == GameState.TESTING_LEVELS) {
                gameState().onExit(game()); //TODO exit other states too?
            }
            PacManGamesEnvironment.THE_CLOCK.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            THE_GAME_CONTROLLER.restart(INTRO);
        }
    },

    SIMULATION_SLOWER {
        @Override
        public void execute() {
            double newRate = PacManGamesEnvironment.THE_CLOCK.getTargetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            PacManGamesEnvironment.THE_CLOCK.setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            PacManGamesEnvironment.THE_UI.showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    },

    SIMULATION_FASTER {
        @Override
        public void execute() {
            double newRate = PacManGamesEnvironment.THE_CLOCK.getTargetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            PacManGamesEnvironment.THE_CLOCK.setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            PacManGamesEnvironment.THE_UI.showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    },

    SIMULATION_ONE_STEP {
        @Override
        public void execute() {
            boolean success = PacManGamesEnvironment.THE_CLOCK.makeOneStep(true);
            if (!success) {
                PacManGamesEnvironment.THE_UI.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return PacManGamesEnvironment.THE_CLOCK.isPaused(); }
    },

    SIMULATION_TEN_STEPS {
        @Override
        public void execute() {
            boolean success = PacManGamesEnvironment.THE_CLOCK.makeSteps(10, true);
            if (!success) {
                PacManGamesEnvironment.THE_UI.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return PacManGamesEnvironment.THE_CLOCK.isPaused(); }
    },

    SIMULATION_RESET {
        @Override
        public void execute() {
            PacManGamesEnvironment.THE_CLOCK.setTargetFrameRate(NUM_TICKS_PER_SEC);
            PacManGamesEnvironment.THE_UI.showFlashMessageSec(0.75, PacManGamesEnvironment.THE_CLOCK.getTargetFrameRate() + "Hz");
        }
    },

    START_ARCADE_GAME {
        @Override
        public void execute() {
            PacManGamesEnvironment.THE_SOUND.stopVoice();
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
            PacManGamesEnvironment.THE_UI.showFlashMessage("Cut scenes test"); //TODO localize
        }
    },

    TEST_LEVELS_BONI {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.restart(GameState.TESTING_LEVELS);
            PacManGamesEnvironment.THE_UI.showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TEST_LEVELS_TEASERS {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.restart(GameState.TESTING_LEVEL_TEASERS);
            PacManGamesEnvironment.THE_UI.showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TOGGLE_AUTOPILOT {
        @Override
        public void execute() {
            toggle(PacManGamesEnvironment.PY_AUTOPILOT);
            boolean auto = PacManGamesEnvironment.PY_AUTOPILOT.get();
            PacManGamesEnvironment.THE_UI.showFlashMessage(PacManGamesEnvironment.THE_ASSETS.text(auto ? "autopilot_on" : "autopilot_off"));
            PacManGamesEnvironment.THE_SOUND.playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
        }
    },

    TOGGLE_DEBUG_INFO {
        @Override
        public void execute() {
            toggle(PacManGamesEnvironment.PY_DEBUG_INFO_VISIBLE);
        }
    },

    TOGGLE_IMMUNITY {
        @Override
        public void execute() {
            toggle(PacManGamesEnvironment.PY_IMMUNITY);
            PacManGamesEnvironment.THE_UI.showFlashMessage(PacManGamesEnvironment.THE_ASSETS.text(PacManGamesEnvironment.PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
            PacManGamesEnvironment.THE_SOUND.playVoice(PacManGamesEnvironment.PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
        }
    },

    TOGGLE_PAUSED {
        @Override
        public void execute() {
            toggle(PacManGamesEnvironment.THE_CLOCK.pausedProperty());
            if (PacManGamesEnvironment.THE_CLOCK.isPaused()) {
                PacManGamesEnvironment.THE_SOUND.stopAll();
            }
            Logger.info("Game ({}) {}", gameVariant(), PacManGamesEnvironment.THE_CLOCK.isPaused() ? "paused" : "resumed");
        }
    },

    PERSPECTIVE_NEXT {
        @Override
        public void execute() {
            PerspectiveID id = PacManGamesEnvironment.PY_3D_PERSPECTIVE.get().next();
            PacManGamesEnvironment.PY_3D_PERSPECTIVE.set(id);
            String msgKey = PacManGamesEnvironment.THE_ASSETS.text("camera_perspective", PacManGamesEnvironment.THE_ASSETS.text("perspective_id_" + id.name()));
            PacManGamesEnvironment.THE_UI.showFlashMessage(msgKey);
        }
    },

    PERSPECTIVE_PREVIOUS {
        @Override
        public void execute() {
            PerspectiveID id = PacManGamesEnvironment.PY_3D_PERSPECTIVE.get().prev();
            PacManGamesEnvironment.PY_3D_PERSPECTIVE.set(id);
            String msgKey = PacManGamesEnvironment.THE_ASSETS.text("camera_perspective", PacManGamesEnvironment.THE_ASSETS.text("perspective_id_" + id.name()));
            PacManGamesEnvironment.THE_UI.showFlashMessage(msgKey);
        }
    },

    TOGGLE_DRAW_MODE {
        @Override
        public void execute() {
            PacManGamesEnvironment.PY_3D_DRAW_MODE.set(PacManGamesEnvironment.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    },

    TOGGLE_PLAY_SCENE_2D_3D {
        @Override
        public void execute() {
            PacManGamesEnvironment.THE_UI.currentGameScene().ifPresent(gameScene -> {
                toggle(PacManGamesEnvironment.PY_3D_ENABLED);
                if (PacManGamesEnvironment.THE_UI_CONFIGS.currentGameSceneIsPlayScene2D()
                    || PacManGamesEnvironment.THE_UI_CONFIGS.currentGameSceneIsPlayScene3D()) {
                    PacManGamesEnvironment.THE_UI.updateGameScene(true);
                    THE_GAME_CONTROLLER.update(); //TODO needed?
                }
                if (!game().isPlaying()) {
                    PacManGamesEnvironment.THE_UI.showFlashMessage(PacManGamesEnvironment.THE_ASSETS.text(PacManGamesEnvironment.PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return isOneOf(gameState(),
                GameState.BOOT, GameState.INTRO, GameState.SETTING_OPTIONS, GameState.HUNTING,
                GameState.TESTING_LEVEL_TEASERS, GameState.TESTING_LEVELS
            );
        }
    },

    TOGGLE_PIP_VISIBILITY {
        @Override
        public void execute() {
            toggle(PacManGamesEnvironment.PY_PIP_ON);
            if (!PacManGamesEnvironment.THE_UI_CONFIGS.currentGameSceneIsPlayScene3D()) {
                PacManGamesEnvironment.THE_UI.showFlashMessage(PacManGamesEnvironment.THE_ASSETS.text(PacManGamesEnvironment.PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    };

    static final int SIMULATION_SPEED_DELTA = 2;
    static final int SIMULATION_SPEED_MIN   = 10;
    static final int SIMULATION_SPEED_MAX   = 240;
}