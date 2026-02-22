/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerParticlesAnimation;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.SphericalEnergizer3D;
import de.amr.pacmanfx.uilib.model3D.Swirl3D;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class MazeFood3D implements Disposable {

    private final PreferencesManager prefs;
    private final AnimationRegistry animationRegistry;
    private final GameLevel level;
    private final Box floor3D;

    private PhongMaterial pelletMaterial;
    private PhongMaterial particleMaterial;

    private Group particleGroupsContainer = new Group();

    private Set<MeshView> pellets3D = Set.of();
    private Set<Energizer3D> energizers3D = Set.of();

    public MazeFood3D(
        PreferencesManager prefs,
        WorldMapColorScheme colorScheme,
        AnimationRegistry animationRegistry,
        GameLevel level,
        List<PhongMaterial> ghostMaterials,
        Box floor3D,
        List<Swirl3D> swirls)
    {
        this.prefs = requireNonNull(prefs);
        requireNonNull(colorScheme);
        this.animationRegistry = requireNonNull(animationRegistry);
        this.level = requireNonNull(level);
        requireNonNull(ghostMaterials);
        this.floor3D = requireNonNull(floor3D);

        pelletMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));
        particleMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()).deriveColor(0, 0.5, 1.5, 0.5));

        createPellets3D();
        createEnergizers3D(ghostMaterials, swirls);
    }

    @Override
    public void dispose() {
        if (pelletMaterial != null) {
            pelletMaterial.diffuseColorProperty().unbind();
            pelletMaterial.specularColorProperty().unbind();
            pelletMaterial = null;
        }
        if (particleMaterial != null) {
            particleMaterial.diffuseColorProperty().unbind();
            particleMaterial.specularColorProperty().unbind();
            particleMaterial = null;
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
        if (particleGroupsContainer != null) {
            particleGroupsContainer.getChildren().clear();
            particleGroupsContainer = null;
            Logger.info("Removed all particle groups");
        }
    }

    public Set<Shape3D> pellets3D() { return Collections.unmodifiableSet(pellets3D); }

    public Set<Energizer3D> energizers3D() { return Collections.unmodifiableSet(energizers3D); }

    public Group particleGroupsContainer() {
        return particleGroupsContainer;
    }

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

    private void createEnergizers3D(List<PhongMaterial> ghostMaterials, List<Swirl3D> swirls) {
        final float radius     = prefs.getFloat("3d.energizer.radius");
        final float minScaling = prefs.getFloat("3d.energizer.scaling.min");
        final float maxScaling = prefs.getFloat("3d.energizer.scaling.max");
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        energizers3D = foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(foodLayer::isEnergizerTile)
            .map(tile -> createAnimatedEnergizer3D(tile, radius, minScaling, maxScaling, ghostMaterials, floor3D, swirls))
            .collect(Collectors.toCollection(HashSet::new));
    }

    private Energizer3D createAnimatedEnergizer3D(
        Vector2i tile,
        float radius,
        float minScaling,
        float maxScaling,
        List<PhongMaterial> ghostMaterials,
        Box floor3D,
        List<Swirl3D> swirls
    ) {
        final var center = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
        final var energizer3D = new SphericalEnergizer3D(
            animationRegistry,
            radius,
            center,
            minScaling,
            maxScaling,
            pelletMaterial,
            tile);

        //TODO there is still a memory leak!
        final EnergizerParticlesAnimation particlesAnimation = createParticlesAnimation(
            energizer3D,
            ghostMaterials,
            swirls,
            floor3D);

        energizer3D.setEatenAnimation(particlesAnimation);

        return energizer3D;
    }

    private EnergizerParticlesAnimation createParticlesAnimation(
        Energizer3D energizer3D,
        List<PhongMaterial> ghostParticleMaterials,
        List<Swirl3D> swirls,
        Box floor3D)
    {
        final Point3D energizerCenter = new Point3D(
            energizer3D.shape().getTranslateX(),
            energizer3D.shape().getTranslateY(),
            energizer3D.shape().getTranslateZ());

        final var particlesAnimation = new EnergizerParticlesAnimation(
            animationRegistry,
            energizerCenter,
            swirls,
            particleMaterial,
            ghostParticleMaterials,
            floor3D);

        particleGroupsContainer.getChildren().add(particlesAnimation.particleShapesGroup());
        // Important: Without gravity, particles would not land on the floor and in turn get attracted by the house!
        particlesAnimation.setGravity(GameLevel3D.GRAVITY);
        return particlesAnimation;
    }
}
