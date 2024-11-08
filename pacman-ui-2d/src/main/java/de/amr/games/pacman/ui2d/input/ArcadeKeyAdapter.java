/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.input;

import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

public interface ArcadeKeyAdapter {

    KeyCodeCombination key(Arcade.Controls control);

    default void register(Keyboard keyboard) {
        allKeys().forEach(kcc -> keyboard.register(kcc, this));
    }

    default void unregister(Keyboard keyboard) {
        allKeys().forEach(kcc -> keyboard.unregister(kcc, this));
    }

    default Stream<KeyCodeCombination> allKeys() {
        return Stream.of(Arcade.Controls.values()).map(this::key);
    }
}
