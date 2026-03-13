/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.ui.d3.Pellet3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.actor.MutableGhost3D;
import de.amr.pacmanfx.uilib.model3D.actor.PacBody;
import de.amr.pacmanfx.uilib.model3D.actor.PacMan3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.geometry.Bounds;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_Factory3D implements Factory3D {

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
    public PacMan3D createPac3D(
        Pac pac,
        PacConfig pacConfig,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(pac);
        requireNonNull(pacConfig);
        requireNonNull(animationRegistry);

        final var pacMan3D = new PacMan3D(
            animationRegistry,
            pac,
            pacConfig.size3D(),
            pacConfig.headColor(),
            pacConfig.eyesColor(),
            pacConfig.palateColor()
        );

        pacMan3D.light().setColor(pacConfig.headColor().desaturate());
        return pacMan3D;
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
        return new MutableGhost3D(
            animationRegistry,
            ghost,
            ghostConfig.createGhostColorSet(),
            Models3D.GHOST_MODEL.dressMesh(),
            Models3D.GHOST_MODEL.pupilsMesh(),
            Models3D.GHOST_MODEL.eyeballsMesh(),
            ghostConfig.size3D()
        );
    }

    @Override
    public PacBody createLivesCounterShape3D(EntityConfig entityConfig) {
        requireNonNull(entityConfig);
        final var pacConfig = entityConfig.pacConfig();
        return Models3D.PAC_MAN_MODEL.createPacBody(
            entityConfig.livesCounter().shapeSize(),
            pacConfig.headColor(),
            pacConfig.eyesColor(),
            pacConfig.palateColor()
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
