/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.input;

import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

/**
 * Maps keyboard keys to Arcade machine buttons.
 */
public interface ArcadeKeyBinding {

    ArcadeKeyBinding DEFAULT_BINDING = new ArcadeKeyBinding.Binding(
        new KeyCodeCombination(KeyCode.DIGIT5),
        new KeyCodeCombination(KeyCode.DIGIT1),
        new KeyCodeCombination(KeyCode.UP),
        new KeyCodeCombination(KeyCode.DOWN),
        new KeyCodeCombination(KeyCode.LEFT),
        new KeyCodeCombination(KeyCode.RIGHT)
    );

    record Binding(
        KeyCodeCombination keyInsertCoin,
        KeyCodeCombination keyStart,
        KeyCodeCombination keyUp,
        KeyCodeCombination keyDown,
        KeyCodeCombination keyLeft,
        KeyCodeCombination keyRight)
        implements ArcadeKeyBinding
    {
        @Override
        public KeyCodeCombination key(Arcade.Button button) {
            return switch (button) {
                case START -> keyStart;
                case COIN -> keyInsertCoin;
                case UP -> keyUp;
                case DOWN -> keyDown;
                case LEFT ->  keyLeft;
                case RIGHT -> keyRight;
            };
        }
    }

    KeyCodeCombination key(Arcade.Button button);

    default void register(Keyboard keyboard) {
        keys().forEach(kcc -> keyboard.register(kcc, this));
    }

    default void unregister(Keyboard keyboard) {
        keys().forEach(kcc -> keyboard.unregister(kcc, this));
    }

    default Stream<KeyCodeCombination> keys() {
        return Stream.of(Arcade.Button.values()).map(this::key);
    }
}