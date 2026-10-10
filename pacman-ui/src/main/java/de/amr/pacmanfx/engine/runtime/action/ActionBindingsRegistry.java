/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.engine.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.engine.runtime.PacManGamesEngine.runAction;

public interface ActionBindingsRegistry extends Disposable {

    ActionBindingsRegistry NO_BINDINGS = new EmptyActionBindingsRegistry();

    String name();

    Map<KeyCodeCombination, GameAction> actionBindings();

    Optional<GameAction> findActionMatchingPressedKeys(Keyboard keyboard);

    default Optional<GameAction> executeMatchingAction(GameActionContext context) {
        final Optional<GameAction> matchingAction = findActionMatchingPressedKeys(context.input().keyboard());
        matchingAction.ifPresent(action -> runAction(action, context));
        return matchingAction;
    }

    void bindActionToKeyCombination(GameAction action, KeyCodeCombination combination);

    void selectAnyMatchingBinding(GameAction action, Set<ActionKeyBinding> bindings);

    void registerAllBindings(Set<ActionKeyBinding> bindings);
}
