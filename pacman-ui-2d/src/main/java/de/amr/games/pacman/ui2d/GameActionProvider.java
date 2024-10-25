package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface GameActionProvider {

    Map<KeyCodeCombination, GameAction> actionBindings();

    default void register(Keyboard keyboard) {
        Logger.info("Register key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            keyboard.register(keyCodeCombination);
        }
    }

    default void register(Keyboard keyboard, KeyCodeCombination kcc) {

    }

    default void unregister(Keyboard keyboard) {
        Logger.info("Unregister key bindings for {}", getClass().getSimpleName());
        for (KeyCodeCombination keyCodeCombination : actionBindings().keySet()) {
            keyboard.unregister(keyCodeCombination);
        }
    }

    default void bindAction(KeyInput keyInput, GameAction action) {
        for (KeyCodeCombination kcc : keyInput.getCombinations()) {
            actionBindings().put(kcc, action);
        }
    }

    default void unbindAction(KeyInput keyInput, GameAction action) {
        for (KeyCodeCombination kcc : keyInput.getCombinations()) {
            actionBindings().remove(kcc, action);
        }
    }

    default void bindAction(KeyCode keyCode, GameAction action) {
        actionBindings().put(new KeyCodeCombination(keyCode), action);
    }

    // TODO check if this works for different key object instances
    default void unbindAction(KeyCode keyCode, GameAction action) {
        actionBindings().remove(new KeyCodeCombination(keyCode), action);
    }

    default Optional<GameAction> firstMatchedAction(Keyboard keyboard) {
        return actionBindings().entrySet().stream()
            .filter(entry -> keyboard.isRegisteredKeyCombinationPressed(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst();
    }
}
