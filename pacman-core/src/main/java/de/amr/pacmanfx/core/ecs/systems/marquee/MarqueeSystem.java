/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems.marquee;

import de.amr.pacmanfx.core.model.entities.marquee.Marquee;

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
