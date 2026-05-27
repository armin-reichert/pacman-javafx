/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.input;

import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static javafx.scene.input.KeyCombination.*;

public final class Keyboard {

    public interface KeyboardStateListener {
        void onKeyboardStateChange(Keyboard keyboard);
    }

    // API for the most common cases
    public static KeyCodeCombination bare(KeyCode code) { return new KeyCodeCombination(code); }
    public static KeyCodeCombination alt(KeyCode code) {
        return new KeyCodeCombination(code, ALT_DOWN);
    }
    public static KeyCodeCombination control(KeyCode code) {
        return new KeyCodeCombination(code, CONTROL_DOWN);
    }
    public static KeyCodeCombination shift(KeyCode code) {
        return new KeyCodeCombination(code, SHIFT_DOWN);
    }
    public static KeyCodeCombination alt_shift(KeyCode code) { return new KeyCodeCombination(code, SHIFT_DOWN, ALT_DOWN); }

    private final Set<KeyboardStateListener> listeners = new HashSet<>();
    private final Map<KeyCodeCombination, ActionBindingsManager> actionBindingsMap = new HashMap<>();

    // Current state
    private final Set<KeyCode> pressedKeys = new HashSet<>(4);
    private boolean shiftDown;
    private boolean controlDown;
    private boolean altDown;
    private boolean metaDown;

    Keyboard() {}

    public void addListener(KeyboardStateListener stateListener) {
        requireNonNull(stateListener);
        listeners.add(stateListener);
    }

    public void removeListener(KeyboardStateListener stateListener) {
        requireNonNull(stateListener);
        listeners.remove(stateListener);
    }

    public Set<KeyCode> pressedKeys() {
        return Collections.unmodifiableSet(pressedKeys);
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

        updateModifierState(event);

        listeners.forEach(listener -> listener.onKeyboardStateChange(this));
    }

    public void onKeyReleased(KeyEvent event) {
        if (Logger.isTraceEnabled()) Logger.trace("Key released: {}", event);

        if (!event.getCode().isModifierKey()) {
            pressedKeys.remove(event.getCode());
        }

        updateModifierState(event);

        listeners.forEach(listener -> listener.onKeyboardStateChange(this));
    }

    private void updateModifierState(KeyEvent event) {
        shiftDown = event.isShiftDown();
        controlDown = event.isControlDown();
        altDown = event.isAltDown();
        metaDown = event.isMetaDown();
    }

    public boolean isKeyPressed(KeyCode keyCode) {
        requireNonNull(keyCode);
        return pressedKeys.contains(keyCode);
    }

    public void registerActionBinding(KeyCodeCombination combination, ActionBindingsManager bindingsManager) {
        requireNonNull(combination);
        requireNonNull(bindingsManager);
        if (actionBindingsMap.get(combination) == bindingsManager) {
            Logger.debug("Key combination '{}' already bound to {}", combination, bindingsManager);
        } else {
            actionBindingsMap.put(combination, bindingsManager);
            Logger.debug("Key combination '{}' bound to {}", combination, bindingsManager);
        }
    }

    public void unregisterActionBinding(KeyCodeCombination combination, ActionBindingsManager bindingsManager) {
        boolean removed = actionBindingsMap.remove(combination, bindingsManager);
        if (removed) {
            Logger.debug("Key code combination '{}' unbound from {}", combination, bindingsManager);
        }
    }

    public boolean isMatching(KeyCodeCombination combination) {
        final KeyCode key = combination.getCode();
        // If the key for this combination is not currently pressed, it cannot match
        if (!pressedKeys.contains(key)) {
            return false;
        }
        return combination.match(syntheticKeyEvent(key));
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