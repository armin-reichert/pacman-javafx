/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.input;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.HashSet;
import java.util.Set;

import static javafx.scene.input.KeyCombination.*;

public class KeyCodeCombinationBuilder {
    private final Set<KeyCombination.Modifier> modifiers = new HashSet<>();

    public static KeyCodeCombinationBuilder combine() {
        return new KeyCodeCombinationBuilder();
    }

    public static KeyCodeCombination bareKey(KeyCode code) { return new KeyCodeCombination(code); }

    public KeyCodeCombinationBuilder alt() {
        modifiers.add(ALT_DOWN);
        return this;
    }

    public KeyCodeCombinationBuilder ctrl() {
        modifiers.add(CONTROL_DOWN);
        return this;
    }

    public KeyCodeCombinationBuilder shift() {
        modifiers.add(SHIFT_DOWN);
        return this;
    }

    public KeyCodeCombination key(KeyCode code) {
        return new KeyCodeCombination(code, modifiers.toArray(new Modifier[0]));
    }
}
