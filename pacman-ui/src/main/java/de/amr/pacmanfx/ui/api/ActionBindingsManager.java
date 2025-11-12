/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
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
        public void assignBindingsToKeyboard(Keyboard keyboard) {}

        @Override
        public void removeBindingsFromKeyboard(Keyboard keyboard) {}

        @Override
        public void bind(GameAction action, Set<ActionBinding> actionBindings) {}

        @Override
        public void setKeyCombination(GameAction action, KeyCombination combination) {}

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

    void assignBindingsToKeyboard(Keyboard keyboard);

    void removeBindingsFromKeyboard(Keyboard keyboard);

    void setKeyCombination(GameAction action, KeyCombination combination);

    void bind(GameAction gameAction, Set<ActionBinding> actionBindings);
}