/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.comp;


import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;

public class Pac3DAnimationComp implements GameEntityComponent {

    private final AnimationRegistry animationRegistry;

    public Pac3DAnimationComp(AnimationRegistry animationRegistry) {
        this.animationRegistry = animationRegistry;
    }

    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    @Override
    public void reset() {}
}
