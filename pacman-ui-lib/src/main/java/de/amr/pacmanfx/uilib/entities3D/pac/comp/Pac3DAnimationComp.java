/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.Pac3DMovementAnimation;

public class Pac3DAnimationComp implements GameEntityComponent {

    private final AnimationRegistry registry;

    private Pac3DMovementAnimation movement;

    private ManagedAnimation chewing;

    private ManagedAnimation dying;

    public Pac3DAnimationComp(AnimationRegistry registry) {
        this.registry = registry;
    }

    public AnimationRegistry registry() {
        return registry;
    }

    public Pac3DMovementAnimation movement() {
        return movement;
    }

    public void setMovement(Pac3DMovementAnimation movement) {
        this.movement = movement;
    }

    public ManagedAnimation chewing() {
        return chewing;
    }

    public void setChewing(ManagedAnimation chewing) {
        this.chewing = chewing;
    }

    public ManagedAnimation dying() {
        return dying;
    }

    public void setDying(ManagedAnimation dying) {
        this.dying = dying;
    }

    @Override
    public void reset() {}
}
