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
public interface JoypadKeyBinding {

    KeyCodeCombination key(NES.Joypad button);

    default void register(Keyboard keyboard) {
        allKeys().forEach(keyboard::register);
    }

    default void unregister(Keyboard keyboard) {
        allKeys().forEach(keyboard::unregister);
    }

    default Stream<KeyCodeCombination> allKeys() {
        return Stream.of(NES.Joypad.values()).map(this::key);
    }
}