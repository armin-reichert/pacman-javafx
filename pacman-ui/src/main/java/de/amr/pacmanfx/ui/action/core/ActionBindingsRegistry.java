/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action.core;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ActionBindingsRegistry extends Disposable {

    ActionBindingsRegistry NO_BINDINGS = new EmptyActionBindingsRegistry();

    String name();

    Map<KeyCodeCombination, GameAction> actionBindings();

    Optional<GameAction> findActionMatchingPressedKeys(Keyboard keyboard);

    default Optional<GameAction> executeMatchingAction(GameAppContext app) {
        final Optional<GameAction> matchingAction = findActionMatchingPressedKeys(app.input().keyboard());
        matchingAction.ifPresent(app::runAction);
        return matchingAction;
    }

    void bindActionToKeyCombination(GameAction action, KeyCodeCombination combination);

    void selectAnyMatchingBinding(GameAction action, Set<ActionKeyBinding> bindings);

    void registerAllBindings(Set<ActionKeyBinding> bindings);
}
