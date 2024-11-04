package de.amr.games.pacman.ui2d.input;

import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.input.KeyCodeCombination;

public record ArcadeKeyAdapterImpl(
    KeyCodeCombination coin,
    KeyCodeCombination start,
    KeyCodeCombination up,
    KeyCodeCombination down,
    KeyCodeCombination left,
    KeyCodeCombination right
) implements ArcadeKeyAdapter {

    @Override
    public KeyCodeCombination key(Arcade.Controls control) {
        return switch (control) {
            case START -> start;
            case COIN -> coin;
            case UP -> up;
            case DOWN -> down;
            case LEFT ->  left;
            case RIGHT -> right;
        };
    }
}
