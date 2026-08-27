/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.marquee.comp.MarqueeLayoutComp;
import de.amr.pacmanfx.core.entities.marquee.comp.MarqueeAnimComp;
import de.amr.pacmanfx.core.entities.marquee.comp.MarqueeVisualComp;

public final class Marquee extends GameEntity {

    public Marquee() {
        setComp(MarqueeAnimComp.class, new MarqueeAnimComp());
        setComp(MarqueeLayoutComp.class, new MarqueeLayoutComp());
        setComp(MarqueeVisualComp.class, new MarqueeVisualComp());
    }

    public MarqueeAnimComp anim() {
        return reqComp(MarqueeAnimComp.class);
    }

    public MarqueeLayoutComp layout() {
        return reqComp(MarqueeLayoutComp.class);
    }

    public MarqueeVisualComp visualization() {
        return reqComp(MarqueeVisualComp.class);
    }
}
