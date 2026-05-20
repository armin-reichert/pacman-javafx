/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3.animation;

import de.amr.basics.Disposable;
import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.*;
import de.amr.pacmanfx.uilib.model3D.animation.EnergizerParticle3D.ParticleState;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.amr.basics.math.RandomNumberSupport.*;
import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the explosion particles that land on the floor are sucked in by 3 swirls
 * inside the ghost house where they accumulate to colored ghost shapes.
 * <p>Particles falling off from the maze are hidden at a certain height below the maze.</p>
 */
public class EnergizerParticlesAnimation3D extends ManagedAnimation implements Disposable {

    private static final byte[] GHOST_PERSONALITIES = {
        RED_GHOST_SHADOW, PINK_GHOST_SPEEDY, CYAN_GHOST_BASHFUL, ORANGE_GHOST_POKEY
    };

    private static byte computeTargetSwirlIndex(byte personality) {
        return switch (personality) {
            case CYAN_GHOST_BASHFUL -> 0;
            case RED_GHOST_SHADOW, PINK_GHOST_SPEEDY -> 1;
            case ORANGE_GHOST_POKEY -> 2;
            default -> throw new IllegalArgumentException("Illegal ghost personality: " + personality);
        };
    }

    private final ParticleAnimationConfig config;
    private final List<Vector3f> swirlBases;
    private final List<PhongMaterial> ghostDressMaterials;
    private final List<EnergizerParticle3D> particles = new ArrayList<>();
    private final Group particlesGroup;
    private final Pool<EnergizerParticle3D> particlePool;
    private final List<ParticlesSwirlAnimation> swirlAnimations = new ArrayList<>();

    private Predicate<EnergizerParticle3D> floorCollisionTest = _ -> false;
    private Predicate<EnergizerParticle3D> outOfWorldTest = _ -> false;

    public EnergizerParticlesAnimation3D(
        GameLevel3D level3D,
        List<PhongMaterial> ghostDressMaterials,
        Pool<EnergizerParticle3D> particlePool,
        ParticleAnimationConfig config,
        Group particlesGroup)
    {
        super("Energizer particles animation");

        this.config = requireNonNull(config);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.particlePool = requireNonNull(particlePool);
        this.particlesGroup = requireNonNull(particlesGroup);

        final Maze3D maze3D = level3D.entities().unique(Maze3D.class);
        final House house = level3D.level().worldMap().terrainLayer().house();

        // The 3 ghost revival positions inside the house from left to right
        swirlBases = Stream.of(CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, ORANGE_GHOST_POKEY)
            .map(house::ghostRevivalTile)
            .map(tile -> tile.scaled(TS).plus(HTS, HTS))
            .map(pos -> new Vector3f(pos.x(), pos.y(), 0))
            .toList();

        swirlBases.forEach(base -> swirlAnimations.add(new ParticlesSwirlAnimation(config.swirl(), base)));

        floorCollisionTest = particle -> particle.collidesWith(maze3D.floor());
        outOfWorldTest = particle -> particle.pos().z() > 50; // positive z is below maze floor

        setFactory(() -> {
            final var timeline = new Timeline(new KeyFrame(Duration.millis(16.666), _ -> {
                updateParticles();
                for (ParticlesSwirlAnimation swirlAnimation : swirlAnimations) {
                    swirlAnimation.update();
                }
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            return timeline;
        });
    }

    @Override
    public void freeResources() {
        swirlAnimations.forEach(ParticlesSwirlAnimation::dispose);
        particles.clear();
        particlesGroup.getChildren().clear();
    }

    public void triggerExplosion(Point3D explosionCenter) {
        requireNonNull(explosionCenter);
        final var position = new Vector3f(explosionCenter.getX(), explosionCenter.getY(), explosionCenter.getZ());
        for (int i = 0; i < config.explosion().particleCount(); ++i) {
            final EnergizerParticle3D particle = particlePool.requestEntry();
            particle.setPosition(position);
            particle.setVelocity(randomParticleVelocity(config.explosion()));
            particle.setState(ParticleState.FLYING_THROUGH_AIR);
            particle.shape().setVisible(true);
            particles.add(particle);
            particlesGroup.getChildren().add(particle.shape());
        }
    }

    private void releaseParticle(EnergizerParticle3D particle) {
        particles.remove(particle);
        particlesGroup.getChildren().remove(particle.shape());
        particlePool.recycleEntry(particle);
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
        Logger.info("New speed: " + newSpeed);
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