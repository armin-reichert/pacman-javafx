/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.engine.action;

import de.amr.pacmanfx.engine.EngineCoreContext;
import de.amr.pacmanfx.engine.input.Keyboard;
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
public class EmptyActionBindingsRegistry implements ActionBindingsRegistry<EngineCoreContext> {

    @Override
    public String name() {
        return "Empty Action Bindings Set";
    }

    @Override
    public void dispose() {}

    @Override
    public Map<KeyCodeCombination, GameAction<EngineCoreContext>> actionBindings() {
        return Map.of();
    }

    @Override
    public void selectAnyMatchingBinding(GameAction<EngineCoreContext> action, Set<ActionKeyBinding<EngineCoreContext>> bindings) {}

    @Override
    public void bindActionToKeyCombination(GameAction<EngineCoreContext> action, KeyCodeCombination combination) {}

    @Override
    public void registerAllBindings(Set<ActionKeyBinding<EngineCoreContext>> bindings) {}

    @Override
    public Optional<GameAction<EngineCoreContext>> findActionMatchingPressedKeys(Keyboard keyboard) {
        return Optional.empty();
    }
}
