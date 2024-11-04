/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.input;

import de.amr.games.pacman.lib.nes.NES;
import javafx.scene.input.KeyCodeCombination;

public record JoypadKeyBindingData(
    KeyCodeCombination select,
    KeyCodeCombination start,
    KeyCodeCombination b,
    KeyCodeCombination a,
    KeyCodeCombination up,
    KeyCodeCombination down,
    KeyCodeCombination left,
    KeyCodeCombination right
) implements JoypadKeyBinding {

    @Override
    public KeyCodeCombination key(NES.Joypad button) {
        return switch (button) {
            case SELECT -> select;
            case START -> start;
            case B -> b;
            case A -> a;
            case UP -> up;
            case DOWN -> down;
            case LEFT -> left;
            case RIGHT -> right;
        };
    }
}