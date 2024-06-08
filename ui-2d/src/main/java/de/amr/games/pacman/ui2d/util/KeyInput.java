/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * @author Armin Reichert
 */
public class KeyInput {

    private final KeyCodeCombination[] combinations;

    public static KeyInput of(KeyCodeCombination... combinations) {
        var keyInput = new KeyInput(combinations);
        Keyboard.register(keyInput);
        return keyInput;
    }

    private KeyInput(KeyCodeCombination... combinations) {
        this.combinations = combinations;
    }

    public KeyCodeCombination[] getCombinations() {
        return combinations;
    }

    public static KeyCodeCombination key(KeyCode code) {
        return new KeyCodeCombination(code);
    }

    public static KeyCodeCombination alt(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
    }

    public static KeyCodeCombination shift(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
    }

    public static KeyCodeCombination shift_alt(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN);
    }

    public static KeyCodeCombination control(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.CONTROL_DOWN);
    }
}