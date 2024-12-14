/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.nes.NES;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

/**
 * @ see <a href="https://www.nesdev.org/wiki/Standard_controller">here</a>
 */
public interface JoypadKeyBinding {

    record Binding(
        KeyCodeCombination keySelect,
        KeyCodeCombination keyStart,
        KeyCodeCombination keyB,
        KeyCodeCombination keyA,
        KeyCodeCombination keyUp,
        KeyCodeCombination keyDown,
        KeyCodeCombination keyLeft,
        KeyCodeCombination keyRight)
        implements JoypadKeyBinding
    {
        @Override
        public KeyCodeCombination key(NES.JoypadButton button) {
            return switch (button) {
                case BTN_SELECT -> keySelect;
                case BTN_START -> keyStart;
                case BTN_B -> keyB;
                case BTN_A -> keyA;
                case BTN_UP -> keyUp;
                case BTN_DOWN -> keyDown;
                case BTN_LEFT -> keyLeft;
                case BTN_RIGHT -> keyRight;
            };
        }
    }

    KeyCodeCombination key(NES.JoypadButton button);

    default void register(Keyboard keyboard) {
        keys().forEach(kcc -> keyboard.register(kcc, this));
    }

    default void unregister(Keyboard keyboard) {
        keys().forEach(kcc -> keyboard.unregister(kcc, this));
    }

    default Stream<KeyCodeCombination> keys() {
        return Stream.of(NES.JoypadButton.values()).map(this::key);
    }
}