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
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

public class DefaultFactory3D implements Factory3D {

    public static final int FLOOR_SPECULAR_POWER = 128;
    public static final int WALL_BASE_SPECULAR_POWER = 64;
    public static final int WALL_TOP_SPECULAR_POWER = 128;

    protected final Map<GhostStateColors, GhostMaterialSet> ghostMaterialsCache = new HashMap<>();
    protected final Map<Float, TriangleMesh> pelletMeshesCache = new HashMap<>();

    @Override
    public void dispose() {
        ghostMaterialsCache.clear();
        pelletMeshesCache.clear();
    }

    @Override
    public Maze3D createMaze3D(UISettings3D globals3D, TerrainLayer terrain, WorldConfig config, WorldMapColorScheme colorScheme, AnimationRegistry animationRegistry) {
        requireNonNull(terrain);
        requireNonNull(config);
        requireNonNull(colorScheme);
        requireNonNull(animationRegistry);

        final Map<String, PhongMaterial> materials = createMazeMaterialMap(config, colorScheme);

        final var maze3D = new Maze3D(terrain);
        maze3D.build(globals3D, materials, config.maze(), config.floor());

        bindFloorMaterialColor(maze3D, materials.get("floorMaterial"));
        bindWallBaseMaterialColor(maze3D, materials.get("wallBaseMaterial"), Color.valueOf(colorScheme.wallStroke()));

        // Currently, only Arcade house is supported
        terrain.optHouse()
            .filter(ArcadeHouse.class::isInstance)
            .map(ArcadeHouse.class::cast)
            .map(house -> new MazeHouse3D(colorScheme, config.house(), animationRegistry, house))
            .ifPresent(maze3D::setHouse3D);

        return maze3D;
    }

    private Map<String, PhongMaterial> createMazeMaterialMap(WorldConfig config, WorldMapColorScheme colorScheme) {
        final PhongMaterial floorMaterial = new PhongMaterial();
        floorMaterial.setSpecularPower(FLOOR_SPECULAR_POWER);

        final PhongMaterial wallBaseMaterial = new PhongMaterial();
        wallBaseMaterial.setSpecularPower(WALL_BASE_SPECULAR_POWER);

        final PhongMaterial wallTopMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.wallFill()));
        wallTopMaterial.setSpecularPower(WALL_TOP_SPECULAR_POWER);

        return Map.of(
            "floorMaterial", floorMaterial,
            "wallBaseMaterial", wallBaseMaterial,
            "wallTopMaterial", wallTopMaterial
        );
    }

    private void bindFloorMaterialColor(Maze3D maze3D, PhongMaterial floorMaterial) {
        floorMaterial.diffuseColorProperty().bind(maze3D.floorColorProperty());
        floorMaterial.specularColorProperty().bind(maze3D.floorColorProperty().map(Color::brighter));
    }

    private void bindWallBaseMaterialColor(Maze3D maze3D, PhongMaterial wallBaseMaterial, Color wallStrokeColor) {
        wallBaseMaterial.diffuseColorProperty().bind(maze3D.wallOpacityProperty()
            .map(opacity -> UfxColors.colorWithOpacity(wallStrokeColor, opacity.doubleValue()))
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
