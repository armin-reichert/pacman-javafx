/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

/**
 * @ see <a href="https://www.nesdev.org/wiki/Standard_controller">here</a>
 */
public interface Joypad {

    KeyCodeCombination select();
    KeyCodeCombination start();

    KeyCodeCombination a();
    KeyCodeCombination b();

    KeyCodeCombination up();
    KeyCodeCombination down();
    KeyCodeCombination left();
    KeyCodeCombination right();

    default void register(Keyboard keyboard) {
        allKeys().forEach(keyboard::register);
    }

    default void unregister(Keyboard keyboard) {
        allKeys().forEach(keyboard::unregister);
    }

    default Stream<KeyCodeCombination> allKeys() {
        return Stream.of(a(), b(), select(), start(), up(), down(),left(), right());
    }
}