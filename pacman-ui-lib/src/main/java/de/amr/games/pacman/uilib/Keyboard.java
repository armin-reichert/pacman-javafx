/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Armin Reichert
 */
public class Keyboard {

    public static KeyCodeCombination naked(KeyCode code) {
        return new KeyCodeCombination(code);
    }

    public static KeyCodeCombination alt(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
    }

    public static KeyCodeCombination shift(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
    }

    public static KeyCodeCombination shift_alt(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN);
    }

    public static KeyCodeCombination control(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.CONTROL_DOWN);
    }

    public static String format(KeyCodeCombination... combinations) {
        return Arrays.stream(combinations)
            .map(KeyCodeCombination::toString)
            .map(s -> "[" + s + "]")
            .collect(Collectors.joining(", "));
    }

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Map<KeyCodeCombination, ActionProvider> knownCombinations = new HashMap<>();
    private final List<KeyCodeCombination> matches = new ArrayList<>(3);

    public void register(KeyCodeCombination combination, ActionProvider client) {
        if (knownCombinations.get(combination) == client) {
            Logger.debug("Key code combination '{}' already registered by {}", combination, client);
        } else {
            knownCombinations.put(combination, client);
            Logger.debug("Key code combination '{}' registered by {}", combination, client);
        }
    }

    public void unregister(KeyCodeCombination combination, ActionProvider client) {
        boolean removed = knownCombinations.remove(combination, client);
        if (removed) {
            Logger.debug("Key code combination '{}' removed by {}", combination, client);
        }
    }

    public void onKeyPressed(KeyEvent key) {
        pressedKeys.add(key.getCode());
        knownCombinations.keySet().stream().filter(kcc -> kcc.match(key)).forEach(matches::add);
        Logger.debug("Key pressed: {}", key);
    }

    public void onKeyReleased(KeyEvent key) {
        pressedKeys.remove(key.getCode());
        matches.clear();
        Logger.debug("Key released: {}", key);
    }

    public boolean isMatching(KeyCodeCombination combination) {
        return matches.contains(combination);
    }

    public boolean isPressed(KeyCode keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public void logCurrentBindings() {
        Logger.info("--------------------------");
        knownCombinations.keySet().stream()
            .sorted(Comparator.comparing(KeyCodeCombination::getDisplayText))
            .forEach(combination -> {
                ActionProvider actionProvider = knownCombinations.get(combination);
                Logger.info("{}: {}", combination, actionProvider.getClass().getSimpleName());
        });
        Logger.info("--------------------------");
    }
}