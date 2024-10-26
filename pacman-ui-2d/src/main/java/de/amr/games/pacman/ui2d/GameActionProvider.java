/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;

public interface GameActionProvider {

    Map<KeyCodeCombination, GameAction> actionBindings();

    default void register(Keyboard keyboard) {
        Logger.info("Register key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            keyboard.register(keyCodeCombination);
        }
    }

    default void unregister(Keyboard keyboard) {
        Logger.info("Unregister key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            keyboard.unregister(keyCodeCombination);
        }
    }

    /**
     * Hook method where actions are bound to keyboard combinations.
     */
    void bindActions();

    default void bindAction(GameAction action, KeyInput keyInput) {
        for (KeyCodeCombination kcc : keyInput.getCombinations()) {
            actionBindings().put(kcc, action);
        }
    }

    default void bindAction(GameAction action, KeyCodeCombination... combinations) {
        bindAction(action, KeyInput.of(combinations));
    }

    default void bindAction(GameAction action, KeyCode... keyCodes) {
        for (KeyCode keyCode : keyCodes) {
            actionBindings().put(new KeyCodeCombination(keyCode), action);
        }
    }

    default Optional<GameAction> firstMatchedAction(Keyboard keyboard) {
        return actionBindings().entrySet().stream()
            .filter(entry -> keyboard.isRegisteredKeyCombinationPressed(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst();
    }

    default void handleInput(GameContext context) {
        context.doFirstCalledAction(this);
    }
}