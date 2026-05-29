/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.input;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

        updateModifierState(event);
        if (!event.getCode().isModifierKey()) {
            pressedKeys.add(event.getCode());
            listeners.forEach(listener -> listener.onKeyboardStateChange(this));
        }
    }

    public void onKeyReleased(KeyEvent event) {
        if (Logger.isTraceEnabled()) Logger.trace("Key released: {}", event);

        updateModifierState(event);
        if (!event.getCode().isModifierKey()) {
            pressedKeys.remove(event.getCode());
            listeners.forEach(listener -> listener.onKeyboardStateChange(this));
        }
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

    public boolean stateMatches(KeyCodeCombination combination) {
        final KeyCode keyCode = combination.getCode();
        return isKeyPressed(keyCode) && combination.match(syntheticKeyPressedEvent(keyCode));
    }

    private KeyEvent syntheticKeyPressedEvent(KeyCode keyCode) {
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