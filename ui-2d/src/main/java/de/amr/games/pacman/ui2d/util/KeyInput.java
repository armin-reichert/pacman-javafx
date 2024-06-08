package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class KeyInput {

    private final KeyCodeCombination[] combinations;

    public static KeyInput of(KeyCodeCombination... combinations) {
        var key = new KeyInput(combinations);
        Keyboard.register(key);
        return key;
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