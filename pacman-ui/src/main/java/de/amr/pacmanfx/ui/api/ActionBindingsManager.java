/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ActionBindingsManager {

    /**
     * Null object pattern.
     */
    class EmptyBindingsManager implements ActionBindingsManager {

        @Override
        public Map<KeyCombination, GameAction> actionByKeyCombination() {
            return Map.of();
        }

        @Override
        public boolean hasNoEntries() { return true; }

        @Override
        public void attach(Keyboard keyboard) {}

        @Override
        public void release(Keyboard keyboard) {}

        @Override
        public void useFirst(GameAction action, Set<ActionBinding> actionBindings) {}

        @Override
        public void useKeyCombination(GameAction action, KeyCombination combination) {}

        @Override
        public void useAll(Set<ActionBinding> actionBindings) {}

        @Override
        public Optional<GameAction> matchingAction(Keyboard keyboard) { return Optional.empty(); }
    }

    /**
     * The single null object.
     */
    ActionBindingsManager EMPTY = new EmptyBindingsManager();

    Map<KeyCombination, GameAction> actionByKeyCombination();

    Optional<GameAction> matchingAction(Keyboard keyboard);

    boolean hasNoEntries();

    void attach(Keyboard keyboard);

    void release(Keyboard keyboard);

    void useKeyCombination(GameAction action, KeyCombination combination);

    void useFirst(GameAction gameAction, Set<ActionBinding> actionBindings);

    void useAll(Set<ActionBinding> actionBindings);
}