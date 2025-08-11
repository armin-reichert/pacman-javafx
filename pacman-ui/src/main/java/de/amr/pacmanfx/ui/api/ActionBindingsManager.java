/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.ui.AbstractGameAction;
import de.amr.pacmanfx.ui.ActionBinding;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ActionBindingsManager {

    Map<KeyCombination, AbstractGameAction> actionByKeyCombination();

    Optional<AbstractGameAction> matchingAction(Keyboard keyboard);

    boolean hasNoEntries();

    void installBindings(Keyboard keyboard);

    void uninstallBindings(Keyboard keyboard);

    void addBinding(ActionBinding actionBinding);

    void bind(AbstractGameAction action, KeyCombination combination);

    /**
     * Finds the first binding in the given list matching the given action and adds it to this map.
     *
     * @param gameAction a game action
     * @param actionBindings an action bindings list
     */
    void useFirst(AbstractGameAction gameAction, Set<ActionBinding> actionBindings);
}