/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.math.Vector3f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerParticlesAnimation;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
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
import javafx.scene.transform.Scale;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class MazeFood3D implements Disposable {

    private final PreferencesManager prefs;
    private final AnimationRegistry animationRegistry;
    private final GameLevel level;
    private final Maze3D maze3D;

    private PhongMaterial pelletMaterial;

    private Set<MeshView> pellets3D = Set.of();
    private Set<Energizer3D> energizers3D = Set.of();

    private final EnergizerParticlesAnimation particlesAnimation;

    public MazeFood3D(
        PreferencesManager prefs,
        WorldMapColorScheme colorScheme,
        AnimationRegistry animationRegistry,
        GameLevel level,
        List<PhongMaterial> ghostMaterials,
        Maze3D maze3D)
    {
        this.prefs = requireNonNull(prefs);
        requireNonNull(colorScheme);
        this.animationRegistry = requireNonNull(animationRegistry);
        this.level = requireNonNull(level);
        requireNonNull(ghostMaterials);
        this.maze3D = requireNonNull(maze3D);

        pelletMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));

        createPellets3D();
        createEnergizers3D(level.worldMap().foodLayer());

        //TODO there is still a memory leak!
        particlesAnimation = createParticlesAnimation(ghostMaterials);
    }

    @Override
    public void dispose() {
        if (pelletMaterial != null) {
            pelletMaterial.diffuseColorProperty().unbind();
            pelletMaterial.specularColorProperty().unbind();
            pelletMaterial = null;
        }
        if (!pellets3D.isEmpty()) {
            pellets3D.forEach(meshView -> {
                meshView.setMaterial(null);
                meshView.setMesh(null);
            });
            pellets3D = Set.of();
            Logger.info("Disposed 3D pellets");
        }
        if (!energizers3D.isEmpty()) {
            energizers3D.forEach(Energizer3D::dispose);
            energizers3D.clear();
            energizers3D = Set.of();
            Logger.info("Disposed 3D energizers");
        }
    }

    public EnergizerParticlesAnimation particlesAnimation() {
        return particlesAnimation;
    }

    public Set<Shape3D> pellets3D() { return Collections.unmodifiableSet(pellets3D); }

    public Set<Energizer3D> energizers3D() { return Collections.unmodifiableSet(energizers3D); }

    private void createPellets3D() {
        final var pelletPrototype = new MeshView(Models3D.PELLET_MODEL.mesh());
        final Bounds bounds = pelletPrototype.getBoundsInLocal();
        final double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        final float radius = prefs.getFloat("3d.pellet.radius");
        final double scaling = (2 * radius) / maxExtent;
        final var scale = new Scale(scaling, scaling, scaling);
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        pellets3D = foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(tile -> !foodLayer.isEnergizerTile(tile))
            .map(tile -> createPellet3D(pelletPrototype.getMesh(), scale, tile))
            .collect(Collectors.toCollection(HashSet::new));
    }

    private MeshView createPellet3D(Mesh pelletMesh, Scale scale, Vector2i tile) {
        final var meshView = new MeshView(pelletMesh);
        meshView.setMaterial(pelletMaterial);
        meshView.setRotationAxis(Rotate.Z_AXIS);
        meshView.setRotate(90);
        meshView.setTranslateX(tile.x() * TS + HTS);
        meshView.setTranslateY(tile.y() * TS + HTS);
        meshView.setTranslateZ(- 6);
        meshView.getTransforms().add(scale);
        meshView.setUserData(tile);
        return meshView;
    }

    private void createEnergizers3D(FoodLayer foodLayer) {
        final float radius = prefs.getFloat("3d.energizer.radius");
        final Supplier<Shape3D> shapeFactory = () -> {
            final var sphere = new Sphere(radius, 48);
            sphere.setMaterial(pelletMaterial);
            return sphere;
        };
        energizers3D = foodLayer.tiles()
            .filter(foodLayer::isEnergizerTile)
            .filter(foodLayer::hasFoodAtTile)
            .map(tile -> createEnergizer3D(tile, shapeFactory))
            .collect(Collectors.toCollection(HashSet::new));
    }

    private Energizer3D createEnergizer3D(Vector2i tile, Supplier<Shape3D> shapeFactory) {
        final var center = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
        final float minScaling = prefs.getFloat("3d.energizer.scaling.min");
        final float maxScaling = prefs.getFloat("3d.energizer.scaling.max");
        final var energizer3D = new Energizer3D(animationRegistry, center, tile, minScaling, maxScaling);
        energizer3D.setShapeFactory(shapeFactory);
        return energizer3D;
    }

    private EnergizerParticlesAnimation createParticlesAnimation(List<PhongMaterial> ghostParticleMaterials) {
        final List<Vector3f> swirlBaseCenters = Stream.of(CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, ORANGE_GHOST_POKEY)
            .map(level::ghost)
            .map(Ghost::startPosition)
            .map(pos -> new Vector3f(pos.x() + HTS, pos.y() + HTS, 0))
            .toList();

        final var particlesAnimation = new EnergizerParticlesAnimation(
            EnergizerParticlesAnimation.DEFAULT_CONFIG,
            animationRegistry,
            swirlBaseCenters,
            ghostParticleMaterials,
            maze3D.floor(),
            maze3D.particlesGroup());

        // Important: Without gravity, particles would not land on the floor and in turn get attracted by the house!
        particlesAnimation.setGravity(GameLevel3D.GRAVITY);

        return particlesAnimation;
    }
}
