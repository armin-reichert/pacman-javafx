/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.input.KeyCodeCombination;

public record ArcadeKeyBinding(
    KeyCodeCombination insertCoinKey,
    KeyCodeCombination startKey,
    KeyCodeCombination upKey,
    KeyCodeCombination downKey,
    KeyCodeCombination leftKey,
    KeyCodeCombination rightKey) {

    public KeyCodeCombination key(Arcade.Button button) {
        return switch (button) {
            case START -> startKey;
            case COIN -> insertCoinKey;
            case UP -> upKey;
            case DOWN -> downKey;
            case LEFT -> leftKey;
            case RIGHT -> rightKey;
        };
    }
}
