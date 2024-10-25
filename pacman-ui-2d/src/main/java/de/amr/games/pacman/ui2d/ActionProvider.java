package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.Map;
import java.util.Optional;

public interface ActionProvider {

    Map<KeyCodeCombination, GameAction> actionBindings();

    default void bindAction(KeyInput keyInput, GameAction action) {
        for (KeyCodeCombination kcc : keyInput.getCombinations()) {
            actionBindings().put(kcc, action);
        }
    }

    default void bindAction(KeyCode keyCode, GameAction action) {
        actionBindings().put(new KeyCodeCombination(keyCode), action);
    }

    default Optional<GameAction> firstMatchedAction(Keyboard keyboard) {
        return actionBindings().entrySet().stream()
            .filter(entry -> keyboard.isRegisteredKeyCombinationPressed(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst();
    }
}
