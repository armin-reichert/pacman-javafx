/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Null-object implementation of {@link ActionBindingsSet}.
 * <p>
 * This implementation performs no action, holds no bindings, and never matches input.
 * It is useful when a subsystem expects a bindings manager but no actual bindings
 * should be active.
 */
public class NullActionBindingsManager implements ActionBindingsSet {

    @Override
    public void dispose() {
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionForKeyCombination() {
        return Map.of();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void activate() {}

    @Override
    public void deactivate() {}

    @Override
    public void registerAnyBindingFromSet(GameAction action, Set<ActionBinding> bindings) {
    }

    @Override
    public void setKeyCombinationFor(GameAction action, KeyCodeCombination combination) {
    }

    @Override
    public void registerAllBindingsFromSet(Set<ActionBinding> bindings) {
    }

    @Override
    public Optional<GameAction> matchingAction(Keyboard keyboard) {
        return Optional.empty();
    }
}
