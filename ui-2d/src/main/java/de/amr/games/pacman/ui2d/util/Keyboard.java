/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.event.EventTarget;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;

/**
 * @author Armin Reichert
 */
public class Keyboard {

    private static final Set<KeyCodeCombination> registeredCombinations = new HashSet<>();
    private static final List<KeyCodeCombination> matchingCombinations = new ArrayList<>(3);

    public static void filterKeyEventsFrom(EventTarget target) {
        target.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            Logger.info("Key press {}", e.getCode());
            matchingCombinations.clear();
            registeredCombinations.stream().filter(c -> c.match(e)).forEach(matchingCombinations::add);
        });
        target.addEventFilter(KeyEvent.KEY_RELEASED, e -> matchingCombinations.clear());
    }

    /**
     * @param input key input
     * @return tells if any of the given key combinations is matched by the current keyboard state
     */
    public static boolean pressed(KeyInput input) {
        var match = Arrays.stream(input.getCombinations()).filter(matchingCombinations::contains).findFirst();
        if (match.isPresent()) {
            Logger.info("Matching key combination: " + match.get());
            return true;
        }
        return false;
    }

    public static void register(KeyInput input) {
        registeredCombinations.addAll(Arrays.asList(input.getCombinations()));
    }
}