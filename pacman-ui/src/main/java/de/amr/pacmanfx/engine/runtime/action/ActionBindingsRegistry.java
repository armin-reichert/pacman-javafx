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

public interface ActionBindingsRegistry<C> extends Disposable {

    ActionBindingsRegistry<?> NO_BINDINGS = new EmptyActionBindingsRegistry();

    @SuppressWarnings("unchecked")
    static <C> ActionBindingsRegistry<C> empty() {
        return (ActionBindingsRegistry<C>) NO_BINDINGS;
    }

    String name();

    Map<KeyCodeCombination, GameAction<C>> actionBindings();

    Optional<GameAction<C>> findActionMatchingPressedKeys(Keyboard keyboard);

    void bindActionToKeyCombination(GameAction<C> action, KeyCodeCombination combination);

    void selectAnyMatchingBinding(GameAction<C> action, Set<ActionKeyBinding> bindings);

    void registerAllBindings(Set<ActionKeyBinding> bindings);
}
