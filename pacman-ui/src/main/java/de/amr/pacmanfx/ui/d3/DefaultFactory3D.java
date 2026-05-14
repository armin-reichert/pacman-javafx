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
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.UfxColors;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.model3D.ghost.*;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3DFactory;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.Pellet3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.uilib.Ufx.colorBoundPhongMaterial;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

public class DefaultFactory3D implements Factory3D {

    public static final int DEFAULT_NUMBER_BOX_SIZE_X = 14;
    public static final int DEFAULT_NUMBER_BOX_SIZE_Y = 8;
    public static final int DEFAULT_NUMBER_BOX_SIZE_Z = 8;

    public static final int FLOOR_SPECULAR_POWER = 128;
    public static final int WALL_BASE_SPECULAR_POWER = 64;

    protected final Map<GhostAppearanceColors, GhostMaterialSet> ghostMaterialsCache = new HashMap<>();
    protected final Map<Float, Mesh> pelletMeshesCache = new HashMap<>();

    @Override
    public void dispose() {
        ghostMaterialsCache.clear();
        pelletMeshesCache.clear();
    }

    @Override
    public MazeMaterials3D createMazeMaterials(WorldMapColorScheme colorScheme, DoubleProperty wallOpacity, ObjectProperty<Color> floorColor) {
        final PhongMaterial floorMaterial = colorBoundPhongMaterial(floorColor);
        floorMaterial.setSpecularPower(FLOOR_SPECULAR_POWER);

        final PhongMaterial wallBaseMaterial = colorBoundPhongMaterial(wallOpacity.map(
            opacity -> UfxColors.colorWithOpacity(Color.valueOf(colorScheme.wallStroke()), opacity.doubleValue())));
        wallBaseMaterial.setSpecularPower(WALL_BASE_SPECULAR_POWER);

        final PhongMaterial wallTopMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.wallFill()));

        return new MazeMaterials3D(floorMaterial, wallBaseMaterial, wallTopMaterial);
    }

    @Override
    public Maze3D createMaze3D(
        GameLevel level,
        EntityConfig entityConfig,
        WorldMapColorScheme colorScheme,
        ManagedAnimationsRegistry animations)
    {
        final Maze3D maze3D = new Maze3D(level, this, entityConfig, colorScheme, animations);
        maze3D.wallOpacityProperty().bind(PROPERTY_3D_WALL_OPACITY);
        maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
        maze3D.floorColorProperty().bind(PROPERTY_3D_FLOOR_COLOR);
        return maze3D;
    }

    @Override
    public Pac3D createPac3D(Pac pac, PacConfig pacConfig, ManagedAnimationsRegistry animations) {
        requireNonNull(pac);
        requireNonNull(pacConfig);
        requireNonNull(animations);
        return Pac3DFactory.createPacMan3D(animations, pac, pacConfig);
    }

    @Override
    public GhostAppearance3D createGhostAppearance3D(
        Ghost ghost,
        GhostConfig ghostConfig,
        ManagedAnimationsRegistry animations)
    {
        requireNonNull(ghost);
        requireNonNull(ghostConfig);
        requireNonNull(animations);

        final GhostAppearanceColors colors = ghostConfig.createGhostColorSet();
        final GhostMaterialSet materials = ghostMaterialsCache.computeIfAbsent(colors, this::createGhostMaterial);
        final GhostMeshSet meshSet = new GhostMeshSet(
            PacManWorld3D.instance().ghostDressMesh(),
            PacManWorld3D.instance().ghostPupilsMesh(),
            PacManWorld3D.instance().ghostEyeballsMesh()
        );

        return new GhostAppearance3D(
            animations,
            ghost,
            colors,
            meshSet,
            materials,
            ghostConfig.size3D()
        );
    }

    @Override
    public Group createLivesCounterShape3D(EntityConfig entityConfig) {
        requireNonNull(entityConfig);
        final PacConfig pacConfig = entityConfig.pacConfig().withModifiedSize3D(entityConfig.livesCounter().shapeSize());
        return Pac3DFactory.createPacBody(pacConfig);
    }

    @Override
    public Pellet3D createPellet3D(PelletConfig3D pelletConfig, PhongMaterial material) {
        final Mesh mesh = scaledPelletMesh(PacManWorld3D.instance().pelletMesh(), pelletConfig);
        final Shape3D shape = new MeshView(mesh);
        shape.setMaterial(material);
        return new Pellet3D(shape);
    }

    @Override
    public Energizer3D createEnergizer3D(EnergizerConfig3D config, ManagedAnimationsRegistry animations, PhongMaterial material) {
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

    public GhostMaterialSet createGhostMaterial(GhostAppearanceColors colors) {
        final var normalMaterials = new GhostComponentMaterialSet(
            coloredPhongMaterial(colors.normalColor().dressColor()),
            coloredPhongMaterial(colors.normalColor().eyeballsColor()),
            coloredPhongMaterial(colors.normalColor().pupilsColor())
        );

        final var frightenedMaterials = new GhostComponentMaterialSet(
            coloredPhongMaterial(colors.frightenedColor().dressColor()),
            coloredPhongMaterial(colors.frightenedColor().eyeballsColor()),
            coloredPhongMaterial(colors.frightenedColor().pupilsColor())
        );

        final var flashingMaterials = new GhostComponentMaterialSet(
            coloredPhongMaterial(colors.flashingColor().dressColor()),
            coloredPhongMaterial(colors.flashingColor().eyeballsColor()),
            coloredPhongMaterial(colors.flashingColor().pupilsColor())
        );

        Logger.info("Created ghost materials for color set {}", colors);
        return new GhostMaterialSet(normalMaterials, frightenedMaterials, flashingMaterials);
    }

    public Mesh scaledPelletMesh(Mesh pelletMesh, PelletConfig3D config) {
        requireNonNull(pelletMesh);
        requireNonNull(config);
        if (!(pelletMesh instanceof TriangleMesh triangleMesh)) {
            throw new IllegalArgumentException("Cannot scale pellet mesh (mo triangle mesh");
        }
        return pelletMeshesCache.computeIfAbsent(config.radius(), r -> {
            Logger.info("Computing scaled pellet mesh of radius {}", r);
            final Bounds bounds = new MeshView(pelletMesh).getBoundsInLocal();
            final double extend = max( max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
            return Ufx.createScaledTriangleMesh(triangleMesh, (2 * r) / extend);
        });
    }
}
