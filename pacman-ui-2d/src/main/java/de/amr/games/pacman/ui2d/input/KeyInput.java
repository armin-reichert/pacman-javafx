/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.input;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Armin Reichert
 */
public class KeyInput {

    private final KeyCodeCombination[] combinations;

    public static KeyInput of(KeyCodeCombination... combinations) {
        return new KeyInput(combinations);
    }

    private KeyInput(KeyCodeCombination... combinations) {
        this.combinations = combinations;
    }

    public KeyCodeCombination[] getCombinations() {
        return combinations;
    }


    @Override
    public String toString() {
        return Arrays.stream(combinations)
            .map(KeyCodeCombination::toString)
            .map(s -> "[" +s + "]")
            .collect(Collectors.joining(", "));
    }
}