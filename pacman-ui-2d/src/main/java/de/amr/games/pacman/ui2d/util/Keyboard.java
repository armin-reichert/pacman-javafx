/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.games.pacman.ui2d.util.KeyInput.key;

/**
 * @author Armin Reichert
 */
public class Keyboard {

    private static final Set<KeyCodeCombination> REGISTERED_COMBINATIONS = new HashSet<>();
    private static final List<KeyCodeCombination> MATCHING_COMBINATIONS = new ArrayList<>(3);

    public static void register(KeyInput input) {
        REGISTERED_COMBINATIONS.addAll(Arrays.asList(input.getCombinations()));
    }

    public static void onKeyPressed(KeyEvent e) {
        Logger.debug("Key pressed: {}", e.getCode());
        REGISTERED_COMBINATIONS.stream()
            .filter(combi -> combi.match(e))
            .forEach(MATCHING_COMBINATIONS::add);
    }

    public static void onKeyReleased(KeyEvent e) {
        Logger.debug("Key released: {}", e.getCode());
        MATCHING_COMBINATIONS.clear();
    }

    /**
     * @param keyInput key input
     * @return tells if any of the given key combinations is matched by the current keyboard state
     */
    public static boolean pressed(KeyInput keyInput) {
        return Arrays.stream(keyInput.getCombinations()).anyMatch(MATCHING_COMBINATIONS::contains);
    }

    public static boolean pressed(KeyCode keyCode) {
        return pressed(KeyInput.of(key(keyCode)));
    }
}