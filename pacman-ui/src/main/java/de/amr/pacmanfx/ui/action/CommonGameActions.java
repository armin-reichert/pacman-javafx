/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.controller.test.CutScenesTestState;
import de.amr.pacmanfx.controller.test.LevelMediumTestState;
import de.amr.pacmanfx.controller.test.LevelShortTestState;
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
import static de.amr.pacmanfx.Validations.stateIsOneOf;
import static de.amr.pacmanfx.controller.GamePlayState.INTRO;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.ui.api.GameScene_Config.SCENE_ID_PLAY_SCENE_2D;
import static de.amr.pacmanfx.ui.api.GameScene_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.api.GameUI.*;
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

    class SteeringAction extends GameAction {
        private final Direction dir;

        SteeringAction(Direction dir) {
            super("STEER_PAC_" + dir);
            this.dir = dir;
        }

        @Override
        public void execute(GameUI ui) { ui.gameContext().gameLevel().pac().setWishDir(dir); }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().optGameLevel().isPresent() && !ui.gameContext().gameLevel().pac().isUsingAutopilot();
        }
    }

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    GameAction ACTION_ARCADE_INSERT_COIN = new GameAction("INSERT_COIN") {
        @Override
        public void execute(GameUI ui) {
            if (ui.gameContext().coinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                ui.soundManager().setEnabled(true);
                ui.gameContext().coinMechanism().insertCoin();
                ui.gameContext().eventManager().publishEvent(GameEventType.CREDIT_ADDED);
            }
            ui.gameContext().gameController().changeGameState(GamePlayState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            if (ui.gameContext().game().isPlaying()) {
                return false;
            }
            return ui.gameContext().gameState() == GamePlayState.SETTING_OPTIONS_FOR_START
                || ui.gameContext().gameState() == INTRO
                || ui.gameContext().optGameLevel().isPresent() && ui.gameContext().optGameLevel().get().isDemoLevel()
                || ui.gameContext().coinMechanism().isEmpty();
        }
    };

    GameAction ACTION_ARCADE_START_GAME = new GameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopVoice();
            ui.gameContext().gameController().changeGameState(GamePlayState.STARTING_GAME_OR_LEVEL);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return  Set.of("PACMAN", "MS_PACMAN", "PACMAN_XXL", "MS_PACMAN_XXL").contains(ui.gameContext().gameController().gameVariant())
                && !ui.gameContext().coinMechanism().isEmpty()
                && (ui.gameContext().gameState() == GamePlayState.INTRO || ui.gameContext().gameState() == GamePlayState.SETTING_OPTIONS_FOR_START)
                && ui.gameContext().game().canStartNewGame();
        }
    };

    GameAction ACTION_BOOT_SHOW_PLAY_VIEW = new GameAction("BOOT_SHOW_PLAY_VIEW") {
        @Override
        public void execute(GameUI ui) {
            ui.showPlayView();
            ui.restart();
        }
    };

    GameAction ACTION_CHEAT_ADD_LIVES = new GameAction("CHEAT_ADD_LIVES") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            ui.gameContext().game().addLives(3);
            ui.showFlashMessage(ui.assets().translated("cheat_add_lives", ui.gameContext().game().lifeCount()));
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.gameContext().optGameLevel().isPresent(); }
    };

    GameAction ACTION_CHEAT_EAT_ALL_PELLETS = new GameAction("CHEAT_EAT_ALL_PELLETS") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            ui.gameContext().gameLevel().worldMap().foodLayer().eatPellets();
            ui.soundManager().pause(SoundID.PAC_MAN_MUNCHING);
            ui.gameContext().eventManager().publishEvent(GameEventType.PAC_FOUND_FOOD);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().optGameLevel().isPresent()
                    && !ui.gameContext().gameLevel().isDemoLevel()
                    && ui.gameContext().gameState() == GamePlayState.HUNTING;
        }
    };

    GameAction ACTION_CHEAT_KILL_GHOSTS = new GameAction("CHEAT_KILL_GHOSTS") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            GameLevel gameLevel = ui.gameContext().gameLevel();
            List<Ghost> vulnerableGhosts = gameLevel.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                gameLevel.energizerVictims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(ghost -> gameLevel.game().onGhostKilled(gameLevel, ghost));
                ui.gameContext().gameController().changeGameState(GamePlayState.GHOST_DYING);
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().gameState() == GamePlayState.HUNTING && ui.gameContext().optGameLevel().isPresent() && !ui.gameContext().gameLevel().isDemoLevel();
        }
    };

    GameAction ACTION_CHEAT_ENTER_NEXT_LEVEL = new GameAction("CHEAT_ENTER_NEXT_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            ui.gameContext().gameController().changeGameState(GamePlayState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().game().isPlaying()
                    && ui.gameContext().gameState() == GamePlayState.HUNTING
                    && ui.gameContext().optGameLevel().isPresent()
                    && ui.gameContext().gameLevel().number() < ui.gameContext().game().lastLevelNumber();
        }
    };

    GameAction ACTION_ENTER_FULLSCREEN = new GameAction("ENTER_FULLSCREEN") {
        @Override
        public void execute(GameUI ui) {
            ui.stage().setFullScreen(true);
        }
    };

    GameAction ACTION_LET_GAME_STATE_EXPIRE = new GameAction("LET_GAME_STATE_EXPIRE") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().letCurrentGameStateExpire();
        }
    };

    GameAction ACTION_OPEN_EDITOR = new GameAction("OPEN_EDITOR") {
        @Override
        public void execute(GameUI ui) {
            ui.showEditorView();
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.currentView() == ui.playView() || ui.currentView() == ui.startPagesView();
        }
    };

    GameAction ACTION_PERSPECTIVE_NEXT = new GameAction("PERSPECTIVE_NEXT") {
        @Override
        public void execute(GameUI ui) {
            PerspectiveID id = PROPERTY_3D_PERSPECTIVE_ID.get().next();
            PROPERTY_3D_PERSPECTIVE_ID.set(id);
            String msgKey = ui.assets().translated("camera_perspective", ui.assets().translated("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }
    };

    GameAction ACTION_PERSPECTIVE_PREVIOUS = new GameAction("PERSPECTIVE_PREVIOUS") {
        @Override
        public void execute(GameUI ui) {
            PerspectiveID id = PROPERTY_3D_PERSPECTIVE_ID.get().prev();
            PROPERTY_3D_PERSPECTIVE_ID.set(id);
            String msgKey = ui.assets().translated("camera_perspective", ui.assets().translated("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }
    };

    GameAction ACTION_QUIT_GAME_SCENE = new GameAction("QUIT_GAME_SCENE") {
        @Override
        public void execute(GameUI ui) {
            ui.quitCurrentGameScene();
            ui.gameContext().gameController().cheatUsedProperty().set(false);
        }
    };

    GameAction ACTION_RESTART_INTRO = new GameAction("RESTART_INTRO") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopAll();
            ui.currentGameScene().ifPresent(GameScene::end);
            boolean isLevelShortTest = ui.gameContext().gameState() instanceof LevelShortTestState;
            if (isLevelShortTest) {
                ui.gameContext().gameState().onExit(ui.gameContext()); //TODO exit other states too?
            }
            ui.clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            ui.gameContext().gameController().restart(INTRO);
        }
    };

    GameAction ACTION_SHOW_HELP = new GameAction("SHOW_HELP") {
        @Override
        public void execute(GameUI ui) {
            ui.playView().showHelp(ui);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return (ui.gameContext().gameController().isCurrentGameVariant("PACMAN")
                || ui.gameContext().gameController().isCurrentGameVariant("PACMAN_XXL")
                || ui.gameContext().gameController().isCurrentGameVariant("MS_PACMAN")
                || ui.gameContext().gameController().isCurrentGameVariant("MS_PACMAN_XXL"))
                && ui.currentView() == ui.playView()
                && ui.currentGameScene().isPresent()
                && ui.currentGameScene().get() instanceof GameScene2D;
        }
    };

    GameAction ACTION_SIMULATION_FASTER = new GameAction("SIMULATION_FASTER") {
        @Override
        public void execute(GameUI ui) {
            double newRate = ui.clock().targetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.clock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            ui.showFlashMessage(Duration.seconds(0.75), prefix + newRate + "Hz");
        }
    };

    GameAction ACTION_SIMULATION_FASTEST = new GameAction("SIMULATION_FASTEST") {
        @Override
        public void execute(GameUI ui) {
            ui.clock().setTargetFrameRate(SIMULATION_SPEED_MAX);
            ui.showFlashMessage(Duration.seconds(0.75), "At maximum speed: %d Hz", SIMULATION_SPEED_MAX);
        }
    };

    GameAction ACTION_SIMULATION_SLOWER = new GameAction("SIMULATION_SLOWER") {
        @Override
        public void execute(GameUI ui) {
            double newRate = ui.clock().targetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.clock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            ui.showFlashMessage(Duration.seconds(0.75), prefix + newRate + "Hz");
        }
    };
    GameAction ACTION_SIMULATION_SLOWEST = new GameAction("SIMULATION_SLOWEST") {
        @Override
        public void execute(GameUI ui) {
            ui.clock().setTargetFrameRate(SIMULATION_SPEED_MIN);
            ui.showFlashMessage(Duration.seconds(0.75), "At minmimum speed: %d Hz", SIMULATION_SPEED_MIN);
        }
    };

    GameAction ACTION_SIMULATION_ONE_STEP = new GameAction("SIMULATION_ONE_STEP") {
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

    GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction("SIMULATION_TEN_STEPS") {
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

    GameAction ACTION_SIMULATION_RESET = new GameAction("SIMULATION_RESET") {
        @Override
        public void execute(GameUI ui) {
            ui.clock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            ui.showFlashMessage(Duration.seconds(0.75), ui.clock().targetFrameRate() + "Hz");
        }
    };

    GameAction ACTION_STEER_UP = new SteeringAction(Direction.UP);
    GameAction ACTION_STEER_DOWN = new SteeringAction(Direction.DOWN);
    GameAction ACTION_STEER_LEFT = new SteeringAction(Direction.LEFT);
    GameAction ACTION_STEER_RIGHT = new SteeringAction(Direction.RIGHT);

    GameAction ACTION_TEST_CUT_SCENES = new GameAction("TEST_CUT_SCENES") {
        @Override
        public void execute(GameUI ui) {
            GameState testState = ui.gameContext().gameController().stateByName(CutScenesTestState.class.getSimpleName());
            ui.gameContext().gameController().changeGameState(testState);
            ui.showFlashMessage("Cut scenes test"); //TODO localize
        }
    };

    GameAction ACTION_TEST_LEVELS_SHORT = new GameAction("TEST_LEVELS_SHORT") {
        @Override
        public void execute(GameUI ui) {
            GameState testState = ui.gameContext().gameController().stateByName(LevelShortTestState.class.getSimpleName());
            ui.gameContext().gameController().restart(testState);
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }
    };

    GameAction ACTION_TEST_LEVELS_MEDIUM = new GameAction("TEST_LEVELS_MEDIUM") {
        @Override
        public void execute(GameUI ui) {
            GameState testState = ui.gameContext().gameController().stateByName(LevelMediumTestState.class.getSimpleName());
            ui.gameContext().gameController().restart(testState);
            ui.showFlashMessage(Duration.seconds(3), "Level TEST MODE");
        }
    };

    GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("TOGGLE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            toggle(ui.gameContext().gameController().usingAutopilotProperty());
            boolean autoPilotOn = ui.gameContext().gameController().usingAutopilotProperty().get();
            ui.showFlashMessage(ui.assets().translated(autoPilotOn ? "autopilot_on" : "autopilot_off"));
            ui.soundManager().playVoice(autoPilotOn ? SoundID.VOICE_AUTOPILOT_ON : SoundID.VOICE_AUTOPILOT_OFF, 0);
        }
    };

    GameAction ACTION_TOGGLE_DASHBOARD = new GameAction("TOGGLE_DASHBOARD") {
        @Override
        public void execute(GameUI ui) {
            ui.playView().dashboard().toggleVisibility();
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
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            toggle(ui.gameContext().gameController().immunityProperty());
            boolean immunityOn = ui.gameContext().gameController().immunityProperty().get();
            ui.showFlashMessage(ui.assets().translated(immunityOn ? "player_immunity_on" : "player_immunity_off"));
            ui.soundManager().playVoice(immunityOn ? SoundID.VOICE_IMMUNITY_ON : SoundID.VOICE_IMMUNITY_OFF, 0);
        }
    };

    GameAction ACTION_TOGGLE_MINI_VIEW_VISIBILITY = new GameAction("TOGGLE_MINI_VIEW_VISIBILITY") {
        @Override
        public void execute(GameUI ui) {
            toggle(PROPERTY_MINI_VIEW_ON);
            if (!ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)) {
                ui.showFlashMessage(ui.assets().translated(PROPERTY_MINI_VIEW_ON.get() ? "pip_on" : "pip_off"));
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
            toggle(ui.clock().pausedProperty());
            if (ui.clock().isPaused()) {
                ui.soundManager().stopAll();
            }
            Logger.info("Game ({}) {}", ui.gameContext().gameController().gameVariant(), ui.clock().isPaused() ? "paused" : "resumed");
        }
    };

    GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction("TOGGLE_PLAY_SCENE_2D_3D") {
        @Override
        public void execute(GameUI ui) {
            ui.currentGameScene().ifPresent(gameScene -> {
                toggle(PROPERTY_3D_ENABLED);
                if (ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D) || ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)) {
                    ui.updateGameScene(true);
                    ui.gameContext().gameController().updateGameState(); //TODO needed?
                }
                if (!ui.gameContext().game().isPlaying()) {
                    ui.showFlashMessage(ui.assets().translated(PROPERTY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            GameState state = ui.gameContext().gameState();
            if (state.name().equals(LevelShortTestState.class.getSimpleName())
                || state.name().equals(LevelMediumTestState.class.getSimpleName())) {
                return true;
            }
            return stateIsOneOf(state, GamePlayState.BOOT, GamePlayState.INTRO, GamePlayState.SETTING_OPTIONS_FOR_START, GamePlayState.HUNTING);
        }
    };
}