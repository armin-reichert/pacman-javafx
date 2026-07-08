/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.input;

public final class Input {

    private final Keyboard keyboard;
    private final Joypad joypad;

    public Input() {
        keyboard = new Keyboard();
        joypad = new Joypad(keyboard);
    }

    public Keyboard keyboard() {
        return keyboard;
    }

    public Joypad joypad() {
        return joypad;
    }
}
