/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.model3D.MutableGhost3D;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Node;

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

    MutableGhost3D createMutableGhost3D(AssetMap assets, AnimationRegistry animationRegistry, Ghost ghost, double size);

    /**
     * Creates the 3D representation of the lives counter for this variant.
     *
     * @param size the desired size of the 3D shape
     * @return the 3D node representing a life icon
     */
    Node createLivesCounterShape3D(AssetMap assets, double size);
}
