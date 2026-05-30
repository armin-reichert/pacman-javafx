/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.input;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

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

    public boolean isButtonPressed(JoypadButton buttonID) {
        return keyboard.stateMatches(keyForButton(buttonID));
    }

    public KeyCodeCombination keyForButton(JoypadButton buttonID) {
        return currentKeyBinding().key(buttonID);
    }

    public void selectNextBinding() {
        selectedIndex = (selectedIndex + 1) % bindings.length;
        Logger.info("Joypad keys: {}", currentKeyBinding());
    }
}