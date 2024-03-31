/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Armin Reichert
 */
public class Keyboard {

    private static final Set<KeyCodeCombination> registeredCombinations = new HashSet<>();
    private static Set<KeyCodeCombination> matchingCombinations = new HashSet<>();

    public static void handleKeyEventsFor(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, Keyboard::updateMatchingCombinations);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, Keyboard::updateMatchingCombinations);
    }

    /**
     * Registers key code combination consisting of  no modifier and given code.
     * @param code key code
     * @return key code combination
     */
    public static KeyCodeCombination just(KeyCode code) {
        return register(new KeyCodeCombination(code));
    }

    /**
     * Registers key code combination consisting of {@code Alt} modifier and given code.
     * @param code key code
     * @return key code combination
     */
    public static KeyCodeCombination alt(KeyCode code) {
        return register(new KeyCodeCombination(code, KeyCombination.ALT_DOWN));
    }

    /**
     * Registers key code combination consisting of {@code Shift} modifier and given code.
     * @param code key code
     * @return key code combination
     */
    public static KeyCodeCombination shift(KeyCode code) {
        return register(new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN));
    }

    /**
     * @param combinations list of key combinations
     * @return tells if any of the given key combinations is matched by the current keyboard state
     */
    public static boolean pressed(KeyCodeCombination... combinations) {
        var match = Arrays.stream(combinations).filter(matchingCombinations::contains).findFirst();
        if (match.isPresent()) {
            Logger.info("Matching key combination found: " + match.get());
            return true;
        }
        return false;
    }

    private static void updateMatchingCombinations(KeyEvent e) {
        matchingCombinations = registeredCombinations.stream()
            .filter(combination -> combination.match(e))
            .collect(Collectors.toSet());
    }

    private static KeyCodeCombination register(KeyCodeCombination combination) {
        registeredCombinations.add(combination);
        return combination;
    }
}