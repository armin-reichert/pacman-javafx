/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.input;

import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    private boolean shiftDown;
    private boolean controlDown;
    private boolean altDown;
    private boolean metaDown;

    private final Map<KeyCombination, ActionBindingsManager> actionBindings = new HashMap<>();

    Keyboard() {}

    public Set<KeyCode> pressedKeys() {
        return pressedKeys;
    }

    public boolean shiftDown() {
        return shiftDown;
    }

    public boolean controlDown() {
        return controlDown;
    }

    public boolean altDown() {
        return altDown;
    }

    public boolean metaDown() {
        return metaDown;
    }

    public void onKeyPressed(KeyEvent event) {
        if (Logger.isTraceEnabled()) Logger.trace("Key pressed: {}", event);
        if (!event.getCode().isModifierKey()) {
            pressedKeys.add(event.getCode());
        }
        // Always update modifier state because e.g. SHIFT + S is a single event
        updateModifierState(event);
    }

    public void onKeyReleased(KeyEvent event) {
        if (Logger.isTraceEnabled()) Logger.trace("Key released: {}", event);
        if (!event.getCode().isModifierKey()) {
            pressedKeys.remove(event.getCode());
        }
        // Always update modifier state because e.g. SHIFT + S is a single event
        updateModifierState(event);
    }

    private void updateModifierState(KeyEvent event) {
        shiftDown = event.isShiftDown();
        controlDown = event.isControlDown();
        altDown = event.isAltDown();
        metaDown = event.isMetaDown();
    }

    public boolean isPressed(KeyCode keyCode) {
        return pressedKeys.contains(keyCode);
    }

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

    public boolean isMatching(KeyCombination combination) {
        KeyCode key = extractKeyCode(combination);

        // If the key for this combination is not currently pressed, it cannot match
        if (!pressedKeys.contains(key)) {
            return false;
        }

        return combination.match(syntheticKeyEvent(key));
    }

    private KeyCode extractKeyCode(KeyCombination combination) {
        if (combination instanceof KeyCodeCombination kc) {
            return kc.getCode();
        }
        throw new IllegalArgumentException("Only KeyCodeCombination is supported");
    }

    private KeyEvent syntheticKeyEvent(KeyCode keyCode) {
        return new KeyEvent(
            KeyEvent.KEY_PRESSED,
            "",
            "",
            keyCode,
            shiftDown,
            controlDown,
            altDown,
            metaDown
        );
    }
}