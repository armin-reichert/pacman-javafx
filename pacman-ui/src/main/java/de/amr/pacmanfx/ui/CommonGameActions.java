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
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.Globals.NUM_TICKS_PER_SEC;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.INTRO;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.ui.api.GameUI_Config.SCENE_ID_PLAY_SCENE_2D;
import static de.amr.pacmanfx.ui.api.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.*;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

/**
 * Common actions for all game variants.
 * <p>
 * For each action there must exist an entry in the {@code localized_texts} resource bundle
 * of the form {@code key=localized_action_name} where {@code key=action.name()} !
 */
public interface CommonGameActions {

    int SIMULATION_SPEED_DELTA = 2;
    int SIMULATION_SPEED_MIN   = 10;
    int SIMULATION_SPEED_MAX   = 240;

    class SteeringAction extends AbstractGameAction {
        private final Direction dir;

        SteeringAction(Direction dir) {
            super("STEER_PAC_" + dir);
            this.dir = dir;
        }

        @Override
        public void execute(GameUI ui) { ui.gameContext().theGameLevel().pac().setWishDir(dir); }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().optGameLevel().isPresent() && !ui.gameContext().theGameLevel().pac().isUsingAutopilot();
        }
    }

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    AbstractGameAction ACTION_ARCADE_INSERT_COIN = new AbstractGameAction("INSERT_COIN") {
        @Override
        public void execute(GameUI ui) {
            if (ui.gameContext().theCoinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                ui.currentSoundManager().setEnabled(true);
                ui.gameContext().theCoinMechanism().insertCoin();
                ui.gameContext().theGameEventManager().publishEvent(GameEventType.CREDIT_ADDED);
            }
            ui.gameContext().theGameController().changeGameState(GameState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            if (ui.gameContext().theGame().isPlaying()) {
                return false;
            }
            return ui.gameContext().theGameState() == GameState.SETTING_OPTIONS_FOR_START
                || ui.gameContext().theGameState() == INTRO
                || ui.gameContext().optGameLevel().isPresent() && ui.gameContext().optGameLevel().get().isDemoLevel()
                || ui.gameContext().theCoinMechanism().isEmpty();
        }
    };

    AbstractGameAction ACTION_ARCADE_START_GAME = new AbstractGameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.currentSoundManager().stopVoice();
            ui.gameContext().theGameController().changeGameState(GameState.STARTING_GAME);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return  Set.of("PACMAN", "MS_PACMAN", "PACMAN_XXL", "MS_PACMAN_XXL").contains(ui.gameContext().theGameController().selectedGameVariant())
                && !ui.gameContext().theCoinMechanism().isEmpty()
                && (ui.gameContext().theGameState() == GameState.INTRO || ui.gameContext().theGameState() == GameState.SETTING_OPTIONS_FOR_START)
                && ui.gameContext().theGame().canStartNewGame();
        }
    };

    AbstractGameAction ACTION_BOOT_SHOW_PLAY_VIEW = new AbstractGameAction("BOOT_SHOW_PLAY_VIEW") {
        @Override
        public void execute(GameUI ui) {
            ui.showPlayView();
            ui.restart();
        }
    };

    AbstractGameAction ACTION_ENTER_FULLSCREEN = new AbstractGameAction("ENTER_FULLSCREEN") {
        @Override
        public void execute(GameUI ui) {
            ui.stage().setFullScreen(true);
        }
    };

    AbstractGameAction ACTION_LET_GAME_STATE_EXPIRE = new AbstractGameAction("LET_GAME_STATE_EXPIRE") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().theGameController().letCurrentGameStateExpire();
        }
    };

    AbstractGameAction ACTION_CHEAT_ADD_LIVES = new AbstractGameAction("CHEAT_ADD_LIVES") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().theGame().addLives(3);
            ui.showFlashMessage(ui.assets().text("cheat_add_lives", ui.gameContext().theGame().lifeCount()));
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.gameContext().optGameLevel().isPresent(); }
    };

    AbstractGameAction ACTION_CHEAT_EAT_ALL_PELLETS = new AbstractGameAction("CHEAT_EAT_ALL_PELLETS") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().theGameLevel().eatAllPellets();
            ui.currentSoundManager().pause(SoundID.PAC_MAN_MUNCHING);
            ui.gameContext().theGameEventManager().publishEvent(GameEventType.PAC_FOUND_FOOD);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().optGameLevel().isPresent()
                    && !ui.gameContext().theGameLevel().isDemoLevel()
                    && ui.gameContext().theGameState() == GameState.HUNTING;
        }
    };

    AbstractGameAction ACTION_CHEAT_KILL_GHOSTS = new AbstractGameAction("CHEAT_KILL_GHOSTS") {
        @Override
        public void execute(GameUI ui) {
            GameLevel level = ui.gameContext().theGameLevel();
            List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                level.victims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(ui.gameContext().theGame()::onGhostKilled);
                ui.gameContext().theGameController().changeGameState(GameState.GHOST_DYING);
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().theGameState() == GameState.HUNTING && ui.gameContext().optGameLevel().isPresent() && !ui.gameContext().theGameLevel().isDemoLevel();
        }
    };

    AbstractGameAction ACTION_CHEAT_ENTER_NEXT_LEVEL = new AbstractGameAction("CHEAT_ENTER_NEXT_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().theGameController().changeGameState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().theGame().isPlaying()
                    && ui.gameContext().theGameState() == GameState.HUNTING
                    && ui.gameContext().optGameLevel().isPresent()
                    && ui.gameContext().theGameLevel().number() < ui.gameContext().theGame().lastLevelNumber();
        }
    };

    AbstractGameAction ACTION_OPEN_EDITOR = new AbstractGameAction("OPEN_EDITOR") {
        @Override
        public void execute(GameUI ui) {
            ui.showEditorView();
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.currentView() == ui.playView() || ui.currentView() == ui.startPagesView();
        }
    };

    AbstractGameAction ACTION_PERSPECTIVE_NEXT = new AbstractGameAction("PERSPECTIVE_NEXT") {
        @Override
        public void execute(GameUI ui) {
            PerspectiveID id = PROPERTY_3D_PERSPECTIVE.get().next();
            PROPERTY_3D_PERSPECTIVE.set(id);
            String msgKey = ui.assets().text("camera_perspective", ui.assets().text("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }
    };

    AbstractGameAction ACTION_PERSPECTIVE_PREVIOUS = new AbstractGameAction("PERSPECTIVE_PREVIOUS") {
        @Override
        public void execute(GameUI ui) {
            PerspectiveID id = PROPERTY_3D_PERSPECTIVE.get().prev();
            PROPERTY_3D_PERSPECTIVE.set(id);
            String msgKey = ui.assets().text("camera_perspective", ui.assets().text("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }
    };

    AbstractGameAction ACTION_QUIT_GAME_SCENE = new AbstractGameAction("QUIT_GAME_SCENE") {
        @Override
        public void execute(GameUI ui) {
            ui.quitCurrentGameScene();
        }
    };

    AbstractGameAction ACTION_RESTART_INTRO = new AbstractGameAction("RESTART_INTRO") {
        @Override
        public void execute(GameUI ui) {
            ui.currentSoundManager().stopAll();
            ui.currentGameScene().ifPresent(GameScene::end);
            if (ui.gameContext().theGameState() == GameState.TESTING_LEVELS_SHORT) {
                ui.gameContext().theGameState().onExit(ui.gameContext()); //TODO exit other states too?
            }
            ui.clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            ui.gameContext().theGameController().restart(INTRO);
        }
    };

    AbstractGameAction ACTION_SHOW_HELP = new AbstractGameAction("SHOW_HELP") {
        @Override
        public void execute(GameUI ui) {
            ui.playView().showHelp(ui);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return (ui.gameContext().theGameController().isSelected("PACMAN")
                || ui.gameContext().theGameController().isSelected("PACMAN_XXL")
                || ui.gameContext().theGameController().isSelected("MS_PACMAN")
                || ui.gameContext().theGameController().isSelected("MS_PACMAN_XXL"))
                && ui.currentView() == ui.playView()
                && ui.currentGameScene().isPresent()
                && ui.currentGameScene().get() instanceof GameScene2D;
        }
    };

    AbstractGameAction ACTION_SIMULATION_FASTER = new AbstractGameAction("SIMULATION_FASTER") {
        @Override
        public void execute(GameUI ui) {
            double newRate = ui.clock().targetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.clock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            ui.showFlashMessage(Duration.seconds(0.75), prefix + newRate + "Hz");
        }
    };

    AbstractGameAction ACTION_SIMULATION_SLOWER = new AbstractGameAction("SIMULATION_SLOWER") {
        @Override
        public void execute(GameUI ui) {
            double newRate = ui.clock().targetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.clock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            ui.showFlashMessage(Duration.seconds(0.75), prefix + newRate + "Hz");
        }
    };

    AbstractGameAction ACTION_SIMULATION_ONE_STEP = new AbstractGameAction("SIMULATION_ONE_STEP") {
        @Override
        public void execute(GameUI ui) {
            boolean success = ui.clock().makeOneStep(true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.clock().isPaused(); }
    };

    AbstractGameAction ACTION_SIMULATION_TEN_STEPS = new AbstractGameAction("SIMULATION_TEN_STEPS") {
        @Override
        public void execute(GameUI ui) {
            boolean success = ui.clock().makeSteps(10, true);
            if (!success) {
                ui.showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.clock().isPaused(); }
    };

    AbstractGameAction ACTION_SIMULATION_RESET = new AbstractGameAction("SIMULATION_RESET") {
        @Override
        public void execute(GameUI ui) {
            ui.clock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            ui.showFlashMessage(Duration.seconds(0.75), ui.clock().targetFrameRate() + "Hz");
        }
    };

    AbstractGameAction ACTION_STEER_UP = new SteeringAction(Direction.UP);
    AbstractGameAction ACTION_STEER_DOWN = new SteeringAction(Direction.DOWN);
    AbstractGameAction ACTION_STEER_LEFT = new SteeringAction(Direction.LEFT);
    AbstractGameAction ACTION_STEER_RIGHT = new SteeringAction(Direction.RIGHT);

    AbstractGameAction ACTION_TEST_CUT_SCENES = new AbstractGameAction("TEST_CUT_SCENES") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().theGameController().changeGameState(GameState.TESTING_CUT_SCENES);
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }
    };

    AbstractGameAction ACTION_TEST_LEVELS_BONI = new AbstractGameAction("TEST_LEVELS_BONI") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().theGameController().restart(GameState.TESTING_LEVELS_SHORT);
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }
    };

    AbstractGameAction ACTION_TEST_LEVELS_TEASERS = new AbstractGameAction("TEST_LEVELS_TEASERS") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().theGameController().restart(GameState.TESTING_LEVELS_MEDIUM);
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }
    };

    AbstractGameAction ACTION_TOGGLE_AUTOPILOT = new AbstractGameAction("TOGGLE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.gameContext().theGameController().propertyUsingAutopilot());
            boolean autoPilotOn = ui.gameContext().theGameController().propertyUsingAutopilot().get();
            ui.showFlashMessage(ui.assets().text(autoPilotOn ? "autopilot_on" : "autopilot_off"));
            ui.currentSoundManager().playVoice(autoPilotOn ? SoundID.VOICE_AUTOPILOT_ON : SoundID.VOICE_AUTOPILOT_OFF, 0);
        }
    };

    AbstractGameAction ACTION_TOGGLE_DASHBOARD = new AbstractGameAction("TOGGLE_DASHBOARD") {
        @Override
        public void execute(GameUI ui) {
            ui.playView().toggleDashboard();
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.currentView().equals(ui.playView());
        }

        @Override
        public String name() {
            return "TOGGLE_DASHBOARD";
        }
    };

    AbstractGameAction ACTION_TOGGLE_DEBUG_INFO = new AbstractGameAction("TOGGLE_DEBUG_INFO") {
        @Override
        public void execute(GameUI ui) {
            toggle(PROPERTY_DEBUG_INFO_VISIBLE);
        }
    };

    AbstractGameAction ACTION_TOGGLE_DRAW_MODE = new AbstractGameAction("TOGGLE_DRAW_MODE") {
        @Override
        public void execute(GameUI ui) {
            PROPERTY_3D_DRAW_MODE.set(PROPERTY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }

        @Override
        public String name() {
            return "TOGGLE_DRAW_MODE";
        }
    };

    AbstractGameAction ACTION_TOGGLE_IMMUNITY = new AbstractGameAction("TOGGLE_IMMUNITY") {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.gameContext().theGameController().propertyImmunity());
            boolean immunityOn = ui.gameContext().theGameController().propertyImmunity().get();
            ui.showFlashMessage(ui.assets().text(immunityOn ? "player_immunity_on" : "player_immunity_off"));
            ui.currentSoundManager().playVoice(immunityOn ? SoundID.VOICE_IMMUNITY_ON : SoundID.VOICE_IMMUNITY_OFF, 0);
        }
    };

    AbstractGameAction ACTION_TOGGLE_MINI_VIEW_VISIBILITY = new AbstractGameAction("TOGGLE_MINI_VIEW_VISIBILITY") {
        @Override
        public void execute(GameUI ui) {
            toggle(PROPERTY_MINI_VIEW_ON);
            if (!ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)) {
                ui.showFlashMessage(ui.assets().text(PROPERTY_MINI_VIEW_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    };

    AbstractGameAction ACTION_TOGGLE_MUTED = new AbstractGameAction("TOGGLE_MUTED") {
        @Override
        public void execute(GameUI ui) {
            PROPERTY_MUTED.set(!PROPERTY_MUTED.get());
        }
    };

    AbstractGameAction ACTION_TOGGLE_PAUSED = new AbstractGameAction("TOGGLE_PAUSED") {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.clock().pausedProperty());
            if (ui.clock().isPaused()) {
                ui.currentSoundManager().stopAll();
            }
            Logger.info("Game ({}) {}", ui.gameContext().theGameController().selectedGameVariant(), ui.clock().isPaused() ? "paused" : "resumed");
        }
    };

    AbstractGameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new AbstractGameAction("TOGGLE_PLAY_SCENE_2D_3D") {
        @Override
        public void execute(GameUI ui) {
            ui.currentGameScene().ifPresent(gameScene -> {
                toggle(PROPERTY_3D_ENABLED);
                if (ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D) || ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)) {
                    ui.updateGameScene(true);
                    ui.gameContext().theGameController().updateGameState(); //TODO needed?
                }
                if (!ui.gameContext().theGame().isPlaying()) {
                    ui.showFlashMessage(ui.assets().text(PROPERTY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return isOneOf(ui.gameContext().theGameState(),
                    GameState.BOOT, GameState.INTRO, GameState.SETTING_OPTIONS_FOR_START, GameState.HUNTING,
                    GameState.TESTING_LEVELS_MEDIUM, GameState.TESTING_LEVELS_SHORT
            );
        }
    };
}