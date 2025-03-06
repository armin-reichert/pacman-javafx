package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.input.KeyCode;
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

    public static final ArcadeKeyBinding DEFAULT_ARCADE_KEY_BINDING = new ArcadeKeyBinding(
        new KeyCodeCombination(KeyCode.DIGIT5),
        new KeyCodeCombination(KeyCode.DIGIT1),
        new KeyCodeCombination(KeyCode.UP),
        new KeyCodeCombination(KeyCode.DOWN),
        new KeyCodeCombination(KeyCode.LEFT),
        new KeyCodeCombination(KeyCode.RIGHT)
    );


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
