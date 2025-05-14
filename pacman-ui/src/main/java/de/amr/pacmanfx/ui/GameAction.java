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
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.INTRO;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static de.amr.pacmanfx.uilib.Ufx.toggle;
import static java.util.function.Predicate.not;

public enum GameAction implements Action {

    CHEAT_ADD_LIVES {
        @Override
        public void execute() {
            theGame().addLives(3);
            theUI().showFlashMessage(theAssets().text("cheat_add_lives", theGame().lifeCount()));
        }

        @Override
        public boolean isEnabled() { return optionalGameLevel().isPresent(); }
    },

    CHEAT_EAT_ALL_PELLETS {
        @Override
        public void execute() {
            optionalGameLevel().ifPresent(level -> {
                List<Vector2i> pelletTiles = level.worldMap().tiles()
                    .filter(not(level::isEnergizerPosition))
                    .filter(level::hasFoodAt)
                    .toList();
                if (!pelletTiles.isEmpty()) {
                    pelletTiles.forEach(level::registerFoodEatenAt);
                    theSound().stopMunchingSound();
                    theGameEventManager().publishEvent(theGame(), GameEventType.PAC_FOUND_FOOD);
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return optionalGameLevel().isPresent() && !optionalGameLevel().get().isDemoLevel() && theGameState() == GameState.HUNTING;
        }
    },

    CHEAT_KILL_GHOSTS {
        @Override
        public void execute() {
            optionalGameLevel().ifPresent(level -> {
                List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
                if (!vulnerableGhosts.isEmpty()) {
                    level.victims().clear(); // resets value of next killed ghost to 200
                    vulnerableGhosts.forEach(theGame()::onGhostKilled);
                    theGameController().changeState(GameState.GHOST_DYING);
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return theGameState() == GameState.HUNTING && optionalGameLevel().isPresent() && !optionalGameLevel().get().isDemoLevel();
        }
    },

    CHEAT_ENTER_NEXT_LEVEL {
        @Override
        public void execute() {
            theGameController().changeState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled() {
            return theGame().isPlaying() && theGameState() == GameState.HUNTING && optionalGameLevel().isPresent()
                && optionalGameLevel().get().number() < theGame().lastLevelNumber();
        }
    },

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    INSERT_COIN {
        @Override
        public void execute() {
            if (theCoinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                theCoinMechanism().insertCoin();
                theSound().enabledProperty().set(true);
                theGameEventManager().publishEvent(theGame(), GameEventType.CREDIT_ADDED);
            }
            if (theGameState() != GameState.SETTING_OPTIONS) {
                theGameController().changeState(GameState.SETTING_OPTIONS);
            }
        }

        @Override
        public boolean isEnabled() {
            if (theGame().isPlaying()) {
                return false;
            }
            return theGameState() == GameState.SETTING_OPTIONS
                || theGameState() == INTRO
                || optionalGameLevel().isPresent() && optionalGameLevel().get().isDemoLevel()
                || theCoinMechanism().isEmpty();
        }
    },

    PLAYER_UP {
        @Override
        public void execute() { requirePac().setWishDir(Direction.UP); }

        @Override
        public boolean isEnabled() { return requirePac() != null && !requirePac().isUsingAutopilot(); }
    },

    PLAYER_DOWN {
        @Override
        public void execute() { requirePac().setWishDir(Direction.DOWN); }

        @Override
        public boolean isEnabled() { return requirePac() != null && !requirePac().isUsingAutopilot(); }
    },

    PLAYER_LEFT {
        @Override
        public void execute() { requirePac().setWishDir(Direction.LEFT); }

        @Override
        public boolean isEnabled() { return requirePac() != null && !requirePac().isUsingAutopilot(); }
    },

    PLAYER_RIGHT {
        @Override
        public void execute() { requirePac().setWishDir(Direction.RIGHT); }

        @Override
        public boolean isEnabled() { return requirePac() != null && !requirePac().isUsingAutopilot(); }
    },

    QUIT_GAME_SCENE {
        @Override
        public void execute() {
            theUI().currentGameScene().ifPresent(GameScene::end);
            theGame().resetEverything();
            if (!theCoinMechanism().isEmpty()) theCoinMechanism().consumeCoin();
            theUI().showStartView();
        }
    },

    RESTART_INTRO {
        @Override
        public void execute() {
            theSound().stopAll();
            theUI().currentGameScene().ifPresent(GameScene::end);
            if (theGameState() == GameState.TESTING_LEVELS) {
                theGameState().onExit(theGame()); //TODO exit other states too?
            }
            theClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            theGameController().restart(INTRO);
        }
    },

    SIMULATION_SLOWER {
        @Override
        public void execute() {
            double newRate = theClock().getTargetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            theUI().showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    },

    SIMULATION_FASTER {
        @Override
        public void execute() {
            double newRate = theClock().getTargetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            theUI().showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    },

    SIMULATION_ONE_STEP {
        @Override
        public void execute() {
            boolean success = theClock().makeOneStep(true);
            if (!success) {
                theUI().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return theClock().isPaused(); }
    },

    SIMULATION_TEN_STEPS {
        @Override
        public void execute() {
            boolean success = theClock().makeSteps(10, true);
            if (!success) {
                theUI().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return theClock().isPaused(); }
    },

    SIMULATION_RESET {
        @Override
        public void execute() {
            theClock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            theUI().showFlashMessageSec(0.75, theClock().getTargetFrameRate() + "Hz");
        }
    },

    START_ARCADE_GAME {
        @Override
        public void execute() {
            theSound().stopVoice();
            theGameController().changeState(GameState.STARTING_GAME);
        }

        @Override
        public boolean isEnabled() {
            return theGameVariant() != GameVariant.MS_PACMAN_TENGEN
                && !theCoinMechanism().isEmpty()
                && (theGameState() == GameState.INTRO || theGameState() == GameState.SETTING_OPTIONS)
                && theGame().canStartNewGame();
        }
    },

    TEST_CUT_SCENES {
        @Override
        public void execute() {
            theGameController().changeState(GameState.TESTING_CUT_SCENES);
            theUI().showFlashMessage("Cut scenes test"); //TODO localize
        }
    },

    TEST_LEVELS_BONI {
        @Override
        public void execute() {
            theGameController().restart(GameState.TESTING_LEVELS);
            theUI().showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TEST_LEVELS_TEASERS {
        @Override
        public void execute() {
            theGameController().restart(GameState.TESTING_LEVEL_TEASERS);
            theUI().showFlashMessageSec(3, "Level TEST MODE");
        }
    },

    TOGGLE_AUTOPILOT {
        @Override
        public void execute() {
            toggle(PY_AUTOPILOT);
            boolean auto = PY_AUTOPILOT.get();
            theUI().showFlashMessage(theAssets().text(auto ? "autopilot_on" : "autopilot_off"));
            theSound().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
        }
    },

    TOGGLE_DEBUG_INFO {
        @Override
        public void execute() {
            toggle(PY_DEBUG_INFO_VISIBLE);
        }
    },

    TOGGLE_IMMUNITY {
        @Override
        public void execute() {
            toggle(PY_IMMUNITY);
            theUI().showFlashMessage(theAssets().text(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
            theSound().playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
        }
    },

    TOGGLE_PAUSED {
        @Override
        public void execute() {
            toggle(theClock().pausedProperty());
            if (theClock().isPaused()) {
                theSound().stopAll();
            }
            Logger.info("Game ({}) {}", theGameVariant(), theClock().isPaused() ? "paused" : "resumed");
        }
    },

    PERSPECTIVE_NEXT {
        @Override
        public void execute() {
            PerspectiveID id = PY_3D_PERSPECTIVE.get().next();
            PY_3D_PERSPECTIVE.set(id);
            String msgKey = theAssets().text("camera_perspective", theAssets().text("perspective_id_" + id.name()));
            theUI().showFlashMessage(msgKey);
        }
    },

    PERSPECTIVE_PREVIOUS {
        @Override
        public void execute() {
            PerspectiveID id = PY_3D_PERSPECTIVE.get().prev();
            PY_3D_PERSPECTIVE.set(id);
            String msgKey = theAssets().text("camera_perspective", theAssets().text("perspective_id_" + id.name()));
            theUI().showFlashMessage(msgKey);
        }
    },

    TOGGLE_DRAW_MODE {
        @Override
        public void execute() {
            PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    },

    TOGGLE_PLAY_SCENE_2D_3D {
        @Override
        public void execute() {
            theUI().currentGameScene().ifPresent(gameScene -> {
                toggle(PY_3D_ENABLED);
                if (theUIConfig().currentGameSceneIsPlayScene2D()
                    || theUIConfig().currentGameSceneIsPlayScene3D()) {
                    theUI().updateGameScene(true);
                    theGameController().update(); //TODO needed?
                }
                if (!theGame().isPlaying()) {
                    theUI().showFlashMessage(theAssets().text(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return isOneOf(theGameState(),
                GameState.BOOT, GameState.INTRO, GameState.SETTING_OPTIONS, GameState.HUNTING,
                GameState.TESTING_LEVEL_TEASERS, GameState.TESTING_LEVELS
            );
        }
    },

    TOGGLE_PIP_VISIBILITY {
        @Override
        public void execute() {
            toggle(PY_PIP_ON);
            if (!theUIConfig().currentGameSceneIsPlayScene3D()) {
                theUI().showFlashMessage(theAssets().text(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    };

    static final int SIMULATION_SPEED_DELTA = 2;
    static final int SIMULATION_SPEED_MIN   = 10;
    static final int SIMULATION_SPEED_MAX   = 240;
}