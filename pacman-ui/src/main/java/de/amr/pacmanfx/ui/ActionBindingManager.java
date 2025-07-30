/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ActionBindingManager {

    Map<KeyCombination, GameAction> actionByCombination();

    boolean isEmpty();

    void updateKeyboard(Keyboard keyboard);

    void removeFromKeyboard(Keyboard keyboard);

    void addBinding(ActionBinding actionBinding);

    void bind(GameAction action, KeyCombination combination);

    /**
     * Finds the first binding in the given list matching the given action and adds it to this map.
     *
     * @param gameAction a game action
     * @param actionBindings an action bindings list
     */
    void use(GameAction gameAction, List<ActionBinding> actionBindings);

    Optional<GameAction> matchingAction(Keyboard keyboard);
}