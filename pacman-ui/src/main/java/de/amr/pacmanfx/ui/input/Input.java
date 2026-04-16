/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.input;

public class Input {

    private static class LazyThreadSafeSingletonHolder {
        static final Input SINGLETON = new Input();
    }

    public static Input instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    public final Keyboard keyboard = new Keyboard();
    public final Joypad joypad = new Joypad(keyboard);
}
