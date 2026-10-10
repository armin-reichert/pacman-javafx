/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.engine.runtime.PacManGamesEngine.runAction;

public interface ActionBindingsRegistry extends Disposable {

    ActionBindingsRegistry NO_BINDINGS = new EmptyActionBindingsRegistry();

    String name();

    Map<KeyCodeCombination, GameAction<GameEngineContext>> actionBindings();

    Optional<GameAction<GameEngineContext>> findActionMatchingPressedKeys(Keyboard keyboard);

    default Optional<GameAction<GameEngineContext>> executeMatchingAction(GameEngineContext context) {
        final Optional<GameAction<GameEngineContext>> matchingAction = findActionMatchingPressedKeys(context.input().keyboard());
        matchingAction.ifPresent(action -> runAction(action, context));
        return matchingAction;
    }

    void bindActionToKeyCombination(GameAction<GameEngineContext> action, KeyCodeCombination combination);

    void selectAnyMatchingBinding(GameAction<GameEngineContext> action, Set<ActionKeyBinding> bindings);

    void registerAllBindings(Set<ActionKeyBinding> bindings);
}
