/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorScheme;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.settings.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.PacMan3DModel;
import de.amr.pacmanfx.uilib.entities3D.factory.Pac3DFactory;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.*;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostStateColors;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.PacSettings;
import de.amr.pacmanfx.uilib.entities3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.entities3D.world.Pellet3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;

import java.util.HashMap;
import java.util.Map;

import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class DefaultFactory3D implements Factory3D {

    protected final Map<GhostStateColors, GhostAppearanceMaterialSet> ghostMaterialsCache = new HashMap<>();
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
        House house,
        TerrainLayer terrain,
        WorldSettings settings,
        WorldMapColorScheme colorScheme) {
        return mazeFactory3D.createMaze3D(house, terrain, settings, colorScheme);
    }

    @Override
    public void createPac3D(Pac pac, PacSettings settings) {
        Pac3DFactory.createPacManView3D(pac, settings);
    }

    @Override
    public void createGhost3D(Ghost ghost, GhostSettings settings) {
        final PacMan3DModel model = PacMan3DModel.instance();
        final Ghost3DViewComp view3D = ensureGhostHas3DView(ghost);
        final var materialSet = ghostMaterialsCache.computeIfAbsent(settings.colors(), this::createGhostMaterial);

        view3D.build(settings, model.ghostDressMesh(), model.ghostPupilsMesh(), model.ghostEyeballsMesh());
        view3D.setAppearanceMaterialSet(materialSet);

    }

    @Override
    public Group createLivesCounterShape3D(WorldSettings settings) {
        requireNonNull(settings);
        final PacSettings livesCounterPacSettings = settings.pac().resized(settings.livesCounter().shapeSize());
        return Pac3DFactory.createPacBody(livesCounterPacSettings, true);
    }

    @Override
    public Pellet3D createPellet3D(Pellet3DSettings settings, PhongMaterial material) {
        requireNonNull(settings);
        requireNonNull(material);

        final Sphere oval = new Sphere(settings.radius());
        oval.setMaterial(material);
        oval.setScaleX(1.25);
        return new Pellet3D(oval);
    }

    @Override
    public Energizer3D createEnergizer3D(Energizer3DSettings settings, PhongMaterial material) {
        requireNonNull(settings);
        requireNonNull(material);

        final var energizer3D = new Energizer3D();
        energizer3D.setShapeFactory(() -> {
            final var shape = new Sphere(settings.radius(), 48);
            shape.setMaterial(material);
            return shape;
        });
        return energizer3D;
    }

    public GhostAppearanceMaterialSet createGhostMaterial(GhostStateColors colors) {
        requireNonNull(colors);

        final var normalMaterials = new Ghost3DMaterialSet(
            coloredPhongMaterial(colors.normal().dressColor()),
            coloredPhongMaterial(colors.normal().eyeballsColor()),
            coloredPhongMaterial(colors.normal().pupilsColor())
        );

        final var frightenedMaterials = new Ghost3DMaterialSet(
            coloredPhongMaterial(colors.frightened().dressColor()),
            coloredPhongMaterial(colors.frightened().eyeballsColor()),
            coloredPhongMaterial(colors.frightened().pupilsColor())
        );

        final var flashingMaterials = new Ghost3DMaterialSet(
            coloredPhongMaterial(colors.flashing().dressColor()),
            coloredPhongMaterial(colors.flashing().eyeballsColor()),
            coloredPhongMaterial(colors.flashing().pupilsColor())
        );

        return new GhostAppearanceMaterialSet(normalMaterials, frightenedMaterials, flashingMaterials);
    }

    private static Ghost3DViewComp ensureGhostHas3DView(Ghost ghost) {
        if (!ghost.hasComp(Ghost3DViewComp.class)) {
            ghost.setComp(Ghost3DViewComp.class, new Ghost3DViewComp());
            ghost.setComp(Ghost3DAnimationComp.class, new Ghost3DAnimationComp());
        }
        return ghost.reqComp(Ghost3DViewComp.class);
    }
}
