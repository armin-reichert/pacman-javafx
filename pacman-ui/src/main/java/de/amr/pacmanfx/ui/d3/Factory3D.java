/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.actor.MutableGhost3D;
import de.amr.pacmanfx.uilib.model3D.actor.PacRepresentation3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public interface Factory3D {

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
     * @param numFlashings      the number of flashing states to support for the ghost (e.g., for frightened mode)
     * @return the 3D representation of a ghost
     */
    MutableGhost3D createMutableGhost3D(Ghost ghost, GhostConfig ghostConfig, AnimationRegistry animationRegistry, int numFlashings);

    /**
     * Creates the 3D representation of the lives counter for this variant.
     *
     * @param entityConfig the configuration object containing parameters for the lives counter's appearance
     * @return the 3D node representing a life icon
     */
    Node createLivesCounterShape3D(EntityConfig entityConfig);

    default Pellet3D createPellet3D(PelletConfig3D pelletConfig, PhongMaterial material, Vector2i tile) {
        final Mesh pelletMesh = Models3D.PELLET_MODEL.mesh();
        final var dummy = new MeshView(pelletMesh);
        final Bounds bounds = dummy.getBoundsInLocal();
        final double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        final double scaling = (2 * pelletConfig.radius()) / maxExtent;
        final Mesh scaledPelletMesh = Models3D.createScaledMesh(pelletMesh, scaling);
        final var pellet3D = new Pellet3D(scaledPelletMesh, tile);
        pellet3D.setMaterial(material);
        //TODO fix rotation in OBJ file
        pellet3D.setRotationAxis(Rotate.Z_AXIS);
        pellet3D.setRotate(90);

        pellet3D.setTranslateX(tile.x() * TS + HTS);
        pellet3D.setTranslateY(tile.y() * TS + HTS);

        return pellet3D;
    }

    default Energizer3D createEnergizer3D(EnergizerConfig3D config, AnimationRegistry animationRegistry, PhongMaterial material, Vector2i tile) {
        final var energizer3D = new Energizer3D(animationRegistry, tile);
        energizer3D.setShapeFactory(() -> {
            final var shape = new Sphere(config.radius(), 48);
            shape.setMaterial(material);
            return shape;
        });
        energizer3D.setPumpingFrequency(config.pumpingFrequency());
        energizer3D.setInflatedSize(config.scalingInflated());
        energizer3D.setExpandedSize(config.scalingExpanded());
        return energizer3D;
    }
}
