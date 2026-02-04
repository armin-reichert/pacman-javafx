/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.input.Keyboard;
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
public class EmptyBindingsManager implements ActionBindingsManager {

    @Override
    public void dispose() {
    }

    @Override
    public Map<KeyCombination, GameAction> actionRegisteredForKeyCombination() {
        return Map.of();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void addAll(Keyboard keyboard) {
    }

    @Override
    public void removeAll(Keyboard keyboard) {
    }

    @Override
    public void registerAnyFrom(GameAction action, Set<ActionBinding> actionBindings) {
    }

    @Override
    public void registerByKeyCombination(GameAction action, KeyCombination combination) {
    }

    @Override
    public void registerAllFrom(Set<ActionBinding> actionBindings) {
    }

    @Override
    public Optional<GameAction> findMatchingAction(Keyboard keyboard) {
        return Optional.empty();
    }
}
