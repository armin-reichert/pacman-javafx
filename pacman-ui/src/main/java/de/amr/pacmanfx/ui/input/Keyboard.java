/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.input;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventTarget;
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

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    // Current state
    private final Collection<KeyCode> pressedKeys = new LinkedHashSet<>();
    private boolean shiftDown;
    private boolean controlDown;
    private boolean altDown;
    private boolean metaDown;

    Keyboard() {}

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void clearState() {
        pressedKeys.clear();
        shiftDown = controlDown = altDown = metaDown = false;
    }

    @Override
    public String toString() {
        return "Keyboard, pressed keys=%s, shift=%s, alt=%s, control=%s, meta=%s".formatted(
            pressedKeys, shiftDown, altDown, controlDown, metaDown
        );
    }

    public void filterKeyEventsFrom(EventTarget target) {
        target.removeEventFilter(KeyEvent.KEY_PRESSED,  this::onKeyPressed);
        target.removeEventFilter(KeyEvent.KEY_RELEASED, this::onKeyPressed);
        target.addEventFilter(KeyEvent.KEY_PRESSED,  this::onKeyPressed);
        target.addEventFilter(KeyEvent.KEY_RELEASED, this::onKeyReleased);
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
        if (!isEnabled()) return;

        boolean changed = updateModifierState(event);
        if (!event.getCode().isModifierKey()) {
            // Handle CTRL+key differently: always report changes (e.g. to zoom in by holding CTRL+PLUS etc.)
            changed = pressedKeys.add(event.getCode()) || controlDown;
        }
        if (changed) {
            fireStateChange();
        }
    }

    private void onKeyReleased(KeyEvent event) {
        if (!isEnabled()) return;

        boolean changed = false;
        if (!event.getCode().isModifierKey()) {
            changed = pressedKeys.remove(event.getCode());
        }
        changed = changed || updateModifierState(event);
        if (changed) {
            fireStateChange();
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

    public boolean isKeyPressed(KeyCode code) {
        requireNonNull(code);
        return pressedKeys.contains(code);
    }

    public boolean stateMatches(KeyCodeCombination combination) {
        final KeyCode keyCode = combination.getCode();
        return isKeyPressed(keyCode) && combination.match(syntheticKeyPressedEvent(keyCode));
    }

    private KeyEvent syntheticKeyPressedEvent(KeyCode code) {
        return new KeyEvent(
            KeyEvent.KEY_PRESSED,
            "",
            "",
            code,
            shiftDown,
            controlDown,
            altDown,
            metaDown
        );
    }

    private void fireStateChange() {
        if (isEnabled()) {
            listeners.forEach(listener -> listener.onKeyboardStateChange(this));
        }
    }
}