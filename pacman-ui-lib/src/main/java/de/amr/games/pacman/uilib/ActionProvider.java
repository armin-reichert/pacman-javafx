/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import de.amr.games.pacman.uilib.input.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;

public interface ActionProvider {

    Keyboard keyboard();
    Map<KeyCodeCombination, Action> actionBindings();

    void bindActions();

    default void enableActionBindings() {
        Logger.info("Enabled key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination combination : actionBindings().keySet()) {
            keyboard().bind(combination, this);
        }
    }

    default void disableActionBindings() {
        Logger.info("Disable key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination combination : actionBindings().keySet()) {
            keyboard().unbind(combination, this);
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

    default Optional<Action> firstTriggeredAction() {
        return actionBindings().keySet().stream()
            .filter(keyboard()::isMatching)
            .map(actionBindings()::get)
            .findFirst();
    }

    default void runTriggeredAction() {
        firstTriggeredAction().ifPresent(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Disabled action '{}' not executed", action);
            }
        });
    }

    default void runTriggeredActionElse(Runnable defaultAction) {
        firstTriggeredAction().ifPresentOrElse(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Disabled action '{}' not executed", action);
            }
        }, defaultAction);
    }

    default void handleKeyboardInput() {
        runTriggeredAction();
    }
}