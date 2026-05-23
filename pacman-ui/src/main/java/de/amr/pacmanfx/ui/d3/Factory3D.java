/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.EnergizerConfig3D;
import de.amr.pacmanfx.ui.config.PelletConfig3D;
import de.amr.pacmanfx.ui.config.WorldConfig;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostConfig;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.Pellet3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.Map;

public interface Factory3D extends Disposable {

    /**
     * Creates a 3D maze.
     *
     * @param terrain the terrain layer
     * @param config world configuration
     * @param colorScheme the map color scheme
     * @param animationRegistry the animation registry
     * @return a 3D maze representing the given terrain
     */
    Maze3D createMaze3D(TerrainLayer terrain, WorldConfig config, WorldMapColorScheme colorScheme, AnimationRegistry animationRegistry);

    /**
     * Creates the 3D representation of Pac for this game variant, including model,
     * materials, and animation bindings.
     *
     * @param pac               the Pac actor whose animations and state drive the model
     * @param pacConfig         the actor 3D configuration object
     * @param animations the registry where animations are stored
     * @return the 3D representation of Pac
     */
    Pac3D createPac3D(Pac pac, PacConfig pacConfig, AnimationRegistry animations);

    /**
     * Creates the 3D representation of a ghost for this game variant, including
     * model, materials, and animation bindings.
     *
     * @param ghost             the ghost actor whose animations and state drive the model
     * @param ghostConfig      the actor 3D configuration object
     * @param animations the registry where animations are stored
     * @return the 3D representation of a ghost
     */
    Ghost3D createGhost3D(Ghost ghost, GhostConfig ghostConfig, AnimationRegistry animations);

    /**
     * Creates the 3D representation of the lives counter for this variant.
     *
     * @param worldConfig the configuration object containing parameters for the lives counter's appearance
     * @return the 3D node representing a life icon
     */
    Node createLivesCounterShape3D(WorldConfig worldConfig);

    /**
     * @param config pellet configuration
     * @param material pellet material
     * @return 3D pellet representation
     */
    Pellet3D createPellet3D(PelletConfig3D config, PhongMaterial material);

    /**
     * @param config energizer configuration
     * @param material energizer material
     * @param animationRegistry the animation registry
     * @return 3D energizer representation
     */
    Energizer3D createEnergizer3D(EnergizerConfig3D config, PhongMaterial material, AnimationRegistry animationRegistry);

    /**
     * @param colorScheme world map colors
     * @param wallOpacity wall opacity
     * @param floorColor floor surface color
     * @return map of Phong materials used for maze creation
     */
    Map<String, PhongMaterial> createMazeMaterials(WorldMapColorScheme colorScheme, DoubleProperty wallOpacity, ObjectProperty<Color> floorColor);
}
