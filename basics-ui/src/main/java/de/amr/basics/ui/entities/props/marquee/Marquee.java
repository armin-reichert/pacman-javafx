/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.marquee;

import de.amr.basics.ecs.GameEntity;

public final class Marquee extends GameEntity {

    public Marquee() {
        setComp(MarqueeLayoutComp.class, new MarqueeLayoutComp());
        setComp(MarqueeVisualComp.class, new MarqueeVisualComp());
    }

    public MarqueeLayoutComp layout() {
        return reqComp(MarqueeLayoutComp.class);
    }

    public MarqueeVisualComp visualization() {
        return reqComp(MarqueeVisualComp.class);
    }
}
