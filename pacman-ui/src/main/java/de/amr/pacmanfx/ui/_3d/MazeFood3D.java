/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerParticlesAnimation;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.ArcadeHouse3D;
import de.amr.pacmanfx.uilib.model3D.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import de.amr.pacmanfx.uilib.model3D.SphericalEnergizer3D;
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

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class MazeFood3D implements Disposable {

    private final PreferencesManager prefs;
    private final AnimationRegistry animationRegistry;
    private final GameLevel level;

    private final Box floor3D;

    private PhongMaterial pelletMaterial;
    private PhongMaterial particleMaterial;

    private Set<Shape3D> pellets3D = Set.of();
    private Set<Energizer3D> energizers3D = Set.of();
    private final Group particleGroupsContainer = new Group();

    public MazeFood3D(
        PreferencesManager prefs,
        WorldMapColorScheme colorScheme,
        AnimationRegistry animationRegistry,
        GameLevel level,
        List<PhongMaterial> ghostMaterials,
        Box floor3D,
        List<ArcadeHouse3D.SwirlAnimation> swirlAnimations)
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
        createEnergizers3D(ghostMaterials, swirlAnimations);
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
            pellets3D.stream().filter(MeshView.class::isInstance).map(MeshView.class::cast).forEach(meshView -> {
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
        if (!particleGroupsContainer.getChildren().isEmpty()) {
            particleGroupsContainer.getChildren().clear();
            Logger.info("Removed all particle groups");
        }
    }

    public Set<Shape3D> pellets3D() { return Collections.unmodifiableSet(pellets3D); }

    public Set<Energizer3D> energizers3D() { return Collections.unmodifiableSet(energizers3D); }

    public Group particleGroupsContainer() {
        return particleGroupsContainer;
    }

    private void createPellets3D() {
        final Mesh mesh = PacManModel3DRepository.instance().pelletMesh();
        final var prototype = new MeshView(mesh);
        final Bounds bounds = prototype.getBoundsInLocal();
        final double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        final float radius = prefs.getFloat("3d.pellet.radius");
        final double scaling = (2 * radius) / maxExtent;
        final var scale = new Scale(scaling, scaling, scaling);
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        pellets3D = foodLayer.tiles().filter(foodLayer::hasFoodAtTile)
                .filter(tile -> !foodLayer.isEnergizerTile(tile))
                .map(tile -> createPellet3D(mesh, scale, tile))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Shape3D createPellet3D(Mesh pelletMesh, Scale scale, Vector2i tile) {
        final var pelletShape = new MeshView(pelletMesh);
        pelletShape.setMaterial(pelletMaterial);
        pelletShape.setRotationAxis(Rotate.Z_AXIS);
        pelletShape.setRotate(90);
        pelletShape.setTranslateX(tile.x() * TS + HTS);
        pelletShape.setTranslateY(tile.y() * TS + HTS);
        pelletShape.setTranslateZ(- 6);
        pelletShape.getTransforms().add(scale);
        pelletShape.setUserData(tile);
        return pelletShape;
    }

    private void createEnergizers3D(List<PhongMaterial> ghostMaterials, List<ArcadeHouse3D.SwirlAnimation> swirlAnimations) {
        final float radius     = prefs.getFloat("3d.energizer.radius");
        final float minScaling = prefs.getFloat("3d.energizer.scaling.min");
        final float maxScaling = prefs.getFloat("3d.energizer.scaling.max");
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        energizers3D = foodLayer.tiles().filter(foodLayer::hasFoodAtTile)
                .filter(foodLayer::isEnergizerTile)
                .map(tile -> createAnimatedEnergizer3D(tile, radius, minScaling, maxScaling, ghostMaterials, floor3D, swirlAnimations))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Energizer3D createAnimatedEnergizer3D(
        Vector2i tile,
        float radius,
        float minScaling,
        float maxScaling,
        List<PhongMaterial> ghostMaterials,
        Box floor3D,
        List<ArcadeHouse3D.SwirlAnimation> swirlAnimations)
    {

        final var energizer3D = createEnergizer3D(tile, radius, minScaling, maxScaling);

        final House house = level.worldMap().terrainLayer().optHouse().orElseThrow();
        final Vector2i[] ghostRevivalTiles = {
            house.ghostRevivalTile(RED_GHOST_SHADOW),
            house.ghostRevivalTile(PINK_GHOST_SPEEDY),
            house.ghostRevivalTile(CYAN_GHOST_BASHFUL),
            house.ghostRevivalTile(ORANGE_GHOST_POKEY),
        };

        final Vector2f[] ghostRevivalCenters = {
            revivalPositionCenter(ghostRevivalTiles[RED_GHOST_SHADOW]),
            revivalPositionCenter(ghostRevivalTiles[PINK_GHOST_SPEEDY]),
            revivalPositionCenter(ghostRevivalTiles[CYAN_GHOST_BASHFUL]),
            revivalPositionCenter(ghostRevivalTiles[ORANGE_GHOST_POKEY])
        };

        setEatenAnimation(energizer3D, ghostMaterials, ghostRevivalCenters, swirlAnimations, floor3D);
        return energizer3D;
    }

    private Vector2f revivalPositionCenter(Vector2i revivalTile) {
        return revivalTile.scaled((float) TS).plus(TS, HTS);
    }

    private Energizer3D createEnergizer3D(Vector2i tile, float energizerRadius, float minScaling, float maxScaling) {
        final var energizerCenter = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
        return new SphericalEnergizer3D(
                animationRegistry,
                energizerRadius,
                energizerCenter,
                minScaling,
                maxScaling,
                pelletMaterial,
                tile);
    }

    private void setEatenAnimation(
        Energizer3D energizer3D,
        List<PhongMaterial> ghostParticleMaterials,
        Vector2f[] ghostRevivalPositions,
        List<ArcadeHouse3D.SwirlAnimation> swirlAnimations,
        Box floor3D)
    {
        final Point3D energizerCenter = new Point3D(
            energizer3D.shape().getTranslateX(),
            energizer3D.shape().getTranslateY(),
            energizer3D.shape().getTranslateZ());

        final var explosion = new EnergizerParticlesAnimation(
            animationRegistry,
            energizerCenter,
            ghostRevivalPositions,
            swirlAnimations,
            particleGroupsContainer,
            particleMaterial,
            ghostParticleMaterials,
            floor3D);

        // Important: Without gravity, explosion particles do not fall to floor and do not return to house!
        explosion.setGravity(GameLevel3D.GRAVITY);

        energizer3D.setEatenAnimation(explosion);
    }
}
