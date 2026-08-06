/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.stork;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;

public class Stork extends GameEntity {

    private boolean bagReleasedFromBeak;

    public Stork() {
        setName("Beatrix von");
        setComponent(MovementComp.class, new MovementComp());
        setComponent(SpriteAnimationComp.class, new SpriteAnimationComp());
    }

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public SpriteAnimationComp spriteAnim() {
        return requireComponent(SpriteAnimationComp.class);
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }
}