/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.EnergizerConfig3D;
import de.amr.pacmanfx.ui.config.EntityConfig;
import de.amr.pacmanfx.ui.config.PelletConfig3D;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.d3.entities.Pellet3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.GhostMaterials;
import de.amr.pacmanfx.uilib.model3D.actor.*;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.PelletModel3D;
import de.amr.pacmanfx.uilib.objimport.MeshHelper;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class DefaultFactory3D implements Factory3D {

    /** Shared 3D model instance for Pac-Man. */
    public static final PacManModel3D PAC_MAN_MODEL = new PacManModel3D();

    /** Shared 3D model instance for ghosts. */
    public static final GhostModel3D GHOST_MODEL = new GhostModel3D();

    /** Shared 3D model instance for pellets. */
    public static final PelletModel3D PELLET_MODEL = new PelletModel3D();

    public static final int DEFAULT_NUMBER_BOX_SIZE_X = 14;
    public static final int DEFAULT_NUMBER_BOX_SIZE_Y = 8;
    public static final int DEFAULT_NUMBER_BOX_SIZE_Z = 8;

    protected final Map<GhostColorSet, GhostMaterials> ghostMaterialsCache = new HashMap<>();
    protected double pelletRadius;
    protected Mesh scaledPelletMesh;

    protected GhostMaterials createGhostMaterial(GhostColorSet colorSet) {
        final var normalMaterials = new GhostComponentMaterials(
            coloredPhongMaterial(colorSet.normal().dress()),
            coloredPhongMaterial(colorSet.normal().eyeballs()),
            coloredPhongMaterial(colorSet.normal().pupils())
        );

        final var frightenedMaterials = new GhostComponentMaterials(
            coloredPhongMaterial(colorSet.frightened().dress()),
            coloredPhongMaterial(colorSet.frightened().eyeballs()),
            coloredPhongMaterial(colorSet.frightened().pupils())
        );

        final var flashingMaterials = new GhostComponentMaterials(
            coloredPhongMaterial(colorSet.flashing().dress()),
            coloredPhongMaterial(colorSet.flashing().eyeballs()),
            coloredPhongMaterial(colorSet.flashing().pupils())
        );

        Logger.info("Created ghost materials for color set {}", colorSet);
        return new GhostMaterials(normalMaterials, frightenedMaterials, flashingMaterials);
    }

    protected Mesh scaledPelletMesh(Mesh originalPelletMesh, PelletConfig3D pelletConfig) {
        requireNonNull(pelletConfig);
        if (scaledPelletMesh == null || pelletConfig.radius() != pelletRadius) {
            pelletRadius = pelletConfig.radius();
            final var dummy = new MeshView(originalPelletMesh);
            final Bounds bounds = dummy.getBoundsInLocal();
            final double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
            final double scaling = (2 * pelletRadius) / maxExtent;
            scaledPelletMesh = MeshHelper.createScaledMesh(originalPelletMesh, scaling);
            Logger.info("Created scaled pellet mesh, config={}", pelletConfig);
        }
        return scaledPelletMesh;
    }

    @Override
    public void dispose() {
        ghostMaterialsCache.clear();
    }

    @Override
    public Maze3D createMaze3D(
        GameLevel level,
        EntityConfig entityConfig,
        WorldMapColorScheme colorScheme,
        AnimationRegistry animations)
    {
        final Maze3D maze3D = new Maze3D(level, this, entityConfig, colorScheme, animations);
        maze3D.wallOpacityProperty().bind(PROPERTY_3D_WALL_OPACITY);
        maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
        maze3D.floorColorProperty().bind(PROPERTY_3D_FLOOR_COLOR);
        return maze3D;
    }

    @Override
    public Pac3D createPac3D(Pac pac, PacConfig pacConfig, AnimationRegistry animations) {
        requireNonNull(pac);
        requireNonNull(pacConfig);
        requireNonNull(animations);

        return new PacMan3D(animations, PAC_MAN_MODEL, pac, pacConfig);
    }

    @Override
    public GhostAppearance3D createGhostAppearance3D(
        Ghost ghost,
        GhostConfig ghostConfig,
        AnimationRegistry animations)
    {
        requireNonNull(ghost);
        requireNonNull(ghostConfig);
        requireNonNull(animations);

        final GhostColorSet colorSet = ghostConfig.createGhostColorSet();
        final GhostMaterials materials = ghostMaterialsCache.computeIfAbsent(colorSet, this::createGhostMaterial);
        final GhostMeshes meshes = new GhostMeshes(
            GHOST_MODEL.dressMesh(),
            GHOST_MODEL.pupilsMesh(),
            GHOST_MODEL.eyeballsMesh()
        );

        return new GhostAppearance3D(
            animations,
            ghost,
            colorSet,
            meshes,
            materials,
            ghostConfig.size3D()
        );
    }

    @Override
    public Group createLivesCounterShape3D(EntityConfig entityConfig) {
        requireNonNull(entityConfig);
        final PacConfig pacConfig = entityConfig.pacConfig().withModifiedSize3D(entityConfig.livesCounter().shapeSize());
        return PAC_MAN_MODEL.createPacBody(pacConfig);
    }

    @Override
    public Pellet3D createPellet3D(PelletConfig3D pelletConfig, PhongMaterial material) {
        final Mesh scaledPelletMesh = scaledPelletMesh(PELLET_MODEL.mesh(), pelletConfig);
        final var pellet3D = new Pellet3D(new MeshView(scaledPelletMesh));
        pellet3D.shape().setMaterial(material);
        return pellet3D;
    }

    @Override
    public Energizer3D createEnergizer3D(EnergizerConfig3D config, AnimationRegistry animations, PhongMaterial material) {
        final var energizer3D = new Energizer3D(animations);
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
        final var numberShape3D = new Box(DEFAULT_NUMBER_BOX_SIZE_X, DEFAULT_NUMBER_BOX_SIZE_Y, DEFAULT_NUMBER_BOX_SIZE_Z);
        numberShape3D.setMaterial(material);
        return numberShape3D;
    }
}
