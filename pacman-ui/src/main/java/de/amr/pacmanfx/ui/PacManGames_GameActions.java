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
import static de.amr.pacmanfx.ui.GameUI_Config.SCENE_ID_PLAY_SCENE_2D;
import static de.amr.pacmanfx.ui.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.GameUI_Properties.*;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

/**
 * Global game actions.
 * <p>
 * For each action there must exist an entry in the {@code localized_texts} resource bundle
 * of the form {@code key=localized_action_name} where {@code key=action.name()} !
 */
public interface PacManGames_GameActions {

    int SIMULATION_SPEED_DELTA = 2;
    int SIMULATION_SPEED_MIN   = 10;
    int SIMULATION_SPEED_MAX   = 240;

    class SteeringAction extends GameAction {
        private final Direction dir;

        SteeringAction(Direction dir) {
            super("STEER_PAC_" + dir);
            this.dir = dir;
        }

        @Override
        public void execute(GameUI ui) { ui.theGameContext().theGameLevel().pac().setWishDir(dir); }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.theGameContext().optGameLevel().isPresent() && !ui.theGameContext().theGameLevel().pac().isUsingAutopilot();
        }
    }

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    GameAction ACTION_ARCADE_INSERT_COIN = new GameAction("INSERT_COIN") {
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
    };

    GameAction ACTION_ARCADE_START_GAME = new GameAction("START_GAME") {
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
    };

    GameAction ACTION_BOOT_SHOW_PLAY_VIEW = new GameAction("BOOT_SHOW_PLAY_VIEW") {
        @Override
        public void execute(GameUI ui) {
            ui.showPlayView();
            ui.restart();
        }
    };

    GameAction ACTION_ENTER_FULLSCREEN = new GameAction("ENTER_FULLSCREEN") {
        @Override
        public void execute(GameUI ui) {
            ui.theStage().setFullScreen(true);
        }
    };

    GameAction ACTION_LET_GAME_STATE_EXPIRE = new GameAction("LET_GAME_STATE_EXPIRE") {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameController().letCurrentGameStateExpire();
        }
    };

    GameAction ACTION_CHEAT_ADD_LIVES = new GameAction("CHEAT_ADD_LIVES") {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGame().addLives(3);
            ui.showFlashMessage(ui.theAssets().text("cheat_add_lives", ui.theGameContext().theGame().lifeCount()));
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.theGameContext().optGameLevel().isPresent(); }
    };

    GameAction ACTION_CHEAT_EAT_ALL_PELLETS = new GameAction("CHEAT_EAT_ALL_PELLETS") {
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
    };

    GameAction ACTION_CHEAT_KILL_GHOSTS = new GameAction("CHEAT_KILL_GHOSTS") {
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
    };

    GameAction ACTION_CHEAT_ENTER_NEXT_LEVEL = new GameAction("CHEAT_ENTER_NEXT_LEVEL") {
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
    };

    GameAction ACTION_OPEN_EDITOR = new GameAction("OPEN_EDITOR") {
        @Override
        public void execute(GameUI ui) {
            ui.showEditorView();
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.currentView() == ui.thePlayView() || ui.currentView() == ui.theStartPagesView();
        }
    };

    GameAction ACTION_PERSPECTIVE_NEXT = new GameAction("PERSPECTIVE_NEXT") {
        @Override
        public void execute(GameUI ui) {
            PerspectiveID id = PROPERTY_3D_PERSPECTIVE.get().next();
            PROPERTY_3D_PERSPECTIVE.set(id);
            String msgKey = ui.theAssets().text("camera_perspective", ui.theAssets().text("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }
    };

    GameAction ACTION_PERSPECTIVE_PREVIOUS = new GameAction("PERSPECTIVE_PREVIOUS") {
        @Override
        public void execute(GameUI ui) {
            PerspectiveID id = PROPERTY_3D_PERSPECTIVE.get().prev();
            PROPERTY_3D_PERSPECTIVE.set(id);
            String msgKey = ui.theAssets().text("camera_perspective", ui.theAssets().text("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }
    };

    GameAction ACTION_QUIT_GAME_SCENE = new GameAction("QUIT_GAME_SCENE") {
        @Override
        public void execute(GameUI ui) {
            ui.quitCurrentGameScene();
        }
    };

    GameAction ACTION_RESTART_INTRO = new GameAction("RESTART_INTRO") {
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
    };

    GameAction ACTION_SHOW_HELP = new GameAction("SHOW_HELP") {
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
    };

    GameAction ACTION_SIMULATION_FASTER = new GameAction("SIMULATION_FASTER") {
        @Override
        public void execute(GameUI ui) {
            double newRate = ui.theGameClock().targetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.theGameClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            ui.showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    };

    GameAction ACTION_SIMULATION_SLOWER = new GameAction("SIMULATION_SLOWER") {
        @Override
        public void execute(GameUI ui) {
            double newRate = ui.theGameClock().targetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.theGameClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            ui.showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    };

    GameAction ACTION_SIMULATION_ONE_STEP = new GameAction("SIMULATION_ONE_STEP") {
        @Override
        public void execute(GameUI ui) {
            boolean success = ui.theGameClock().makeOneStep(true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.theGameClock().isPaused(); }
    };

    GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction("SIMULATION_TEN_STEPS") {
        @Override
        public void execute(GameUI ui) {
            boolean success = ui.theGameClock().makeSteps(10, true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.theGameClock().isPaused(); }
    };

    GameAction ACTION_SIMULATION_RESET = new GameAction("SIMULATION_RESET") {
        @Override
        public void execute(GameUI ui) {
            ui.theGameClock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            ui.showFlashMessageSec(0.75, ui.theGameClock().targetFrameRate() + "Hz");
        }
    };

    GameAction ACTION_STEER_UP = new SteeringAction(Direction.UP);
    GameAction ACTION_STEER_DOWN = new SteeringAction(Direction.DOWN);
    GameAction ACTION_STEER_LEFT = new SteeringAction(Direction.LEFT);
    GameAction ACTION_STEER_RIGHT = new SteeringAction(Direction.RIGHT);

    GameAction ACTION_TEST_CUT_SCENES = new GameAction("TEST_CUT_SCENES") {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameController().changeGameState(GameState.TESTING_CUT_SCENES);
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }
    };

    GameAction ACTION_TEST_LEVELS_BONI = new GameAction("TEST_LEVELS_BONI") {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameController().restart(GameState.TESTING_LEVELS_SHORT);
            ui.showFlashMessageSec(3, "Level TEST MODE");
        }
    };

    GameAction ACTION_TEST_LEVELS_TEASERS = new GameAction("TEST_LEVELS_TEASERS") {
        @Override
        public void execute(GameUI ui) {
            ui.theGameContext().theGameController().restart(GameState.TESTING_LEVELS_MEDIUM);
            ui.showFlashMessageSec(3, "Level TEST MODE");
        }
    };

    GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("TOGGLE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.theGameContext().theGameController().propertyUsingAutopilot());
            boolean autoPilotOn = ui.theGameContext().theGameController().propertyUsingAutopilot().get();
            ui.showFlashMessage(ui.theAssets().text(autoPilotOn ? "autopilot_on" : "autopilot_off"));
            ui.theSound().playVoice(autoPilotOn ? SoundID.VOICE_AUTOPILOT_ON : SoundID.VOICE_AUTOPILOT_OFF, 0);
        }
    };

    GameAction ACTION_TOGGLE_DASHBOARD = new GameAction("TOGGLE_DASHBOARD") {
        @Override
        public void execute(GameUI ui) {
            ui.thePlayView().toggleDashboard();
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

    GameAction ACTION_TOGGLE_DEBUG_INFO = new GameAction("TOGGLE_DEBUG_INFO") {
        @Override
        public void execute(GameUI ui) {
            toggle(PROPERTY_DEBUG_INFO_VISIBLE);
        }
    };

    GameAction ACTION_TOGGLE_DRAW_MODE = new GameAction("TOGGLE_DRAW_MODE") {
        @Override
        public void execute(GameUI ui) {
            PROPERTY_3D_DRAW_MODE.set(PROPERTY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }

        @Override
        public String name() {
            return "TOGGLE_DRAW_MODE";
        }
    };

    GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("TOGGLE_IMMUNITY") {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.theGameContext().theGameController().propertyImmunity());
            boolean immunityOn = ui.theGameContext().theGameController().propertyImmunity().get();
            ui.showFlashMessage(ui.theAssets().text(immunityOn ? "player_immunity_on" : "player_immunity_off"));
            ui.theSound().playVoice(immunityOn ? SoundID.VOICE_IMMUNITY_ON : SoundID.VOICE_IMMUNITY_OFF, 0);
        }
    };

    GameAction ACTION_TOGGLE_MINI_VIEW_VISIBILITY = new GameAction("TOGGLE_MINI_VIEW_VISIBILITY") {
        @Override
        public void execute(GameUI ui) {
            toggle(PROPERTY_MINI_VIEW_ON);
            if (!ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)) {
                ui.showFlashMessage(ui.theAssets().text(PROPERTY_MINI_VIEW_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    };

    GameAction ACTION_TOGGLE_MUTED = new GameAction("TOGGLE_MUTED") {
        @Override
        public void execute(GameUI ui) {
            PROPERTY_MUTED.set(!PROPERTY_MUTED.get());
        }
    };

    GameAction ACTION_TOGGLE_PAUSED = new GameAction("TOGGLE_PAUSED") {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.theGameClock().pausedProperty());
            if (ui.theGameClock().isPaused()) {
                ui.theSound().stopAll();
            }
            Logger.info("Game ({}) {}", ui.theGameContext().theGameController().selectedGameVariant(), ui.theGameClock().isPaused() ? "paused" : "resumed");
        }
    };

    GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction("TOGGLE_PLAY_SCENE_2D_3D") {
        @Override
        public void execute(GameUI ui) {
            ui.currentGameScene().ifPresent(gameScene -> {
                toggle(PROPERTY_3D_ENABLED);
                if (ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D) || ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)) {
                    ui.updateGameScene(true);
                    ui.theGameContext().theGameController().updateGameState(); //TODO needed?
                }
                if (!ui.theGameContext().theGame().isPlaying()) {
                    ui.showFlashMessage(ui.theAssets().text(PROPERTY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
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
    };
}