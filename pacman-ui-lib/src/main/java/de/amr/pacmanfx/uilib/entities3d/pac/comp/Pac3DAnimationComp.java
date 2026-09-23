/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3d.pac.comp;

import de.amr.basics.ecs.GameEntityComp;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3d.pac.anim.Pac3DAnimationID;
import de.amr.pacmanfx.uilib.entities3d.pac.anim.Pac3DMovementAnimation;

public class Pac3DAnimationComp implements GameEntityComp {

    private final AnimationRegistry registry;

    private Pac3DMovementAnimation movement;

    private ManagedAnimation chewing;

    private ManagedAnimation dying;

    public Pac3DAnimationComp(AnimationRegistry registry) {
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
