/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.config.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.config.world.WorldSettings;
import de.amr.pacmanfx.ui.gamescene.d3.entities.Maze3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.model3D.ghost.*;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3DFactory;
import de.amr.pacmanfx.uilib.model3D.pac.PacSettings;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.Pellet3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class DefaultFactory3D implements Factory3D {

    protected final Map<GhostStateColors, GhostMaterialSet> ghostMaterialsCache = new HashMap<>();
    protected final Map<Float, TriangleMesh> pelletMeshesCache = new HashMap<>();
    protected final MazeFactory3D mazeFactory3D;

    public DefaultFactory3D() {
        mazeFactory3D = new MazeFactory3D();
    }

    @Override
    public void dispose() {
        ghostMaterialsCache.clear();
        pelletMeshesCache.clear();
    }

    @Override
    public Maze3D createMaze3D(
        TerrainLayer terrain, WorldSettings config,
        WorldMapColorScheme colorScheme, AnimationRegistry animationRegistry) {

        return mazeFactory3D.createMaze3D(terrain, config, colorScheme, animationRegistry);
    }


    @Override
    public Pac3D createPac3D(Pac pac, PacSettings config, AnimationRegistry animationRegistry) {
        return Pac3DFactory.createPacMan3D(animationRegistry, pac, config);
    }

    @Override
    public Ghost3D createGhost3D(Ghost ghost, GhostSettings config, AnimationRegistry animationRegistry) {
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
    public Group createLivesCounterShape3D(WorldSettings worldConfig) {
        requireNonNull(worldConfig);

        final PacSettings pacConfig = worldConfig.pac().withModifiedSize3D(worldConfig.livesCounter().shapeSize());

        return Pac3DFactory.createPacBody(pacConfig, true);
    }

    @Override
    public Pellet3D createPellet3D(Pellet3DSettings config, PhongMaterial material) {
        requireNonNull(config);
        requireNonNull(material);

        final Sphere oval = new Sphere(config.radius());
        oval.setMaterial(material);
        oval.setScaleX(1.25);
        return new Pellet3D(oval);
    }

    @Override
    public Energizer3D createEnergizer3D(Energizer3DSettings config, PhongMaterial material, AnimationRegistry animationRegistry) {
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
            coloredPhongMaterial(colors.normal().dressColor()),
            coloredPhongMaterial(colors.normal().eyeballsColor()),
            coloredPhongMaterial(colors.normal().pupilsColor())
        );

        final var frightenedMaterials = new GhostComponentMaterialSet(
            coloredPhongMaterial(colors.frightened().dressColor()),
            coloredPhongMaterial(colors.frightened().eyeballsColor()),
            coloredPhongMaterial(colors.frightened().pupilsColor())
        );

        final var flashingMaterials = new GhostComponentMaterialSet(
            coloredPhongMaterial(colors.flashing().dressColor()),
            coloredPhongMaterial(colors.flashing().eyeballsColor()),
            coloredPhongMaterial(colors.flashing().pupilsColor())
        );

        return new GhostMaterialSet(normalMaterials, frightenedMaterials, flashingMaterials);
    }
}
