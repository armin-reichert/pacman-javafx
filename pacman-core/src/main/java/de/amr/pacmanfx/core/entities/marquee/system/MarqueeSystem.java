/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.marquee.system;

import de.amr.pacmanfx.core.entities.Marquee;

public class MarqueeSystem {

    public static class SingletonHolder {
        static final MarqueeSystem SINGLETON = new MarqueeSystem();
    }

    public static MarqueeSystem instance() {
        return SingletonHolder.SINGLETON;
    }

    public void update(Marquee marquee) {
        marquee.runner().tickTimer().doTick();
    }

    public void start(Marquee marquee) {
        marquee.runner().tickTimer().restartIndefinitely();
    }
}
