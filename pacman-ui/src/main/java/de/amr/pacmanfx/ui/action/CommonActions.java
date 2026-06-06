/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.basics.fsm.State;
import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.app.AppConstants;
import de.amr.pacmanfx.ui.app.AppContext;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.subviews.editor.EditorView;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import static de.amr.pacmanfx.core.Globals.NUM_TICKS_PER_SEC;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

/**
 * Common actions for all game variants.
 * <p>
 * For each action there must exist an entry in the {@code localized_texts} resource bundle
 * of the form {@code key=localized_action_name} where {@code key=action.name()} !
 */
public final class CommonActions {

    public static void editMapFile(AppContext context, File worldMapFile) {
        createEditMapFileAction(worldMapFile).executeIfEnabled(context);
    }

    public static GameAction createEditMapFileAction(File worldMapFile) {
        return new GameAction("edit_map_file") {
            @Override
            protected void doAction(AppContext context) {

                final SubViewManager subViews = context.ui().subViews();
                subViews.ensureEditorViewCreated();
                subViews.optEditorView().map(EditorView::editor).ifPresent(editor -> {
                    editor.init(context.customMapDir());
                    try {
                        if (subViews.trySelectEditorView()) {
                            editor.start();
                            if (worldMapFile != null) {
                                editor.editFile(worldMapFile);
                            }
                            context.stopGame();
                        }
                    } catch (IOException x) {
                        Logger.error(x, "Could not open map file {}", worldMapFile);
                        context.shortMessage("Cannot open world map file");
                    }
                    catch (WorldMapParseException x) {
                        Logger.error(x, "Error reading map file data from {}", worldMapFile);
                        context.shortMessage("Cannot read world map file data");
                    }
                });
            }
        };
    }

    public static final int SIM_SPEED_DELTA = 2;
    public static final int SIM_SPEED_MIN = 5;
    public static final int SIM_SPEED_MAX = 300;

    public static final GameAction ACTION_BOOT_SHOW_PLAY_VIEW = new GameAction("boot_show_play_view") {
        @Override
        protected void doAction(AppContext context) {
            context.coinMechanism().setNumCoins(0);
            context.ui().subViews().selectGamePlayView();
            context.startGame();
        }
    };

    public static final GameAction ACTION_ENTER_FULLSCREEN = new GameAction("enter_fullscreen") {
        @Override
        protected void doAction(AppContext context) {
            context.ui().view().stage().setFullScreen(true);
        }
    };

    public static final GameAction ACTION_LET_GAME_STATE_EXPIRE = new GameAction("let_game_state_expire") {
        @Override
        protected void doAction(AppContext context) {
            context.currentGameContext().gameState().expire();
        }
    };

    public static final GameAction ACTION_OPEN_EDITOR = new GameAction("open_editor") {
        @Override
        protected void doAction(AppContext context) {
            context.stopGame();

            final SubViewManager subViews = context.ui().subViews();
            subViews.ensureEditorViewCreated();
            subViews.trySelectEditorView();
            subViews.optEditorView().map(EditorView::editor).ifPresent(editor -> {
                editor.init(context.customMapDir());
                editor.start();
            });

        }
    };

    public static final GameAction ACTION_PERSPECTIVE_NEXT = new GameAction("perspective_next") {
        @Override
        protected void doAction(AppContext context) {
            PerspectiveID id = AppConstants.PROPERTY_3D_PERSPECTIVE_ID.get().next();
            AppConstants.PROPERTY_3D_PERSPECTIVE_ID.set(id);
            String msgKey = context.ui().translations().translate("camera_perspective",
                context.ui().translations().translate("perspective_id_" + id.name()));
            context.shortMessage(msgKey);
        }
    };

    public static final GameAction ACTION_PERSPECTIVE_PREVIOUS = new GameAction("perspective_previous") {
        @Override
        protected void doAction(AppContext context) {
            PerspectiveID id = AppConstants.PROPERTY_3D_PERSPECTIVE_ID.get().prev();
            AppConstants.PROPERTY_3D_PERSPECTIVE_ID.set(id);
            String msgKey = context.ui().translations().translate("camera_perspective",
                context.ui().translations().translate("perspective_id_" + id.name()));
            context.shortMessage(msgKey);
        }
    };

    public static final GameAction ACTION_QUIT_GAME_SCENE = new GameAction("quit_game_scene") {
        @Override
        protected void doAction(AppContext context) {
            final GameModel game = context.currentGameContext().gameModel();
            game.cheats().clear(); //TODO needed?
            context.stopGame();
            context.ui().gameScenes().quitCurrentGameScene(context);
            context.ui().subViews().selectStartView();
        }
    };

    public static final GameAction ACTION_RESTART_INTRO = new GameAction("restart_intro") {
        @Override
        protected void doAction(AppContext appContext) {
            //TODO check this code
            appContext.stopGame();

            final GameContext gameContext = appContext.currentGameContext();
            final State<GameContext> gameState = gameContext.gameState();
            boolean isLevelShortTest = gameState instanceof LevelShortTestState;
            if (isLevelShortTest) {
                gameState.onExit(appContext.currentGameContext()); //TODO exit other states too?
            }
            gameContext.gameFlow().restartState(GameStateID.GAME_INTRO.name());
            appContext.gameClock().start();
        }
    };

    public static final GameAction ACTION_SHOW_HELP = new GameAction("show_help") {
        @Override
        protected void doAction(AppContext context) {
            context.ui().subViews().gamePlayView().showHelp(context);
        }

        @Override
        public boolean isEnabled(AppContext context) {
            boolean isArcadeGame = GameVariant.isArcadeGameName(context.currentGameVariantName());
            boolean isPlayScene2D = context.ui().gameScenes().currentGameSceneHasID(context, CommonSceneID.PLAY_SCENE_2D);
            return isArcadeGame && isPlayScene2D;
        }
    };

    public static final GameAction ACTION_SIMULATION_FASTER = new GameAction("simulation_faster") {
        @Override
        protected void doAction(AppContext context) {
            final GameClock clock = context.gameClock();
            final int newRate = Math.clamp(clock.targetFrameRate() + SIM_SPEED_DELTA, SIM_SPEED_MIN, SIM_SPEED_MAX);
            clock.setTargetFrameRate(newRate);

            final String message = newRate == SIM_SPEED_MAX ? "At maximum speed: %d Hz" : "%d Hz";
            context.shortMessage(Duration.seconds(0.75), message.formatted(newRate));
        }
    };

    public static final GameAction ACTION_SIMULATION_FASTEST = new GameAction("simulation_fastest") {
        @Override
        protected void doAction(AppContext context) {
            context.gameClock().setTargetFrameRate(SIM_SPEED_MAX);
            context.shortMessage(Duration.seconds(0.75), "At maximum speed: %d Hz", SIM_SPEED_MAX);
        }
    };

    public static final GameAction ACTION_SIMULATION_SLOWER = new GameAction("simulation_slower") {
        @Override
        protected void doAction(AppContext context) {
            final GameClock clock = context.gameClock();
            final int newRate = Math.clamp(clock.targetFrameRate() - SIM_SPEED_DELTA, SIM_SPEED_MIN, SIM_SPEED_MAX);
            clock.setTargetFrameRate(newRate);

            final String message = newRate == SIM_SPEED_MIN ? "At minimum speed: %d Hz" : "%d Hz";
            context.shortMessage(Duration.seconds(0.75), message.formatted(newRate));
        }
    };

    public static final GameAction ACTION_SIMULATION_SLOWEST = new GameAction("simulation_slowest") {
        @Override
        protected void doAction(AppContext context) {
            context.gameClock().setTargetFrameRate(SIM_SPEED_MIN);
            context.shortMessage(Duration.seconds(0.75), "At minimum speed: %d Hz", SIM_SPEED_MIN);
        }
    };

    public static final GameAction ACTION_SIMULATION_ONE_STEP = new GameAction("simulation_one_step") {
        @Override
        protected void doAction(AppContext context) {
            boolean success = context.gameClock().makeOneStep(true);
            if (!success) {
                context.shortMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(AppContext context) { return context.gameClock().getUpdatesDisabled(); }
    };

    public static final GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction("simulation_ten_steps") {
        @Override
        protected void doAction(AppContext context) {
            boolean success = context.gameClock().makeSteps(10, true);
            if (!success) {
                context.shortMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled(AppContext context) { return context.gameClock().getUpdatesDisabled(); }
     };

    public static final GameAction ACTION_SIMULATION_RESET = new GameAction("simulation_reset") {
        @Override
        protected void doAction(AppContext context) {
            context.gameClock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            context.shortMessage(Duration.seconds(0.75), context.gameClock().targetFrameRate() + "Hz");
        }
    };

    public static final GameAction ACTION_STEER_UP = new SteeringAction(Direction.UP);
    public static final GameAction ACTION_STEER_DOWN = new SteeringAction(Direction.DOWN);
    public static final GameAction ACTION_STEER_LEFT = new SteeringAction(Direction.LEFT);
    public static final GameAction ACTION_STEER_RIGHT = new SteeringAction(Direction.RIGHT);

    public static final GameAction ACTION_TOGGLE_COLLISION_STRATEGY = new GameAction("toggle_collision_strategy") {
        @Override
        protected void doAction(AppContext appContext) {
            final CollisionStrategy strategy = appContext.currentGameContext().collisionStrategy();
            final CollisionStrategy newStrategy = strategy == CollisionStrategy.CENTER_DISTANCE
                ? CollisionStrategy.SAME_TILE : CollisionStrategy.CENTER_DISTANCE;
            appContext.setCollisionStrategy(newStrategy);
            if (newStrategy == CollisionStrategy.SAME_TILE) {
                appContext.shortMessage("Using original Arcade collision strategy (same tile check)"); //TODO localize
            } else {
                appContext.shortMessage("Using fail-safe collision strategy"); //TODO localize
            }
        }
    };

    public static final GameAction ACTION_TOGGLE_DASHBOARD = new GameAction("toggle_dashboard") {
        @Override
        protected void doAction(AppContext context) {
            context.ui().subViews().gamePlayView().dashboard().toggleVisibility();
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return context.ui().subViews().isSelected(context.ui().subViews().gamePlayView());
        }
    };

    public static final GameAction ACTION_TOGGLE_DEBUG_INFO = new GameAction("toggle_debug_info") {
        @Override
        protected void doAction(AppContext context) {
            toggleBooleanProperty(AppConstants.PROPERTY_DEBUG_INFO_VISIBLE);
        }
    };

    public static final GameAction ACTION_TOGGLE_DRAW_MODE = new GameAction("toggle_draw_mode") {
        @Override
        protected void doAction(AppContext context) {
            AppConstants.PROPERTY_3D_DRAW_MODE.set(AppConstants.PROPERTY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    };

    public static final GameAction ACTION_TOGGLE_KEYBOARD_MONITOR = new GameAction("toggle_keyboard_monitor") {
        @Override
        protected void doAction(AppContext context) {
            toggleBooleanProperty(AppConstants.PROPERTY_KEYBOARD_MONITOR_VISIBLE);
        }
    };

    public static final GameAction ACTION_TOGGLE_MINI_VIEW_VISIBILITY = new GameAction("toggle_mini_view_visibility") {
        @Override
        protected void doAction(AppContext context) {
            toggleBooleanProperty(AppConstants.PROPERTY_MINI_VIEW_ON);
            if (!context.ui().gameScenes().currentGameSceneHasID(context, CommonSceneID.PLAY_SCENE_3D)) {
                context.shortMessage(context.ui().translations().translate(AppConstants.PROPERTY_MINI_VIEW_ON.get()
                    ? "pip_on" : "pip_off"));
            }
        }
    };

    public static final GameAction ACTION_TOGGLE_MUTED = new GameAction("toggle_muted") {
        @Override
        protected void doAction(AppContext context) {
            AppConstants.PROPERTY_MUTED.set(!AppConstants.PROPERTY_MUTED.get());
        }
    };

    public static final GameAction ACTION_TOGGLE_PAUSED = new GameAction("toggle_paused") {
        @Override
        protected void doAction(AppContext context) {
            toggleBooleanProperty(context.gameClock().updatesDisabledProperty());
            if (context.gameClock().getUpdatesDisabled()) {
                final UIConfig currentConfig = context.currentUIConfig();
                context.ui().sounds().stopAll();
                currentConfig.optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            }
            Logger.info("Game ({}) {}", context.currentGameVariantName(), context.gameClock().getUpdatesDisabled() ? "paused" : "resumed");
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return context.ui().subViews().isSelected(context.ui().subViews().gamePlayView());
        }
    };

    public static final GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction("toggle_play_scene_2d_3d") {
        @Override
        protected void doAction(AppContext context) {
            toggleBooleanProperty(AppConstants.PROPERTY_3D_ENABLED);
            final boolean is3DEnabled = AppConstants.PROPERTY_3D_ENABLED.get();
            if (!inPlayScene(context)) {
                context.shortMessage(context.ui().translations().translate(is3DEnabled ? "use_3D_scene" : "use_2D_scene"));
            }
            if (isLevelPlaying(context)) {
                context.ui().gameScenes().forceGameSceneUpdate(context);
            }
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return context.ui().subViews().isSelected(context.ui().subViews().gamePlayView());
        }

        private boolean inPlayScene(AppContext context) {
            return context.ui().gameScenes().currentGameSceneHasID(context, CommonSceneID.PLAY_SCENE_2D)
                || context.ui().gameScenes().currentGameSceneHasID(context, CommonSceneID.PLAY_SCENE_3D);
        }

        private boolean isLevelPlaying(AppContext context) {
            return context.currentGameContext().gameState().nameIsOneOf(GameStateID.GAME_LEVEL_PLAYING.name());
        }
    };
}