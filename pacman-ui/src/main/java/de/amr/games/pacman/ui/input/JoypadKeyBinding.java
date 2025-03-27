/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import javafx.scene.input.KeyCodeCombination;

public record JoypadKeyBinding(
        KeyCodeCombination selectButtonKey,
        KeyCodeCombination startButtonKey,
        KeyCodeCombination bButtonKey,
        KeyCodeCombination aButtonKey,
        KeyCodeCombination upButtonKey,
        KeyCodeCombination downButtonKey,
        KeyCodeCombination leftButtonKey,
        KeyCodeCombination rightButtonKey)
{
    public KeyCodeCombination key(NES_JoypadButton button) {
        return switch (button) {
            case BUTTON_SELECT -> selectButtonKey;
            case BUTTON_START -> startButtonKey;
            case BUTTON_B -> bButtonKey;
            case BUTTON_A -> aButtonKey;
            case BUTTON_UP -> upButtonKey;
            case BUTTON_DOWN -> downButtonKey;
            case BUTTON_LEFT -> leftButtonKey;
            case BUTTON_RIGHT -> rightButtonKey;
        };
    }
}
