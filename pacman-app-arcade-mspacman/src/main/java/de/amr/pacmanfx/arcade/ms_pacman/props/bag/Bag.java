/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.props.bag;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.Movement;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;

public class Bag extends GameEntity {

    private boolean open;

    public Bag() {
        setName("Birkin");
        setComponent(Movement.class, new Movement());
        setComponent(SpriteAnimationComp.class, new SpriteAnimationComp());
    }

    public Movement movement() {
        return assertComponent(Movement.class);
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