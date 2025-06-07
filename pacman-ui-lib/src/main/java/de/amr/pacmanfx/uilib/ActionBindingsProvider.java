/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib;

import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Collection of bindings between actions and key combinations. Implemented by the "views" (game view,
 * editor view, start pages view) and the game scenes.
 */
public interface ActionBindingsProvider {

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

    default void bind(GameAction action, KeyCombination... combinations) {
        requireNonNull(action);
        requireNonNull(combinations);
        for (KeyCombination combination : combinations) {
            actionBindings().put(combination, action);
        }
    }

    default void bind(GameAction action, List<KeyCombination> combinations) {
        requireNonNull(action);
        requireNonNull(combinations);
        for (KeyCombination combination : combinations) {
            actionBindings().put(combination, action);
        }
    }

    default void bind(GameAction action, KeyCode... keyCodes) {
        requireNonNull(keyCodes);
        for (KeyCode keyCode : keyCodes) {
            actionBindings().put(new KeyCodeCombination(keyCode), action);
        }
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

    default void handleKeyboardInput() { runMatchingAction(); }
}