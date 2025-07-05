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
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.INTRO;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public interface PacManGames_GameActions {
    int SIMULATION_SPEED_DELTA = 2;
    int SIMULATION_SPEED_MIN   = 10;
    int SIMULATION_SPEED_MAX   = 240;

    record SteeringAction(Direction dir) implements GameAction {
        @Override
        public void execute(PacManGames_UI ui) { theGameLevel().pac().setWishDir(dir); }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return optGameLevel().isPresent() && !theGameLevel().pac().isUsingAutopilot();
        }

        @Override
        public String name() {
            return "STEER_PAC_" + dir;
        }
    }

    GameAction ACTION_SIMULATION_FASTER = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            double newRate = theClock().targetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            ui.showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }

        @Override
        public String name() {
            return "SIMULATION_FASTER";
        }
    };
    GameAction ACTION_SIMULATION_SLOWER = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            double newRate = theClock().targetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            ui.showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }

        @Override
        public String name() {
            return "SIMULATION_SLOWER";
        }
    };
    GameAction ACTION_ENTER_FULLSCREEN = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            ui.stage().setFullScreen(true);
        }

        @Override
        public String name() {
            return "ENTER_FULLSCREEN";
        }
    };
    GameAction ACTION_LET_GAME_STATE_EXPIRE = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameController().letCurrentGameStateExpire();
        }
    };
    GameAction ACTION_CHEAT_ADD_LIVES = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGame().addLives(3);
            ui.showFlashMessage(theAssets().text("cheat_add_lives", theGame().lifeCount()));
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) { return optGameLevel().isPresent(); }

        @Override
        public String name() {
            return "CHEAT_ADD_LIVES";
        }
    };
    GameAction ACTION_CHEAT_EAT_ALL_PELLETS = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameLevel().eatAllPellets();
            theSound().pauseMunchingSound();
            theGameEventManager().publishEvent(theGame(), GameEventType.PAC_FOUND_FOOD);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return optGameLevel().isPresent()
                    && !theGameLevel().isDemoLevel()
                    && theGameState() == GameState.HUNTING;
        }

        @Override
        public String name() {
            return "CHEAT_EAT_ALL_PELLETS";
        }
    };
    GameAction ACTION_CHEAT_KILL_GHOSTS = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            GameLevel level = theGameLevel();
            List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                level.victims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(theGame()::onGhostKilled);
                theGameController().changeGameState(GameState.GHOST_DYING);
            }
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return theGameState() == GameState.HUNTING && optGameLevel().isPresent() && !theGameLevel().isDemoLevel();
        }

        @Override
        public String name() {
            return "CHEAT_KILL_GHOSTS";
        }
    };
    GameAction ACTION_CHEAT_ENTER_NEXT_LEVEL = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameController().changeGameState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return theGame().isPlaying()
                    && theGameState() == GameState.HUNTING
                    && optGameLevel().isPresent()
                    && theGameLevel().number() < theGame().lastLevelNumber();
        }

        @Override
        public String name() {
            return "CHEAT_ENTER_NEXT_LEVEL";
        }
    };
    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    GameAction ACTION_ARCADE_INSERT_COIN = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            if (theCoinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                theCoinMechanism().insertCoin();
                theSound().enabledProperty().set(true);
                theGameEventManager().publishEvent(theGame(), GameEventType.CREDIT_ADDED);
            }
            theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            if (theGame().isPlaying()) {
                return false;
            }
            return theGameState() == GameState.SETTING_OPTIONS
                    || theGameState() == INTRO
                    || optGameLevel().isPresent() && optGameLevel().get().isDemoLevel()
                    || theCoinMechanism().isEmpty();
        }

        @Override
        public String name() {
            return "INSERT_COIN";
        }
    };
    GameAction ACTION_STEER_UP = new SteeringAction(Direction.UP);
    GameAction ACTION_STEER_DOWN = new SteeringAction(Direction.DOWN);
    GameAction ACTION_STEER_LEFT = new SteeringAction(Direction.LEFT);
    GameAction ACTION_STEER_RIGHT = new SteeringAction(Direction.RIGHT);
    GameAction ACTION_QUIT_GAME_SCENE = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            ui.currentGameScene().ifPresent(GameScene::end);
            theGame().resetEverything();
            if (!theCoinMechanism().isEmpty()) theCoinMechanism().consumeCoin();
            ui.showStartView();
        }

        @Override
        public String name() {
            return "QUIT_GAME_SCENE";
        }
    };
    GameAction ACTION_RESTART_INTRO = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theSound().stopAll();
            ui.currentGameScene().ifPresent(GameScene::end);
            if (theGameState() == GameState.TESTING_LEVELS_SHORT) {
                theGameState().onExit(theGame()); //TODO exit other states too?
            }
            theClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            theGameController().restart(INTRO);
        }

        @Override
        public String name() {
            return "RESTART_INTRO";
        }
    };
    GameAction ACTION_BOOT_SHOW_GAME_VIEW = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            ui.showGameView();
            ui.restart();
        }

        @Override
        public String name() {
            return "BOOT_SHOW_GAME_VIEW";
        }
    };
    GameAction ACTION_SIMULATION_ONE_STEP = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            boolean success = theClock().makeOneStep(true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) { return theClock().isPaused(); }

        @Override
        public String name() {
            return "SIMULATION_ONE_STEP";
        }
    };
    GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            boolean success = theClock().makeSteps(10, true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) { return theClock().isPaused(); }

        @Override
        public String name() {
            return "SIMULATION_TEN_STEPS";
        }
    };
    GameAction ACTION_SIMULATION_RESET = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theClock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            ui.showFlashMessageSec(0.75, theClock().targetFrameRate() + "Hz");
        }

        @Override
        public String name() {
            return "SIMULATION_RESET";
        }
    };
    GameAction ACTION_ARCADE_START_GAME = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theSound().stopVoice();
            theGameController().changeGameState(GameState.STARTING_GAME);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return  Set.of("PACMAN", "MS_PACMAN", "PACMAN_XXL", "MS_PACMAN_XXL").contains(theGameController().selectedGameVariant())
                    && !theCoinMechanism().isEmpty()
                    && (theGameState() == GameState.INTRO || theGameState() == GameState.SETTING_OPTIONS)
                    && theGame().canStartNewGame();
        }

        @Override
        public String name() {
            return "START_GAME";
        }
    };
    GameAction ACTION_TEST_CUT_SCENES = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameController().changeGameState(GameState.TESTING_CUT_SCENES);
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }

        @Override
        public String name() {
            return "TEST_CUT_SCENES";
        }
    };
    GameAction ACTION_TEST_LEVELS_BONI = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameController().restart(GameState.TESTING_LEVELS_SHORT);
            ui.showFlashMessageSec(3, "Level TEST MODE");
        }

        @Override
        public String name() {
            return "TEST_LEVELS_BONI";
        }
    };
    GameAction ACTION_TEST_LEVELS_TEASERS = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameController().restart(GameState.TESTING_LEVELS_MEDIUM);
            ui.showFlashMessageSec(3, "Level TEST MODE");
        }

        @Override
        public String name() {
            return "TEST_LEVELS_TEASERS";
        }
    };
    GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            toggle(PacManGames_UI.PY_USING_AUTOPILOT);
            boolean auto = PacManGames_UI.PY_USING_AUTOPILOT.get();
            ui.showFlashMessage(theAssets().text(auto ? "autopilot_on" : "autopilot_off"));
            theSound().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
        }

        @Override
        public String name() {
            return "TOGGLE_AUTOPILOT";
        }
    };
    GameAction ACTION_TOGGLE_DEBUG_INFO = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            toggle(PacManGames_UI.PY_DEBUG_INFO_VISIBLE);
        }

        @Override
        public String name() {
            return "TOGGLE_DEBUG_INFO";
        }
    };
    GameAction ACTION_TOGGLE_IMMUNITY = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            toggle(PacManGames_UI.PY_IMMUNITY);
            ui.showFlashMessage(theAssets().text(PacManGames_UI.PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
            theSound().playVoice(PacManGames_UI.PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
        }

        @Override
        public String name() {
            return "TOGGLE_IMMUNITY";
        }
    };
    GameAction ACTION_TOGGLE_MUTED = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theSound().toggleMuted();
        }

        @Override
        public String name() {
            return "TOGGLE_MUTED";
        }
    };
    GameAction ACTION_TOGGLE_PAUSED = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            toggle(theClock().pausedProperty());
            if (theClock().isPaused()) {
                theSound().stopAll();
            }
            Logger.info("Game ({}) {}", theGameController().selectedGameVariant(), theClock().isPaused() ? "paused" : "resumed");
        }

        @Override
        public String name() {
            return "TOGGLE_PAUSED";
        }
    };
    GameAction ACTION_PERSPECTIVE_NEXT = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            PerspectiveID id = PacManGames_UI.PY_3D_PERSPECTIVE.get().next();
            PacManGames_UI.PY_3D_PERSPECTIVE.set(id);
            String msgKey = theAssets().text("camera_perspective", theAssets().text("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }

        @Override
        public String name() {
            return "PERSPECTIVE_NEXT";
        }
    };
    GameAction ACTION_PERSPECTIVE_PREVIOUS = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            PerspectiveID id = PacManGames_UI.PY_3D_PERSPECTIVE.get().prev();
            PacManGames_UI.PY_3D_PERSPECTIVE.set(id);
            String msgKey = theAssets().text("camera_perspective", theAssets().text("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }

        @Override
        public String name() {
            return "PERSPECTIVE_PREVIOUS";
        }
    };
    GameAction ACTION_SHOW_HELP = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            ui.gameView().showHelp();
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return (theGameController().isSelected("PACMAN")
                    || theGameController().isSelected("PACMAN_XXL")
                    || theGameController().isSelected("MS_PACMAN")
                    || theGameController().isSelected("MS_PACMAN_XXL"))
                    && ui.currentView() == ui.gameView()
                    && ui.currentGameScene().isPresent()
                    && ui.currentGameScene().get() instanceof GameScene2D;
        }

        @Override
        public String name() {
            return "SHOW_HELP";
        }
    };
    GameAction ACTION_TOGGLE_DASHBOARD = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            ui.gameView().dashboard().toggleVisibility();
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return ui.currentView().equals(ui.gameView());
        }

        @Override
        public String name() {
            return "TOGGLE_DASHBOARD";
        }
    };
    GameAction ACTION_TOGGLE_DRAW_MODE = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            PacManGames_UI.PY_3D_DRAW_MODE.set(PacManGames_UI.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }

        @Override
        public String name() {
            return "TOGGLE_DRAW_MODE";
        }
    };
    GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            ui.currentGameScene().ifPresent(gameScene -> {
                toggle(PacManGames_UI.PY_3D_ENABLED);
                if (ui.currentGameSceneIsPlayScene2D() || ui.currentGameSceneIsPlayScene3D()) {
                    ui.updateGameScene(true);
                    theGameController().updateGameState(); //TODO needed?
                }
                if (!theGame().isPlaying()) {
                    ui.showFlashMessage(theAssets().text(PacManGames_UI.PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return isOneOf(theGameState(),
                    GameState.BOOT, GameState.INTRO, GameState.SETTING_OPTIONS, GameState.HUNTING,
                    GameState.TESTING_LEVELS_MEDIUM, GameState.TESTING_LEVELS_SHORT
            );
        }

        @Override
        public String name() {
            return "TOGGLE_PLAY_SCENE_2D_3D";
        }
    };
    GameAction ACTION_TOGGLE_PIP_VISIBILITY = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            toggle(PacManGames_UI.PY_PIP_ON);
            if (!ui.currentGameSceneIsPlayScene3D()) {
                ui.showFlashMessage(theAssets().text(PacManGames_UI.PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }

        @Override
        public String name() {
            return "TOGGLE_PIP_VISIBILITY";
        }
    };
}
