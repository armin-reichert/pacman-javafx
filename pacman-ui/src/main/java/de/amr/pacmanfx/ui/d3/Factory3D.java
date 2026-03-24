/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.uilib.UfxColors;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.actor.GhostAppearance3D;
import de.amr.pacmanfx.uilib.model3D.actor.PacRepresentation3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;

import static de.amr.pacmanfx.uilib.Ufx.colorBoundPhongMaterial;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;

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
    GhostAppearance3D createGhostAppearance3D(Ghost ghost, GhostConfig ghostConfig, AnimationRegistry animationRegistry);

    /**
     * Creates the 3D representation of the lives counter for this variant.
     *
     * @param entityConfig the configuration object containing parameters for the lives counter's appearance
     * @return the 3D node representing a life icon
     */
    Node createLivesCounterShape3D(EntityConfig entityConfig);

    Pellet3D createPellet3D(PelletConfig3D pelletConfig, PhongMaterial material);

    Energizer3D createEnergizer3D(EnergizerConfig3D config, AnimationRegistry animationRegistry, PhongMaterial material);

    Shape3D createNumberShape3D(UIConfig uiConfig, int numberIndex);

    default MazeMaterials3D createMazeMaterials(WorldMapColorScheme colorScheme, DoubleProperty wallOpacity, ObjectProperty<Color> floorColor) {
        final PhongMaterial floorMaterial = colorBoundPhongMaterial(floorColor);
        floorMaterial.setSpecularPower(128);

        final PhongMaterial wallBaseMaterial = colorBoundPhongMaterial(wallOpacity.map(
            opacity -> UfxColors.colorWithOpacity(Color.valueOf(colorScheme.wallStroke()), opacity.doubleValue())));
        wallBaseMaterial.setSpecularPower(64);

        final PhongMaterial wallTopMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.wallFill()));

        return new MazeMaterials3D(floorMaterial, wallBaseMaterial, wallTopMaterial);
    }
}
