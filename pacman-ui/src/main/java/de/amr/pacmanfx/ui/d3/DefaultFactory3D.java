/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.ArcadeHouse;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.EnergizerConfig3D;
import de.amr.pacmanfx.ui.config.PelletConfig3D;
import de.amr.pacmanfx.ui.config.WorldConfig;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.d3.entities.MazeHouse3D;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.UfxColors;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.uilib.Ufx.colorBoundPhongMaterial;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

public class DefaultFactory3D implements Factory3D {

    public static final int FLOOR_SPECULAR_POWER = 128;
    public static final int WALL_BASE_SPECULAR_POWER = 64;

    protected final Map<GhostStateColors, GhostMaterialSet> ghostMaterialsCache = new HashMap<>();
    protected final Map<Float, TriangleMesh> pelletMeshesCache = new HashMap<>();

    @Override
    public void dispose() {
        ghostMaterialsCache.clear();
        pelletMeshesCache.clear();
    }

    @Override
    public Maze3D createMaze3D(TerrainLayer terrain, WorldConfig config, WorldMapColorScheme colorScheme, AnimationRegistry animationRegistry) {
        requireNonNull(terrain);
        requireNonNull(config);
        requireNonNull(colorScheme);
        requireNonNull(animationRegistry);

        final var maze3D = new Maze3D();

        //TODO this cyclic dependency is dubious
        final Map<String, PhongMaterial> materials = createMazeMaterials(colorScheme, maze3D.wallOpacityProperty(), maze3D.floorColorProperty());
        maze3D.setMaterials(materials);

        maze3D.createAndAddFloor3D(config.floor(), terrain, materials);
        maze3D.createAndAddObstacles3D(config.maze(), terrain, materials);

        // Currently, only Arcade house is supported
        terrain.optHouse()
            .filter(ArcadeHouse.class::isInstance)
            .map(ArcadeHouse.class::cast)
            .map(house -> new MazeHouse3D(colorScheme, config.house(), animationRegistry, house))
            .ifPresent(maze3D::setHouse3D);

        return maze3D;
    }

    private Map<String, PhongMaterial> createMazeMaterials(WorldMapColorScheme colorScheme, DoubleProperty wallOpacity, ObjectProperty<Color> floorColor) {
        final PhongMaterial floorMaterial = new PhongMaterial();
        floorMaterial.diffuseColorProperty().bind(floorColor);
        floorMaterial.specularColorProperty().bind(floorColor.map(Color::brighter));
        floorMaterial.setSpecularPower(FLOOR_SPECULAR_POWER);

        final PhongMaterial wallBaseMaterial = colorBoundPhongMaterial(
            wallOpacity.map(opacity ->
                UfxColors.colorWithOpacity(
                    Color.valueOf(colorScheme.wallStroke()), opacity.doubleValue())));
        wallBaseMaterial.setSpecularPower(WALL_BASE_SPECULAR_POWER);

        final PhongMaterial wallTopMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.wallFill()));

        return Map.of(
            "floorMaterial", floorMaterial,
            "wallBaseMaterial", wallBaseMaterial,
            "wallTopMaterial", wallTopMaterial
        );
    }

    @Override
    public Pac3D createPac3D(Pac pac, PacConfig config, AnimationRegistry animationRegistry) {
        return Pac3DFactory.createPacMan3D(animationRegistry, pac, config);
    }

    @Override
    public Ghost3D createGhost3D(Ghost ghost, GhostConfig config, AnimationRegistry animationRegistry) {
        return new Ghost3D(
            animationRegistry,
            ghost,
            config,
            new GhostMeshSet(
                PacManWorld3D.instance().ghostDressMesh(),
                PacManWorld3D.instance().ghostPupilsMesh(),
                PacManWorld3D.instance().ghostEyeballsMesh()
            ),
            ghostMaterialsCache.computeIfAbsent(config.colors(), this::createGhostMaterial));
    }

    @Override
    public Group createLivesCounterShape3D(WorldConfig worldConfig) {
        requireNonNull(worldConfig);

        final PacConfig pacConfig = worldConfig.pacConfig().withModifiedSize3D(worldConfig.livesCounter().shapeSize());

        return Pac3DFactory.createPacBody(pacConfig, true);
    }

    @Override
    public Pellet3D createPellet3D(PelletConfig3D config, PhongMaterial material) {
        requireNonNull(config);
        requireNonNull(material);

        final Sphere oval = new Sphere(config.radius());
        oval.setMaterial(material);
        oval.setScaleX(1.25);
        return new Pellet3D(oval);
    }

    @Override
    public Energizer3D createEnergizer3D(EnergizerConfig3D config, PhongMaterial material, AnimationRegistry animationRegistry) {
        requireNonNull(config);
        requireNonNull(material);
        requireNonNull(animationRegistry);

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

    public GhostMaterialSet createGhostMaterial(GhostStateColors colors) {
        requireNonNull(colors);

        final var normalMaterials = new GhostComponentMaterialSet(
            coloredPhongMaterial(colors.normalColors().dressColor()),
            coloredPhongMaterial(colors.normalColors().eyeballsColor()),
            coloredPhongMaterial(colors.normalColors().pupilsColor())
        );

        final var frightenedMaterials = new GhostComponentMaterialSet(
            coloredPhongMaterial(colors.frightenedColors().dressColor()),
            coloredPhongMaterial(colors.frightenedColors().eyeballsColor()),
            coloredPhongMaterial(colors.frightenedColors().pupilsColor())
        );

        final var flashingMaterials = new GhostComponentMaterialSet(
            coloredPhongMaterial(colors.flashingColors().dressColor()),
            coloredPhongMaterial(colors.flashingColors().eyeballsColor()),
            coloredPhongMaterial(colors.flashingColors().pupilsColor())
        );

        return new GhostMaterialSet(normalMaterials, frightenedMaterials, flashingMaterials);
    }

    private TriangleMesh scaledPelletMesh(TriangleMesh pelletMesh, PelletConfig3D config) {
        return pelletMeshesCache.computeIfAbsent(config.radius(), r -> {
            final Bounds bounds = new MeshView(pelletMesh).getBoundsInLocal();
            final double extend = max( max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
            return Ufx.createScaledTriangleMesh(pelletMesh, (2 * r) / extend);
        });
    }
}
