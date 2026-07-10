/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.input;

import javafx.scene.input.KeyCodeCombination;

import static java.util.Objects.requireNonNull;

public record JoypadKeyBinding(
    KeyCodeCombination select,
    KeyCodeCombination start,
    KeyCodeCombination b,
    KeyCodeCombination a,
    KeyCodeCombination up,
    KeyCodeCombination down,
    KeyCodeCombination left,
    KeyCodeCombination right)
{
    public KeyCodeCombination key(JoypadButton buttonID) {
        requireNonNull(buttonID);
        return switch (buttonID) {
            case SELECT -> select;
            case START -> start;
            case A -> a;
            case B -> b;
            case UP -> up;
            case DOWN -> down;
            case LEFT -> left;
            case RIGHT -> right;
        };
    }

    @Override
    public String toString() {
        return "SELECT=[%s] START=[%s] B=[%s] A=[%s] UP=[%s] DOWN=[%s] LEFT=[%s] RIGHT=[%s]".formatted(
            select, start, b, a, up, down, left, right
        );
    }
}
