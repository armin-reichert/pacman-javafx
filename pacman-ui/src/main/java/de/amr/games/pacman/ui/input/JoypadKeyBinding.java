/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.nes.NES_JoypadButtonID;
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
    public KeyCodeCombination key(NES_JoypadButtonID buttonID) {
        return switch (buttonID) {
            case SELECT -> selectButtonKey;
            case START -> startButtonKey;
            case A -> aButtonKey;
            case B -> bButtonKey;
            case UP -> upButtonKey;
            case DOWN -> downButtonKey;
            case LEFT -> leftButtonKey;
            case RIGHT -> rightButtonKey;
        };
    }
}
