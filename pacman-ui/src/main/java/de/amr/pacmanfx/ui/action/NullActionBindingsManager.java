/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import javafx.scene.input.KeyCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Null-object implementation of {@link ActionBindingsManager}.
 * <p>
 * This implementation performs no action, holds no bindings, and never matches input.
 * It is useful when a subsystem expects a bindings manager but no actual bindings
 * should be active.
 */
public class NullActionBindingsManager implements ActionBindingsManager {

    @Override
    public void dispose() {
    }

    @Override
    public Map<KeyCombination, GameAction> keyCombinationToActionMap() {
        return Map.of();
    }

    @Override
    public boolean noBindings() {
        return true;
    }

    @Override
    public void pluginKeyboard() {}

    @Override
    public void unplugKeyboard() {}

    @Override
    public void bindOne(GameAction action, Set<ActionBinding> bindings) {
    }

    @Override
    public void bind(GameAction action, KeyCombination combination) {
    }

    @Override
    public void bindAll(Set<ActionBinding> bindings) {
    }

    @Override
    public Optional<GameAction> matchingAction() {
        return Optional.empty();
    }
}
