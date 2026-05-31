/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.Globals.NUM_TICKS_PER_SEC;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

/**
 * Common actions for all game variants.
 * <p>
 * For each action there must exist an entry in the {@code localized_texts} resource bundle
 * of the form {@code key=localized_action_name} where {@code key=action.name()} !
 */
public final class CommonActions {

    public static final int SIM_SPEED_DELTA = 2;
    public static final int SIM_SPEED_MIN = 5;
    public static final int SIM_SPEED_MAX = 300;

    public static final GameAction ACTION_BOOT_SHOW_PLAY_VIEW = new GameAction("boot_show_play_view") {
        @Override
        protected void doAction(GameUI ui) {
            ui.services().gameContext().coinMechanism().setNumCoins(0);
            ui.services().selectGamePlayView();
            ui.restart();
        }
    };

    public static final GameAction ACTION_ENTER_FULLSCREEN = new GameAction("enter_fullscreen") {
        @Override
        protected void doAction(GameUI ui) {
            ui.view().stage().setFullScreen(true);
        }
    };

    public static final GameAction ACTION_LET_GAME_STATE_EXPIRE = new GameAction("let_game_state_expire") {
        @Override
        protected void doAction(GameUI ui) {
            ui.services().gameContext().game().flow().state().expire();
        }
    };

    public static final GameAction ACTION_OPEN_EDITOR = new GameAction("open_editor") {
        @Override
        protected void doAction(GameUI ui) {
            ui.openWorldMapFileInEditor(null);
        }
    };

    public static final GameAction ACTION_PERSPECTIVE_NEXT = new GameAction("perspective_next") {
        @Override
        protected void doAction(GameUI ui) {
            PerspectiveID id = GameUI_Constants.PROPERTY_3D_PERSPECTIVE_ID.get().next();
            GameUI_Constants.PROPERTY_3D_PERSPECTIVE_ID.set(id);
            String msgKey = ui.services().translations().translate("camera_perspective",
                ui.services().translations().translate("perspective_id_" + id.name()));
            ui.services().showFlashMessage(msgKey);
        }
    };

    public static final GameAction ACTION_PERSPECTIVE_PREVIOUS = new GameAction("perspective_previous") {
        @Override
        protected void doAction(GameUI ui) {
            PerspectiveID id = GameUI_Constants.PROPERTY_3D_PERSPECTIVE_ID.get().prev();
            GameUI_Constants.PROPERTY_3D_PERSPECTIVE_ID.set(id);
            String msgKey = ui.services().translations().translate("camera_perspective",
                ui.services().translations().translate("perspective_id_" + id.name()));
            ui.services().showFlashMessage(msgKey);
        }
    };

    public static final GameAction ACTION_QUIT_GAME_SCENE = new GameAction("quit_game_scene") {
        @Override
        protected void doAction(GameUI ui) {
            final Game game = ui.services().gameContext().game();
            game.cheats().clear(); //TODO needed?
            ui.services().gameScenes().quitCurrentGameScene(ui);
        }
    };

    public static final GameAction ACTION_RESTART_INTRO = new GameAction("restart_intro") {
        @Override
        protected void doAction(GameUI ui) {
            ui.stopGame();
            final Game game = ui.services().gameContext().game();
            boolean isLevelShortTest = game.flow().state() instanceof LevelShortTestState;
            if (isLevelShortTest) {
                game.flow().state().onExit(game); //TODO exit other states too?
            }
            game.flow().restartStateWithName(CanonicalGameState.INTRO.name());
            ui.services().gameClock().start();
        }
    };

    public static final GameAction ACTION_SHOW_HELP = new GameAction("show_help") {
        @Override
        protected void doAction(GameUI ui) {
            ui.services().gamePlayView().showHelp(ui);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            boolean isArcadeGame = GameVariant.isArcadeGameName(ui.services().gameContext().gameVariantName());
            boolean isPlayScene2D = ui.services().gameScenes().currentGameSceneHasID(ui, CommonSceneID.PLAY_SCENE_2D);
            return isArcadeGame && isPlayScene2D;
        }
    };

    public static final GameAction ACTION_SIMULATION_FASTER = new GameAction("simulation_faster") {
        @Override
        protected void doAction(GameUI ui) {
            final GameClock clock = ui.services().gameClock();
            final int newRate = Math.clamp(clock.targetFrameRate() + SIM_SPEED_DELTA, SIM_SPEED_MIN, SIM_SPEED_MAX);
            clock.setTargetFrameRate(newRate);

            final String message = newRate == SIM_SPEED_MAX ? "At maximum speed: %d Hz" : "%d Hz";
            ui.services().showFlashMessage(Duration.seconds(0.75), message.formatted(newRate));
        }
    };

    public static final GameAction ACTION_SIMULATION_FASTEST = new GameAction("simulation_fastest") {
        @Override
        protected void doAction(GameUI ui) {
            ui.services().gameClock().setTargetFrameRate(SIM_SPEED_MAX);
            ui.services().showFlashMessage(Duration.seconds(0.75), "At maximum speed: %d Hz", SIM_SPEED_MAX);
        }
    };

    public static final GameAction ACTION_SIMULATION_SLOWER = new GameAction("simulation_slower") {
        @Override
        protected void doAction(GameUI ui) {
            final GameClock clock = ui.services().gameClock();
            final int newRate = Math.clamp(clock.targetFrameRate() - SIM_SPEED_DELTA, SIM_SPEED_MIN, SIM_SPEED_MAX);
            clock.setTargetFrameRate(newRate);

            final String message = newRate == SIM_SPEED_MIN ? "At minimum speed: %d Hz" : "%d Hz";
            ui.services().showFlashMessage(Duration.seconds(0.75), message.formatted(newRate));
        }
    };

    public static final GameAction ACTION_SIMULATION_SLOWEST = new GameAction("simulation_slowest") {
        @Override
        protected void doAction(GameUI ui) {
            ui.services().gameClock().setTargetFrameRate(SIM_SPEED_MIN);
            ui.services().showFlashMessage(Duration.seconds(0.75), "At minimum speed: %d Hz", SIM_SPEED_MIN);
        }
    };

    public static final GameAction ACTION_SIMULATION_ONE_STEP = new GameAction("simulation_one_step") {
        @Override
        protected void doAction(GameUI ui) {
            boolean success = ui.services().gameClock().makeOneStep(true);
            if (!success) {
                ui.services().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.services().gameClock().getUpdatesDisabled(); }
    };

    public static final GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction("simulation_ten_steps") {
        @Override
        protected void doAction(GameUI ui) {
            boolean success = ui.services().gameClock().makeSteps(10, true);
            if (!success) {
                ui.services().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.services().gameClock().getUpdatesDisabled(); }
     };

    public static final GameAction ACTION_SIMULATION_RESET = new GameAction("simulation_reset") {
        @Override
        protected void doAction(GameUI ui) {
            ui.services().gameClock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            ui.services().showFlashMessage(Duration.seconds(0.75), ui.services().gameClock().targetFrameRate() + "Hz");
        }
    };

    public static final GameAction ACTION_STEER_UP = new SteeringAction(Direction.UP);
    public static final GameAction ACTION_STEER_DOWN = new SteeringAction(Direction.DOWN);
    public static final GameAction ACTION_STEER_LEFT = new SteeringAction(Direction.LEFT);
    public static final GameAction ACTION_STEER_RIGHT = new SteeringAction(Direction.RIGHT);

    public static final GameAction ACTION_TOGGLE_COLLISION_STRATEGY = new GameAction("toggle_collision_strategy") {
        @Override
        protected void doAction(GameUI ui) {
            final Game game = ui.services().gameContext().game();
            CollisionStrategy collisionStrategy = game.collisionStrategy();
            if (collisionStrategy == CollisionStrategy.CENTER_DISTANCE) {
                game.setCollisionStrategy(CollisionStrategy.SAME_TILE);
            } else {
                game.setCollisionStrategy(CollisionStrategy.CENTER_DISTANCE);
            }
            if (game.collisionStrategy() == CollisionStrategy.SAME_TILE) {
                ui.services().showFlashMessage("Using original Arcade collision strategy"); //TODO localize
            } else {
                ui.services().showFlashMessage("Using improved collision strategy"); //TODO localize
            }
        }
    };

    public static final GameAction ACTION_TOGGLE_DASHBOARD = new GameAction("toggle_dashboard") {
        @Override
        protected void doAction(GameUI ui) {
            ui.services().dashboard().toggleVisibility();
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.services().subViews().isPlayViewSelected();
        }
    };

    public static final GameAction ACTION_TOGGLE_DEBUG_INFO = new GameAction("toggle_debug_info") {
        @Override
        protected void doAction(GameUI ui) {
            toggleBooleanProperty(GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE);
        }
    };

    public static final GameAction ACTION_TOGGLE_DRAW_MODE = new GameAction("toggle_draw_mode") {
        @Override
        protected void doAction(GameUI ui) {
            GameUI_Constants.PROPERTY_3D_DRAW_MODE.set(GameUI_Constants.PROPERTY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    };

    public static final GameAction ACTION_TOGGLE_KEYBOARD_MONITOR = new GameAction("toggle_keyboard_monitor") {
        @Override
        protected void doAction(GameUI ui) {
            toggleBooleanProperty(GameUI_Constants.PROPERTY_KEYBOARD_MONITOR_VISIBLE);
        }
    };

    public static final GameAction ACTION_TOGGLE_MINI_VIEW_VISIBILITY = new GameAction("toggle_mini_view_visibility") {
        @Override
        protected void doAction(GameUI ui) {
            toggleBooleanProperty(GameUI_Constants.PROPERTY_MINI_VIEW_ON);
            if (!ui.services().gameScenes().currentGameSceneHasID(ui, CommonSceneID.PLAY_SCENE_3D)) {
                ui.services().showFlashMessage(ui.services().translations().translate(GameUI_Constants.PROPERTY_MINI_VIEW_ON.get()
                    ? "pip_on" : "pip_off"));
            }
        }
    };

    public static final GameAction ACTION_TOGGLE_MUTED = new GameAction("toggle_muted") {
        @Override
        protected void doAction(GameUI ui) {
            GameUI_Constants.PROPERTY_MUTED.set(!GameUI_Constants.PROPERTY_MUTED.get());
        }
    };

    public static final GameAction ACTION_TOGGLE_PAUSED = new GameAction("toggle_paused") {
        @Override
        protected void doAction(GameUI ui) {
            toggleBooleanProperty(ui.services().gameClock().updatesDisabledProperty());
            if (ui.services().gameClock().getUpdatesDisabled()) {
                final UIConfig currentConfig = ui.services().configurations().getOrCreateUIConfig(ui.services().gameContext().gameVariantName());
                ui.services().sounds().stopAll();
                currentConfig.optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            }
            Logger.info("Game ({}) {}", ui.services().gameContext().gameVariantName(), ui.services().gameClock().getUpdatesDisabled() ? "paused" : "resumed");
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.services().subViews().isPlayViewSelected();
        }
    };

    public static final GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction("toggle_play_scene_2d_3d") {
        @Override
        protected void doAction(GameUI ui) {
            final Game game = ui.services().gameContext().game();
            toggleBooleanProperty(GameUI_Constants.PROPERTY_3D_ENABLED);
            final boolean is3DEnabled = GameUI_Constants.PROPERTY_3D_ENABLED.get();
            if (!inPlayScene(ui)) {
                ui.services().showFlashMessage(ui.services().translations().translate(is3DEnabled ? "use_3D_scene" : "use_2D_scene"));
            }
            if (isLevelPlaying(game)) {
                ui.services().gameScenes().forceGameSceneUpdate(ui);
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.services().subViews().isPlayViewSelected();
        }

        private boolean inPlayScene(GameUI ui) {
            return ui.services().gameScenes().currentGameSceneHasID(ui, CommonSceneID.PLAY_SCENE_2D)
                || ui.services().gameScenes().currentGameSceneHasID(ui, CommonSceneID.PLAY_SCENE_3D);
        }

        private boolean isLevelPlaying(Game game) {
            return game.flow().state().matchesByName(CanonicalGameState.LEVEL_PLAYING.name());
        }
    };
}