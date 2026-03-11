/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;

public interface Factory3D {
    /**
     * Creates the 3D representation of Pac‑Man or Ms. Pac‑Man for this game
     * variant, including model, materials, and animation bindings.
     *
     * @param animationRegistry the registry where animations are stored
     * @param pac the Pac‑Man actor whose animations and state drive the model
     * @param size the desired size of the 3D model
     * @return the 3D representation of Pac‑Man
     */
    PacBase3D createPac3D(AssetMap assets, AnimationRegistry animationRegistry, Pac pac, double size);

}
