/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.input;

import de.amr.pacmanfx.ui.ActionBindingsManager;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;

import static javafx.scene.input.KeyCombination.*;

/**
 * Handles keyboard input and matching of key combinations against registered action.
 */
public final class Keyboard {

    // Provide an API for the most common cases
    public static KeyCombination bare(KeyCode code) { return new KeyCodeCombination(code); }
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

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Map<KeyCombination, ActionBindingsManager> actionBindings = new HashMap<>();
    private final List<KeyCombination> matches = new ArrayList<>(3);

    public void setBinding(KeyCombination combination, ActionBindingsManager bindingsManager) {
        if (actionBindings.get(combination) == bindingsManager) {
            Logger.debug("Key combination '{}' already bound to action {}", combination, bindingsManager);
        } else {
            actionBindings.put(combination, bindingsManager);
            Logger.debug("Key combination '{}' is bound to action {}", combination, bindingsManager);
        }
    }

    public Map<KeyCombination, ActionBindingsManager> actionBindings() {
        return actionBindings;
    }

    public void removeBinding(KeyCombination combination, ActionBindingsManager client) {
        boolean removed = actionBindings.remove(combination, client);
        if (removed) {
            Logger.debug("Key code combination '{}' bound to {}", combination, client);
        }
    }

    public void onKeyPressed(KeyEvent key) {
        Logger.trace("Key pressed: {}", key);
        pressedKeys.add(key.getCode());
        actionBindings.keySet().stream().filter(combination -> combination.match(key)).forEach(matches::add);
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
}