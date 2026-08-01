/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities;

public class MarqueeSystem {

    public static class SingletonHolder {
        static final MarqueeSystem SINGLETON = new MarqueeSystem();
    }

    public static MarqueeSystem instance() {
        return SingletonHolder.SINGLETON;
    }

    public void update(Marquee marquee) {
        marquee.timer().runner().doTick();
    }

    public void start(Marquee marquee) {
        marquee.timer().runner().restartIndefinitely();
    }
}
