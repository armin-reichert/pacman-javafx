/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    private final SimulationActions simulationActions;
    private final SteeringActions steeringActions;
    private final Camera3DActions camera3DActions;
    private final EditorActions editorActions;
    private final CheatActions cheatActions;
    private final TestActions sceneTestActions;

    private final Set<ActionKeyBinding> commonBindings;

    public CommonActions(Game game) {
        this.game = Objects.requireNonNull(game);

        simulationActions = new SimulationActions(game);
        steeringActions = new SteeringActions(game);
        camera3DActions = new Camera3DActions(game);
        editorActions = new EditorActions(game);
        cheatActions = new CheatActions(game);
        sceneTestActions = new TestActions(game);

        commonBindings = createBindings();
    }

    public SimulationActions simulationActions() {
        return simulationActions;
    }

    public SteeringActions steeringActions() {
        return steeringActions;
    }

    public Camera3DActions camera3DActions() {
        return camera3DActions;
    }

    public EditorActions editorActions() {
        return editorActions;
    }

    public CheatActions cheatActions() {
        return cheatActions;
    }

    public TestActions sceneTestActions() {
        return sceneTestActions;
    }

    public Set<ActionKeyBinding> commonBindings() {
        return commonBindings;
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

    private Set<ActionKeyBinding> createBindings() {
        final Set<ActionKeyBinding> bindings = new HashSet<>();

        bindings.addAll(Set.of(
            new ActionKeyBinding(actionEnterFullScreen(),            bare(KeyCode.F11)),
            new ActionKeyBinding(editorActions().actionOpenEditor(), alt_shift(KeyCode.E)),
            new ActionKeyBinding(actionQuit(),                       bare(KeyCode.Q)),
            new ActionKeyBinding(actionShowHelp(),                   bare(KeyCode.H)),
            new ActionKeyBinding(actionStartGame(),                  bare(KeyCode.F3)),
            new ActionKeyBinding(actionToggleCollisionStrategy(),    alt(KeyCode.S)),
            new ActionKeyBinding(actionToggleDashboard(),            bare(KeyCode.F1), alt(KeyCode.B)),
            new ActionKeyBinding(actionToggleDebugInfo(),            alt(KeyCode.D)),
            new ActionKeyBinding(actionToggleKeyboardMonitor(),      alt(KeyCode.K)),
            new ActionKeyBinding(actionToggleMiniViewVisibility(),   bare(KeyCode.F2)),
            new ActionKeyBinding(actionToggleMuted(),                alt(KeyCode.M)),
            new ActionKeyBinding(actionTogglePaused(),               bare(KeyCode.P), bare(KeyCode.F5)),
            new ActionKeyBinding(actionTogglePlayScene2D3D(),        alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3))
        ));

        bindings.addAll(simulationActions().bindings());

        return Collections.unmodifiableSet(bindings);
    }
}