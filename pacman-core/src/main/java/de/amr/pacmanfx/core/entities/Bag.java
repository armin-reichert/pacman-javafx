/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;

public class Bag extends GameEntity {

    private boolean open;

    public Bag() {
        setName("Birkin");
        setComp(MovementComp.class, new MovementComp());
        setComp(SpriteAnimationComp.class, new SpriteAnimationComp());
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