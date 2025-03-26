/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.ui.GameUI.THE_KEYBOARD;

public interface GameActionProvider {

    Map<KeyCodeCombination, GameAction> actionBindings();

    /**
     * Hook method where actions are bound to keyboard combinations.
     */
    void bindGameActions();

    default void registerGameActionKeyBindings() {
        Logger.info("Register key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            THE_KEYBOARD.register(keyCodeCombination, this);
        }
    }

    default void unregisterGameActionKeyBindings() {
        Logger.info("Unregister key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            THE_KEYBOARD.unregister(keyCodeCombination, this);
        }
    }

    default void bind(GameAction action, KeyCodeCombination... combinations) {
        for (KeyCodeCombination kcc : combinations) {
            actionBindings().put(kcc, action);
        }
    }

    default void bind(GameAction action, KeyCode... keyCodes) {
        for (KeyCode keyCode : keyCodes) {
            actionBindings().put(new KeyCodeCombination(keyCode), action);
        }
    }

    default Optional<GameAction> firstMatchedAction() {
        return actionBindings().entrySet().stream()
            .filter(entry -> THE_KEYBOARD.isMatching(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst();
    }

    // Actions
    default void ifTriggeredRunAction() {
        firstMatchedAction().ifPresent(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Action '{}' not executed, not enabled", action);
            }
        });
    }

    default void ifTriggeredRunActionElse(Runnable defaultAction) {
        firstMatchedAction().ifPresentOrElse(action -> {
            if (action.isEnabled()) {
                action.execute();
            } else {
                Logger.info("Action '{}' not executed, not enabled", action);
            }
        }, defaultAction);
    }

    default void handleInput() {
        ifTriggeredRunAction();
    }
}