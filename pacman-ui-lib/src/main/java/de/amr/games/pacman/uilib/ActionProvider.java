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

    void bindGameActions();

    default void enableActionBindings(Keyboard keyboard) {
        Logger.info("Enabled key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            keyboard.register(keyCodeCombination, this);
        }
    }

    default void disableActionBindings(Keyboard keyboard) {
        Logger.info("Disable key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            keyboard.unregister(keyCodeCombination, this);
        }
    }

    default void bind(Action action, KeyCodeCombination... combinations) {
        for (KeyCodeCombination kcc : combinations) {
            actionBindings().put(kcc, action);
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

    default void handleInput(Keyboard keyboard) {
        runTriggeredAction(keyboard);
    }
}