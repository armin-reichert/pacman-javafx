/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.input;

import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

public interface ArcadeKeyBinding {

    KeyCodeCombination key(Arcade.Controls control);

    default void register(Keyboard keyboard) {
        allKeys().forEach(keyboard::register);
    }

    default void unregister(Keyboard keyboard) {
        allKeys().forEach(keyboard::unregister);
    }

    default Stream<KeyCodeCombination> allKeys() {
        return Stream.of(Arcade.Controls.values()).map(this::key);
    }
}
