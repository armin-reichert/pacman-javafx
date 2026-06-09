/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameConstants;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.subviews.editor.EditorView;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.util.Optional;

import static de.amr.pacmanfx.core.Globals.NUM_TICKS_PER_SEC;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

/**
 * Common actions for all game variants.
 * <p>
 * For each action, there must exist a line of the form <pre>{@code action.<actionID>=localized text}</pre>
 * in the global UI resource bundle.
 */
public final class CommonActions {

    // Pac-Man steering actions

    public static final GameAction ACTION_STEER_UP = new SteeringAction(Direction.UP);

    public static final GameAction ACTION_STEER_DOWN = new SteeringAction(Direction.DOWN);

    public static final GameAction ACTION_STEER_LEFT = new SteeringAction(Direction.LEFT);

    public static final GameAction ACTION_STEER_RIGHT = new SteeringAction(Direction.RIGHT);


    // Map editor actions

    /**
     * @param mapFile map file to edit or {@code null}
     * @return action which opens the map editor and edits the given map file if any
     */
    public static GameAction createEditMapFileAction(File mapFile) {

        return new GameAction("edit_map_file") {

            @Override
            protected void doAction(Game game) {
                openMapEditor(game).ifPresent(editor -> {
                    startEditor(game, editor);
                    if (mapFile != null) {
                        try {
                            editor.editFile(mapFile);
                        } catch (Exception x) {
                            game.shortMessage("Cannot edit map file");
                            Logger.error(x, "Cannot edit map file {}", mapFile);
                        }
                    }
                });
            }
        };
    }

    public static final GameAction ACTION_OPEN_EDITOR = new GameAction("open_editor") {

        @Override
        protected void doAction(Game game) {
            openMapEditor(game).ifPresent(editor -> startEditor(game, editor));
        }
    };

    private static void startEditor(Game game, TileMapEditor editor) {
        game.stopGame();
        editor.init(GameConstants.CUSTOM_MAP_DIR);
        editor.start();
    }

    private static Optional<TileMapEditor> openMapEditor(Game game) {
        final SubViewManager subViews = game.ui().subViews();
        subViews.ensureEditorViewCreated();

        final TileMapEditor editor = subViews.optEditorView().map(EditorView::editor).orElse(null);
        if (editor == null) {
            game.shortMessage("Cannot access the map editor.");
            return Optional.empty();
        }

        if (!subViews.trySelectEditorView()) {
            game.shortMessage("Cannot open the map editor.");
            return Optional.empty();
        }

        return Optional.of(editor);
    }

    // Other actions

    public static final GameAction ACTION_BOOT_SHOW_PLAY_VIEW = new GameAction("boot_show_play_view") {

        @Override
        protected void doAction(Game game) {
            game.coinMechanism().setNumCoins(0);
            game.startGame();
            game.ui().subViews().selectGamePlayView();
        }
    };

    public static final GameAction ACTION_ENTER_FULLSCREEN = new GameAction("enter_fullscreen") {

        @Override
        protected void doAction(Game game) {
            game.ui().view().stage().setFullScreen(true);
        }
    };

    public static final GameAction ACTION_LET_GAME_STATE_EXPIRE = new GameAction("let_game_state_expire") {

        @Override
        protected void doAction(Game game) {
            game.currentGameContext().state().expire();
        }
    };

    public static final GameAction ACTION_PERSPECTIVE_NEXT = new GameAction("perspective_next") {

        @Override
        protected void doAction(Game game) {
            final PerspectiveID nextID = GameConstants.PROPERTY_3D_PERSPECTIVE_ID.get().next();
            GameConstants.PROPERTY_3D_PERSPECTIVE_ID.set(nextID);

            final TranslationManager translations = game.ui().translations();
            final String msgKey = translations.translate(
                "camera_perspective",
                translations.translate("perspective_id_" + nextID.name())
            );
            game.shortMessage(msgKey);
        }
    };

    public static final GameAction ACTION_PERSPECTIVE_PREVIOUS = new GameAction("perspective_previous") {

        @Override
        protected void doAction(Game game) {
            final PerspectiveID prevID = GameConstants.PROPERTY_3D_PERSPECTIVE_ID.get().prev();
            GameConstants.PROPERTY_3D_PERSPECTIVE_ID.set(prevID);

            final TranslationManager translations = game.ui().translations();
            final String msgKey = translations.translate(
                "camera_perspective",
                translations.translate("perspective_id_" + prevID.name())
            );
            game.shortMessage(msgKey);
        }
    };

    public static final GameAction ACTION_QUIT_GAME_SCENE = new GameAction("quit_game_scene") {

        @Override
        protected void doAction(Game game) {
            final GameModel gameModel = game.currentGameContext().model();

            game.stopGame();
            gameModel.cheats().clear(); //TODO needed?
            game.ui().gameScenes().quitCurrentGameScene(game);
            game.ui().subViews().selectStartView();
        }
    };

    //TODO check this code
    public static final GameAction ACTION_RESTART_INTRO = new GameAction("restart_intro") {

        @Override
        protected void doAction(Game game) {
            final GameContext gameContext = game.currentGameContext();
            final GameState gameState = gameContext.state();

            if (gameState instanceof TestState) {
                gameState.onExit(gameContext);
            }

            game.stopGame();
            game.clock().start();
            gameContext.flow().restartState(GameStateID.GAME_INTRO);
        }
    };

    public static final GameAction ACTION_SHOW_HELP = new GameAction("show_help") {

        @Override
        protected void doAction(Game game) {
            game.ui().subViews().gamePlayView().showHelp(game);
        }

        @Override
        public boolean isEnabled(Game game) {
            final GameSceneManager gameScenes = game.ui().gameScenes();
            final String variantName = game.currentGameVariantName();
            final boolean isArcadeGame = GameVariant.isArcadeGameName(variantName);
            return isArcadeGame &&
                  (gameScenes.currentGameSceneHasID(game, CommonSceneID.INTRO_SCENE)
                || gameScenes.currentGameSceneHasID(game, CommonSceneID.START_SCENE)
                || gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D));
        }
    };

    //TODO localize message
    public static final GameAction ACTION_SIMULATION_FASTER = new GameAction("simulation_faster") {

        @Override
        protected void doAction(Game game) {
            final GameClock clock = game.clock();
            final int newRate = Math.clamp(clock.targetFrameRate() + GameConstants.SIM_SPEED_DELTA,
                GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
            clock.setTargetFrameRate(newRate);

            final String msg = newRate == GameConstants.SIM_SPEED_MAX ? "At maximum speed: %d Hz" : "%d Hz";
            game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
        }
    };

    //TODO localize message
    public static final GameAction ACTION_SIMULATION_FASTEST = new GameAction("simulation_fastest") {

        @Override
        protected void doAction(Game game) {
            game.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MAX);
            final String msg = "At maximum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MAX);
            game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
        }
    };

    //TODO localize message
    public static final GameAction ACTION_SIMULATION_SLOWER = new GameAction("simulation_slower") {

        @Override
        protected void doAction(Game game) {
            final GameClock clock = game.clock();
            final int newRate = Math.clamp(clock.targetFrameRate() - GameConstants.SIM_SPEED_DELTA,
                GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
            clock.setTargetFrameRate(newRate);

            final String msg = newRate == GameConstants.SIM_SPEED_MIN ? "At minimum speed: %d Hz" : "%d Hz";
            game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
        }
    };

    //TODO localize message
    public static final GameAction ACTION_SIMULATION_SLOWEST = new GameAction("simulation_slowest") {

        @Override
        protected void doAction(Game game) {
            game.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MIN);
            final String msg = "At minimum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MIN);
            game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
        }
    };

    public static final GameAction ACTION_SIMULATION_ONE_STEP = new GameAction("simulation_one_step") {

        @Override
        protected void doAction(Game game) {
            final boolean failure = !game.clock().makeOneStep(true);
            if (failure) {
                game.shortMessage("Simulation step error!");
            }
        }

        @Override
        public boolean isEnabled(Game game) { return game.clock().getUpdatesDisabled(); }
    };

    public static final GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction("simulation_ten_steps") {

        @Override
        protected void doAction(Game game) {
            final boolean failure = !game.clock().makeSteps(10, true);
            if (failure) {
                game.shortMessage("Simulation steps error!");
            }
        }

        @Override
        public boolean isEnabled(Game game) { return game.clock().getUpdatesDisabled(); }
     };

    public static final GameAction ACTION_SIMULATION_RESET = new GameAction("simulation_reset") {

        @Override
        protected void doAction(Game game) {
            game.clock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), game.clock().targetFrameRate() + "Hz");
        }
    };

    //TODO localize message
    public static final GameAction ACTION_TOGGLE_COLLISION_STRATEGY = new GameAction("toggle_collision_strategy") {

        @Override
        protected void doAction(Game game) {
            final CollisionStrategy strategy = game.currentGameContext().collisionStrategy();
            final CollisionStrategy newStrategy = strategy == CollisionStrategy.CENTER_DISTANCE
                ? CollisionStrategy.SAME_TILE : CollisionStrategy.CENTER_DISTANCE;

            game.setCollisionStrategy(newStrategy);

            if (newStrategy == CollisionStrategy.SAME_TILE) {
                game.shortMessage("Using original Arcade collision strategy (same tile check)");
            } else {
                game.shortMessage("Using fail-safe collision strategy");
            }
        }
    };

    public static final GameAction ACTION_TOGGLE_DASHBOARD = new GameAction("toggle_dashboard") {

        @Override
        protected void doAction(Game game) {
            game.ui().subViews().gamePlayView().dashboard().toggleVisibility();
        }

        @Override
        public boolean isEnabled(Game game) {
            final SubViewManager subViews = game.ui().subViews();
            return subViews.isSelected(subViews.gamePlayView());
        }
    };

    public static final GameAction ACTION_TOGGLE_DEBUG_INFO = new GameAction("toggle_debug_info") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(GameConstants.PROPERTY_DEBUG_INFO_VISIBLE);
        }
    };

    public static final GameAction ACTION_TOGGLE_DRAW_MODE = new GameAction("toggle_draw_mode") {

        @Override
        protected void doAction(Game game) {
            Ufx.toggleProperty(GameConstants.PROPERTY_3D_DRAW_MODE, DrawMode.LINE, DrawMode.FILL);
        }
    };

    public static final GameAction ACTION_TOGGLE_KEYBOARD_MONITOR = new GameAction("toggle_keyboard_monitor") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(GameConstants.PROPERTY_KEYBOARD_MONITOR_VISIBLE);
        }
    };

    public static final GameAction ACTION_TOGGLE_MINI_VIEW_VISIBILITY = new GameAction("toggle_mini_view_visibility") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(GameConstants.PROPERTY_MINI_VIEW_ON);
            if (!game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D)) {
                final String msg = game.ui().translations().translate(
                    GameConstants.PROPERTY_MINI_VIEW_ON.get() ? "pip_on" : "pip_off");
                game.shortMessage(msg);
            }
        }
    };

    public static final GameAction ACTION_TOGGLE_MUTED = new GameAction("toggle_muted") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(GameConstants.PROPERTY_MUTED);
        }
    };

    public static final GameAction ACTION_TOGGLE_PAUSED = new GameAction("toggle_paused") {

        @Override
        protected void doAction(Game game) {
            final GameClock gameClock = game.clock();
            toggleBooleanProperty(gameClock.updatesDisabledProperty());
            final boolean paused = gameClock.getUpdatesDisabled();
            if (paused) {
                game.ui().sounds().stopAll();
                game.currentUIConfig().optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            }
        }

        @Override
        public boolean isEnabled(Game game) {
            final SubViewManager subViews = game.ui().subViews();
            return subViews.isSelected(subViews.gamePlayView());
        }
    };

    public static final GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction("toggle_play_scene_2d_3d") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(GameConstants.PROPERTY_3D_ENABLED);
            final boolean is3DEnabled = GameConstants.PROPERTY_3D_ENABLED.get();
            if (!inPlayScene(game)) {
                game.shortMessage(game.ui().translations().translate(is3DEnabled ? "use_3D_scene" : "use_2D_scene"));
            }
            if (isLevelPlaying(game)) {
                game.ui().gameScenes().forceGameSceneUpdate(game);
            }
        }

        @Override
        public boolean isEnabled(Game game) {
            final SubViewManager subViews = game.ui().subViews();
            return subViews.isSelected(subViews.gamePlayView());
        }

        private boolean inPlayScene(Game game) {
            final GameSceneManager gameScenes = game.ui().gameScenes();
            return gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D)
                || gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D);
        }

        private boolean isLevelPlaying(Game game) {
            final GameState gameState = game.currentGameContext().state();
            return GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
        }
    };
}