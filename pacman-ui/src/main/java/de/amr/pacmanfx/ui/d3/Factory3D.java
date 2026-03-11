/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.ActorConfig;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.model3D.GhostColorSet;
import de.amr.pacmanfx.uilib.model3D.MutableGhost3D;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Node;

public interface Factory3D {

    /**
     * Creates the 3D representation of Pac for this game variant, including model,
     * materials, and animation bindings.
     *
     * @param pac               the Pac actor whose animations and state drive the model
     * @param assets            the asset map to retrieve colors and other assets
     * @param actorConfig     the actor 3D configuration object
     * @param animationRegistry the registry where animations are stored
     * @return the 3D representation of Pac
     */
    PacBase3D createPac3D(Pac pac, AssetMap assets, ActorConfig actorConfig, AnimationRegistry animationRegistry);

    /**
     * Creates the 3D representation of a ghost for this game variant, including
     * model, materials, and animation bindings.
     *
     * @param ghost             the ghost actor whose animations and state drive the model
     * @param assets            the asset map to retrieve colors and other assets
     * @param actorConfig     the actor 3D configuration object
     * @param colorSet          the color set to use for the ghost's body, eyes, pupils, and eyeballs
     * @param animationRegistry the registry where animations are stored
     * @param numFlashings      the number of flashing states to support for the ghost (e.g., for frightened mode)
     * @return the 3D representation of a ghost
     */
    MutableGhost3D createMutableGhost3D(Ghost ghost, AssetMap assets, ActorConfig actorConfig, GhostColorSet colorSet, AnimationRegistry animationRegistry, int numFlashings);

    /**
     * Creates the 3D representation of the lives counter for this variant.
     *
     * @param size the desired size of the 3D shape
     * @return the 3D node representing a life icon
     */
    Node createLivesCounterShape3D(AssetMap assets, double size);
}
