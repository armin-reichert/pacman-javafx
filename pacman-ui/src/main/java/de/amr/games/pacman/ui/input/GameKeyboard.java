/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.input;

import de.amr.games.pacman.lib.nes.NES_JoypadButtonID;
import de.amr.games.pacman.uilib.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.stream.Stream;

import static de.amr.games.pacman.ui.Globals.THE_UI;

public class GameKeyboard extends Keyboard {

    public static final KeyCodeCombination KEY_FULLSCREEN = Keyboard.naked(KeyCode.F11);
    public static final KeyCodeCombination KEY_MUTE = Keyboard.alt(KeyCode.M);
    public static final KeyCodeCombination KEY_OPEN_EDITOR = Keyboard.shift_alt(KeyCode.E);

    /** Keys as used in MAME emulator */
    public static final ArcadeKeyBinding MAME_ARCADE_KEY_BINDING = new ArcadeKeyBinding(
        new KeyCodeCombination(KeyCode.DIGIT5),
        new KeyCodeCombination(KeyCode.DIGIT1),
        new KeyCodeCombination(KeyCode.UP),
        new KeyCodeCombination(KeyCode.DOWN),
        new KeyCodeCombination(KeyCode.LEFT),
        new KeyCodeCombination(KeyCode.RIGHT)
    );

    static final JoypadKeyBinding CURSOR_KEY_JOYPAD_KEY_BINDING = new JoypadKeyBinding(
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
    static final JoypadKeyBinding MESEN_2_JOYPAD_KEY_BINDING = new JoypadKeyBinding(
        new KeyCodeCombination(KeyCode.U),
        new KeyCodeCombination(KeyCode.I),
        new KeyCodeCombination(KeyCode.J),
        new KeyCodeCombination(KeyCode.K),
        new KeyCodeCombination(KeyCode.W),
        new KeyCodeCombination(KeyCode.S),
        new KeyCodeCombination(KeyCode.A),
        new KeyCodeCombination(KeyCode.D)
    );

    private final ArcadeKeyBinding arcadeKeyBinding = MAME_ARCADE_KEY_BINDING;

    private final JoypadKeyBinding[] joypadKeyBindings = {
        CURSOR_KEY_JOYPAD_KEY_BINDING, MESEN_2_JOYPAD_KEY_BINDING
    };
    private int selectedJoypadIndex;

    public ArcadeKeyBinding currentArcadeKeyBinding() {
        return arcadeKeyBinding;
    }

    public JoypadKeyBinding currentJoypadKeyBinding() {
        return joypadKeyBindings[selectedJoypadIndex];
    }

    public Stream<KeyCodeCombination> currentJoypadKeys() {
        return Stream.of(NES_JoypadButtonID.values()).map(currentJoypadKeyBinding()::key);
    }

    public void enableCurrentJoypad() {
        currentJoypadKeys().forEach(combination -> THE_UI.keyboard().register(combination, this));
    }

    public void disableCurrentJoypad() {
        currentJoypadKeys().forEach(combination -> THE_UI.keyboard().unregister(combination, this));
    }

    public void selectNextJoypad() {
        selectedJoypadIndex = (selectedJoypadIndex + 1) % joypadKeyBindings.length;
    }
}