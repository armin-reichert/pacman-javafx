/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.pac;

import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;

public class PacView3D_Animation {

    private final AnimationRegistry registry;

    private PacView3D_MovementAnimation movement;

    private ManagedAnimation chewing;

    private ManagedAnimation dying;

    public PacView3D_Animation(AnimationRegistry registry) {
        this.registry = registry;
    }

    public PacView3D_MovementAnimation movement() {
        return movement;
    }

    public void setMovement(PacView3D_MovementAnimation movement) {
        this.movement = movement;
        registry.register(PacView3D_AnimationID.MOVING, movement.managedAnimation());
    }

    public ManagedAnimation chewing() {
        return chewing;
    }

    public void setChewing(ManagedAnimation chewing) {
        this.chewing = chewing;
        registry.register(PacView3D_AnimationID.CHEWING, chewing);
    }

    public ManagedAnimation dying() {
        return dying;
    }

    public void setDying(ManagedAnimation dying) {
        this.dying = dying;
        registry.register(PacView3D_AnimationID.DYING,   dying);
    }
}
