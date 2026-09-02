/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;

public class Stork extends GameEntity {

    private boolean bagReleasedFromBeak;

    public Stork() {
        setName("Beatrix von");
        setComp(MovementComp.class, new MovementComp());
        setComp(SpriteAnimationComp.class, new SpriteAnimationComp());
        setComp(RenderingComp.class, new RenderingComp(RenderingLayer.PROPS));
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