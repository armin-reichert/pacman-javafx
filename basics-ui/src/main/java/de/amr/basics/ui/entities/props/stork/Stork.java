/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.stork;

import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.comp.MovementComp;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;
import de.amr.basics.ui.rendering.Renderable;

public class Stork extends GameEntity implements Renderable {

    private boolean bagReleasedFromBeak;

    public Stork() {
        setName("Beatrix von");
        setComp(MovementComp.class, new MovementComp());
        setComp(SpriteAnimationComp.class, new SpriteAnimationComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }

    public MovementComp movement() {
        return reqComp(MovementComp.class);
    }

    public SpriteAnimationComp spriteAnim() {
        return reqComp(SpriteAnimationComp.class);
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }
}