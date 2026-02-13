/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.Vector3f;

public interface EnergizerFragment {
    void dispose();

    void fly(Vector3f gravity);

    void move();
}
