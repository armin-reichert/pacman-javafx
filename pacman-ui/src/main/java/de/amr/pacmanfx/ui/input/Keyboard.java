/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.input;

import de.amr.pacmanfx.ui.ActionBindingManager;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCombination.*;

/**
 * Handles keyboard input and matching of key combinations against registered action.
 */
public final class Keyboard {

    // Provide an API for the most common cases
    public static KeyCombination nude(KeyCode code) { return new KeyCodeCombination(code); }
    public static KeyCombination alt(KeyCode code) {
        return new KeyCodeCombination(code, ALT_DOWN);
    }
    public static KeyCombination control(KeyCode code) {
        return new KeyCodeCombination(code, CONTROL_DOWN);
    }
    public static KeyCombination shift(KeyCode code) {
        return new KeyCodeCombination(code, SHIFT_DOWN);
    }
    public static KeyCombination alt_shift(KeyCode code) { return new KeyCodeCombination(code, SHIFT_DOWN, ALT_DOWN); }

    public static String format(KeyCombination... combinations) {
        return Arrays.stream(combinations)
            .map(KeyCombination::toString)
            .map(s -> "[" + s + "]")
            .collect(Collectors.joining(", "));
    }

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Map<KeyCombination, ActionBindingManager> actionBindingMaps = new HashMap<>();
    private final List<KeyCombination> matches = new ArrayList<>(3);

    public void setBinding(KeyCombination combination, ActionBindingManager actionBindingSupport) {
        if (actionBindingMaps.get(combination) == actionBindingSupport) {
            Logger.debug("Key combination '{}' already bound to action {}", combination, actionBindingSupport);
        } else {
            actionBindingMaps.put(combination, actionBindingSupport);
            Logger.debug("Key combination '{}' is bound to action {}", combination, actionBindingSupport);
        }
    }

    public void removeBinding(KeyCombination combination, ActionBindingManager client) {
        boolean removed = actionBindingMaps.remove(combination, client);
        if (removed) {
            Logger.debug("Key code combination '{}' bound to {}", combination, client);
        }
    }

    public void onKeyPressed(KeyEvent key) {
        Logger.trace("Key pressed: {}", key);
        pressedKeys.add(key.getCode());
        actionBindingMaps.keySet().stream().filter(combination -> combination.match(key)).forEach(matches::add);
    }

    public void onKeyReleased(KeyEvent key) {
        Logger.trace("Key released: {}", key);
        pressedKeys.remove(key.getCode());
        matches.clear();
    }

    public boolean isMatching(KeyCombination combination) {
        return matches.contains(combination);
    }

    public boolean isPressed(KeyCode keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public void logCurrentBindings() {
        Logger.debug("--------------------------");
        actionBindingMaps.keySet().stream()
            .sorted(Comparator.comparing(KeyCombination::getDisplayText))
            .forEach(combination -> {
                ActionBindingManager actionBindingManager = actionBindingMaps.get(combination);
                Logger.debug("{}: {}", combination, actionBindingManager.getClass().getSimpleName());
        });
        Logger.debug("--------------------------");
    }
}