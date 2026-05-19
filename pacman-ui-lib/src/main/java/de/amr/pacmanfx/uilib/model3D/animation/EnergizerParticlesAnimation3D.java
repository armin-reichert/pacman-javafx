/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
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

import static de.amr.basics.math.RandomNumberSupport.*;
import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the explosion particles that land on the floor are sucked in by 3 swirls
 * inside the ghost house where they accumulate to colored ghost shapes.
 * <p>Particles falling off from the maze are hidden at a certain height below the maze.</p>
 */
public class EnergizerParticlesAnimation3D extends ManagedAnimation {

    private static final Duration FRAME_DURATION = Duration.millis(16.666); // 60 Hz

    private static final byte[] GHOST_PERSONALITIES = {
        RED_GHOST_SHADOW, PINK_GHOST_SPEEDY, CYAN_GHOST_BASHFUL, ORANGE_GHOST_POKEY
    };

    private static byte randomGhostPersonality() {
        return GHOST_PERSONALITIES[randomInt(0, GHOST_PERSONALITIES.length)];
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
        ParticleAnimationConfig config,
        List<Vector3f> swirlBases,
        List<PhongMaterial> ghostDressMaterials,
        Pool<EnergizerParticle3D> particlePool,
        Group particlesGroup)
    {
        super("Energizer Particles Animation");

        this.config = requireNonNull(config);
        this.swirlBases = requireNonNull(swirlBases);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.particlePool = requireNonNull(particlePool);
        this.particlesGroup = requireNonNull(particlesGroup);

        swirlBases.forEach(center -> {
            final var swirlAnimation = new ParticlesSwirlAnimation(config.swirl(), center);
            swirlAnimations.add(swirlAnimation);
        });

        setFactory(this::createAnimationTimer);
    }

    public void setFloorCollisionTest(Predicate<EnergizerParticle3D> floorCollisionTest) {
        this.floorCollisionTest = floorCollisionTest;
    }

    public void setOutOfWorldTest(Predicate<EnergizerParticle3D> outOfWorldTest) {
        this.outOfWorldTest = outOfWorldTest;
    }

    private Animation createAnimationTimer() {
        final var driver = new Timeline(new KeyFrame(FRAME_DURATION, _ -> update()));
        driver.setCycleCount(Animation.INDEFINITE);
        return driver;
    }

    @Override
    protected void freeResources() {
        swirlAnimations.forEach(ParticlesSwirlAnimation::dispose);
        particles.clear();
        particlesGroup.getChildren().clear();
    }

    public void triggerEnergizerExplosion(Point3D center) {
        requireNonNull(center);
        Logger.info("Trigger energizer explosion at point {}", center);
        for (int i = 0; i < config.explosion().particleCount(); ++i) {
            final EnergizerParticle3D p = particlePool.getEntry();
            p.setPosition(new Vector3f(center.getX(), center.getY(), center.getZ()));
            p.setVelocity(randomParticleVelocity(config.explosion()));
            p.setState(ParticleState.FLYING_THROUGH_AIR);
            p.shape().setVisible(true);
            particles.add(p);
            particlesGroup.getChildren().add(p.shape());
        }
    }

    private void releaseParticle(EnergizerParticle3D particle) {
        particles.remove(particle);
        particlesGroup.getChildren().remove(particle.shape());
        particlePool.recycleEntry(particle);
    }

    private Vector3f randomParticleVelocity(ExplosionConfig cfg) {
        final int xDir = chance(0.5) ? -1 : 1;
        final int yDir = chance(0.5) ? -1 : 1;
        return new Vector3f(
            xDir * randomFloat(cfg.particleMinSpeedXY(), cfg.particleMaxSpeedXY()),
            yDir * randomFloat(cfg.particleMinSpeedXY(), cfg.particleMaxSpeedXY()),
            -randomFloat(cfg.particleMinSpeedZ(), cfg.particleMaxSpeedZ())
        );
    }

    private void update() {
        updateParticles();
        for (ParticlesSwirlAnimation swirlAnimation : swirlAnimations) {
            swirlAnimation.update();
        }
    }

    private void updateParticles() {
        // Iterate backwards to avoid concurrent modification exception
        for (int i = particles.size() - 1; i >= 0; --i) {
            final var particle = particles.get(i);
            switch (particle.state()) {
                case FLYING_THROUGH_AIR -> updateStateFlying(particle);
                case ATTRACTED_BY_HOUSE -> updateStateAttracted(particle);
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
            onParticleLeftView(particle);
        }
    }

    private void onParticleLeftView(EnergizerParticle3D particle) {
        releaseParticle(particle);
        particle.setState(ParticleState.OUT_OF_WORLD);
    }

    private void updateStateAttracted(EnergizerParticle3D particle) {
        final Vector3f target = swirlBases.get(particle.targetSwirlIndex());
        final boolean targetReached = moveParticleTowardsTarget(particle, target);
        if (targetReached) {
            onParticleReachedTarget(particle);
        }
    }

    /* When a particle lands on the maze floor, it is resized to a uniform size and gets attracted by a randomly
     * assigned swirl inside the ghost house. Once it reaches its target position (on the swirl surface), it is
     * integrated into the swirl and moves forever on the swirl surface.
     */
    private void onParticleLandedOnFloor(EnergizerParticle3D particle) {
        final byte personality = randomGhostPersonality();
        final byte targetSwirlIndex = switch (personality) {
            case CYAN_GHOST_BASHFUL -> 0;
            case RED_GHOST_SHADOW, PINK_GHOST_SPEEDY -> 1;
            case ORANGE_GHOST_POKEY -> 2;
            default -> throw new IllegalArgumentException("Illegal ghost ID: " + personality);
        };

        particle.shape().setMaterial(ghostDressMaterials.get(personality));

        particle.setTargetSwirlIndex(targetSwirlIndex);
        particle.shape().setRadius(0.5 * config.attraction().particleSize());

        // assuming floor surface at z=0
        particle.setPosition(new Vector3f(particle.position().x(), particle.position().y(), -particle.shape().getRadius()));

        final Vector3f swirlCenter = swirlBases.get(targetSwirlIndex);
        final float speed = randomFloat(config.attraction().particleMinSpeed(), config.attraction().particleMaxSpeed());
        final Vector3f velocity = swirlCenter.sub(particle.position()).setToLength(speed);
        particle.setVelocity(velocity);

        particle.setState(ParticleState.ATTRACTED_BY_HOUSE);
    }

    private boolean moveParticleTowardsTarget(EnergizerParticle3D particle, Vector3f target) {
        final double dist = particle.position().euclideanDist(target);
        final boolean targetReached = dist <= particle.velocity().length();
        if (!targetReached) {
            particle.move();
            final float newSpeed = particle.velocity().length() + config.attraction().acceleration();
            final Vector3f newVelocity = particle.velocity().normalized().mul(newSpeed);
            particle.setVelocity(newVelocity);
        }
        return targetReached;
    }

    private void onParticleReachedTarget(EnergizerParticle3D particle) {
        this.particles.remove(particle);
        swirlAnimations.get(particle.targetSwirlIndex()).addParticle(particle);
    }
}