/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorScheme;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.settings.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostSettings;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.PacSettings;
import de.amr.pacmanfx.uilib.entities3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.entities3D.world.Pellet3D;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;

public interface Factory3D extends Disposable {

    /**
     * Creates the 3D representation of Pac for this game variant, including model,
     * materials, and animation bindings.
     *
     * @param pac               the Pac actor whose animations and state drive the model
     * @param pacConfig         the actor 3D configuration object
     */
    void createPac3D(Pac pac, PacSettings pacConfig);

    /**
     * Creates the 3D representation of a ghost for this game variant, including
     * model, materials, and animation bindings.
     *
     * @param ghost             the ghost actor whose animations and state drive the model
     * @param ghostConfig      the actor 3D configuration object
     */
    void createGhost3D(Ghost ghost, GhostSettings ghostConfig);

    /**
     * Creates the 3D representation of the lives counter for this variant.
     *
     * @param worldConfig the configuration object containing parameters for the lives counter's appearance
     * @return the 3D node representing a life icon
     */
    Node createLivesCounterShape3D(WorldSettings worldConfig);

    /**
     * @param config pellet configuration
     * @param material pellet material
     * @return 3D pellet representation
     */
    Pellet3D createPellet3D(Pellet3DSettings config, PhongMaterial material);

    /**
     * @param config energizer configuration
     * @param material energizer material
     * @return 3D energizer representation
     */
    Energizer3D createEnergizer3D(Energizer3DSettings config, PhongMaterial material);
}
