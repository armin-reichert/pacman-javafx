/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

/**
 * Maps keyboard keys to Arcade machine buttons.
 */
public interface ArcadeInput {

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