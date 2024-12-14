/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;

public interface GameActionProvider {

    Map<KeyCodeCombination, GameAction> actionBindings();

    /**
     * Hook method where actions are bound to keyboard combinations.
     */
    void bindGameActions();

    default void registerGameActionKeyBindings(Keyboard keyboard) {
        Logger.info("Register key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            keyboard.register(keyCodeCombination, this);
        }
    }

    default void unregisterGameActionKeyBindings(Keyboard keyboard) {
        Logger.info("Unregister key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            keyboard.unregister(keyCodeCombination, this);
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

    default Optional<GameAction> firstMatchedAction(Keyboard keyboard) {
        return actionBindings().entrySet().stream()
            .filter(entry -> keyboard.isMatching(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst();
    }

    default void handleInput(GameContext context) {
        context.ifGameActionTriggeredRunIt(this);
    }
}