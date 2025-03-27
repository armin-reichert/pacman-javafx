/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import javafx.scene.input.KeyCodeCombination;

public record Joypad(
        KeyCodeCombination select,
        KeyCodeCombination start,
        KeyCodeCombination b,
        KeyCodeCombination a,
        KeyCodeCombination up,
        KeyCodeCombination down,
        KeyCodeCombination left,
        KeyCodeCombination right)
{
    public KeyCodeCombination key(NES_JoypadButton button) {
        return switch (button) {
            case BUTTON_SELECT -> select;
            case BUTTON_START -> start;
            case BUTTON_B -> b;
            case BUTTON_A -> a;
            case BUTTON_UP -> up;
            case BUTTON_DOWN -> down;
            case BUTTON_LEFT -> left;
            case BUTTON_RIGHT -> right;
        };
    }
}
