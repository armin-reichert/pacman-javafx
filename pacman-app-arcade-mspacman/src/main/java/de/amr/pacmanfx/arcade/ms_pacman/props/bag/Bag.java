/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.props.bag;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;

public class Bag extends GameEntity {

    private boolean open;

    public Bag() {
        setName("Birkin");
        setComponent(MovementComp.class, new MovementComp());
        setComponent(SpriteAnimationComp.class, new SpriteAnimationComp());
    }

    public MovementComp movement() {
        return assertComponent(MovementComp.class);
    }

    public SpriteAnimationComp spriteAnim() {
        return assertComponent(SpriteAnimationComp.class);
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }
}