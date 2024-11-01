package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

/**
 * @ see <a href="https://www.nesdev.org/wiki/Standard_controller">here</a>
 */
public interface NES_Controller {

    NES_Controller DEFAULT_CONTROLLER = new NES_Controller() {
        @Override
        public KeyCodeCombination a() {
            // use "N" because it is located right of "B" on Western keyboards
            return KeyInput.kcc(KeyCode.N);
        }

        @Override
        public KeyCodeCombination b() {
            return KeyInput.kcc(KeyCode.B);
        }

        @Override
        public KeyCodeCombination select() {
            return KeyInput.kcc(KeyCode.TAB);
        }

        @Override
        public KeyCodeCombination start() {
            return KeyInput.kcc(KeyCode.ENTER);
        }

        @Override
        public KeyCodeCombination up() {
            return KeyInput.kcc(KeyCode.UP);
        }

        @Override
        public KeyCodeCombination down() {
            return KeyInput.kcc(KeyCode.DOWN);
        }

        @Override
        public KeyCodeCombination left() {
            return KeyInput.kcc(KeyCode.LEFT);
        }

        @Override
        public KeyCodeCombination right() {
            return KeyInput.kcc(KeyCode.RIGHT);
        }
    };

    KeyCodeCombination a();
    KeyCodeCombination b();

    KeyCodeCombination select();
    KeyCodeCombination start();

    KeyCodeCombination up();
    KeyCodeCombination down();
    KeyCodeCombination left();
    KeyCodeCombination right();

    default Stream<KeyCodeCombination> allKeys() {
        return Stream.of(a(), b(), select(), start(), up(), down(),left(), right());
    }
}