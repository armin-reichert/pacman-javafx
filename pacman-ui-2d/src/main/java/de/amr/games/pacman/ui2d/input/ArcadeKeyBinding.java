package de.amr.games.pacman.ui2d.input;

import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

public record ArcadeKeyBinding(
    KeyCodeCombination keyInsertCoin,
    KeyCodeCombination keyStart,
    KeyCodeCombination keyUp,
    KeyCodeCombination keyDown,
    KeyCodeCombination keyLeft,
    KeyCodeCombination keyRight)
{
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

    public Stream<KeyCodeCombination> keys() {
        return Stream.of(Arcade.Button.values()).map(this::key);
    }
}
