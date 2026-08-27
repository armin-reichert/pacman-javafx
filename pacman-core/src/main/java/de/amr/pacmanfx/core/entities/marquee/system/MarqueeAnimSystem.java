/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.marquee.system;

import de.amr.pacmanfx.core.entities.Marquee;

public class MarqueeAnimSystem {

    public static class SingletonHolder {
        static final MarqueeAnimSystem SINGLETON = new MarqueeAnimSystem();
    }

    public static MarqueeAnimSystem instance() {
        return SingletonHolder.SINGLETON;
    }

    public void update(Marquee marquee) {
        marquee.anim().tickTimer().doTick();
    }

    public void start(Marquee marquee) {
        marquee.anim().tickTimer().restartIndefinitely();
    }
}
