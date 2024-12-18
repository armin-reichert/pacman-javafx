package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.input.KeyCodeCombination;

public record ArcadeKeyBinding(
    KeyCodeCombination keyInsertCoin,
    KeyCodeCombination keyStart,
    KeyCodeCombination keyUp,
    KeyCodeCombination keyDown,
    KeyCodeCombination keyLeft,
    KeyCodeCombination keyRight)
    implements ArcadeInput
{
    @Override
    public KeyCodeCombination key(Arcade.Button button) {
        return switch (button) {
            case START -> keyStart;
            case COIN -> keyInsertCoin;
            case UP -> keyUp;
            case DOWN -> keyDown;
            case LEFT -> keyLeft;
            case RIGHT -> keyRight;
        };
    }
}
