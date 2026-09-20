/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.props.marquee;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public final class Marquee extends GameEntity implements Renderable {

    public Marquee() {
        setComp(MarqueeLayoutComp.class, new MarqueeLayoutComp());
        setComp(MarqueeVisualComp.class, new MarqueeVisualComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }

    public MarqueeLayoutComp layout() {
        return reqComp(MarqueeLayoutComp.class);
    }

    public MarqueeVisualComp visualization() {
        return reqComp(MarqueeVisualComp.class);
    }
}
