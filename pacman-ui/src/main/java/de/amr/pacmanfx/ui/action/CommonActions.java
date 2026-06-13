/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static de.amr.pacmanfx.ui.input.Keyboard.alt;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;
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
    private final UISettingsActions uiSettingsActions;

    private final Set<ActionKeyBinding> commonBindings;

    public CommonActions(Game game) {
        this.game = Objects.requireNonNull(game);

        simulationActions = new SimulationActions(game);
        steeringActions = new SteeringActions(game);
        camera3DActions = new Camera3DActions(game);
        editorActions = new EditorActions(game);
        cheatActions = new CheatActions(game);
        sceneTestActions = new TestActions(game);
        uiSettingsActions = new UISettingsActions(game);

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

    public UISettingsActions uiSettingsActions() {
        return uiSettingsActions;
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
            new ActionKeyBinding(actionQuit(),                    bare(KeyCode.Q)),
            new ActionKeyBinding(actionStartGame(),               bare(KeyCode.F3)),
            new ActionKeyBinding(actionToggleCollisionStrategy(), alt(KeyCode.S)),
            new ActionKeyBinding(actionTogglePlayScene2D3D(),     alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3))
        ));

        bindings.addAll(editorActions.bindings());
        bindings.addAll(uiSettingsActions.bindings());
        bindings.addAll(simulationActions().bindings());

        return Collections.unmodifiableSet(bindings);
    }
}