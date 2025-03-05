/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.input;

import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

/**
 * @ see <a href="https://www.nesdev.org/wiki/Standard_controller">here</a>
 */
public interface JoypadKeyBinding {

    // My current bindings, might be crap
    JoypadKeyBinding JOYPAD_CURSOR_KEYS = new JoypadKeyBinding.Binding(
        new KeyCodeCombination(KeyCode.SPACE),
        new KeyCodeCombination(KeyCode.ENTER),
        new KeyCodeCombination(KeyCode.B),
        new KeyCodeCombination(KeyCode.N),
        new KeyCodeCombination(KeyCode.UP),
        new KeyCodeCombination(KeyCode.DOWN),
        new KeyCodeCombination(KeyCode.LEFT),
        new KeyCodeCombination(KeyCode.RIGHT)
    );

    // Like Mesen emulator key set #2
    JoypadKeyBinding JOYPAD_WASD = new JoypadKeyBinding.Binding(
        new KeyCodeCombination(KeyCode.U),
        new KeyCodeCombination(KeyCode.I),
        new KeyCodeCombination(KeyCode.J),
        new KeyCodeCombination(KeyCode.K),
        new KeyCodeCombination(KeyCode.W),
        new KeyCodeCombination(KeyCode.S),
        new KeyCodeCombination(KeyCode.A),
        new KeyCodeCombination(KeyCode.D)
    );

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
        public KeyCodeCombination key(NES_JoypadButton button) {
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

    KeyCodeCombination key(NES_JoypadButton button);

    default void register(Keyboard keyboard) {
        keys().forEach(kcc -> keyboard.register(kcc, this));
    }

    default void unregister(Keyboard keyboard) {
        keys().forEach(kcc -> keyboard.unregister(kcc, this));
    }

    default Stream<KeyCodeCombination> keys() {
        return Stream.of(NES_JoypadButton.values()).map(this::key);
    }
}