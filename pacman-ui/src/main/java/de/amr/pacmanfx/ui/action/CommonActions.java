/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
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
import javafx.scene.input.KeyCode;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.model.GameRules.NUM_TICKS_PER_SEC;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

/**
 * Common actions for all game variants.
 * <p>
 * For each action, there must exist a line of the form <pre>{@code action.<actionID>=localized text}</pre>
 * in the global UI resource bundle.
 */
public final class CommonActions {

    abstract class AbstractGameAction extends GameAction {
    
        protected AbstractGameAction(String key) {
            super(game, key);
        }
    }
    
    private final Game game;

    private final TestActions sceneTestActions;

    public CommonActions(Game game) {
        this.game = Objects.requireNonNull(game);

        actionSteerUp = new SteeringAction(game, Direction.UP);
        actionSteerDown = new SteeringAction(game, Direction.DOWN);
        actionSteerLeft = new SteeringAction(game, Direction.LEFT);
        actionSteerRight = new SteeringAction(game, Direction.RIGHT);

        sceneTestActions = new TestActions(game);

        commonBindings = createCommonBindings();
        cheatActionBindings = createCheatActionBindings();
        steeringActionBindings = createSteeringActionBindings();
        sceneTestsBindings = createSceneTestsBindings();
    }

    // Scene test actions

    public TestActions sceneTestActions() {
        return sceneTestActions;
    }

    // Pac-Man steering actions

    public final GameAction actionSteerUp;
    public final GameAction actionSteerDown;
    public final GameAction actionSteerLeft;
    public final GameAction actionSteerRight;

    // Map editor actions

    /**
     * @param mapFile map file to edit or {@code null}
     * @return action which opens the map editor and edits the given map file if any
     */
    public GameAction createEditMapFileAction(File mapFile) {

        return new AbstractGameAction("edit_map_file") {

            @Override
            protected void doAction(Game game) {
                openMapEditor().ifPresent(editor -> {
                    startEditor(editor);
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

    public final GameAction ACTION_OPEN_EDITOR = new AbstractGameAction("open_editor") {

        @Override
        protected void doAction(Game game) {
            openMapEditor().ifPresent(editor -> startEditor(editor));
        }
    };

    private void startEditor(TileMapEditor editor) {
        game.stop();
        editor.init(GameConstants.CUSTOM_MAP_DIR);
        editor.start();
    }

    private Optional<TileMapEditor> openMapEditor() {
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

    public final GameAction ACTION_START_GAME = new AbstractGameAction("start_game") {

        @Override
        protected void doAction(Game game) {
            game.start();
        }
    };

    public final GameAction ACTION_QUIT = new AbstractGameAction("quit") {

        @Override
        protected void doAction(Game game) {
            Logger.info("Call QUIT handler for {}", game.ui().subViews().currentView());
            game.ui().subViews().currentView().handleQuit(game);
        }
    };

    public final GameAction ACTION_ENTER_FULLSCREEN = new AbstractGameAction("enter_fullscreen") {

        @Override
        protected void doAction(Game game) {
            game.ui().view().stage().setFullScreen(true);
        }
    };

    public final GameAction ACTION_LET_GAME_STATE_EXPIRE = new AbstractGameAction("let_game_state_expire") {

        @Override
        protected void doAction(Game game) {
            game.currentGameContext().state().expire();
        }
    };

    public final GameAction ACTION_PERSPECTIVE_NEXT = new AbstractGameAction("perspective_next") {

        @Override
        protected void doAction(Game game) {
            final PerspectiveID nextID = game.ui().settings3D().cameraPerspectiveIdProperty().get().next();
            game.ui().settings3D().cameraPerspectiveIdProperty().set(nextID);

            final TranslationManager translations = game.ui().translations();
            final String msgKey = translations.translate(
                "camera_perspective",
                translations.translate("perspective_id_" + nextID.name())
            );
            game.shortMessage(msgKey);
        }
    };

    public final GameAction ACTION_PERSPECTIVE_PREVIOUS = new AbstractGameAction("perspective_previous") {

        @Override
        protected void doAction(Game game) {
            final PerspectiveID prevID = game.ui().settings3D().cameraPerspectiveIdProperty().get().prev();
            game.ui().settings3D().cameraPerspectiveIdProperty().set(prevID);

            final TranslationManager translations = game.ui().translations();
            final String msgKey = translations.translate(
                "camera_perspective",
                translations.translate("perspective_id_" + prevID.name())
            );
            game.shortMessage(msgKey);
        }
    };

    //TODO check this code
    public final GameAction ACTION_RESTART_INTRO = new AbstractGameAction("restart_intro") {

        @Override
        protected void doAction(Game game) {
            final GameContext gameContext = game.currentGameContext();
            final GameState gameState = gameContext.state();

            if (gameState instanceof TestState) {
                gameState.onExit(gameContext);
            }

            game.stop();
            game.clock().start();
            gameContext.flow().restartState(GameStateID.GAME_INTRO);
        }
    };

    public final GameAction ACTION_SHOW_HELP = new AbstractGameAction("show_help") {

        @Override
        protected void doAction(Game game) {
            game.ui().subViews().gamePlayView().showHelp(game);
        }

        @Override
        public boolean isEnabled(Game game) {
            final GameSceneManager gameScenes = game.ui().gameScenes();
            final String variantName = game.currentGameVariantName();
            final boolean isArcadeGame = GameVariantID.isArcadeGameName(variantName);
            return isArcadeGame &&
                  (gameScenes.currentGameSceneHasID(game, CommonSceneID.INTRO_SCENE)
                || gameScenes.currentGameSceneHasID(game, CommonSceneID.START_SCENE)
                || gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D));
        }
    };

    //TODO localize message
    public final GameAction ACTION_SIMULATION_FASTER = new AbstractGameAction("simulation_faster") {

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
    public final GameAction ACTION_SIMULATION_FASTEST = new AbstractGameAction("simulation_fastest") {

        @Override
        protected void doAction(Game game) {
            game.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MAX);
            final String msg = "At maximum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MAX);
            game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
        }
    };

    //TODO localize message
    public final GameAction ACTION_SIMULATION_SLOWER = new AbstractGameAction("simulation_slower") {

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
    public final GameAction ACTION_SIMULATION_SLOWEST = new AbstractGameAction("simulation_slowest") {

        @Override
        protected void doAction(Game game) {
            game.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MIN);
            final String msg = "At minimum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MIN);
            game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
        }
    };

    public final GameAction ACTION_SIMULATION_ONE_STEP = new AbstractGameAction("simulation_one_step") {

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

    public final GameAction ACTION_SIMULATION_TEN_STEPS = new AbstractGameAction("simulation_ten_steps") {

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

    public final GameAction ACTION_SIMULATION_RESET = new AbstractGameAction("simulation_reset") {

        @Override
        protected void doAction(Game game) {
            game.clock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), game.clock().targetFrameRate() + "Hz");
        }
    };

    //TODO localize message
    public final GameAction ACTION_TOGGLE_COLLISION_STRATEGY = new AbstractGameAction("toggle_collision_strategy") {

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

    public final GameAction ACTION_TOGGLE_DASHBOARD = new AbstractGameAction("toggle_dashboard") {

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

    public final GameAction ACTION_TOGGLE_DEBUG_INFO = new AbstractGameAction("toggle_debug_info") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(game.ui().settings().debugInfoVisibleProperty);
        }
    };

    public final GameAction ACTION_TOGGLE_DRAW_MODE = new AbstractGameAction("toggle_draw_mode") {

        @Override
        protected void doAction(Game game) {
            Ufx.toggleProperty(game.ui().settings3D().drawModeProperty(), DrawMode.LINE, DrawMode.FILL);
        }
    };

    public final GameAction ACTION_TOGGLE_KEYBOARD_MONITOR = new AbstractGameAction("toggle_keyboard_monitor") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(game.ui().settings().keyboardMonitorVisibleProperty);
        }
    };

    public final GameAction ACTION_TOGGLE_MINI_VIEW_VISIBILITY = new AbstractGameAction("toggle_mini_view_visibility") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(game.ui().settings().miniViewOnProperty);
            if (!game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D)) {
                final String msg = game.ui().translations().translate(
                    game.ui().settings().miniViewOnProperty.get() ? "pip_on" : "pip_off");
                game.shortMessage(msg);
            }
        }
    };

    public final GameAction ACTION_TOGGLE_MUTED = new AbstractGameAction("toggle_muted") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(game.ui().settings().mutedProperty);
        }
    };

    public final GameAction ACTION_TOGGLE_PAUSED = new AbstractGameAction("toggle_paused") {

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

    public final GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new AbstractGameAction("toggle_play_scene_2d_3d") {

        @Override
        protected void doAction(Game game) {
            toggleBooleanProperty(game.ui().settings3D().view3DEnabledProperty());
            final boolean is3DEnabled = game.ui().settings3D().view3DEnabledProperty().get();
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

    // Binding sets

    /** Steering key bindings (arrow keys, optionally with Ctrl). */
    public final Set<ActionKeyBinding> steeringActionBindings;

    /** Common global key bindings used across all views/scenes. */
    public final Set<ActionKeyBinding> commonBindings;

    /** Cheat key bindings (Alt + key). */
    public final Set<ActionKeyBinding> cheatActionBindings;

    /** Key bindings for scene/level test utilities. */
    public final Set<ActionKeyBinding> sceneTestsBindings;

    private Set<ActionKeyBinding> createCommonBindings() {
        return Set.of(
            new ActionKeyBinding(ACTION_ENTER_FULLSCREEN,                     bare(KeyCode.F11)),
            new ActionKeyBinding(ACTION_OPEN_EDITOR,                          alt_shift(KeyCode.E)),
            new ActionKeyBinding(ACTION_QUIT,                                 bare(KeyCode.Q)),
            new ActionKeyBinding(ACTION_SHOW_HELP,                            bare(KeyCode.H)),
            new ActionKeyBinding(ACTION_SIMULATION_SLOWER,                    alt(KeyCode.MINUS)),
            new ActionKeyBinding(ACTION_SIMULATION_SLOWEST,                   alt_shift(KeyCode.MINUS)),
            new ActionKeyBinding(ACTION_SIMULATION_FASTER,                    alt(KeyCode.PLUS)),
            new ActionKeyBinding(ACTION_SIMULATION_FASTEST,                   alt_shift(KeyCode.PLUS)),
            new ActionKeyBinding(ACTION_SIMULATION_RESET,                     alt(KeyCode.DIGIT0)),
            new ActionKeyBinding(ACTION_SIMULATION_ONE_STEP,                  shift(KeyCode.P), shift(KeyCode.F5)),
            new ActionKeyBinding(ACTION_SIMULATION_TEN_STEPS,                 shift(KeyCode.SPACE)),
            new ActionKeyBinding(ACTION_START_GAME,                           bare(KeyCode.F3)),
            new ActionKeyBinding(ACTION_TOGGLE_COLLISION_STRATEGY,            alt(KeyCode.S)),
            new ActionKeyBinding(ACTION_TOGGLE_DASHBOARD,                     bare(KeyCode.F1), alt(KeyCode.B)),
            new ActionKeyBinding(ACTION_TOGGLE_DEBUG_INFO,                    alt(KeyCode.D)),
            new ActionKeyBinding(ACTION_TOGGLE_KEYBOARD_MONITOR,              alt(KeyCode.K)),
            new ActionKeyBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY,          bare(KeyCode.F2)),
            new ActionKeyBinding(ACTION_TOGGLE_MUTED,                         alt(KeyCode.M)),
            new ActionKeyBinding(ACTION_TOGGLE_PAUSED,                        bare(KeyCode.P), bare(KeyCode.F5)),
            new ActionKeyBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D,              alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),

            // Cheats
            new ActionKeyBinding(game.cheatActions().ACTION_TOGGLE_AUTOPILOT, alt(KeyCode.A)),
            new ActionKeyBinding(game.cheatActions().ACTION_TOGGLE_IMMUNITY,  alt(KeyCode.I))
        );
    }

    private Set<ActionKeyBinding> createCheatActionBindings() {
        return Set.of(
            new ActionKeyBinding(game.cheatActions().ACTION_EAT_ALL_PELLETS,  alt(KeyCode.E)),
            new ActionKeyBinding(game.cheatActions().ACTION_ADD_LIVES,        alt(KeyCode.L)),
            new ActionKeyBinding(game.cheatActions().ACTION_ENTER_NEXT_LEVEL, alt(KeyCode.N)),
            new ActionKeyBinding(game.cheatActions().ACTION_KILL_GHOSTS,      alt(KeyCode.X))
        );
    }

    private Set<ActionKeyBinding> createSteeringActionBindings() {
        return Set.of(
            new ActionKeyBinding(actionSteerUp, bare(KeyCode.UP), control(KeyCode.UP)),
            new ActionKeyBinding(actionSteerDown, bare(KeyCode.DOWN), control(KeyCode.DOWN)),
            new ActionKeyBinding(actionSteerLeft, bare(KeyCode.LEFT), control(KeyCode.LEFT)),
            new ActionKeyBinding(actionSteerRight, bare(KeyCode.RIGHT), control(KeyCode.RIGHT))
        );
    }

    private Set<ActionKeyBinding> createSceneTestsBindings() {
        return Set.of(
            new ActionKeyBinding(sceneTestActions.ACTION_CUT_SCENES_TEST, alt(KeyCode.C)),
            new ActionKeyBinding(sceneTestActions.ACTION_SHORT_LEVEL_TEST, alt(KeyCode.T)),
            new ActionKeyBinding(sceneTestActions.ACTION_MEDIUM_LEVEL_TEST, alt_shift(KeyCode.T))
        );
    }
}