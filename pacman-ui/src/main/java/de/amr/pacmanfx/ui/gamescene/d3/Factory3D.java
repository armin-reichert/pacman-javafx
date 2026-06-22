/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.config.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.config.world.WorldSettings;
import de.amr.pacmanfx.ui.gamescene.d3.entities.Maze3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostSettings;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.PacSettings;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.Pellet3D;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;

public interface Factory3D extends Disposable {

    /**
     * Creates a 3D maze.
     *
     * @param drawMode draw mode property (wireframe or normal mode)
     * @param terrain the terrain layer
     * @param config world configuration
     * @param colorScheme the map color scheme
     * @param animationRegistry the animation registry
     * @return a 3D maze representing the given terrain
     */
    Maze3D createMaze3D(ObjectProperty<DrawMode> drawMode, TerrainLayer terrain, WorldSettings config, WorldMapColorScheme colorScheme, AnimationRegistry animationRegistry);

    /**
     * Creates the 3D representation of Pac for this game variant, including model,
     * materials, and animation bindings.
     *
     * @param pac               the Pac actor whose animations and state drive the model
     * @param pacConfig         the actor 3D configuration object
     * @param animations the registry where animations are stored
     * @return the 3D representation of Pac
     */
    Pac3D createPac3D(Pac pac, PacSettings pacConfig, AnimationRegistry animations);

    /**
     * Creates the 3D representation of a ghost for this game variant, including
     * model, materials, and animation bindings.
     *
     * @param ghost             the ghost actor whose animations and state drive the model
     * @param ghostConfig      the actor 3D configuration object
     * @param animations the registry where animations are stored
     * @return the 3D representation of a ghost
     */
    Ghost3D createGhost3D(Ghost ghost, GhostSettings ghostConfig, AnimationRegistry animations);

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
     * @param animationRegistry the animation registry
     * @return 3D energizer representation
     */
    Energizer3D createEnergizer3D(Energizer3DSettings config, PhongMaterial material, AnimationRegistry animationRegistry);
}
