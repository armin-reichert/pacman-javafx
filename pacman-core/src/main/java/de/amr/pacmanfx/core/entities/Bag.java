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
        setComponent(MovementComp.class, new MovementComp());
        setComponent(SpriteAnimationComp.class, new SpriteAnimationComp());
    }

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public SpriteAnimationComp spriteAnim() {
        return requireComponent(SpriteAnimationComp.class);
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }
}