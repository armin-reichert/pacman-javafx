/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.view3d;

import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.entities3D.ghost.*;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D;
import de.amr.pacmanfx.ui.entities3D.world.EnergizerView3D;
import de.amr.pacmanfx.ui.entities3D.world.PelletView3D;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.settings.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D_System;
import de.amr.pacmanfx.uilib.view3d.Pac3DShapeFactory;
import de.amr.pacmanfx.uilib.view3d.PacManMeshes3D;
import de.amr.pacmanfx.uilib.view3d.PacSettings;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;

import java.util.HashMap;
import java.util.Map;

import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

//TODO Remove factory classes and integrate into 3d systems
public class DefaultFactory3D implements Factory3D {

    private static class SingletonHolder {
        static final DefaultFactory3D SINGLETON = new DefaultFactory3D();
    }

    public static DefaultFactory3D instance() {
        return SingletonHolder.SINGLETON;
    }

    protected Pac3DShapeFactory pac3DShapeFactory = new Pac3DShapeFactory();

    protected final Map<GhostColorsByState, GhostView3D_MaterialSet> ghostMaterialsCache = new HashMap<>();
    protected final Map<Float, TriangleMesh> pelletMeshesCache = new HashMap<>();

    protected DefaultFactory3D() {}

    @Override
    public void dispose() {
        ghostMaterialsCache.clear();
        pelletMeshesCache.clear();
    }

    @Override
    public void createPac3D(CommonGameSystems systems, Pac pac, PacSettings settings) {
        final PacView3D_System pacView3DSystem = systems.assertSystem(PacView3D_System.class);
        final PacView3D view3D = pacView3DSystem.createPacManView3D(pac3DShapeFactory, pac, settings);
        pac.setComponent(PacView3D.class, view3D);
    }

    @Override
    public void createGhost3D(CommonGameSystems systems, Ghost ghost, GhostSettings settings) {
        final PacManMeshes3D model3D = PacManMeshes3D.instance();
        final GhostView3D_Systems ghostView3DSystems = systems.assertSystem(GhostView3D_Systems.class);
        final GhostView3D view3D = ghostView3DSystems.buildGhostView3D(
            settings,
            model3D.ghostDressMesh(), model3D.ghostPupilsMesh(), model3D.ghostEyeballsMesh());
        ghost.setComponent(GhostView3D.class, view3D);

        ghost.setComponent(GhostView3D_Animations.class, new GhostView3D_Animations());

        final GhostView3D_MaterialSet materialSet = ghostMaterialsCache.computeIfAbsent(
            settings.colors(), this::createGhostMaterial);
        view3D.setAppearanceMaterialSet(materialSet);
    }

    @Override
    public Group createLivesCounterShape3D(WorldSettings settings) {
        requireNonNull(settings);
        final PacSettings livesCounterPacSettings = settings.pac().resized(settings.livesCounter().shapeSize());
        return pac3DShapeFactory.createPacBody(livesCounterPacSettings, true);
    }

    @Override
    public PelletView3D createPellet3D(Pellet3DSettings settings, PhongMaterial material) {
        requireNonNull(settings);
        requireNonNull(material);

        final Sphere oval = new Sphere(settings.radius());
        oval.setMaterial(material);
        oval.setScaleX(1.25);
        return new PelletView3D(oval);
    }

    @Override
    public EnergizerView3D createEnergizer3D(Energizer3DSettings settings, PhongMaterial material) {
        requireNonNull(settings);
        requireNonNull(material);

        final var energizer3D = new EnergizerView3D();
        energizer3D.setShapeFactory(() -> {
            final var shape = new Sphere(settings.radius(), 48);
            shape.setMaterial(material);
            return shape;
        });
        return energizer3D;
    }

    public GhostView3D_MaterialSet createGhostMaterial(GhostColorsByState colors) {
        requireNonNull(colors);

        final var normalMaterials = new GhostView3D_PartsMaterials(
            coloredPhongMaterial(colors.normal().dressColor()),
            coloredPhongMaterial(colors.normal().eyeballsColor()),
            coloredPhongMaterial(colors.normal().pupilsColor())
        );

        final var frightenedMaterials = new GhostView3D_PartsMaterials(
            coloredPhongMaterial(colors.frightened().dressColor()),
            coloredPhongMaterial(colors.frightened().eyeballsColor()),
            coloredPhongMaterial(colors.frightened().pupilsColor())
        );

        final var flashingMaterials = new GhostView3D_PartsMaterials(
            coloredPhongMaterial(colors.flashing().dressColor()),
            coloredPhongMaterial(colors.flashing().eyeballsColor()),
            coloredPhongMaterial(colors.flashing().pupilsColor())
        );

        return new GhostView3D_MaterialSet(normalMaterials, frightenedMaterials, flashingMaterials);
    }
}
