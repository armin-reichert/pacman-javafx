/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.input.KeyCode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

/**
 * Common actions for all game variants.
 * <p>
 * For each action, there must exist a line of the form <pre>{@code action.<actionID>=localized text}</pre>
 * in the global UI resource bundle.
 */
public final class CommonActions {

    private final SimulationActions simulationActions;
    private final GameFlowActions gameFlowActions;
    private final SteeringActions steeringActions;
    private final Camera3DActions camera3DActions;
    private final EditorActions editorActions;
    private final CheatActions cheatActions;
    private final TestActions sceneTestActions;
    private final UISettingsActions uiSettingsActions;

    private final GameAction actionToggleCollisionStrategy;

    private final Set<ActionKeyBinding> commonBindings;

    public CommonActions(Game game) {
        simulationActions = new SimulationActions(game);
        gameFlowActions = new GameFlowActions(game);
        steeringActions = new SteeringActions(game);
        camera3DActions = new Camera3DActions(game);
        editorActions = new EditorActions(game);
        cheatActions = new CheatActions(game);
        sceneTestActions = new TestActions(game);
        uiSettingsActions = new UISettingsActions(game);

        actionToggleCollisionStrategy = new GameAction(game, "toggle_collision_strategy") {
            @Override
            protected void doAction() {
                final CollisionStrategy oldStrategy = game.currentGameContext().collisionStrategy();
                final CollisionStrategy newStrategy = oldStrategy == CollisionStrategy.CENTER_DISTANCE
                    ? CollisionStrategy.SAME_TILE
                    : CollisionStrategy.CENTER_DISTANCE;

                game.setCollisionStrategy(newStrategy);

                if (newStrategy == CollisionStrategy.SAME_TILE) {
                    game.shortMessage("Using original Arcade collision strategy (same tile check)");
                } else {
                    game.shortMessage("Using fail-safe collision strategy");
                }
            }
        };

        final Set<ActionKeyBinding> bindings = new HashSet<>();
        bindings.addAll(simulationActions.bindings());
        bindings.addAll(gameFlowActions.bindings());
        bindings.addAll(editorActions.bindings());
        bindings.addAll(uiSettingsActions.bindings());
        bindings.add(new ActionKeyBinding(actionToggleCollisionStrategy, combine().alt().key(KeyCode.S)));

        commonBindings = Collections.unmodifiableSet(bindings);
    }

    public SimulationActions simulationActions() {
        return simulationActions;
    }

    public GameFlowActions gameFlowActions() {
        return gameFlowActions;
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

    public GameAction actionToggleCollisionStrategy() {
        return actionToggleCollisionStrategy;
    }

    public Set<ActionKeyBinding> commonBindings() {
        return commonBindings;
    }
}