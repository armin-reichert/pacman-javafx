/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.anim;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.pac.PacStateSystem;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;

public interface Pac3DMovementAnimation {

    ManagedAnimation managedAnimation();

    void update(GameEntity pac, PacStateSystem pacStateSystem);

    void setPowerMode(boolean power);
}
