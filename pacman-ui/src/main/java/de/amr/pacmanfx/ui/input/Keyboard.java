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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

public final class Keyboard {

    public interface StateListener {
        void onKeyboardStateChange(Keyboard keyboard);
    }

    private final Set<StateListener> listeners = ConcurrentHashMap.newKeySet();

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    // Current state
    private final Set<KeyCode> pressedKeys = new LinkedHashSet<>();
    private boolean shiftDown;
    private boolean controlDown;
    private boolean altDown;
    private boolean metaDown;

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void clearState() {
        pressedKeys.clear();
        shiftDown = controlDown = altDown = metaDown = false;
        fireStateChange();
    }

    @Override
    public String toString() {
        return "Keyboard, pressed keys=%s, shift=%s, alt=%s, control=%s, meta=%s".formatted(
            pressedKeys, shiftDown, altDown, controlDown, metaDown
        );
    }

    public void filterKeyEventsFrom(EventTarget target) {
        requireNonNull(target);

        // As there is no API contract that event filter is never added twice, I remove it first to be safe
        target.removeEventFilter(KeyEvent.KEY_PRESSED,  this::onKeyPressed);
        target.addEventFilter(KeyEvent.KEY_PRESSED,  this::onKeyPressed);

        target.removeEventFilter(KeyEvent.KEY_RELEASED, this::onKeyReleased);
        target.addEventFilter(KeyEvent.KEY_RELEASED, this::onKeyReleased);
    }

    public void addStateListener(StateListener stateListener) {
        requireNonNull(stateListener);
        if (listeners.contains(stateListener)) {
            Logger.warn("State listener is already registered: {}", stateListener);
        }
        else {
            listeners.add(stateListener);
            Logger.info("KeyboardStateListener added: {}", stateListener);
        }
    }

    public void removeStateListener(StateListener stateListener) {
        requireNonNull(stateListener);
        boolean removed = listeners.remove(stateListener);
        if (removed) {
            Logger.info("KeyboardStateListener removed: {}", stateListener);
        }
        else {
            Logger.warn("State listener not registered: {}", stateListener);
        }
    }

    public Set<KeyCode> pressedKeys() {
        return Collections.unmodifiableSet(pressedKeys);
    }

    public boolean anyNormalKeyPressed() {
        return !pressedKeys.isEmpty();
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
        requireNonNull(combination);
        final KeyCode code = combination.getCode();
        return isKeyPressed(code) && combination.match(syntheticEvent(code));
    }

    private KeyEvent syntheticEvent(KeyCode code) {
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