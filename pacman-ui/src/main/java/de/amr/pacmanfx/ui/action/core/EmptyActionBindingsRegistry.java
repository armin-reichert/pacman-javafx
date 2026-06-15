/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action.core;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Null-object implementation of {@link ActionBindingsRegistry}.
 * <p>
 * This implementation performs no action, holds no bindings, and never matches input.
 * It is useful when a subsystem expects a bindings manager but no actual bindings
 * should be active.
 */
public class EmptyActionBindingsRegistry implements ActionBindingsRegistry {

    @Override
    public String name() {
        return "Empty Action Bindings Set";
    }

    @Override
    public void dispose() {}

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return Map.of();
    }

    @Override
    public void selectAnyMatchingBinding(GameAction action, Set<ActionKeyBinding> bindings) {}

    @Override
    public void bindActionToKeyCombination(GameAction action, KeyCodeCombination combination) {}

    @Override
    public void registerAllBindings(Set<ActionKeyBinding> bindings) {}

    @Override
    public Optional<GameAction> findActionMatchingPressedKeys(Keyboard keyboard) {
        return Optional.empty();
    }
}
