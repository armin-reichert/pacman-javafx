/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.pac;

import de.amr.basics.ui.animation.ManagedAnimation;

public interface PacView3D_MovementAnimation {

    ManagedAnimation managedAnimation();

    void update();

    void setPowerMode(boolean power);
}
