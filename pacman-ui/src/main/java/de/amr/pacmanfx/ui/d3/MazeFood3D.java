/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.math.Vector3f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.d3.config.Config3D;
import de.amr.pacmanfx.ui.d3.config.EnergizerConfig3D;
import de.amr.pacmanfx.ui.d3.config.PelletConfig3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerParticlesAnimation;
import de.amr.pacmanfx.uilib.model3D.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class MazeFood3D implements Disposable {

    private final FoodLayer foodLayer;
    private final Set<Shape3D> pellets3D = new HashSet<>();
    private final Set<Energizer3D> energizers3D = new HashSet<>();
    private final Supplier<Shape3D> energizerShapeFactory;

    private EnergizerParticlesAnimation explodedEnergizerParticlesAnimation;

    public MazeFood3D(
        Config3D config3D,
        WorldMapColorScheme colorScheme,
        AnimationRegistry animationRegistry,
        GameLevel level,
        List<PhongMaterial> ghostMaterials,
        Maze3D maze3D)
    {
        requireNonNull(colorScheme);
        requireNonNull(animationRegistry);
        requireNonNull(level);
        requireNonNull(ghostMaterials);
        requireNonNull(maze3D);

        this.foodLayer = level.worldMap().foodLayer();

        final var pelletMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));

        energizerShapeFactory = () -> {
            final var shape = new Sphere(config3D.energizer().radius(), 48);
            shape.setMaterial(pelletMaterial);
            return shape;
        };

        final PelletConfig3D pelletConfig = config3D.pellet();
        final EnergizerConfig3D energizerConfig = config3D.energizer();
        createPellets3D(pelletConfig, pelletMaterial, maze3D.floorTop() - pelletConfig.floorElevation());
        createEnergizers3D(energizerConfig, animationRegistry, maze3D.floorTop() - energizerConfig.floorElevation());

        // The bottom center positions of the swirls where the particles of exploded energizers eventually are displayed
        final List<Vector2f> swirlBaseCenters = Stream.of(CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, ORANGE_GHOST_POKEY)
            .map(level::ghost)
            .map(Ghost::startPosition)
            .map(pos -> pos.plus(HTS, HTS))
            .toList();

        explodedEnergizerParticlesAnimation = createEnergizerParticlesAnimation(animationRegistry, maze3D, ghostMaterials, swirlBaseCenters);
    }

    @Override
    public void dispose() {
        if (!pellets3D.isEmpty()) {
            pellets3D.forEach(shape3D -> {
                shape3D.setMaterial(null);
                if (shape3D instanceof MeshView meshView) {
                    meshView.setMesh(null);
                }
            });
            Logger.info("Disposed 3D pellets");
        }
        if (!energizers3D.isEmpty()) {
            energizers3D.forEach(Energizer3D::dispose);
            energizers3D.clear();
            Logger.info("Disposed 3D energizers");
        }
        if (explodedEnergizerParticlesAnimation != null) {
            explodedEnergizerParticlesAnimation.dispose();
            explodedEnergizerParticlesAnimation = null;
        }
    }

    public Set<Shape3D> pellets3D() { return Collections.unmodifiableSet(pellets3D); }

    public Set<Energizer3D> energizers3D() { return Collections.unmodifiableSet(energizers3D); }

    public void startAnimation() {
        explodedEnergizerParticlesAnimation.playFromStart();
    }

    public void stopAnimation() {
        explodedEnergizerParticlesAnimation.stop();
    }

    public void createEnergizerExplosion(Energizer3D energizer) {
        final Point3D point = energizer.shape().localToScene(Point3D.ZERO);
        final Vector3f origin = new Vector3f(point.getX(), point.getY(), point.getZ());
        explodedEnergizerParticlesAnimation.createEnergizerExplosion(origin);
    }

    private void createPellets3D(PelletConfig3D config, PhongMaterial pelletMaterial, double z) {
        final Mesh pelletMesh = Models3D.PELLET_MODEL.mesh();
        final double scaling = computePelletScaling(config, pelletMesh);
        final Mesh scaledPelletMesh = Models3D.createScaledMesh(pelletMesh, scaling);
        pellets3D.clear(); // just in case
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(tile -> !foodLayer.isEnergizerTile(tile))
            .map(tile -> createPelletShape3D(scaledPelletMesh, pelletMaterial, tile, z))
            .forEach(pellets3D::add);
    }

    private double computePelletScaling(PelletConfig3D config, Mesh pelletMesh) {
        final var dummy = new MeshView(pelletMesh);
        final Bounds bounds = dummy.getBoundsInLocal();
        final double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        return (2 * config.radius()) / maxExtent;
    }

    private Shape3D createPelletShape3D(Mesh mesh, PhongMaterial material, Vector2i tile, double z) {
        final Shape3D pellet3D = new MeshView(mesh);
        pellet3D.setMaterial(material);
        pellet3D.setRotationAxis(Rotate.Z_AXIS);
        pellet3D.setRotate(90);
        pellet3D.setTranslateX(tile.x() * TS + HTS);
        pellet3D.setTranslateY(tile.y() * TS + HTS);
        pellet3D.setTranslateZ(z);
        pellet3D.setUserData(tile);
        return pellet3D;
    }

    private void createEnergizers3D(EnergizerConfig3D config, AnimationRegistry animationRegistry, double z) {
        energizers3D.clear(); // just in case
        foodLayer.tiles()
            .filter(foodLayer::isEnergizerTile)
            .filter(foodLayer::hasFoodAtTile)
            .map(tile -> createEnergizer3D(config, animationRegistry, tile, z, energizerShapeFactory))
            .forEach(energizers3D::add);
    }

    private Energizer3D createEnergizer3D(EnergizerConfig3D config, AnimationRegistry animationRegistry, Vector2i tile, double z, Supplier<Shape3D> shapeFactory) {
        final var energizer3D = new Energizer3D(animationRegistry, tile, z);
        energizer3D.setShapeFactory(shapeFactory);
        energizer3D.setPumpingFrequency(config.pumpingFrequency());
        energizer3D.setInflatedSize(config.scalingInflated());
        energizer3D.setExpandedSize(config.scalingExpanded());
        return energizer3D;
    }

    private EnergizerParticlesAnimation createEnergizerParticlesAnimation(
        AnimationRegistry animationRegistry, Maze3D maze3D, List<PhongMaterial> ghostMaterials, List<Vector2f> swirlBaseCenters) {
        return new EnergizerParticlesAnimation(
            EnergizerParticlesAnimation.DEFAULT_CONFIG,
            animationRegistry,
            swirlBaseCenters,
            ghostMaterials,
            maze3D.floor(),
            maze3D.particlesGroup());
    }
}
