/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.entities.bag;

import de.amr.basics.rendering.RenderingLayer;
import de.amr.basics.rendering.Renderable;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.comp.MovementComp;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;

public class Bag extends GameEntity implements Renderable {

    private boolean open;

    public Bag() {
        setName("Birkin");
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

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }
}