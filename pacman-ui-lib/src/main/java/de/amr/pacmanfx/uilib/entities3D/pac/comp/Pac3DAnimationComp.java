/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.Pac3DMovementAnimation;

public class Pac3DAnimationComp implements GameEntityComponent {

    private final AnimationRegistry animationRegistry;
    private Pac3DMovementAnimation movementAnimation;

    public Pac3DAnimationComp(AnimationRegistry animationRegistry) {
        this.animationRegistry = animationRegistry;
    }

    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    public Pac3DMovementAnimation movementAnimation() {
        return movementAnimation;
    }

    public void setMovementAnimation(Pac3DMovementAnimation movementAnimation) {
        this.movementAnimation = movementAnimation;
    }

    @Override
    public void reset() {}
}
