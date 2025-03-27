/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.uilib.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import static de.amr.games.pacman.ui.input.ArcadeKeyBinding.DEFAULT_ARCADE_KEY_BINDING;
import static de.amr.games.pacman.ui.input.JoypadKeyBinding.JOYPAD_CURSOR_KEYS;
import static de.amr.games.pacman.ui.input.JoypadKeyBinding.JOYPAD_WASD;

public class GameKeyboard extends Keyboard {

    public static final KeyCodeCombination KEY_FULLSCREEN = Keyboard.naked(KeyCode.F11);
    public static final KeyCodeCombination KEY_MUTE = Keyboard.alt(KeyCode.M);
    public static final KeyCodeCombination KEY_OPEN_EDITOR = Keyboard.shift_alt(KeyCode.E);

    private final ArcadeKeyBinding arcadeKeyBinding = DEFAULT_ARCADE_KEY_BINDING;
    private final JoypadKeyBinding[] joypadKeyBindings = new JoypadKeyBinding[] { JOYPAD_CURSOR_KEYS, JOYPAD_WASD };
    private int joypadIndex;

    public ArcadeKeyBinding arcade() {
        return arcadeKeyBinding;
    }

    public JoypadKeyBinding joypad() {
        return joypadKeyBindings[joypadIndex];
    }

    public void selectNextJoypadKeyBinding() {
        joypadIndex = (joypadIndex + 1) % joypadKeyBindings.length;
    }
}
