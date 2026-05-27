/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.input;

import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Joypad {

    /** <code>Select=SPACE, Start=ENTER, B=B, A=N, Up/Down/Left/Right=cursor keys </code> */
    public static final JoypadKeyBinding BINDING1 = new JoypadKeyBinding(
        new KeyCodeCombination(KeyCode.SPACE),
        new KeyCodeCombination(KeyCode.ENTER),
        new KeyCodeCombination(KeyCode.B),
        new KeyCodeCombination(KeyCode.N),
        new KeyCodeCombination(KeyCode.UP),
        new KeyCodeCombination(KeyCode.DOWN),
        new KeyCodeCombination(KeyCode.LEFT),
        new KeyCodeCombination(KeyCode.RIGHT)
    );

    /** <code>Select=U, Start=I, B=J, A=K, Up/Down/Left/Right=W/S/A/D</code> */
    public static final JoypadKeyBinding BINDING2 = new JoypadKeyBinding(
        new KeyCodeCombination(KeyCode.U),
        new KeyCodeCombination(KeyCode.I),
        new KeyCodeCombination(KeyCode.J),
        new KeyCodeCombination(KeyCode.K),
        new KeyCodeCombination(KeyCode.W),
        new KeyCodeCombination(KeyCode.S),
        new KeyCodeCombination(KeyCode.A),
        new KeyCodeCombination(KeyCode.D)
    );

    private final JoypadKeyBinding[] bindings = {
        BINDING1, BINDING2
    };

    private final Keyboard keyboard;
    private int selectedIndex;

    Joypad(Keyboard keyboard) {
        this.keyboard = requireNonNull(keyboard);
    }

    public JoypadKeyBinding currentKeyBinding() {
        return bindings[selectedIndex];
    }

    public Stream<KeyCodeCombination> currentKeys() {
        return Stream.of(JoypadButton.values()).map(currentKeyBinding()::key);
    }

    public boolean isButtonPressed(JoypadButton buttonID) {
        return keyboard.stateMatches(keyForButton(buttonID));
    }

    public KeyCodeCombination keyForButton(JoypadButton buttonID) {
        return currentKeyBinding().key(buttonID);
    }

    public void setBindings(ActionBindingsSet actionBindingsManager) {
        currentKeys().forEach(combination -> GameActionBindingsSet.registerActionBindingsManager(combination, actionBindingsManager));
    }

    public void removeBindings(ActionBindingsSet actionBindingsManager) {
        currentKeys().forEach(combination -> GameActionBindingsSet.unregisterActionBindingsManager(combination, actionBindingsManager));
    }

    public void selectNextBinding(ActionBindingsSet actionBindingsManager) {
        selectedIndex = (selectedIndex + 1) % bindings.length;
        setBinding(selectedIndex, actionBindingsManager);
        Logger.info("Joypad keys: {}", currentKeyBinding());
    }

    private void setBinding(int index, ActionBindingsSet actionBindingsManager) {
        removeBindings(actionBindingsManager);
        selectedIndex = index;
        setBindings(actionBindingsManager);
    }
}