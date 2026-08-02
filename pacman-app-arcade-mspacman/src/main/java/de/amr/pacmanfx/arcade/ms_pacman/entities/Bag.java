/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.entities;

import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.components.MovementComp;
import de.amr.pacmanfx.core.ecs.components.SpriteAnimComp;

public class Bag extends GameEntity {

    private boolean open;

    public Bag(SpriteAnimationContainer container) {
        name = "Birkin";
        setComponent(MovementComp.class, new MovementComp());
        setComponent(SpriteAnimComp.class, new SpriteAnimComp());
    }

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public SpriteAnimComp spriteAnim() {
        return requireComponent(SpriteAnimComp.class);
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }
}
