/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.marquee.comp.MarqueeLayoutComp;
import de.amr.pacmanfx.core.entities.marquee.comp.MarqueeRunnerComp;
import de.amr.pacmanfx.core.entities.marquee.comp.MarqueeVisualComp;

public final class Marquee extends GameEntity {

    public Marquee() {
        setComp(MarqueeRunnerComp.class, new MarqueeRunnerComp());
        setComp(MarqueeLayoutComp.class, new MarqueeLayoutComp());
        setComp(MarqueeVisualComp.class, new MarqueeVisualComp());
    }

    public MarqueeRunnerComp runner() {
        return requireComp(MarqueeRunnerComp.class);
    }

    public MarqueeLayoutComp layout() {
        return requireComp(MarqueeLayoutComp.class);
    }

    public MarqueeVisualComp visualization() {
        return requireComp(MarqueeVisualComp.class);
    }
}
