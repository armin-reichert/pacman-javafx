/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.input;

import javafx.event.EventTarget;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;

/**
 * @author Armin Reichert
 */
public class Keyboard {

    private static final Set<KeyCodeCombination> registeredCombinations = new HashSet<>();
    private static final List<KeyCodeCombination> matchingCombinations = new ArrayList<>(3);

    public static void handleKeyEventsFor(EventTarget target) {
        target.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            matchingCombinations.clear();
            registeredCombinations.stream().filter(c -> c.match(e)).forEach(matchingCombinations::add);
        });
        target.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            matchingCombinations.clear();
        });
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
     * Registers key code combination consisting of {@code Control} modifier and given code.
     * @param code key code
     * @return key code combination
     */
    public static KeyCodeCombination control(KeyCode code) {
        return register(new KeyCodeCombination(code, KeyCombination.CONTROL_DOWN));
    }

    /**
     * @param combinations list of key combinations
     * @return tells if any of the given key combinations is matched by the current keyboard state
     */
    public static boolean pressed(KeyCodeCombination... combinations) {
        var match = Arrays.stream(combinations).filter(matchingCombinations::contains).findFirst();
        if (match.isPresent()) {
            Logger.info("Matching key combination: " + match.get());
            return true;
        }
        return false;
    }

    private static KeyCodeCombination register(KeyCodeCombination combination) {
        registeredCombinations.add(combination);
        return combination;
    }
}