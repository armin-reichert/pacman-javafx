/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;

/**
 * @author Armin Reichert
 */
public class Keyboard {

    private static final Set<KeyCodeCombination> registeredInput = new HashSet<>();
    private static final List<KeyCodeCombination> matchingCombinations = new ArrayList<>(3);

    public static void onKeyPressed(KeyEvent e) {
        Logger.debug("Key pressed: {}", e.getCode());
        registeredInput.stream().filter(c -> c.match(e)).forEach(matchingCombinations::add);
    }

    public static void onKeyReleased(KeyEvent e) {
        Logger.debug("Key released: {}", e.getCode());
        matchingCombinations.clear();
    }

    /**
     * @param keyInput key input
     * @return tells if any of the given key combinations is matched by the current keyboard state
     */
    public static boolean pressed(KeyInput keyInput) {
        return Arrays.stream(keyInput.getCombinations()).anyMatch(matchingCombinations::contains);
    }

    public static void register(KeyInput input) {
        registeredInput.addAll(Arrays.asList(input.getCombinations()));
    }
}