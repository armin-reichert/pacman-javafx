/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;

public interface ActionProvider {

    Map<KeyCodeCombination, Action> actionBindings();

    void bindActions();

    default void enableActionBindings(Keyboard keyboard) {
        Logger.info("Enabled key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination combination : actionBindings().keySet()) {
            keyboard.bind(combination, this);
        }
    }

    default void disableActionBindings(Keyboard keyboard) {
        Logger.info("Disable key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination combination : actionBindings().keySet()) {
            keyboard.unbind(combination, this);
        }
    }

    default void bind(Action action, KeyCodeCombination... combinations) {
        for (KeyCodeCombination combination : combinations) {
            actionBindings().put(combination, action);
        }
    }

    default void bind(Action action, KeyCode... keyCodes) {
        for (KeyCode keyCode : keyCodes) {
            actionBindings().put(new KeyCodeCombination(keyCode), action);
        }
    }

    default Optional<Action> firstTriggeredAction(Keyboard keyboard) {
        return actionBindings().keySet().stream()
            .filter(keyboard::isMatching)
            .map(actionBindings()::get)
            .findFirst();
    }

    default void runTriggeredAction(Keyboard keyboard) {
        firstTriggeredAction(keyboard).ifPresent(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Disabled action '{}' not executed", action);
            }
        });
    }

    default void runTriggeredActionElse(Keyboard keyboard, Runnable defaultAction) {
        firstTriggeredAction(keyboard).ifPresentOrElse(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Disabled action '{}' not executed", action);
            }
        }, defaultAction);
    }

    default void handleKeyboardInput(Keyboard keyboard) {
        runTriggeredAction(keyboard);
    }
}