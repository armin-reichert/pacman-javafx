/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.input;

import de.amr.games.pacman.lib.nes.NES;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

/**
 * @ see <a href="https://www.nesdev.org/wiki/Standard_controller">here</a>
 */
public interface JoypadKeyAdapter {

    record Definition(
        KeyCodeCombination select,
        KeyCodeCombination start,
        KeyCodeCombination b,
        KeyCodeCombination a,
        KeyCodeCombination up,
        KeyCodeCombination down,
        KeyCodeCombination left,
        KeyCodeCombination right) implements JoypadKeyAdapter
    {
        @Override
        public KeyCodeCombination keyCombination(NES.Joypad control) {
            return switch (control) {
                case SELECT -> select;
                case START -> start;
                case B -> b;
                case A -> a;
                case UP -> up;
                case DOWN -> down;
                case LEFT -> left;
                case RIGHT -> right;
            };
        }
    }

    KeyCodeCombination keyCombination(NES.Joypad control);

    default void register(Keyboard keyboard) {
        allKeys().forEach(kcc -> keyboard.register(kcc, this));
    }

    default void unregister(Keyboard keyboard) {
        allKeys().forEach(kcc -> keyboard.unregister(kcc, this));
    }

    default Stream<KeyCodeCombination> allKeys() {
        return Stream.of(NES.Joypad.values()).map(this::keyCombination);
    }
}