/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.input;

public record Input(Keyboard keyboard, Joypad joypad) {

    public static Input instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    private static Input createSingleton() {
        final Keyboard kb = new Keyboard();
        return new Input(kb, new Joypad(kb));
    }

    private static class LazyThreadSafeSingletonHolder {
        static final Input SINGLETON = createSingleton();
    }
}
