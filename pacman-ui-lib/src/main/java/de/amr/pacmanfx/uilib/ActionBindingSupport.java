/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib;

import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;

/**
 * Collection of bindings between actions and key combinations. Implemented by the "views" (game view,
 * editor view, start pages view) and the game scenes.
 */
public interface ActionBindingSupport {

    static Map.Entry<GameAction, Set<KeyCombination>> createBinding(GameAction action, KeyCombination... combinations) {
        return entry(action, Set.of(combinations));
    }

    /**
     * @return the keyboard providing the ke combinations
     */
    Keyboard keyboard();

    /**
     * @return map for storing the key combination to action binding information
     */
    Map<KeyCombination, GameAction> actionBindings();

    default void updateActionBindings() {
        for (KeyCombination combination : actionBindings().keySet()) {
            keyboard().setBinding(combination, this);
        }
        Logger.info("Key bindings updated for {}", getClass().getSimpleName());
    }

    default void deleteActionBindings() {
        for (KeyCombination combination : actionBindings().keySet()) {
            keyboard().removeBinding(combination, this);
        }
        Logger.info("Key bindings cleared for {}", getClass().getSimpleName());
    }

    default void bindActionToKeyCombination(GameAction action, KeyCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionBindings().put(combination, action);
    }

    default Optional<GameAction> matchingAction() {
        return actionBindings().keySet().stream()
            .filter(keyboard()::isMatching)
            .map(actionBindings()::get)
            .findFirst();
    }

    default void runMatchingAction() {
        matchingAction().ifPresent(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Disabled action '{}' not executed", action);
            }
        });
    }

    default void runMatchingActionOrElse(Runnable defaultAction) {
        matchingAction().ifPresentOrElse(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Disabled action '{}' not executed", action);
            }
        }, defaultAction);
    }
}