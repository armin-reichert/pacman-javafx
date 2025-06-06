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
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.uilib.GameAction;
import de.amr.pacmanfx.uilib.GameScene;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.INTRO;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public interface PacManGames_Actions {

    int SIMULATION_SPEED_DELTA = 2;
    int SIMULATION_SPEED_MIN   = 10;
    int SIMULATION_SPEED_MAX   = 240;

    record PlayerSteeringAction(Direction dir) implements GameAction {
        @Override
        public void execute() { theGameLevel().pac().setWishDir(dir); }

        @Override
        public boolean isEnabled() { return optGameLevel().isPresent() && !theGameLevel().pac().isUsingAutopilot(); }

        @Override
        public String toString() {
            return "SteerPlayer_" + dir;
        }
    }

    GameAction CHEAT_ADD_LIVES = new GameAction() {
        @Override
        public void execute() {
            theGame().addLives(3);
            theUI().showFlashMessage(theAssets().text("cheat_add_lives", theGame().lifeCount()));
        }

        @Override
        public boolean isEnabled() { return optGameLevel().isPresent(); }
    };

    GameAction CHEAT_EAT_ALL_PELLETS = new GameAction() {
        @Override
        public void execute() {
            theGameLevel().eatAllPellets();
            theSound().stopMunchingSound();
            theGameEventManager().publishEvent(theGame(), GameEventType.PAC_FOUND_FOOD);
        }

        @Override
        public boolean isEnabled() {
            return optGameLevel().isPresent()
                    && !theGameLevel().isDemoLevel()
                    && theGameState() == GameState.HUNTING;
        }
    };

    GameAction CHEAT_KILL_GHOSTS = new GameAction() {
        @Override
        public void execute() {
            GameLevel level = theGameLevel();
            List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                level.victims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(theGame()::onGhostKilled);
                theGameController().changeGameState(GameState.GHOST_DYING);
            }
        }

        @Override
        public boolean isEnabled() {
            return theGameState() == GameState.HUNTING
                    && optGameLevel().isPresent()
                    && !theGameLevel().isDemoLevel();
        }
    };

    GameAction CHEAT_ENTER_NEXT_LEVEL = new GameAction() {
        @Override
        public void execute() {
            theGameController().changeGameState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled() {
            return theGame().isPlaying()
                    && theGameState() == GameState.HUNTING
                    && optGameLevel().isPresent()
                    && theGameLevel().number() < theGame().lastLevelNumber();
        }
    };

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    GameAction INSERT_COIN = new GameAction() {
        @Override
        public void execute() {
            if (theCoinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                theCoinMechanism().insertCoin();
                theSound().enabledProperty().set(true);
                theGameEventManager().publishEvent(theGame(), GameEventType.CREDIT_ADDED);
            }
            theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public boolean isEnabled() {
            if (theGame().isPlaying()) {
                return false;
            }
            return theGameState() == GameState.SETTING_OPTIONS
                || theGameState() == INTRO
                || optGameLevel().isPresent() && optGameLevel().get().isDemoLevel()
                || theCoinMechanism().isEmpty();
        }
    };

    GameAction PLAYER_UP = new PlayerSteeringAction(Direction.UP);

    GameAction PLAYER_DOWN = new PlayerSteeringAction(Direction.DOWN);

    GameAction PLAYER_LEFT = new PlayerSteeringAction(Direction.LEFT);

    GameAction PLAYER_RIGHT = new PlayerSteeringAction(Direction.RIGHT);

    GameAction QUIT_GAME_SCENE = new GameAction() {
        @Override
        public void execute() {
            theUI().currentGameScene().ifPresent(GameScene::end);
            theGame().resetEverything();
            if (!theCoinMechanism().isEmpty()) theCoinMechanism().consumeCoin();
            theUI().showStartView();
        }
    };

    GameAction RESTART_INTRO = new GameAction() {
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
    };

    GameAction SHOW_GAME_VIEW_AND_RESTART_GAME = new GameAction() {
        @Override
        public void execute() {
            theUI().showGameView();
            theUI().restart();
        }
    };

    GameAction SIMULATION_SLOWER = new GameAction() {
        @Override
        public void execute() {
            double newRate = theClock().targetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            theUI().showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    };

    GameAction SIMULATION_FASTER = new GameAction() {
        @Override
        public void execute() {
            double newRate = theClock().targetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            theUI().showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    };

    GameAction SIMULATION_ONE_STEP = new GameAction() {
        @Override
        public void execute() {
            boolean success = theClock().makeOneStep(true);
            if (!success) {
                theUI().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return theClock().isPaused(); }
    };

    GameAction SIMULATION_TEN_STEPS = new GameAction() {
        @Override
        public void execute() {
            boolean success = theClock().makeSteps(10, true);
            if (!success) {
                theUI().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return theClock().isPaused(); }
    };

    GameAction SIMULATION_RESET = new GameAction() {
        @Override
        public void execute() {
            theClock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            theUI().showFlashMessageSec(0.75, theClock().targetFrameRate() + "Hz");
        }
    };

    GameAction START_ARCADE_GAME = new GameAction() {
        @Override
        public void execute() {
            theSound().stopVoice();
            theGameController().changeGameState(GameState.STARTING_GAME);
        }

        @Override
        public boolean isEnabled() {
            return theGameVariant() != GameVariant.MS_PACMAN_TENGEN
                && !theCoinMechanism().isEmpty()
                && (theGameState() == GameState.INTRO || theGameState() == GameState.SETTING_OPTIONS)
                && theGame().canStartNewGame();
        }
    };

    GameAction TEST_CUT_SCENES = new GameAction() {
        @Override
        public void execute() {
            theGameController().changeGameState(GameState.TESTING_CUT_SCENES);
            theUI().showFlashMessage("Cut scenes test"); //TODO localize
        }
    };

    GameAction TEST_LEVELS_BONI = new GameAction() {
        @Override
        public void execute() {
            theGameController().restart(GameState.TESTING_LEVELS);
            theUI().showFlashMessageSec(3, "Level TEST MODE");
        }
    };

    GameAction TEST_LEVELS_TEASERS = new GameAction() {
        @Override
        public void execute() {
            theGameController().restart(GameState.TESTING_LEVEL_TEASERS);
            theUI().showFlashMessageSec(3, "Level TEST MODE");
        }
    };

    GameAction TOGGLE_AUTOPILOT = new GameAction() {
        @Override
        public void execute() {
            toggle(PY_USING_AUTOPILOT);
            boolean auto = PY_USING_AUTOPILOT.get();
            theUI().showFlashMessage(theAssets().text(auto ? "autopilot_on" : "autopilot_off"));
            theSound().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
        }
    };

    GameAction TOGGLE_DEBUG_INFO = new GameAction() {
        @Override
        public void execute() {
            toggle(PY_DEBUG_INFO_VISIBLE);
        }
    };

    GameAction TOGGLE_IMMUNITY = new GameAction() {
        @Override
        public void execute() {
            toggle(PY_IMMUNITY);
            theUI().showFlashMessage(theAssets().text(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
            theSound().playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
        }
    };

    GameAction TOGGLE_PAUSED = new GameAction() {
        @Override
        public void execute() {
            toggle(theClock().pausedProperty());
            if (theClock().isPaused()) {
                theSound().stopAll();
            }
            Logger.info("Game ({}) {}", theGameVariant(), theClock().isPaused() ? "paused" : "resumed");
        }
    };

    GameAction PERSPECTIVE_NEXT = new GameAction() {
        @Override
        public void execute() {
            PerspectiveID id = PY_3D_PERSPECTIVE.get().next();
            PY_3D_PERSPECTIVE.set(id);
            String msgKey = theAssets().text("camera_perspective", theAssets().text("perspective_id_" + id.name()));
            theUI().showFlashMessage(msgKey);
        }
    };

    GameAction PERSPECTIVE_PREVIOUS = new GameAction() {
        @Override
        public void execute() {
            PerspectiveID id = PY_3D_PERSPECTIVE.get().prev();
            PY_3D_PERSPECTIVE.set(id);
            String msgKey = theAssets().text("camera_perspective", theAssets().text("perspective_id_" + id.name()));
            theUI().showFlashMessage(msgKey);
        }
    };

    GameAction TOGGLE_DRAW_MODE = new GameAction() {
        @Override
        public void execute() {
            PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    };

    GameAction TOGGLE_PLAY_SCENE_2D_3D = new GameAction() {
        @Override
        public void execute() {
            theUI().currentGameScene().ifPresent(gameScene -> {
                toggle(PY_3D_ENABLED);
                if (theUIConfig().currentGameSceneIsPlayScene2D()
                    || theUIConfig().currentGameSceneIsPlayScene3D()) {
                    theUI().updateGameScene(true);
                    theGameController().updateGameState(); //TODO needed?
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
    };

    GameAction TOGGLE_PIP_VISIBILITY = new GameAction() {
        @Override
        public void execute() {
            toggle(PY_PIP_ON);
            if (!theUIConfig().currentGameSceneIsPlayScene3D()) {
                theUI().showFlashMessage(theAssets().text(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    };
}