/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3.animation;

import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.EnergizerParticle3D;
import de.amr.pacmanfx.uilib.model3D.animation.EnergizerParticlesAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.ParticleAnimationConfig;
import de.amr.pacmanfx.uilib.model3D.animation.Pool;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;

public class ManagedEnergizerParticlesAnimation3D extends ManagedAnimation {

    private EnergizerParticlesAnimation3D energizerParticlesAnimation3D;

    public ManagedEnergizerParticlesAnimation3D(
        GameLevel3D level3D,
        List<PhongMaterial> ghostDressMaterials,
        Pool<EnergizerParticle3D> particlePool,
        ParticleAnimationConfig particleAnimationConfig) {
        super("Energizer Particles Animation");
        setFactory(() -> {
            final Maze3D maze3D = level3D.entities().unique(Maze3D.class);
            final House house = level3D.level().worldMap().terrainLayer().house();

            // The 3 ghost revival positions inside the house from left to right
            final List<Vector3f> swirlBases = Stream.of(CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, ORANGE_GHOST_POKEY)
                .map(house::ghostRevivalTile)
                .map(tile -> tile.scaled(TS).plus(HTS, HTS))
                .map(pos -> new Vector3f(pos.x(), pos.y(), 0))
                .toList();

            energizerParticlesAnimation3D = new EnergizerParticlesAnimation3D(
                particleAnimationConfig,
                swirlBases,
                ghostDressMaterials,
                particlePool,
                maze3D.particlesGroup()
            );
            energizerParticlesAnimation3D.setFloorCollisionTest(particle -> particle.collidesWith(maze3D.floor()));
            energizerParticlesAnimation3D.setOutOfWorldTest(particle -> particle.pos().z() > 50); // positive z is below maze floor

            final var timeline = new Timeline(new KeyFrame(Duration.millis(16.666), _ -> energizerParticlesAnimation3D.tick()));
            timeline.setCycleCount(Animation.INDEFINITE);

            return timeline;
        });
    }

    public void triggerExplosion(Point3D center) {
        animationFX(); // ensure wrapped animation is created
        energizerParticlesAnimation3D.triggerExplosion(center);
    }
}
