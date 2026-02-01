/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages the mapping between keyboard input and high-level {@link GameAction}s.
 * <p>
 * An {@code ActionBindingsManager} is responsible for:
 * <ul>
 *   <li>tracking which {@link KeyCombination}s trigger which actions,</li>
 *   <li>activating and releasing bindings on a {@link Keyboard} instance,</li>
 *   <li>resolving the currently pressed keys into a matching {@link GameAction},</li>
 *   <li>supporting dynamic rebinding at runtime.</li>
 * </ul>
 *
 * Implementations typically maintain an internal map from key combinations to actions
 * and update the {@link Keyboard} with listeners or state transitions when bindings
 * are activated or released.
 *
 * <h2>Lifecycle</h2>
 * <ul>
 *   <li>{@link #activateBindings(Keyboard)} registers all known bindings with the keyboard.</li>
 *   <li>{@link #removeAllBindings(Keyboard)} unregisters them again.</li>
 *   <li>{@link #dispose()} frees any internal resources (if needed).</li>
 * </ul>
 *
 * <h2>Null Object</h2>
 * {@link #EMPTY} provides a safe no-op implementation that never matches actions
 * and performs no registration. This avoids null checks in client code.
 */
public interface ActionBindingsManager extends Disposable {

    /**
     * Null-object implementation of {@link ActionBindingsManager}.
     * <p>
     * This implementation performs no action, holds no bindings, and never matches input.
     * It is useful when a subsystem expects a bindings manager but no actual bindings
     * should be active.
     */
    class EmptyBindingsManager implements ActionBindingsManager {

        @Override
        public void dispose() {}

        @Override
        public Map<KeyCombination, GameAction> actionForKeyCombination() {
            return Map.of();
        }

        @Override
        public boolean hasNoBindings() {
            return true;
        }

        @Override
        public void activateBindings(Keyboard keyboard) {}

        @Override
        public void removeAllBindings(Keyboard keyboard) {}

        @Override
        public void registerAnyBindingFrom(GameAction action, Set<ActionBinding> actionBindings) {}

        @Override
        public void triggerActionByKeyCombination(GameAction action, KeyCombination combination) {}

        @Override
        public void registerAllBindingsFrom(Set<ActionBinding> actionBindings) {}

        @Override
        public Optional<GameAction> matchingAction(Keyboard keyboard) {
            return Optional.empty();
        }
    }

    /**
     * The global null-object instance.
     */
    ActionBindingsManager EMPTY = new EmptyBindingsManager();

    /**
     * Returns an immutable view of all key combinations currently bound to actions.
     *
     * @return a map from {@link KeyCombination} to {@link GameAction}
     */
    Map<KeyCombination, GameAction> actionForKeyCombination();

    /**
     * Determines whether the current keyboard state matches any registered action.
     * <p>
     * Implementations typically inspect the pressed keys on the given {@link Keyboard}
     * and compare them against known {@link KeyCombination}s.
     *
     * @param keyboard the keyboard to inspect
     * @return the matching action, or an empty {@code Optional} if none match
     */
    Optional<GameAction> matchingAction(Keyboard keyboard);

    /**
     * Indicates whether this manager currently holds no bindings.
     *
     * @return {@code true} if no key combinations are registered
     */
    boolean hasNoBindings();

    /**
     * Activates all known bindings on the given keyboard.
     * <p>
     * This typically means registering listeners or updating internal keyboard state
     * so that {@link #matchingAction(Keyboard)} can resolve actions.
     *
     * @param keyboard the keyboard on which to activate bindings
     */
    void activateBindings(Keyboard keyboard);

    /**
     * Removes all bindings previously activated on the given keyboard.
     * <p>
     * After calling this method, no key combinations should trigger actions.
     *
     * @param keyboard the keyboard from which to remove bindings
     */
    void removeAllBindings(Keyboard keyboard);

    /**
     * Assigns a new key combination to the given action, replacing any previous binding.
     *
     * @param action      the action to rebind
     * @param combination the new key combination
     */
    void triggerActionByKeyCombination(GameAction action, KeyCombination combination);

    /**
     * Registers exactly one binding for the given action from the provided set.
     * <p>
     * Implementations may choose the first valid binding, the preferred one,
     * or apply custom selection logic.
     *
     * @param action         the action to bind
     * @param actionBindings the candidate bindings
     */
    void registerAnyBindingFrom(GameAction action, Set<ActionBinding> actionBindings);

    /**
     * Registers all provided bindings.
     * <p>
     * This is typically used when loading predefined binding sets (e.g., cheat keys,
     * steering keys, or debug keys).
     *
     * @param actionBindings the bindings to register
     */
    void registerAllBindingsFrom(Set<ActionBinding> actionBindings);
}
