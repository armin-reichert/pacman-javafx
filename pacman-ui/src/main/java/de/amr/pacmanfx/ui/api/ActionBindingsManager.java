/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.ui.ActionBinding;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ActionBindingsManager {

    Map<KeyCombination, GameAction> actionByKeyCombination();

    Optional<GameAction> matchingAction(Keyboard keyboard);

    boolean hasNoEntries();

    void assignBindingsToKeyboard(Keyboard keyboard);

    void removeBindingsFromKeyboard(Keyboard keyboard);

    void setKeyCombination(GameAction action, KeyCombination combination);

    /**
     * Assigns the binding for the given action to this map.
     *
     * @param gameAction a game action
     * @param actionBindings an action bindings list
     */
    void useBindings(GameAction gameAction, Set<ActionBinding> actionBindings);
}