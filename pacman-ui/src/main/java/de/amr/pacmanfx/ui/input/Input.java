/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.input;

public record Input(Keyboard keyboard, Joypad joypad) {

    private static Input create() {
        final Keyboard kb = new Keyboard();
        return new Input(kb, new Joypad(kb));
    }

    private static class LazyThreadSafeSingletonHolder {
        static final Input SINGLETON = create();
    }

    public static Input instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }
}
