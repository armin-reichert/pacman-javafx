/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCodeCombination;

public record JoypadKeyBinding(KeyCodeCombination select, KeyCodeCombination start,
                               KeyCodeCombination b, KeyCodeCombination a,
                               KeyCodeCombination up, KeyCodeCombination down, KeyCodeCombination left, KeyCodeCombination right)
        implements Joypad {
}
