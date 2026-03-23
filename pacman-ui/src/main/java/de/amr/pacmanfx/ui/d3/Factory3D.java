/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.actor.MutableGhost3D;
import de.amr.pacmanfx.uilib.model3D.actor.PacRepresentation3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;

public interface Factory3D extends Disposable {

    /**
     * Creates the 3D representation of Pac for this game variant, including model,
     * materials, and animation bindings.
     *
     * @param pac               the Pac actor whose animations and state drive the model
     * @param pacConfig         the actor 3D configuration object
     * @param animationRegistry the registry where animations are stored
     * @return the 3D representation of Pac
     */
    PacRepresentation3D createPac3D(Pac pac, PacConfig pacConfig, AnimationRegistry animationRegistry);

    /**
     * Creates the 3D representation of a ghost for this game variant, including
     * model, materials, and animation bindings.
     *
     * @param ghost             the ghost actor whose animations and state drive the model
     * @param ghostConfig      the actor 3D configuration object
     * @param animationRegistry the registry where animations are stored
     * @return the 3D representation of a ghost
     */
    MutableGhost3D createMutableGhost3D(Ghost ghost, GhostConfig ghostConfig, AnimationRegistry animationRegistry);

    /**
     * Creates the 3D representation of the lives counter for this variant.
     *
     * @param entityConfig the configuration object containing parameters for the lives counter's appearance
     * @return the 3D node representing a life icon
     */
    Node createLivesCounterShape3D(EntityConfig entityConfig);

    Pellet3D createPellet3D(PelletConfig3D pelletConfig, PhongMaterial material);

    Energizer3D createEnergizer3D(EnergizerConfig3D config, AnimationRegistry animationRegistry, PhongMaterial material);

    //TODO rethink parameters
    PhongMaterial getGhostNumberMaterial(UIConfig uiConfig, int numberIndex);
}
