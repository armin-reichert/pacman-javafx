/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.input;

import de.amr.pacmanfx.lib.nes.JoypadButton;
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
    public KeyCodeCombination key(JoypadButton buttonID) {
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

    @Override
    public String toString() {
        return "SELECT=[%s] START=[%s] B=[%s] A=[%s] UP=[%s] DOWN=[%s] LEFT=[%s] RIGHT=[%s]".formatted(
            selectButtonKey, startButtonKey, bButtonKey, aButtonKey, upButtonKey, downButtonKey, leftButtonKey, rightButtonKey
        );
    }
}
