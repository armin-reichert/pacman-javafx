/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;
import static javafx.scene.input.KeyCombination.*;

public final class Keyboard {

    public interface StateListener {
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

    private final Set<StateListener> listeners = ConcurrentHashMap.newKeySet();

    // Current state
    private final Collection<KeyCode> pressedKeys = new LinkedHashSet<>();
    private boolean shiftDown;
    private boolean controlDown;
    private boolean altDown;
    private boolean metaDown;

    Keyboard() {}

    public void filterEventsForScene(Scene scene) {
        scene.removeEventFilter(KeyEvent.KEY_PRESSED,  this::onKeyPressed);
        scene.removeEventFilter(KeyEvent.KEY_RELEASED, this::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_PRESSED,  this::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::onKeyReleased);
    }

    public void addStateListener(StateListener stateListener) {
        requireNonNull(stateListener);
        listeners.add(stateListener);
        Logger.info("KeyboardStateListener added: {}", stateListener);
    }

    public void removeStateListener(StateListener stateListener) {
        requireNonNull(stateListener);
        listeners.remove(stateListener);
        Logger.info("KeyboardStateListener removed: {}", stateListener);
    }

    public Collection<KeyCode> pressedKeys() {
        return Collections.unmodifiableCollection(pressedKeys);
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

    private void onKeyPressed(KeyEvent event) {
        boolean changed = updateModifierState(event);
        if (!event.getCode().isModifierKey()) {
            changed = pressedKeys.add(event.getCode());
        }
        if (changed) {
            listeners.forEach(listener -> listener.onKeyboardStateChange(this));
        }
    }

    private void onKeyReleased(KeyEvent event) {
        if (!event.getCode().isModifierKey()) {
            pressedKeys.remove(event.getCode());
        }
    }

    private boolean updateModifierState(KeyEvent event) {
        boolean changed = false;
        if (shiftDown != event.isShiftDown()) {
            shiftDown = event.isShiftDown();
            changed = true;
        }
        if (controlDown != event.isControlDown()) {
            controlDown = event.isControlDown();
            changed = true;
        }
        if (altDown != event.isAltDown()) {
            altDown = event.isAltDown();
            changed = true;
        }
        if (metaDown != event.isMetaDown()) {
            metaDown = event.isMetaDown();
            changed = true;
        }
        return changed;
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