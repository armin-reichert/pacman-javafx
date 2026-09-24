/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.stork;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;

public class Stork extends GameEntity {

    private boolean bagReleasedFromBeak;

    public Stork() {
        setName("Beatrix von");
        setComp(MovementComp.class, new MovementComp());
        setComp(SpriteAnimationComp.class, new SpriteAnimationComp());
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