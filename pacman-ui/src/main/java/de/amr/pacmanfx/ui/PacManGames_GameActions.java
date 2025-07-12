/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.Perspective;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.Globals.NUM_TICKS_PER_SEC;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.INTRO;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.ui.GameUIContext.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public interface PacManGames_GameActions {

    int SIMULATION_SPEED_DELTA = 2;
    int SIMULATION_SPEED_MIN   = 10;
    int SIMULATION_SPEED_MAX   = 240;

    record SteeringAction(Direction dir) implements GameAction {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) { gameContext.theGameLevel().pac().setWishDir(dir); }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) {
            return gameContext.optGameLevel().isPresent() && !gameContext.theGameLevel().pac().isUsingAutopilot();
        }

        @Override
        public String name() {
            return "STEER_PAC_" + dir;
        }
    }

    GameAction ACTION_SIMULATION_FASTER = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            ui.stage().setFullScreen(true);
        }

        @Override
        public String name() {
            return "ENTER_FULLSCREEN";
        }
    };

    GameAction ACTION_LET_GAME_STATE_EXPIRE = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            gameContext.theGameController().letCurrentGameStateExpire();
        }

        @Override
        public String name() {
            return "LET_GAME_STATE_EXPIRE";
        }
    };

    GameAction ACTION_CHEAT_ADD_LIVES = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            gameContext.theGame().addLives(3);
            ui.showFlashMessage(theAssets().text("cheat_add_lives", gameContext.theGame().lifeCount()));
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) { return gameContext.optGameLevel().isPresent(); }

        @Override
        public String name() {
            return "CHEAT_ADD_LIVES";
        }
    };

    GameAction ACTION_CHEAT_EAT_ALL_PELLETS = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            gameContext.theGameLevel().eatAllPellets();
            theSound().pause(SoundID.PAC_MAN_MUNCHING);
            gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.PAC_FOUND_FOOD);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) {
            return gameContext.optGameLevel().isPresent()
                    && !gameContext.theGameLevel().isDemoLevel()
                    && gameContext.theGameState() == GameState.HUNTING;
        }

        @Override
        public String name() {
            return "CHEAT_EAT_ALL_PELLETS";
        }
    };

    GameAction ACTION_CHEAT_KILL_GHOSTS = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            GameLevel level = gameContext.theGameLevel();
            List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                level.victims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(gameContext.theGame()::onGhostKilled);
                gameContext.theGameController().changeGameState(GameState.GHOST_DYING);
            }
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) {
            return gameContext.theGameState() == GameState.HUNTING && gameContext.optGameLevel().isPresent() && !gameContext.theGameLevel().isDemoLevel();
        }

        @Override
        public String name() {
            return "CHEAT_KILL_GHOSTS";
        }
    };

    GameAction ACTION_CHEAT_ENTER_NEXT_LEVEL = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            gameContext.theGameController().changeGameState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) {
            return gameContext.theGame().isPlaying()
                    && gameContext.theGameState() == GameState.HUNTING
                    && gameContext.optGameLevel().isPresent()
                    && gameContext.theGameLevel().number() < gameContext.theGame().lastLevelNumber();
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            if (gameContext.theCoinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                theSound().setEnabled(true);
                gameContext.theCoinMechanism().insertCoin();
                gameContext.theGameEventManager().publishEvent(gameContext.theGame(), GameEventType.CREDIT_ADDED);
            }
            gameContext.theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) {
            if (gameContext.theGame().isPlaying()) {
                return false;
            }
            return gameContext.theGameState() == GameState.SETTING_OPTIONS
                    || gameContext.theGameState() == INTRO
                    || gameContext.optGameLevel().isPresent() && gameContext.optGameLevel().get().isDemoLevel()
                    || gameContext.theCoinMechanism().isEmpty();
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            ui.gameView().quitCurrentGameScene();
        }

        @Override
        public String name() {
            return "QUIT_GAME_SCENE";
        }
    };

    GameAction ACTION_RESTART_INTRO = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            theSound().stopAll();
            ui.currentGameScene().ifPresent(GameScene::end);
            if (gameContext.theGameState() == GameState.TESTING_LEVELS_SHORT) {
                gameContext.theGameState().onExit(gameContext); //TODO exit other states too?
            }
            theClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            gameContext.theGameController().restart(INTRO);
        }

        @Override
        public String name() {
            return "RESTART_INTRO";
        }
    };

    GameAction ACTION_BOOT_SHOW_GAME_VIEW = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            boolean success = theClock().makeOneStep(true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) { return theClock().isPaused(); }

        @Override
        public String name() {
            return "SIMULATION_ONE_STEP";
        }
    };

    GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            boolean success = theClock().makeSteps(10, true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) { return theClock().isPaused(); }

        @Override
        public String name() {
            return "SIMULATION_TEN_STEPS";
        }
    };

    GameAction ACTION_SIMULATION_RESET = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            theSound().stopVoice();
            gameContext.theGameController().changeGameState(GameState.STARTING_GAME);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) {
            return  Set.of("PACMAN", "MS_PACMAN", "PACMAN_XXL", "MS_PACMAN_XXL").contains(gameContext.theGameController().selectedGameVariant())
                    && !gameContext.theCoinMechanism().isEmpty()
                    && (gameContext.theGameState() == GameState.INTRO || gameContext.theGameState() == GameState.SETTING_OPTIONS)
                    && gameContext.theGame().canStartNewGame();
        }

        @Override
        public String name() {
            return "START_GAME";
        }
    };

    GameAction ACTION_TEST_CUT_SCENES = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            gameContext.theGameController().changeGameState(GameState.TESTING_CUT_SCENES);
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }

        @Override
        public String name() {
            return "TEST_CUT_SCENES";
        }
    };

    GameAction ACTION_TEST_LEVELS_BONI = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            gameContext.theGameController().restart(GameState.TESTING_LEVELS_SHORT);
            ui.showFlashMessageSec(3, "Level TEST MODE");
        }

        @Override
        public String name() {
            return "TEST_LEVELS_BONI";
        }
    };

    GameAction ACTION_TEST_LEVELS_TEASERS = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            gameContext.theGameController().restart(GameState.TESTING_LEVELS_MEDIUM);
            ui.showFlashMessageSec(3, "Level TEST MODE");
        }

        @Override
        public String name() {
            return "TEST_LEVELS_TEASERS";
        }
    };

    GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            toggle(PY_USING_AUTOPILOT);
            boolean autoPilotOn = PY_USING_AUTOPILOT.get();
            ui.showFlashMessage(theAssets().text(autoPilotOn ? "autopilot_on" : "autopilot_off"));
            theSound().playVoice(autoPilotOn ? SoundID.VOICE_AUTOPILOT_ON : SoundID.VOICE_AUTOPILOT_OFF, 0);
        }

        @Override
        public String name() {
            return "TOGGLE_AUTOPILOT";
        }
    };

    GameAction ACTION_TOGGLE_DEBUG_INFO = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            toggle(PY_DEBUG_INFO_VISIBLE);
        }

        @Override
        public String name() {
            return "TOGGLE_DEBUG_INFO";
        }
    };

    GameAction ACTION_TOGGLE_IMMUNITY = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            toggle(PY_IMMUNITY);
            boolean immunityOn = PY_IMMUNITY.get();
            ui.showFlashMessage(theAssets().text(immunityOn ? "player_immunity_on" : "player_immunity_off"));
            theSound().playVoice(immunityOn ? SoundID.VOICE_IMMUNITY_ON : SoundID.VOICE_IMMUNITY_OFF, 0);
        }

        @Override
        public String name() {
            return "TOGGLE_IMMUNITY";
        }
    };

    GameAction ACTION_TOGGLE_MUTED = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            ui.mutedProperty().set(!ui.mutedProperty().get());
        }

        @Override
        public String name() {
            return "TOGGLE_MUTED";
        }
    };

    GameAction ACTION_TOGGLE_PAUSED = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            toggle(theClock().pausedProperty());
            if (theClock().isPaused()) {
                theSound().stopAll();
            }
            Logger.info("Game ({}) {}", gameContext.theGameController().selectedGameVariant(), theClock().isPaused() ? "paused" : "resumed");
        }

        @Override
        public String name() {
            return "TOGGLE_PAUSED";
        }
    };

    GameAction ACTION_PERSPECTIVE_NEXT = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            Perspective.ID id = PacManGames_UI.PY_3D_PERSPECTIVE.get().next();
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            Perspective.ID id = PacManGames_UI.PY_3D_PERSPECTIVE.get().prev();
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            ui.gameView().showHelp();
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) {
            return (gameContext.theGameController().isSelected("PACMAN")
                    || gameContext.theGameController().isSelected("PACMAN_XXL")
                    || gameContext.theGameController().isSelected("MS_PACMAN")
                    || gameContext.theGameController().isSelected("MS_PACMAN_XXL"))
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            ui.gameView().dashboard().toggleVisibility();
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) {
            return ui.currentView().equals(ui.gameView());
        }

        @Override
        public String name() {
            return "TOGGLE_DASHBOARD";
        }
    };

    GameAction ACTION_TOGGLE_DRAW_MODE = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            PacManGames_UI.PY_3D_DRAW_MODE.set(PacManGames_UI.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }

        @Override
        public String name() {
            return "TOGGLE_DRAW_MODE";
        }
    };

    GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            ui.currentGameScene().ifPresent(gameScene -> {
                toggle(PacManGames_UI.PY_3D_ENABLED);
                if (ui.currentGameSceneIsPlayScene2D() || ui.currentGameSceneIsPlayScene3D()) {
                    ui.updateGameScene(true);
                    gameContext.theGameController().updateGameState(); //TODO needed?
                }
                if (!gameContext.theGame().isPlaying()) {
                    ui.showFlashMessage(theAssets().text(PacManGames_UI.PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui, GameContext gameContext) {
            return isOneOf(gameContext.theGameState(),
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
        public void execute(PacManGames_UI ui, GameContext gameContext) {
            toggle(PacManGames_UI.PY_MINI_VIEW_ON);
            if (!ui.currentGameSceneIsPlayScene3D()) {
                ui.showFlashMessage(theAssets().text(PacManGames_UI.PY_MINI_VIEW_ON.get() ? "pip_on" : "pip_off"));
            }
        }

        @Override
        public String name() {
            return "TOGGLE_PIP_VISIBILITY";
        }
    };
}
