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
import de.amr.pacmanfx.ui.config.EnergizerConfig3D;
import de.amr.pacmanfx.ui.config.PelletConfig3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerParticlesAnimation;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;
import static java.util.Objects.requireNonNull;

public class MazeFood3D implements Disposable {

    public static final double PELLET_EATING_DELAY_SEC = 0.05;

    private final FoodLayer foodLayer;
    private final Set<Pellet3D> pellets3D = new HashSet<>();
    private final Set<Energizer3D> energizers3D = new HashSet<>();

    private EnergizerParticlesAnimation explodedEnergizerParticlesAnimation;

    public MazeFood3D(
        Factory3D factory3D,
        PelletConfig3D pelletConfig3D,
        EnergizerConfig3D energizerConfig3D,
        WorldMapColorScheme colorScheme,
        AnimationRegistry animationRegistry,
        GameLevel level,
        List<PhongMaterial> ghostMaterials,
        Maze3D maze3D)
    {
        requireNonNull(factory3D);
        requireNonNull(pelletConfig3D);
        requireNonNull(energizerConfig3D);
        requireNonNull(colorScheme);
        requireNonNull(animationRegistry);
        requireNonNull(level);
        requireNonNull(ghostMaterials);
        requireNonNull(maze3D);

        this.foodLayer = level.worldMap().foodLayer();

        final var pelletMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));
        createPellets3D(factory3D, pelletConfig3D, pelletMaterial, maze3D.floorTop() - pelletConfig3D.floorElevation());
        createEnergizers3D(factory3D, energizerConfig3D, animationRegistry, pelletMaterial, maze3D.floorTop() - energizerConfig3D.floorElevation());

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
            pellets3D.forEach(Pellet3D::dispose);
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

    public Set<Pellet3D> pellets3D() { return Collections.unmodifiableSet(pellets3D); }

    public Set<Energizer3D> energizers3D() { return Collections.unmodifiableSet(energizers3D); }

    public void startParticlesAnimation() {
        explodedEnergizerParticlesAnimation.playFromStart();
    }

    public void stopParticlesAnimation() {
        explodedEnergizerParticlesAnimation.stop();
    }

    public void createEnergizerExplosion(Energizer3D energizer) {
        final Point3D point = energizer.shape().localToScene(Point3D.ZERO);
        final Vector3f origin = new Vector3f(point.getX(), point.getY(), point.getZ());
        explodedEnergizerParticlesAnimation.createEnergizerExplosion(origin);
    }

    /**
     * Handles Pac-Man eating food at the given tile (pellet or energizer).
     *
     * @param pelletContainer the JavaFX group containing the pellet shapes (used for removing eaten pellets)
     * @param tile the tile where food was eaten
     */
    public void removeFoodAt(Group pelletContainer, Vector2i tile) {
        final Energizer3D energizer3D = energizers3D().stream()
            .filter(e3D -> tile.equals(e3D.tile()))
            .findFirst().orElse(null);
        if (energizer3D != null) {
            energizer3D.stopPumping();
            energizer3D.hide();
            createEnergizerExplosion(energizer3D);
        } else {
            pellets3D().stream()
                .filter(pellet3D -> tile.equals(pellet3D.tile()))
                .findFirst()
                .ifPresent(pellet3D -> removePellet3DAfterDelay(pelletContainer, pellet3D));
        }
    }

    /**
     * Removes all pellet visualizations (used when all pellets are eaten at once).
     */
    public void removeAllPellets3D(Group pelletContainer) {
        pellets3D().forEach(pellet3D -> pelletContainer.getChildren().remove(pellet3D.shape()));
    }

    /**
     * Schedules removal of a single pellet after a short delay (visual feedback).
     *
     * @param pellet3D the pellet shape to remove
     */
    private void removePellet3DAfterDelay(Group pelletContainer, Pellet3D pellet3D) {
        pauseSecThen(PELLET_EATING_DELAY_SEC, () -> pelletContainer.getChildren().remove(pellet3D.shape())).play();
    }

    private void createPellets3D(Factory3D factory3D, PelletConfig3D config, PhongMaterial pelletMaterial, double z) {
        pellets3D.clear(); // just in case
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(tile -> !foodLayer.isEnergizerTile(tile))
            .map(tile -> {
                final Pellet3D pellet3D = factory3D.createPellet3D(config, pelletMaterial);
                pellet3D.setLocation(tile, z);
                return pellet3D;
            }).forEach(pellets3D::add);
    }

    private void createEnergizers3D(
        Factory3D factory3D,
        EnergizerConfig3D config,
        AnimationRegistry animationRegistry,
        PhongMaterial material,
        double z)
    {
        energizers3D.clear(); // just in case
        foodLayer.tiles()
            .filter(foodLayer::isEnergizerTile)
            .filter(foodLayer::hasFoodAtTile)
            .map(tile -> {
                final Energizer3D energizer3D = factory3D.createEnergizer3D(config, animationRegistry, material);
                energizer3D.setLocation(tile, z);
                return energizer3D;
            })
            .forEach(energizers3D::add);
    }

    private EnergizerParticlesAnimation createEnergizerParticlesAnimation(
        AnimationRegistry animationRegistry,
        Maze3D maze3D,
        List<PhongMaterial> ghostMaterials,
        List<Vector2f> swirlBaseCenters)
    {
        return new EnergizerParticlesAnimation(
            EnergizerParticlesAnimation.DEFAULT_CONFIG,
            animationRegistry,
            swirlBaseCenters,
            ghostMaterials,
            maze3D.floor(),
            maze3D.particlesGroup());
    }
}
