/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.EnergizerConfig3D;
import de.amr.pacmanfx.ui.config.EntityConfig;
import de.amr.pacmanfx.ui.config.PelletConfig3D;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
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

public interface Factory3D extends Disposable {

    Maze3D createMaze3D(
        GameLevel level,
        EntityConfig entityConfig,
        WorldMapColorScheme colorScheme,
        ManagedAnimationsRegistry animations);

    /**
     * Creates the 3D representation of Pac for this game variant, including model,
     * materials, and animation bindings.
     *
     * @param pac               the Pac actor whose animations and state drive the model
     * @param pacConfig         the actor 3D configuration object
     * @param animations the registry where animations are stored
     * @return the 3D representation of Pac
     */
    Pac3D createPac3D(Pac pac, PacConfig pacConfig, ManagedAnimationsRegistry animations);

    /**
     * Creates the 3D representation of a ghost for this game variant, including
     * model, materials, and animation bindings.
     *
     * @param ghost             the ghost actor whose animations and state drive the model
     * @param ghostConfig      the actor 3D configuration object
     * @param animations the registry where animations are stored
     * @return the 3D representation of a ghost
     */
    Ghost3D createGhostAppearance3D(Ghost ghost, GhostConfig ghostConfig, ManagedAnimationsRegistry animations);

    /**
     * Creates the 3D representation of the lives counter for this variant.
     *
     * @param entityConfig the configuration object containing parameters for the lives counter's appearance
     * @return the 3D node representing a life icon
     */
    Node createLivesCounterShape3D(EntityConfig entityConfig);

    Pellet3D createPellet3D(PelletConfig3D pelletConfig, PhongMaterial material);

    Energizer3D createEnergizer3D(EnergizerConfig3D config, ManagedAnimationsRegistry animations, PhongMaterial material);

    MazeMaterials3D createMazeMaterials(WorldMapColorScheme colorScheme, DoubleProperty wallOpacity, ObjectProperty<Color> floorColor);
}
