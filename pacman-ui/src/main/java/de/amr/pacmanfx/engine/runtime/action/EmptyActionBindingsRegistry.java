/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.engine.runtime.action;

import de.amr.pacmanfx.engine.action.GameAction;
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
public class EmptyActionBindingsRegistry implements ActionBindingsRegistry {

    @Override
    public String name() {
        return "Empty Action Bindings Set";
    }

    @Override
    public void dispose() {}

    @Override
    public Map<KeyCodeCombination, GameAction<GameEngineContext>> actionBindings() {
        return Map.of();
    }

    @Override
    public void selectAnyMatchingBinding(GameAction<GameEngineContext> action, Set<ActionKeyBinding> bindings) {}

    @Override
    public void bindActionToKeyCombination(GameAction<GameEngineContext> action, KeyCodeCombination combination) {}

    @Override
    public void registerAllBindings(Set<ActionKeyBinding> bindings) {}

    @Override
    public Optional<GameAction<GameEngineContext>> findActionMatchingPressedKeys(Keyboard keyboard) {
        return Optional.empty();
    }
}
