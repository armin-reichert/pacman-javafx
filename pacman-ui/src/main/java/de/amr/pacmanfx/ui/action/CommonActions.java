/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

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

    private final Game game;

    private final SteeringActions steeringActions;
    private final TestActions sceneTestActions;

    private final Set<ActionKeyBinding> commonBindings;
    private final Set<ActionKeyBinding> cheatActionBindings;
    private final Set<ActionKeyBinding> sceneTestsBindings;

    public CommonActions(Game game) {
        this.game = Objects.requireNonNull(game);

        steeringActions = new SteeringActions(game);
        sceneTestActions = new TestActions(game);

        commonBindings = createCommonBindings();
        cheatActionBindings = createCheatActionBindings();
        sceneTestsBindings = createSceneTestsBindings();
    }

    public SteeringActions steeringActions() {
        return steeringActions;
    }

    public TestActions sceneTestActions() {
        return sceneTestActions;
    }

    // Bindings sets

    public Set<ActionKeyBinding> commonBindings() {
        return commonBindings;
    }

    public Set<ActionKeyBinding> cheatActionBindings() {
        return cheatActionBindings;
    }

    public Set<ActionKeyBinding> sceneTestsBindings() {
        return sceneTestsBindings;
    }

    // Map editor actions

    /**
     * @param mapFile map file to edit or {@code null}
     * @return action which opens the map editor and edits the given map file if any
     */
    public GameAction createEditMapFileAction(File mapFile) {

        return new GameAction(game, "edit_map_file") {
            @Override
            protected void doAction() {
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

    private GameAction actionOpenEditor;

    public GameAction actionOpenEditor() {
        if (actionOpenEditor == null) {
            actionOpenEditor = new GameAction(game, "open_editor") {
                @Override
                protected void doAction() {
                    openMapEditor().ifPresent(editor -> startEditor(editor));
                }
            };
        }
        return actionOpenEditor;
    }

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

    private GameAction actionStartGame;
    
    public GameAction actionStartGame() {
        if (actionStartGame == null) {
            actionStartGame = new GameAction(game, "start_game") {
                @Override
                protected void doAction() {
                    game.start();
                }
            };
        }
        return actionStartGame;
    }

    private GameAction actionQuit;

    public GameAction actionQuit() {
        if (actionQuit == null) {
            actionQuit = new GameAction(game, "quit") {
                @Override
                protected void doAction() {
                    Logger.info("Call QUIT handler for {}", game.ui().subViews().currentView());
                    game.ui().subViews().currentView().handleQuit(game);
                }
            };
        }
        return actionQuit;
    }

    private GameAction actionEnterFullScreen;

    public GameAction actionEnterFullScreen() {
        if (actionEnterFullScreen == null) {
            actionEnterFullScreen = new GameAction(game, "enter_fullscreen") {
                @Override
                protected void doAction() {
                    game.ui().view().stage().setFullScreen(true);
                }
            };
        }
        return actionEnterFullScreen;
    }

    private GameAction actionLetGameStateExpire;

    public GameAction actionLetGameStateExpire() {
        if (actionLetGameStateExpire == null) {
            actionLetGameStateExpire = new GameAction(game, "let_game_state_expire") {
                @Override
                protected void doAction() {
                    game.currentGameContext().state().expire();
                }
            };
        }
        return actionLetGameStateExpire;
    }

    private GameAction actionPerspectiveNext;

    public GameAction actionPerspectiveNext() {
        if (actionPerspectiveNext == null) {
            actionPerspectiveNext = new GameAction(game, "perspective_next") {
                @Override
                protected void doAction() {
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
        }
        return actionPerspectiveNext;
    }

    private GameAction actionPerspectivePrevious;

    public GameAction actionPerspectivePrevious() {
        if (actionPerspectivePrevious == null) {
            actionPerspectivePrevious = new GameAction(game, "perspective_previous") {
                @Override
                protected void doAction() {
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
        }
        return actionPerspectivePrevious;
    }

    private GameAction actionRestartIntro;

    public GameAction actionRestartIntro() {
        if (actionRestartIntro == null) {
            actionRestartIntro = new GameAction(game, "restart_intro") {
                @Override
                protected void doAction() {
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
        }
        return actionRestartIntro;
    }

    private GameAction actionShowHelp;

    public GameAction actionShowHelp() {
        if (actionShowHelp == null) {
            actionShowHelp = new GameAction(game, "show_help") {
                @Override
                protected void doAction() {
                    game.ui().subViews().gamePlayView().showHelp(game);
                }

                @Override
                public boolean isEnabled() {
                    final GameSceneManager gameScenes = game.ui().gameScenes();
                    final String variantName = game.currentGameVariantName();
                    final boolean isArcadeGame = GameVariantID.isArcadeGameName(variantName);
                    return isArcadeGame &&
                        (gameScenes.currentGameSceneHasID(game, CommonSceneID.INTRO_SCENE)
                            || gameScenes.currentGameSceneHasID(game, CommonSceneID.START_SCENE)
                            || gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D));
                }
            };
        }
        return actionShowHelp;
    }

    private GameAction actionSimulationFaster;

    public GameAction actionSimulationFaster() {
        if (actionSimulationFaster == null) {
            actionSimulationFaster = new GameAction(game, "simulation_faster") {
                @Override
                protected void doAction() {
                    final GameClock clock = game.clock();
                    final int newRate = Math.clamp(clock.targetFrameRate() + GameConstants.SIM_SPEED_DELTA,
                        GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                    clock.setTargetFrameRate(newRate);

                    final String msg = newRate == GameConstants.SIM_SPEED_MAX ? "At maximum speed: %d Hz" : "%d Hz";
                    game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
                }
            };
        }
        return actionSimulationFaster;
    }

    private GameAction actionSimulationFastest;

    public GameAction actionSimulationFastest() {
        if (actionSimulationFastest == null) {
            actionSimulationFastest = new GameAction(game, "simulation_fastest") {
                @Override
                protected void doAction() {
                    game.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MAX);
                    final String msg = "At maximum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MAX);
                    game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
                }
            };
        }
        return actionSimulationFastest;
    }

    private GameAction actionSimulationSlower;

    public GameAction actionSimulationSlower() {
        if (actionSimulationSlower == null) {
            actionSimulationSlower = new GameAction(game, "simulation_slower") {
                @Override
                protected void doAction() {
                    final GameClock clock = game.clock();
                    final int newRate = Math.clamp(clock.targetFrameRate() - GameConstants.SIM_SPEED_DELTA,
                        GameConstants.SIM_SPEED_MIN, GameConstants.SIM_SPEED_MAX);
                    clock.setTargetFrameRate(newRate);

                    final String msg = newRate == GameConstants.SIM_SPEED_MIN ? "At minimum speed: %d Hz" : "%d Hz";
                    game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg.formatted(newRate));
                }
            };
        }
        return actionSimulationSlower;
    }

    private GameAction actionSimulationSlowest;

    public GameAction actionSimulationSlowest() {
        if (actionSimulationSlowest == null) {
            actionSimulationSlowest = new GameAction(game, "simulation_slowest") {
                @Override
                protected void doAction() {
                    game.clock().setTargetFrameRate(GameConstants.SIM_SPEED_MIN);
                    final String msg = "At minimum speed: %d Hz".formatted(GameConstants.SIM_SPEED_MIN);
                    game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), msg);
                }
            };
        }
        return actionSimulationSlowest;
    }

    private GameAction actionSimulationOneStep;

    public GameAction actionSimulationOneStep() {
        if (actionSimulationOneStep == null) {
            actionSimulationOneStep = new GameAction(game, "simulation_one_step") {
                @Override
                protected void doAction() {
                    final boolean failure = !game.clock().makeOneStep(true);
                    if (failure) {
                        game.shortMessage("Simulation step error!");
                    }
                }

                @Override
                public boolean isEnabled() { return game.clock().getUpdatesDisabled(); }
            };
        }
        return actionSimulationOneStep;
    }

    private GameAction actionSimulationTenSteps;

    public GameAction actionSimulationTenSteps() {
        if (actionSimulationTenSteps == null) {
            actionSimulationTenSteps = new GameAction(game, "simulation_ten_steps") {
                @Override
                protected void doAction() {
                    final boolean failure = !game.clock().makeSteps(10, true);
                    if (failure) {
                        game.shortMessage("Simulation steps error!");
                    }
                }

                @Override
                public boolean isEnabled() { return game.clock().getUpdatesDisabled(); }
            };
        }
        return actionSimulationTenSteps;
    }

    private GameAction actionSimulationReset;

    public GameAction actionSimulationReset() {
        if (actionSimulationReset == null) {
            actionSimulationReset = new GameAction(game, "simulation_reset") {
                @Override
                protected void doAction() {
                    game.clock().setTargetFrameRate(NUM_TICKS_PER_SEC);
                    game.shortMessage(Duration.seconds(GameConstants.SIM_STEP_MESSAGE_SEC), game.clock().targetFrameRate() + "Hz");
                }
            };
        }
        return actionSimulationReset;
    }

    private GameAction actionToggleCollisionStrategy;

    public GameAction actionToggleCollisionStrategy() {
        if (actionToggleCollisionStrategy == null) {
            actionToggleCollisionStrategy = new GameAction(game, "toggle_collision_strategy") {
                @Override
                protected void doAction() {
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
        }
        return actionToggleCollisionStrategy;
    }

    private GameAction actionToggleDashboard;

    public GameAction actionToggleDashboard() {
        if (actionToggleDashboard == null) {
            actionToggleDashboard = new GameAction(game, "toggle_dashboard") {
                @Override
                protected void doAction() {
                    game.ui().subViews().gamePlayView().dashboard().toggleVisibility();
                }

                @Override
                public boolean isEnabled() {
                    final SubViewManager subViews = game.ui().subViews();
                    return subViews.isSelected(subViews.gamePlayView());
                }
            };
        }
        return actionToggleDashboard;
    }


    private GameAction actionToggleDebugInfo;

    public GameAction actionToggleDebugInfo() {
        if (actionToggleDebugInfo == null) {
            actionToggleDebugInfo = new GameAction(game, "toggle_debug_info") {
                @Override
                protected void doAction() {
                    toggleBooleanProperty(game.ui().settings().debugInfoVisibleProperty);
                }
            };
        }
        return actionToggleDebugInfo;
    }

    private GameAction actionToggleDrawMode;

    public GameAction actionToggleDrawMode() {
        if (actionToggleDrawMode == null) {
            actionToggleDrawMode = new GameAction(game, "toggle_draw_mode") {
                @Override
                protected void doAction() {
                    Ufx.toggleProperty(game.ui().settings3D().drawModeProperty(), DrawMode.LINE, DrawMode.FILL);
                }
            };
        }
        return actionToggleDrawMode;
    }

    private GameAction actionToggleKeyboardMonitor;

    public GameAction actionToggleKeyboardMonitor() {
        if (actionToggleKeyboardMonitor == null) {
            actionToggleKeyboardMonitor = new GameAction(game, "toggle_keyboard_monitor") {
                @Override
                protected void doAction() {
                    toggleBooleanProperty(game.ui().settings().keyboardMonitorVisibleProperty);
                }
            };
        }
        return actionToggleKeyboardMonitor;
    }

    private GameAction actionToggleMiniViewVisibility;

    public GameAction actionToggleMiniViewVisibility() {
        if (actionToggleMiniViewVisibility == null) {
            actionToggleMiniViewVisibility = new GameAction(game, "toggle_mini_view_visibility") {
                @Override
                protected void doAction() {
                    toggleBooleanProperty(game.ui().settings().miniViewOnProperty);
                    if (!game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D)) {
                        final String msg = game.ui().translations().translate(
                            game.ui().settings().miniViewOnProperty.get() ? "pip_on" : "pip_off");
                        game.shortMessage(msg);
                    }
                }
            };
        }
        return actionToggleMiniViewVisibility;
    }

    private GameAction actionToggleMuted;

    public GameAction actionToggleMuted() {
        if (actionToggleMuted == null) {
            actionToggleMuted = new GameAction(game, "toggle_muted") {
                @Override
                protected void doAction() {
                    toggleBooleanProperty(game.ui().settings().mutedProperty);
                }
            };
        }
        return actionToggleMuted;
    }

    private GameAction actionTogglePaused;

    public GameAction actionTogglePaused() {
        if (actionTogglePaused == null) {
            actionTogglePaused = new GameAction(game, "toggle_paused") {
                @Override
                protected void doAction() {
                    final GameClock gameClock = game.clock();
                    toggleBooleanProperty(gameClock.updatesDisabledProperty());
                    final boolean paused = gameClock.getUpdatesDisabled();
                    if (paused) {
                        game.ui().sounds().stopAll();
                        game.currentUIConfig().optSoundEffects().ifPresent(GameSoundEffects::stopAll);
                    }
                }

                @Override
                public boolean isEnabled() {
                    final SubViewManager subViews = game.ui().subViews();
                    return subViews.isSelected(subViews.gamePlayView());
                }
            };
        }
        return actionTogglePaused;
    }

    private GameAction actionTogglePlayScene2D3D;

    public GameAction actionTogglePlayScene2D3D() {
        if  (actionTogglePlayScene2D3D == null) {
            actionTogglePlayScene2D3D = new GameAction(game, "toggle_play_scene_2d_3d") {

                @Override
                protected void doAction() {
                    toggleBooleanProperty(game.ui().settings3D().view3DEnabledProperty());
                    final boolean is3DEnabled = game.ui().settings3D().view3DEnabledProperty().get();
                    if (!inPlayScene()) {
                        game.shortMessage(game.ui().translations().translate(is3DEnabled ? "use_3D_scene" : "use_2D_scene"));
                    }
                    if (isLevelPlaying()) {
                        game.ui().gameScenes().forceGameSceneUpdate(game);
                    }
                }

                @Override
                public boolean isEnabled() {
                    final SubViewManager subViews = game.ui().subViews();
                    return subViews.isSelected(subViews.gamePlayView());
                }

                private boolean inPlayScene() {
                    final GameSceneManager gameScenes = game.ui().gameScenes();
                    return gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D)
                        || gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D);
                }

                private boolean isLevelPlaying() {
                    final GameState gameState = game.currentGameContext().state();
                    return GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
                }
            };
        }
        return actionTogglePlayScene2D3D;
    }

    // Binding sets

    private Set<ActionKeyBinding> createCommonBindings() {
        return Set.of(
            new ActionKeyBinding(actionEnterFullScreen(),          bare(KeyCode.F11)),
            new ActionKeyBinding(actionOpenEditor(),               alt_shift(KeyCode.E)),
            new ActionKeyBinding(actionQuit(),                     bare(KeyCode.Q)),
            new ActionKeyBinding(actionShowHelp(),                 bare(KeyCode.H)),
            new ActionKeyBinding(actionSimulationSlower(),         alt(KeyCode.MINUS)),
            new ActionKeyBinding(actionSimulationSlowest(),        alt_shift(KeyCode.MINUS)),
            new ActionKeyBinding(actionSimulationFaster(),         alt(KeyCode.PLUS)),
            new ActionKeyBinding(actionSimulationFastest(),        alt_shift(KeyCode.PLUS)),
            new ActionKeyBinding(actionSimulationReset(),          alt(KeyCode.DIGIT0)),
            new ActionKeyBinding(actionSimulationOneStep(),        shift(KeyCode.P), shift(KeyCode.F5)),
            new ActionKeyBinding(actionSimulationTenSteps(),       shift(KeyCode.SPACE)),
            new ActionKeyBinding(actionStartGame(),                bare(KeyCode.F3)),
            new ActionKeyBinding(actionToggleCollisionStrategy(),  alt(KeyCode.S)),
            new ActionKeyBinding(actionToggleDashboard(),          bare(KeyCode.F1), alt(KeyCode.B)),
            new ActionKeyBinding(actionToggleDebugInfo(),          alt(KeyCode.D)),
            new ActionKeyBinding(actionToggleKeyboardMonitor(),    alt(KeyCode.K)),
            new ActionKeyBinding(actionToggleMiniViewVisibility(), bare(KeyCode.F2)),
            new ActionKeyBinding(actionToggleMuted(),              alt(KeyCode.M)),
            new ActionKeyBinding(actionTogglePaused(),             bare(KeyCode.P), bare(KeyCode.F5)),
            new ActionKeyBinding(actionTogglePlayScene2D3D(),      alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),

            // Cheats
            new ActionKeyBinding(game.cheatActions().actionToggleAutopilot(), alt(KeyCode.A)),
            new ActionKeyBinding(game.cheatActions().actionToggleImmunity(),  alt(KeyCode.I))
        );
    }

    private Set<ActionKeyBinding> createCheatActionBindings() {
        return Set.of(
            new ActionKeyBinding(game.cheatActions().actionEatAllPellets(),  alt(KeyCode.E)),
            new ActionKeyBinding(game.cheatActions().actionAddLives(),       alt(KeyCode.L)),
            new ActionKeyBinding(game.cheatActions().actionEnterNextLevel(), alt(KeyCode.N)),
            new ActionKeyBinding(game.cheatActions().actionKillGhosts(),     alt(KeyCode.X))
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