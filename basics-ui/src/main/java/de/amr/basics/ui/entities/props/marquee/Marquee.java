/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.marquee;

import de.amr.basics.ecs.GameEntity;

public final class Marquee extends GameEntity {

    public Marquee() {
        setComponent(MarqueeLayoutComp.class, new MarqueeLayoutComp());
        setComponent(MarqueeColorsComp.class, new MarqueeColorsComp());
    }

    public MarqueeLayoutComp layout() {
        return assertComponent(MarqueeLayoutComp.class);
    }

    public MarqueeColorsComp colors() {
        return assertComponent(MarqueeColorsComp.class);
    }
}
