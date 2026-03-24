/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.ui.d3.Pellet3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.GhostMaterials;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.actor.*;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_Factory3D implements Factory3D {

    private static final int NUMBER_BOX_SIZE_X = 14;
    private static final int NUMBER_BOX_SIZE_Y = 8;
    private static final int NUMBER_BOX_SIZE_Z = 8;

    private final Map<GhostColorSet, GhostMaterials> ghostMaterialsCache = new HashMap<>();

    private GhostMeshes createGhostMeshes() {
        return new GhostMeshes(
            Models3D.GHOST_MODEL.dressMesh(),
            Models3D.GHOST_MODEL.pupilsMesh(),
            Models3D.GHOST_MODEL.eyeballsMesh()
        );
    }

    private GhostMaterials getOrCreateGhostMaterials(GhostColorSet colorSet) {
        GhostMaterials materials = ghostMaterialsCache.get(colorSet);
        if (materials == null) {
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

            materials = new GhostMaterials(normalMaterials, frightenedMaterials, flashingMaterials);
            ghostMaterialsCache.put(colorSet, materials);

            Logger.info("Added ghost materials into cache for color set {}", colorSet);
        }
        return materials;
    }

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
    public void dispose() {
        ghostMaterialsCache.clear();
    }

    @Override
    public PacMan3D createPac3D(
        Pac pac,
        PacConfig pacConfig,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(pac);
        requireNonNull(pacConfig);
        requireNonNull(animationRegistry);

        final var pacMan3D = new PacMan3D(animationRegistry, pac, pacConfig.size3D(), pacConfig.colors());

        pacMan3D.light().setColor(pacConfig.colors().head().desaturate());
        return pacMan3D;
    }

    @Override
    public GhostAppearance3D createGhostAppearance3D(
        Ghost ghost,
        GhostConfig ghostConfig,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(ghost);
        requireNonNull(ghostConfig);
        requireNonNull(animationRegistry);

        final GhostColorSet colorSet = ghostConfig.createGhostColorSet();
        final GhostMaterials materials = getOrCreateGhostMaterials(colorSet);
        final GhostMeshes meshes = createGhostMeshes();

        return new GhostAppearance3D(
            animationRegistry,
            ghost,
            colorSet,
            meshes,
            materials,
            ghostConfig.size3D()
        );
    }

    @Override
    public PacBody createLivesCounterShape3D(EntityConfig entityConfig) {
        requireNonNull(entityConfig);
        final PacConfig pacConfig = entityConfig.pacConfig();
        return Models3D.PAC_MAN_MODEL.createPacBody(entityConfig.livesCounter().shapeSize(), pacConfig.colors());
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

    @Override
    public Shape3D createNumberShape3D(UIConfig uiConfig, int numberIndex) {
        final Image image = uiConfig.killedGhostPointsImage(numberIndex);
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(image);
        final var numberShape3D = new Box(NUMBER_BOX_SIZE_X, NUMBER_BOX_SIZE_Y, NUMBER_BOX_SIZE_Z);
        numberShape3D.setMaterial(material);
        return numberShape3D;
    }
}
