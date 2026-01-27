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

public interface ActionBindingsManager extends Disposable {

    /**
     * Null object pattern.
     */
    class EmptyBindingsManager implements ActionBindingsManager {

        @Override
        public void dispose() {}

        @Override
        public Map<KeyCombination, GameAction> actionForKeyCombination() {
            return Map.of();
        }

        @Override
        public boolean hasNoBindings() { return true; }

        @Override
        public void activateBindings(Keyboard keyboard) {}

        @Override
        public void releaseBindings(Keyboard keyboard) {}

        @Override
        public void useAnyBinding(GameAction action, Set<ActionBinding> actionBindings) {}

        @Override
        public void setKeyCombination(GameAction action, KeyCombination combination) {}

        @Override
        public void useAllBindings(Set<ActionBinding> actionBindings) {}

        @Override
        public Optional<GameAction> matchingAction(Keyboard keyboard) { return Optional.empty(); }
    }

    /**
     * The single null object.
     */
    ActionBindingsManager EMPTY = new EmptyBindingsManager();

    Map<KeyCombination, GameAction> actionForKeyCombination();

    Optional<GameAction> matchingAction(Keyboard keyboard);

    boolean hasNoBindings();

    void activateBindings(Keyboard keyboard);

    void releaseBindings(Keyboard keyboard);

    void setKeyCombination(GameAction action, KeyCombination combination);

    void useAnyBinding(GameAction gameAction, Set<ActionBinding> actionBindings);

    void useAllBindings(Set<ActionBinding> actionBindings);
}