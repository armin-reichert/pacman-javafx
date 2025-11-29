/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.pacmanfx.Globals.NUM_TICKS_PER_SEC;
import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
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
public final class CommonGameActions {

    public static final int SIMULATION_SPEED_DELTA = 2;
    public static final int SIMULATION_SPEED_MIN   = 5;
    public static final int SIMULATION_SPEED_MAX   = 300;

    public static final GameAction ACTION_BOOT_SHOW_PLAY_VIEW = new GameAction("BOOT_SHOW_PLAY_VIEW") {
        @Override
        public void execute(GameUI ui) {
            THE_GAME_BOX.setNumCoins(0);
            ui.showPlayView();
            ui.restart();
        }
    };

    public static final GameAction ACTION_ENTER_FULLSCREEN = new GameAction("ENTER_FULLSCREEN") {
        @Override
        public void execute(GameUI ui) {
            ui.stage().setFullScreen(true);
        }
    };

    public static final GameAction ACTION_LET_GAME_STATE_EXPIRE = new GameAction("LET_GAME_STATE_EXPIRE") {
        @Override
        public void execute(GameUI ui) {
            ui.context().currentGame().terminateCurrentGameState();
        }
    };

    public static final GameAction ACTION_OPEN_EDITOR = new GameAction("OPEN_EDITOR") {
        @Override
        public void execute(GameUI ui) {
            ui.showEditorView();
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.currentView() == ui.playView() || ui.currentView() == ui.startPagesView();
        }
    };

    public static final GameAction ACTION_PERSPECTIVE_NEXT = new GameAction("PERSPECTIVE_NEXT") {
        @Override
        public void execute(GameUI ui) {
            PerspectiveID id = PROPERTY_3D_PERSPECTIVE_ID.get().next();
            PROPERTY_3D_PERSPECTIVE_ID.set(id);
            String msgKey = ui.assets().translated("camera_perspective", ui.assets().translated("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }
    };

    public static final GameAction ACTION_PERSPECTIVE_PREVIOUS = new GameAction("PERSPECTIVE_PREVIOUS") {
        @Override
        public void execute(GameUI ui) {
            PerspectiveID id = PROPERTY_3D_PERSPECTIVE_ID.get().prev();
            PROPERTY_3D_PERSPECTIVE_ID.set(id);
            String msgKey = ui.assets().translated("camera_perspective", ui.assets().translated("perspective_id_" + id.name()));
            ui.showFlashMessage(msgKey);
        }
    };

    public static final GameAction ACTION_QUIT_GAME_SCENE = new GameAction("QUIT_GAME_SCENE") {
        @Override
        public void execute(GameUI ui) {
            ui.quitCurrentGameScene();
            ui.context().cheatUsedProperty().set(false);
        }
    };

    public static final GameAction ACTION_RESTART_INTRO = new GameAction("RESTART_INTRO") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopAll();
            ui.currentGameScene().ifPresent(GameScene::end);
            boolean isLevelShortTest = ui.context().currentGame().state() instanceof LevelShortTestState;
            if (isLevelShortTest) {
                ui.context().currentGame().state().onExit(ui.context()); //TODO exit other states too?
            }
            ui.clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            ui.context().currentGame().restart("INTRO");
        }
    };

    public static final GameAction ACTION_SHOW_HELP = new GameAction("SHOW_HELP") {
        @Override
        public void execute(GameUI ui) {
            ui.playView().showHelp(ui);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            boolean isArcadeGame = StandardGameVariant.isArcadeGameName(ui.context().gameVariantName());
            boolean isPlayScene2D = ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D);
            return isArcadeGame && isPlayScene2D;
        }
    };

    public static final GameAction ACTION_SIMULATION_FASTER = new GameAction("SIMULATION_FASTER") {
        @Override
        public void execute(GameUI ui) {
            double newRate = ui.clock().targetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.clock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            ui.showFlashMessage(Duration.seconds(0.75), prefix + newRate + "Hz");
        }
    };

    public static final GameAction ACTION_SIMULATION_FASTEST = new GameAction("SIMULATION_FASTEST") {
        @Override
        public void execute(GameUI ui) {
            ui.clock().setTargetFrameRate(SIMULATION_SPEED_MAX);
            ui.showFlashMessage(Duration.seconds(0.75), "At maximum speed: %d Hz", SIMULATION_SPEED_MAX);
        }
    };

    public static final GameAction ACTION_SIMULATION_SLOWER = new GameAction("SIMULATION_SLOWER") {
        @Override
        public void execute(GameUI ui) {
            double newRate = ui.clock().targetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            ui.clock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            ui.showFlashMessage(Duration.seconds(0.75), prefix + newRate + "Hz");
        }
    };
    public static final GameAction ACTION_SIMULATION_SLOWEST = new GameAction("SIMULATION_SLOWEST") {
        @Override
        public void execute(GameUI ui) {
            ui.clock().setTargetFrameRate(SIMULATION_SPEED_MIN);
            ui.showFlashMessage(Duration.seconds(0.75), "At minmimum speed: %d Hz", SIMULATION_SPEED_MIN);
        }
    };

    public static final GameAction ACTION_SIMULATION_ONE_STEP = new GameAction("SIMULATION_ONE_STEP") {
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

    public static final GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction("SIMULATION_TEN_STEPS") {
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

    public static final GameAction ACTION_SIMULATION_RESET = new GameAction("SIMULATION_RESET") {
        @Override
        public void execute(GameUI ui) {
            ui.clock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            ui.showFlashMessage(Duration.seconds(0.75), ui.clock().targetFrameRate() + "Hz");
        }
    };

    public static final GameAction ACTION_STEER_UP = new SteeringAction(Direction.UP);
    public static final GameAction ACTION_STEER_DOWN = new SteeringAction(Direction.DOWN);
    public static final GameAction ACTION_STEER_LEFT = new SteeringAction(Direction.LEFT);
    public static final GameAction ACTION_STEER_RIGHT = new SteeringAction(Direction.RIGHT);

    public static final GameAction ACTION_TOGGLE_COLLISION_STRATEGY = new GameAction("TOGGLE_COLLISION_STRATEGY") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            CollisionStrategy collisionStrategy = game.collisionStrategy();
            if (collisionStrategy == CollisionStrategy.CENTER_DISTANCE) {
                game.setCollisionStrategy(CollisionStrategy.SAME_TILE);
            } else {
                game.setCollisionStrategy(CollisionStrategy.CENTER_DISTANCE);
            }
            if (game.collisionStrategy() == CollisionStrategy.SAME_TILE) {
                ui.showFlashMessage("Using original Arcade collision strategy"); //TODO localize
            } else {
                ui.showFlashMessage("Using improved collision strategy"); //TODO localize
            }
        }
    };

    public static final GameAction ACTION_TOGGLE_DASHBOARD = new GameAction("TOGGLE_DASHBOARD") {
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

    public static final GameAction ACTION_TOGGLE_DEBUG_INFO = new GameAction("TOGGLE_DEBUG_INFO") {
        @Override
        public void execute(GameUI ui) {
            toggle(PROPERTY_DEBUG_INFO_VISIBLE);
        }
    };

    public static final GameAction ACTION_TOGGLE_DRAW_MODE = new GameAction("TOGGLE_DRAW_MODE") {
        @Override
        public void execute(GameUI ui) {
            PROPERTY_3D_DRAW_MODE.set(PROPERTY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }

        @Override
        public String name() {
            return "TOGGLE_DRAW_MODE";
        }
    };

    public static final GameAction ACTION_TOGGLE_MINI_VIEW_VISIBILITY = new GameAction("TOGGLE_MINI_VIEW_VISIBILITY") {
        @Override
        public void execute(GameUI ui) {
            toggle(PROPERTY_MINI_VIEW_ON);
            if (!ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)) {
                ui.showFlashMessage(ui.assets().translated(PROPERTY_MINI_VIEW_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    };

    public static final GameAction ACTION_TOGGLE_MUTED = new GameAction("TOGGLE_MUTED") {
        @Override
        public void execute(GameUI ui) {
            PROPERTY_MUTED.set(!PROPERTY_MUTED.get());
        }
    };

    public static final GameAction ACTION_TOGGLE_PAUSED = new GameAction("TOGGLE_PAUSED") {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.clock().pausedProperty());
            if (ui.clock().isPaused()) {
                ui.soundManager().stopAll();
            }
            Logger.info("Game ({}) {}", ui.context().gameVariantName(), ui.clock().isPaused() ? "paused" : "resumed");
        }
    };

    public static final GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction("TOGGLE_PLAY_SCENE_2D_3D") {
        @Override
        public void execute(GameUI ui) {
            ui.currentGameScene().ifPresent(gameScene -> {
                toggle(PROPERTY_3D_ENABLED);
                if (ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D) || ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)) {
                    ui.updateGameScene(true);
                    ui.context().currentGame().gameControl().update(); //TODO needed?
                }
                if (!ui.context().currentGame().isPlaying()) {
                    ui.showFlashMessage(ui.assets().translated(PROPERTY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            FsmState<GameContext> state = ui.context().currentGame().state();
            if (state.name().equals(LevelShortTestState.class.getSimpleName())
                || state.name().equals(LevelMediumTestState.class.getSimpleName())) {
                return true;
            }
            return Set.of("BOOT", "INTRO", "SETTING_OPTIONS_FOR_START", "HUNTING").contains(state.name());
        }
    };
}