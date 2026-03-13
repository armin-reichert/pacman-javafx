/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.ui.d3.Pellet3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.actor.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.actor.MsPacManBody;
import de.amr.pacmanfx.uilib.model3D.actor.MutableGhost3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.geometry.Bounds;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_Factory3D implements Factory3D {

    @Override
    public MsPacMan3D createPac3D(
        Pac pac,
        PacConfig pacConfig,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(pac);
        requireNonNull(pacConfig);
        requireNonNull(animationRegistry);

        final var msPacMan3D = new MsPacMan3D(
            animationRegistry,
            pac,
            pacConfig.size3D(),
            pacConfig.headColor(),
            pacConfig.eyesColor(),
            pacConfig.palateColor(),
            pacConfig.hairbowColor(),
            pacConfig.hairBowPearlsColor(),
            pacConfig.boobsColor()
        );

        msPacMan3D.light().setColor(pacConfig.headColor().desaturate());
        return msPacMan3D;
    }

    @Override
    public MutableGhost3D createMutableGhost3D(
        Ghost ghost,
        GhostConfig ghostConfig,
        AnimationRegistry animationRegistry,
        int numFlashings)
    {
        requireNonNull(ghost);
        requireNonNull(ghostConfig);
        requireNonNull(animationRegistry);

        return new MutableGhost3D(
            animationRegistry,
            ghost,
            ghostConfig.createGhostColorSet(),
            Models3D.GHOST_MODEL.dressMesh(),
            Models3D.GHOST_MODEL.pupilsMesh(),
            Models3D.GHOST_MODEL.eyeballsMesh(),
            ghostConfig.size3D(),
            numFlashings
        );
    }

    @Override
    public MsPacManBody createLivesCounterShape3D(EntityConfig entityConfig) {
        requireNonNull(entityConfig);

        final var pacConfig = entityConfig.pacConfig();
        return Models3D.PAC_MAN_MODEL.createMsPacManBody(
            entityConfig.livesCounter().shapeSize(),
            pacConfig.headColor(),
            pacConfig.eyesColor(),
            pacConfig.palateColor(),
            pacConfig.hairbowColor(),
            pacConfig.hairBowPearlsColor(),
            pacConfig.boobsColor()
        );
    }

    @Override
    public Pellet3D createPellet3D(PelletConfig3D pelletConfig, PhongMaterial material, Vector2i tile) {
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

    @Override
    public Energizer3D createEnergizer3D(EnergizerConfig3D config, AnimationRegistry animationRegistry, PhongMaterial material, Vector2i tile) {
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
