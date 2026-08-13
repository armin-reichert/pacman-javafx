/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.rules.ActorCollisionRules;
import de.amr.pacmanfx.core.model.rules.CollisionStrategy;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import javafx.scene.input.KeyCode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static de.amr.basics.util.Ufx.toggleProperty;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

/**
 * Common actions for all game variants.
 * <p>
 * For each action, there must exist a line of the form <pre>{@code action.<actionID>=localized text}</pre>
 * in the global UI resource bundle.
 */
public final class CommonGameActions {

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

    public CommonGameActions() {
        simulationActions = new SimulationActions();
        gameFlowActions = new GameFlowActions();
        steeringActions = new SteeringActions();
        camera3DActions = new Camera3DActions();
        editorActions = new EditorActions();
        cheatActions = new CheatActions();
        sceneTestActions = new TestActions();
        uiSettingsActions = new UISettingsActions();

        actionToggleCollisionStrategy = new GameAction("toggle_collision_strategy") {
            @Override
            public void doAction(GameAppContext app) {
                final GameContext game = app.game();
                final ActorCollisionRules collisionRules = game.rules().actorCollisionRules();
                toggleProperty(collisionRules.collisionStrategyProperty(), CollisionStrategy.SAME_TILE, CollisionStrategy.CENTER_DISTANCE);
                final CollisionStrategy strategy = collisionRules.getCollisionStrategy();
                if (strategy == CollisionStrategy.SAME_TILE) {
                    app.ui().shortMessage("Using original Arcade collision strategy (same tile)");
                } else {
                    app.ui().shortMessage("Using safe collision strategy");
                }
            }
        };

        final Set<ActionKeyBinding> bindings = new HashSet<>();
        bindings.addAll(simulationActions.bindings());
        bindings.addAll(gameFlowActions.bindings());
        bindings.addAll(editorActions.bindings());
        bindings.addAll(uiSettingsActions.bindings());
        bindings.add(new ActionKeyBinding(actionToggleCollisionStrategy(), combine().alt().key(KeyCode.S)));

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

    public Set<ActionKeyBinding> bindings() {
        return commonBindings;
    }
}