/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.anim;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.pac.PacStateSystem;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;

public abstract class Pac3DMovementAnimation extends ManagedAnimation {

    public Pac3DMovementAnimation(String label) {
        super(label);
    }

    public abstract void update(GameEntity pac, PacStateSystem pacStateSystem);

    public abstract void setPowerMode(boolean power);
}
