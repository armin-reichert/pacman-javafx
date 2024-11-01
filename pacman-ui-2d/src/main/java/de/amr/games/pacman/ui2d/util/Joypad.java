package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

/**
 * @ see <a href="https://www.nesdev.org/wiki/Standard_controller">here</a>
 */
public interface Joypad {

    KeyCodeCombination select();
    KeyCodeCombination start();

    KeyCodeCombination a();
    KeyCodeCombination b();

    KeyCodeCombination up();
    KeyCodeCombination down();
    KeyCodeCombination left();
    KeyCodeCombination right();

    default Stream<KeyCodeCombination> allKeys() {
        return Stream.of(a(), b(), select(), start(), up(), down(),left(), right());
    }
}