/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.pac;

import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;

public class PacView3DAnimation {

    private final AnimationRegistry registry;

    private Pac3DMovementAnimation movement;

    private ManagedAnimation chewing;

    private ManagedAnimation dying;

    public PacView3DAnimation(AnimationRegistry registry) {
        this.registry = registry;
    }

    public Pac3DMovementAnimation movement() {
        return movement;
    }

    public void setMovement(Pac3DMovementAnimation movement) {
        this.movement = movement;
        registry.register(Pac3DAnimationID.MOVING, movement.managedAnimation());
    }

    public ManagedAnimation chewing() {
        return chewing;
    }

    public void setChewing(ManagedAnimation chewing) {
        this.chewing = chewing;
        registry.register(Pac3DAnimationID.CHEWING, chewing);
    }

    public ManagedAnimation dying() {
        return dying;
    }

    public void setDying(ManagedAnimation dying) {
        this.dying = dying;
        registry.register(Pac3DAnimationID.DYING,   dying);
    }
}
