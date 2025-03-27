/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.uilib.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui.input.ArcadeKeyBinding.DEFAULT_ARCADE_KEY_BINDING;

public class GameKeyboard extends Keyboard {

    public static final KeyCodeCombination KEY_FULLSCREEN = Keyboard.naked(KeyCode.F11);
    public static final KeyCodeCombination KEY_MUTE = Keyboard.alt(KeyCode.M);
    public static final KeyCodeCombination KEY_OPEN_EDITOR = Keyboard.shift_alt(KeyCode.E);

    // My current bindings, might be crap
    static final JoypadKeyBinding JOYPAD_CURSOR_KEYS = new JoypadKeyBinding(
            new KeyCodeCombination(KeyCode.SPACE),
            new KeyCodeCombination(KeyCode.ENTER),
            new KeyCodeCombination(KeyCode.B),
            new KeyCodeCombination(KeyCode.N),
            new KeyCodeCombination(KeyCode.UP),
            new KeyCodeCombination(KeyCode.DOWN),
            new KeyCodeCombination(KeyCode.LEFT),
            new KeyCodeCombination(KeyCode.RIGHT)
    );

    // Like Mesen emulator key set #2
    static final JoypadKeyBinding JOYPAD_WASD = new JoypadKeyBinding(
            new KeyCodeCombination(KeyCode.U),
            new KeyCodeCombination(KeyCode.I),
            new KeyCodeCombination(KeyCode.J),
            new KeyCodeCombination(KeyCode.K),
            new KeyCodeCombination(KeyCode.W),
            new KeyCodeCombination(KeyCode.S),
            new KeyCodeCombination(KeyCode.A),
            new KeyCodeCombination(KeyCode.D)
    );

    private final ArcadeKeyBinding arcadeKeyBinding = DEFAULT_ARCADE_KEY_BINDING;
    private final JoypadKeyBinding[] joypads = { JOYPAD_CURSOR_KEYS, JOYPAD_WASD };
    private int selectedJoypadIndex;

    public ArcadeKeyBinding arcade() {
        return arcadeKeyBinding;
    }

    public JoypadKeyBinding selectedJoypad() {
        return joypads[selectedJoypadIndex];
    }

    public Stream<KeyCodeCombination> joypadKeys() {
        return Stream.of(NES_JoypadButton.values()).map(selectedJoypad()::key);
    }

    public void enableSelectedJoypad() {
        joypadKeys().forEach(kcc -> THE_UI.keyboard().register(kcc, this));
    }

    public void disableSelectedJoypad() {
        joypadKeys().forEach(combination -> THE_UI.keyboard().unregister(combination, this));
    }

    public void selectNextJoypad() {
        selectedJoypadIndex = (selectedJoypadIndex + 1) % joypads.length;
    }
}
