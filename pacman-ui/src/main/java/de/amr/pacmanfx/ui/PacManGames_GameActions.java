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
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public interface PacManGames_GameActions {

    int SIMULATION_SPEED_DELTA = 2;
    int SIMULATION_SPEED_MIN   = 10;
    int SIMULATION_SPEED_MAX   = 240;

    record SteeringAction(Direction dir) implements GameAction {
        @Override
        public void execute(GameUI ui) { ui.theGameContext().theGameLevel().pac().setWishDir(dir); }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.theGameContext().optGameLevel().isPresent() && !ui.theGameContext().theGameLevel().pac().isUsingAutopilot();
        }

        @Override
        public String name() {
            return "STEER_PAC_" + dir;
        }
    }

    GameAction ACTION_SIMULATION_FASTER = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            double newRate = ui.theGameClock().targetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.theGameClock().setTargetFrameRate(newRate);
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
        public void execute(GameUI ui) {
            double newRate = ui.theGameClock().targetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.theGameClock().setTargetFrameRate(newRate);
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
        public void execute(GameUI ui) {
            ui.theStage().setFullScreen(true);
        }

        @Override
        public String name() {
            return "ENTER_FULLSCREEN";
        }
    };

    GameAction ACTION_LET_GAME_STATE_EXPIRE = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameController().letCurrentGameStateExpire();
        }

        @Override
        public String name() {
            return "LET_GAME_STATE_EXPIRE";
        }
    };

    GameAction ACTION_CHEAT_ADD_LIVES = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGame().addLives(3);
            ui.showFlashMessage(ui.theAssets().text("cheat_add_lives", ui.theGameContext().theGame().lifeCount()));
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.theGameContext().optGameLevel().isPresent(); }

        @Override
        public String name() {
            return "CHEAT_ADD_LIVES";
        }
    };

    GameAction ACTION_CHEAT_EAT_ALL_PELLETS = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameLevel().eatAllPellets();
            ui.theSound().pause(SoundID.PAC_MAN_MUNCHING);
            ui.theGameContext().theGameEventManager().publishEvent(GameEventType.PAC_FOUND_FOOD);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.theGameContext().optGameLevel().isPresent()
                    && !ui.theGameContext().theGameLevel().isDemoLevel()
                    && ui.theGameContext().theGameState() == GameState.HUNTING;
        }

        @Override
        public String name() {
            return "CHEAT_EAT_ALL_PELLETS";
        }
    };

    GameAction ACTION_CHEAT_KILL_GHOSTS = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            GameLevel level = ui.theGameContext().theGameLevel();
            List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                level.victims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(ui.theGameContext().theGame()::onGhostKilled);
                ui.theGameContext().theGameController().changeGameState(GameState.GHOST_DYING);
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.theGameContext().theGameState() == GameState.HUNTING && ui.theGameContext().optGameLevel().isPresent() && !ui.theGameContext().theGameLevel().isDemoLevel();
        }

        @Override
        public String name() {
            return "CHEAT_KILL_GHOSTS";
        }
    };

    GameAction ACTION_CHEAT_ENTER_NEXT_LEVEL = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameController().changeGameState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.theGameContext().theGame().isPlaying()
                    && ui.theGameContext().theGameState() == GameState.HUNTING
                    && ui.theGameContext().optGameLevel().isPresent()
                    && ui.theGameContext().theGameLevel().number() < ui.theGameContext().theGame().lastLevelNumber();
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
        public void execute(GameUI ui) {
            if (ui.theGameContext().theCoinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                ui.theSound().setEnabled(true);
                ui.theGameContext().theCoinMechanism().insertCoin();
                ui.theGameContext().theGameEventManager().publishEvent(GameEventType.CREDIT_ADDED);
            }
            ui.theGameContext().theGameController().changeGameState(GameState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            if (ui.theGameContext().theGame().isPlaying()) {
                return false;
            }
            return ui.theGameContext().theGameState() == GameState.SETTING_OPTIONS_FOR_START
                    || ui.theGameContext().theGameState() == INTRO
                    || ui.theGameContext().optGameLevel().isPresent() && ui.theGameContext().optGameLevel().get().isDemoLevel()
                    || ui.theGameContext().theCoinMechanism().isEmpty();
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
        public void execute(GameUI ui) {
            ui.thePlayView().quitCurrentGameScene();
        }

        @Override
        public String name() {
            return "QUIT_GAME_SCENE";
        }
    };

    GameAction ACTION_RESTART_INTRO = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theSound().stopAll();
            ui.currentGameScene().ifPresent(GameScene::end);
            if (ui.theGameContext().theGameState() == GameState.TESTING_LEVELS_SHORT) {
                ui.theGameContext().theGameState().onExit(ui.theGameContext()); //TODO exit other states too?
            }
            ui.theGameClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            ui.theGameContext().theGameController().restart(INTRO);
        }

        @Override
        public String name() {
            return "RESTART_INTRO";
        }
    };

    GameAction ACTION_BOOT_SHOW_PLAY_VIEW = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.showPlayView();
            ui.restart();
        }

        @Override
        public String name() {
            return "BOOT_SHOW_PLAY_VIEW";
        }
    };

    GameAction ACTION_SIMULATION_ONE_STEP = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            boolean success = ui.theGameClock().makeOneStep(true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.theGameClock().isPaused(); }

        @Override
        public String name() {
            return "SIMULATION_ONE_STEP";
        }
    };

    GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            boolean success = ui.theGameClock().makeSteps(10, true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.theGameClock().isPaused(); }

        @Override
        public String name() {
            return "SIMULATION_TEN_STEPS";
        }
    };

    GameAction ACTION_SIMULATION_RESET = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theGameClock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            ui.showFlashMessageSec(0.75, ui.theGameClock().targetFrameRate() + "Hz");
        }

        @Override
        public String name() {
            return "SIMULATION_RESET";
        }
    };

    GameAction ACTION_ARCADE_START_GAME = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theSound().stopVoice();
            ui.theGameContext().theGameController().changeGameState(GameState.STARTING_GAME);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return  Set.of("PACMAN", "MS_PACMAN", "PACMAN_XXL", "MS_PACMAN_XXL").contains(ui.theGameContext().theGameController().selectedGameVariant())
                    && !ui.theGameContext().theCoinMechanism().isEmpty()
                    && (ui.theGameContext().theGameState() == GameState.INTRO || ui.theGameContext().theGameState() == GameState.SETTING_OPTIONS_FOR_START)
                    && ui.theGameContext().theGame().canStartNewGame();
        }

        @Override
        public String name() {
            return "START_GAME";
        }
    };

    GameAction ACTION_TEST_CUT_SCENES = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameController().changeGameState(GameState.TESTING_CUT_SCENES);
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }

        @Override
        public String name() {
            return "TEST_CUT_SCENES";
        }
    };

    GameAction ACTION_TEST_LEVELS_BONI = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameController().restart(GameState.TESTING_LEVELS_SHORT);
            ui.showFlashMessageSec(3, "Level TEST MODE");
        }

        @Override
        public String name() {
            return "TEST_LEVELS_BONI";
        }
    };

    GameAction ACTION_TEST_LEVELS_TEASERS = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameController().restart(GameState.TESTING_LEVELS_MEDIUM);
            ui.showFlashMessageSec(3, "Level TEST MODE");
        }

        @Override
        public String name() {
            return "TEST_LEVELS_TEASERS";
        }
    };

    GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.theGameContext().propertyUsingAutopilot());
            boolean autoPilotOn = ui.theGameContext().propertyUsingAutopilot().get();
            ui.showFlashMessage(ui.theAssets().text(autoPilotOn ? "autopilot_on" : "autopilot_off"));
            ui.theSound().playVoice(autoPilotOn ? SoundID.VOICE_AUTOPILOT_ON : SoundID.VOICE_AUTOPILOT_OFF, 0);
        }

        @Override
        public String name() {
            return "TOGGLE_AUTOPILOT";
        }
    };

    GameAction ACTION_TOGGLE_DEBUG_INFO = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.propertyDebugInfoVisible());
        }

        @Override
        public String name() {
            return "TOGGLE_DEBUG_INFO";
        }
    };

    GameAction ACTION_TOGGLE_IMMUNITY = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.theGameContext().propertyImmunity());
            boolean immunityOn = ui.theGameContext().propertyImmunity().get();
            ui.showFlashMessage(ui.theAssets().text(immunityOn ? "player_immunity_on" : "player_immunity_off"));
            ui.theSound().playVoice(immunityOn ? SoundID.VOICE_IMMUNITY_ON : SoundID.VOICE_IMMUNITY_OFF, 0);
        }

        @Override
        public String name() {
            return "TOGGLE_IMMUNITY";
        }
    };

    GameAction ACTION_TOGGLE_MUTED = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.propertyMuted().set(!ui.propertyMuted().get());
        }

        @Override
        public String name() {
            return "TOGGLE_MUTED";
        }
    };

    GameAction ACTION_TOGGLE_PAUSED = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.theGameClock().pausedProperty());
            if (ui.theGameClock().isPaused()) {
                ui.theSound().stopAll();
            }
            Logger.info("Game ({}) {}", ui.theGameContext().theGameController().selectedGameVariant(), ui.theGameClock().isPaused() ? "paused" : "resumed");
        }

        @Override
        public String name() {
            return "TOGGLE_PAUSED";
        }
    };

    GameAction ACTION_PERSPECTIVE_NEXT = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            Perspective.ID id = ui.property3DPerspective().get().next();
            ui.property3DPerspective().set(id);
            String msgKey = ui.theAssets().text("camera_perspective", ui.theAssets().text("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }

        @Override
        public String name() {
            return "PERSPECTIVE_NEXT";
        }
    };

    GameAction ACTION_PERSPECTIVE_PREVIOUS = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            Perspective.ID id = ui.property3DPerspective().get().prev();
            ui.property3DPerspective().set(id);
            String msgKey = ui.theAssets().text("camera_perspective", ui.theAssets().text("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }

        @Override
        public String name() {
            return "PERSPECTIVE_PREVIOUS";
        }
    };

    GameAction ACTION_SHOW_HELP = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.thePlayView().showHelp(ui);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return (ui.theGameContext().theGameController().isSelected("PACMAN")
                    || ui.theGameContext().theGameController().isSelected("PACMAN_XXL")
                    || ui.theGameContext().theGameController().isSelected("MS_PACMAN")
                    || ui.theGameContext().theGameController().isSelected("MS_PACMAN_XXL"))
                    && ui.currentView() == ui.thePlayView()
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
        public void execute(GameUI ui) {
            ui.thePlayView().dashboard().toggleVisibility();
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.currentView().equals(ui.thePlayView());
        }

        @Override
        public String name() {
            return "TOGGLE_DASHBOARD";
        }
    };

    GameAction ACTION_TOGGLE_DRAW_MODE = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.property3DDrawMode().set(ui.property3DDrawMode().get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }

        @Override
        public String name() {
            return "TOGGLE_DRAW_MODE";
        }
    };

    GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction() {
        @Override
        public void execute(GameUI ui) {
            ui.currentGameScene().ifPresent(gameScene -> {
                toggle(ui.property3DEnabled());
                if (ui.currentGameSceneIsPlayScene2D() || ui.currentGameSceneIsPlayScene3D()) {
                    ui.updateGameScene(true);
                    ui.theGameContext().theGameController().updateGameState(); //TODO needed?
                }
                if (!ui.theGameContext().theGame().isPlaying()) {
                    ui.showFlashMessage(ui.theAssets().text(ui.property3DEnabled().get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return isOneOf(ui.theGameContext().theGameState(),
                    GameState.BOOT, GameState.INTRO, GameState.SETTING_OPTIONS_FOR_START, GameState.HUNTING,
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
        public void execute(GameUI ui) {
            toggle(ui.propertyMiniViewOn());
            if (!ui.currentGameSceneIsPlayScene3D()) {
                ui.showFlashMessage(ui.theAssets().text(ui.propertyMiniViewOn().get() ? "pip_on" : "pip_off"));
            }
        }

        @Override
        public String name() {
            return "TOGGLE_PIP_VISIBILITY";
        }
    };
}
