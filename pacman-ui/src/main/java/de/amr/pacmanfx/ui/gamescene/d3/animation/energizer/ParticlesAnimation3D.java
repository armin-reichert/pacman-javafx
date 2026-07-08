/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3.animation.energizer;

import de.amr.basics.Disposable;
import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.EnergizerParticle3D;
import de.amr.pacmanfx.uilib.model3D.animation.EnergizerParticle3D.ParticleState;
import de.amr.pacmanfx.uilib.model3D.animation.Pool;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.amr.basics.math.RandomNumberSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the explosion particles that land on the floor are sucked in by 3 swirls
 * inside the ghost house where they accumulate to colored ghost shapes.
 * <p>Particles falling off from the maze are hidden at a certain height below the maze.</p>
 */
public class ParticlesAnimation3D extends ManagedAnimation implements Disposable {

    private static final byte[] GHOST_PERSONALITIES = {
        GameModel.RED_GHOST_SHADOW, GameModel.PINK_GHOST_SPEEDY, GameModel.CYAN_GHOST_BASHFUL, GameModel.ORANGE_GHOST_POKEY
    };

    private static byte computeTargetSwirlIndex(byte personality) {
        return switch (personality) {
            case GameModel.CYAN_GHOST_BASHFUL -> 0;
            case GameModel.RED_GHOST_SHADOW, GameModel.PINK_GHOST_SPEEDY -> 1;
            case GameModel.ORANGE_GHOST_POKEY -> 2;
            default -> throw new IllegalArgumentException("Illegal ghost personality: " + personality);
        };
    }

    private final ParticlesAnimationConfig config;
    private final List<Vector3f> swirlBases;
    private final List<PhongMaterial> ghostDressMaterials;
    private final List<EnergizerParticle3D> particles = new ArrayList<>();
    private final Group particlesGroup;
    private final Pool<EnergizerParticle3D> particlePool;
    private final List<SwirlAnimation3D> swirlAnimations = new ArrayList<>();

    private final Predicate<EnergizerParticle3D> floorCollisionTest;
    private final Predicate<EnergizerParticle3D> outOfWorldTest;

    public ParticlesAnimation3D(
        House house,
        List<PhongMaterial> ghostDressMaterials,
        Pool<EnergizerParticle3D> particlePool,
        ParticlesAnimationConfig config,
        Group particlesGroup,
        Predicate<EnergizerParticle3D> floorCollisionTest,
        Predicate<EnergizerParticle3D> outOfWorldTest)
    {
        super("Energizer particles animation");

        this.config = requireNonNull(config);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.particlePool = requireNonNull(particlePool);
        this.particlesGroup = requireNonNull(particlesGroup);
        this.floorCollisionTest = requireNonNull(floorCollisionTest);
        this.outOfWorldTest = requireNonNull(outOfWorldTest);

        // The 3 ghost revival positions inside the house from left to right
        swirlBases = Stream.of(GameModel.CYAN_GHOST_BASHFUL, GameModel.PINK_GHOST_SPEEDY, GameModel.ORANGE_GHOST_POKEY)
            .map(house::ghostRevivalTile)
            .map(tile -> tile.scaled(WorldMap.TS).plus(WorldMap.TS, WorldMap.HTS))
            .map(pos -> new Vector3f(pos.x(), pos.y(), 0))
            .toList();

        swirlBases.forEach(base -> swirlAnimations.add(new SwirlAnimation3D(config.swirl(), base)));

        setFactory(() -> {
            final var timeline = new Timeline(new KeyFrame(Duration.millis(16.666), _ -> {
                updateParticles();
                for (SwirlAnimation3D swirlAnimation : swirlAnimations) {
                    swirlAnimation.update();
                }
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            return timeline;
        });
    }

    @Override
    public void freeResources() {
        swirlAnimations.forEach(SwirlAnimation3D::dispose);
        particles.clear();
        particlesGroup.getChildren().clear();
    }

    public void triggerExplosion(Point3D explosionCenter) {
        requireNonNull(explosionCenter);
        final var position = new Vector3f(explosionCenter.getX(), explosionCenter.getY(), explosionCenter.getZ());
        particlePool.recycle(particles);
        particles.clear();
        particlesGroup.getChildren().clear();
        for (int i = 0; i < config.explosion().particleCount(); ++i) {
            final EnergizerParticle3D particle = particlePool.provideItem();
            particle.setPosition(position);
            particle.setVelocity(randomParticleVelocity(config.explosion()));
            particle.setState(ParticleState.FLYING_THROUGH_AIR);
            particle.shape().setVisible(true);
            particles.add(particle);
            particlesGroup.getChildren().add(particle.shape());
        }
        // ensure wrapped JavaFX animation is created and plays
        playOrContinue();
    }

    private void releaseParticle(EnergizerParticle3D particle) {
        particles.remove(particle);
        particlesGroup.getChildren().remove(particle.shape());
        particlePool.recycle(particle);
    }

    private Vector3f randomParticleVelocity(ExplosionConfig config) {
        final int xDir = chance(0.5) ? -1 : 1;
        final int yDir = chance(0.5) ? -1 : 1;
        return new Vector3f(
            xDir * randomFloat(config.particleMinSpeedXY(), config.particleMaxSpeedXY()),
            yDir * randomFloat(config.particleMinSpeedXY(), config.particleMaxSpeedXY()),
            -randomFloat(config.particleMinSpeedZ(), config.particleMaxSpeedZ())
        );
    }

    private void updateParticles() {
        // Iterate backwards to avoid concurrent modification exception
        for (int i = particles.size() - 1; i >= 0; --i) {
            final var particle = particles.get(i);
            switch (particle.state()) {
                case FLYING_THROUGH_AIR -> updateStateFlying(particle);
                case ATTRACTED_BY_SWIRL -> updateStateAttracted(particle);
                case OUT_OF_WORLD -> {}
            }
        }
    }

    private void updateStateFlying(EnergizerParticle3D particle) {
        particle.fly(config.explosion().gravity());
        if (floorCollisionTest.test(particle)) {
            onParticleLandedOnFloor(particle);
        }
        else if (outOfWorldTest.test(particle)) {
            onParticleOutOfWorld(particle);
        }
    }

    private void onParticleOutOfWorld(EnergizerParticle3D particle) {
        releaseParticle(particle);
        particle.setState(ParticleState.OUT_OF_WORLD);
    }

    private void updateStateAttracted(EnergizerParticle3D particle) {
        final Vector3f target = swirlBases.get(particle.targetSwirlIndex());
        if (particleReachedTarget(particle, target)) {
            onParticleReachedTarget(particle);
        }
        else {
             moveParticle(particle, config.attraction().acceleration());
        }
    }

    private boolean particleReachedTarget(EnergizerParticle3D particle, Vector3f target) {
        final double distanceToTarget = particle.pos().euclideanDist(target);
        final float speed = particle.velocity().length();
        return (distanceToTarget < speed);
    }

    private void moveParticle(EnergizerParticle3D particle, float acceleration) {
        particle.move();
        final float newSpeed = Math.clamp(particle.velocity().length() + acceleration, 0, 1.5f);
        particle.setVelocity(particle.velocity().setToLength(newSpeed));
    }

    private void onParticleReachedTarget(EnergizerParticle3D particle) {
        particles.remove(particle);
        swirlAnimations.get(particle.targetSwirlIndex()).addParticle(particle);
    }

    /* When a particle lands on the maze floor, it is resized to a uniform size and gets attracted by a randomly
     * assigned swirl inside the ghost house. Once it reaches its target position (on the swirl surface), it is
     * integrated into the swirl and moves forever on the swirl surface.
     */
    private void onParticleLandedOnFloor(EnergizerParticle3D particle) {
        final byte personality = randomByteArrayElement(GHOST_PERSONALITIES);
        final byte targetSwirlIndex = computeTargetSwirlIndex(personality);
        final Vector3f swirlCenter = swirlBases.get(targetSwirlIndex);

        particle.setTargetSwirlIndex(targetSwirlIndex);

        // Place particle exactly on floor surface, assuming floor surface at z=0
        particle.setPosition(new Vector3f(particle.pos().x(), particle.pos().y(), -particle.shape().getRadius()));

        // Let particle move at random speed towards its swirl's center
        final float speed = randomFloat(config.attraction().particleMinSpeed(), config.attraction().particleMaxSpeed());
        particle.setVelocity(swirlCenter.minus(particle.pos()).setToLength(speed));

        particle.setState(ParticleState.ATTRACTED_BY_SWIRL);

        particle.shape().setMaterial(ghostDressMaterials.get(personality));
        particle.shape().setRadius(0.5 * config.attraction().particleSize());
    }
}