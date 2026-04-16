/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

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
public class NullActionBindingsManager implements ActionBindingsManager {

    @Override
    public void dispose() {
    }

    @Override
    public Map<KeyCombination, GameAction> keyCombinationToActionMap() {
        return Map.of();
    }

    @Override
    public boolean empty() {
        return true;
    }

    @Override
    public void addToKeyboard() {}

    @Override
    public void removeFromKeyboard() {}

    @Override
    public void registerOne(GameAction action, Set<ActionBinding> actionBindings) {
    }

    @Override
    public void bindActionToKeyCombination(GameAction action, KeyCombination combination) {
    }

    @Override
    public void registerAll(Set<ActionBinding> actionBindings) {
    }

    @Override
    public Optional<GameAction> findMatchingAction(Keyboard keyboard) {
        return Optional.empty();
    }
}
