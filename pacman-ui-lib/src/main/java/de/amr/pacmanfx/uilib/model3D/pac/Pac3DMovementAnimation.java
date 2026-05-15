/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;

public abstract class Pac3DMovementAnimation extends ManagedAnimation {

    public Pac3DMovementAnimation(String label) {
        super(label);
    }

    public abstract void update(Pac pac);

    public abstract void setPowerMode(boolean power);
}
