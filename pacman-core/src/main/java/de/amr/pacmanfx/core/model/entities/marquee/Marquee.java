/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.marquee;

import de.amr.pacmanfx.core.ecs.GameEntity;

public final class Marquee extends GameEntity {

    public Marquee() {
        setComponent(MarqueeRunnerComp.class, new MarqueeRunnerComp());
        setComponent(MarqueeLayoutComp.class, new MarqueeLayoutComp());
        setComponent(MarqueeVisualComp.class, new MarqueeVisualComp());
    }

    public MarqueeRunnerComp runner() {
        return requireComponent(MarqueeRunnerComp.class);
    }

    public MarqueeLayoutComp layout() {
        return requireComponent(MarqueeLayoutComp.class);
    }

    public MarqueeVisualComp visualization() {
        return requireComponent(MarqueeVisualComp.class);
    }
}
