/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.game.GameVariantConfig;
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
import de.amr.pacmanfx.uilib.model3D.world.NumberBox3D;
import de.amr.pacmanfx.uilib.model3D.world.Pellet3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
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
        TerrainLayer terrain, WorldSettings settings,
        WorldMapColorScheme colorScheme, AnimationRegistry animationRegistry) {

        return mazeFactory3D.createMaze3D(terrain, settings, colorScheme, animationRegistry);
    }


    @Override
    public Pac3D createPac3D(Pac pac, PacSettings settings, AnimationRegistry animationRegistry) {
        return Pac3DFactory.createPacMan3D(animationRegistry, pac, settings);
    }

    @Override
    public Ghost3D createGhost3D(Ghost ghost, GhostSettings settings, AnimationRegistry animationRegistry) {
        return new Ghost3D(
            animationRegistry,
            ghost,
            settings,
            new GhostMeshSet(
                PacManWorld3D.instance().ghostDressMesh(),
                PacManWorld3D.instance().ghostPupilsMesh(),
                PacManWorld3D.instance().ghostEyeballsMesh()
            ),
            ghostMaterialsCache.computeIfAbsent(settings.colors(), this::createGhostMaterial));
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
    public Energizer3D createEnergizer3D(Energizer3DSettings settings, PhongMaterial material, AnimationRegistry animationRegistry) {
        requireNonNull(settings);
        requireNonNull(material);
        requireNonNull(animationRegistry);

        final var energizer3D = new Energizer3D(animationRegistry);
        energizer3D.setShapeFactory(() -> {
            final var shape = new Sphere(settings.radius(), 48);
            shape.setMaterial(material);
            return shape;
        });
        energizer3D.setPumpingFrequency(settings.pumpingFrequency());
        energizer3D.setInflatedSize(settings.scalingInflated());
        energizer3D.setExpandedSize(settings.scalingExpanded());

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

    @Override
    public Node createNumberBox3D(GameVariantConfig gameVariant, int index) {
        final Image numberImage = createNumberImage(gameVariant, index);
        return new NumberBox3D(numberImage);
    }

    protected Image createNumberImage(GameVariantConfig gameVariant, int index) {
        return gameVariant.killedGhostPointsImage(index);
    }
}
