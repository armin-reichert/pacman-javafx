/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.ui.d3.Pellet3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.GhostMaterials;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.actor.*;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.geometry.Bounds;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import org.tinylog.Logger;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Factory3D implements Factory3D {

    private double pelletRadius;
    private Mesh scaledPelletMesh;

    private Mesh scaledPelletMesh(PelletConfig3D pelletConfig) {
        requireNonNull(pelletConfig);
        if (scaledPelletMesh == null || pelletConfig.radius() != pelletRadius) {
            pelletRadius = pelletConfig.radius();
            final Mesh originalPelletMesh = Models3D.PELLET_MODEL.mesh();
            final var dummy = new MeshView(originalPelletMesh);
            final Bounds bounds = dummy.getBoundsInLocal();
            final double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
            final double scaling = (2 * pelletRadius) / maxExtent;
            scaledPelletMesh = Models3D.createScaledMesh(originalPelletMesh, scaling);
            Logger.info("Created scaled pellet mesh, config={}", pelletConfig);
        }
        return scaledPelletMesh;
    }

    @Override
    public MsPacMan3D createPac3D(
        Pac pac,
        PacConfig pacConfig,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(pac);
        requireNonNull(pacConfig);
        requireNonNull(animationRegistry);

        var pac3D = new MsPacMan3D(
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

        pac3D.light().setColor(pacConfig.headColor().desaturate());
        return pac3D;
    }

    @Override
    public MutableGhost3D createMutableGhost3D(
        Ghost ghost,
        GhostConfig ghostConfig,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(ghost);
        requireNonNull(ghostConfig);
        requireNonNull(animationRegistry);

        final GhostColorSet colorSet = ghostConfig.createGhostColorSet();
        final GhostMaterials materials = createGhostMaterials(colorSet);
        final GhostMeshes meshes = createGhostMeshes();

        return new MutableGhost3D(
            animationRegistry,
            ghost,
            colorSet,
            meshes,
            materials,
            ghostConfig.size3D()
        );
    }

    private GhostMeshes createGhostMeshes() {
        return new GhostMeshes(
            Models3D.GHOST_MODEL.dressMesh(),
            Models3D.GHOST_MODEL.pupilsMesh(),
            Models3D.GHOST_MODEL.eyeballsMesh()
        );
    }

    private GhostMaterials createGhostMaterials(GhostColorSet colorSet) {
        final var normalMaterials = new GhostComponentMaterials(
            coloredPhongMaterial(colorSet.normal().dressColor()),
            coloredPhongMaterial(colorSet.normal().eyeballsColor()),
            coloredPhongMaterial(colorSet.normal().pupilsColor())
        );

        final var frightenedMaterials = new GhostComponentMaterials(
            coloredPhongMaterial(colorSet.frightened().dressColor()),
            coloredPhongMaterial(colorSet.frightened().eyeballsColor()),
            coloredPhongMaterial(colorSet.frightened().pupilsColor())
        );

        final var flashingMaterials = new GhostComponentMaterials(
            coloredPhongMaterial(colorSet.flashing().dressColor()),
            coloredPhongMaterial(colorSet.flashing().eyeballsColor()),
            coloredPhongMaterial(colorSet.flashing().pupilsColor())
        );

        return new GhostMaterials(normalMaterials, frightenedMaterials, flashingMaterials);
    }

    @Override
    public MsPacManBody createLivesCounterShape3D(EntityConfig entityConfig) {
        requireNonNull(entityConfig);

        final PacConfig pacConfig = entityConfig.pacConfig();
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
    public Pellet3D createPellet3D(PelletConfig3D pelletConfig, PhongMaterial material) {
        final Mesh scaledPelletMesh = scaledPelletMesh(pelletConfig);
        final var pellet3D = new Pellet3D(new MeshView(scaledPelletMesh));
        pellet3D.shape().setMaterial(material);
        return pellet3D;
    }

    @Override
    public Energizer3D createEnergizer3D(EnergizerConfig3D config, AnimationRegistry animationRegistry, PhongMaterial material) {
        final var energizer3D = new Energizer3D(animationRegistry);
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
