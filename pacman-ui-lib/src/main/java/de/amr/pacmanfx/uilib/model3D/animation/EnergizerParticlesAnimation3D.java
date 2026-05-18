/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.EnergizerParticle3D.FragmentState;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.basics.math.RandomNumberSupport.*;
import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the explosion particles that land on the floor are sucked in by 3 swirls
 * inside the ghost house where they accumulate to colored ghost shapes.
 * <p>Particles falling off from the maze are hidden at a certain height below the maze.</p>
 */
public class EnergizerParticlesAnimation3D extends ManagedAnimation {

    public record Config(
        ExplosionConfig explosion,
        AttractionConfig attraction,
        SwirlConfig swirl)
    {}

    public record ExplosionConfig(
        Vector3f gravity,
        int   particleCount,
        float particleMeanRadius,
        float particleMinSpeedXY,
        float particleMaxSpeedXY,
        float particleMinSpeedZ,
        float particleMaxSpeedZ)
    {}

    public record AttractionConfig(
        float acceleration,
        float particleSize,
        float particleMinSpeed,
        float particleMaxSpeed)
    {}

    public record SwirlConfig(
        float radius,
        float height,
        float upwardsSpeed,
        float rotationSpeed)
    {}

    public static final Config DEFAULT_CONFIG = new Config(
        new ExplosionConfig(new Vector3f(0, 0, 0.1f), 300, 0.25f, 0.05f, 0.4f, 1.5f, 6),
        new AttractionConfig(0.004f, 0.4f, 0.3f, 0.5f),
        new SwirlConfig(4, 20, 0.3f, 0.05f)
    );

    private static final Duration FRAME_DURATION = Duration.millis(1000.0 / 60);

    private static final byte[] GHOST_IDS = {
        RED_GHOST_SHADOW,
        PINK_GHOST_SPEEDY,
        CYAN_GHOST_BASHFUL,
        ORANGE_GHOST_POKEY
    };

    private static byte randomGhostID() {
        return GHOST_IDS[randomInt(0, GHOST_IDS.length)];
    }

    private final Config config;
    private final List<Vector3f> swirlBaseCenters;
    private final Box floor3D;
    private final List<PhongMaterial> ghostDressMaterials;
    private final List<EnergizerParticle3D> particles = new ArrayList<>();
    private final Group particlesGroup;
    private final Pool<EnergizerParticle3D> particlePool;
    private final List<ParticlesSwirlAnimation> swirlAnimations = new ArrayList<>();


    public EnergizerParticlesAnimation3D(
        Config config,
        List<Vector2f> swirlBaseCentersXY,
        List<PhongMaterial> ghostDressMaterials,
        Box floor3D,
        Pool<EnergizerParticle3D> particlePool,
        Group particlesGroup)
    {
        super("Energizer Particles Animation");

        this.config = requireNonNull(config);
        requireNonNull(swirlBaseCentersXY);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.floor3D = requireNonNull(floor3D);
        this.particlePool = requireNonNull(particlePool);
        this.particlesGroup = requireNonNull(particlesGroup);

        swirlBaseCenters = swirlBaseCentersXY.stream().map(xy -> new Vector3f(xy.x(), xy.y(), floorSurfaceZ())).toList();
        swirlBaseCenters.forEach(center -> {
            final var swirlAnimation = new ParticlesSwirlAnimation(config.swirl, center);
            swirlAnimations.add(swirlAnimation);
        });

        setFactory(this::createAnimationDriver);
    }

    private Animation createAnimationDriver() {
        final var driver = new Timeline(new KeyFrame(FRAME_DURATION, _ -> update()));
        driver.setCycleCount(Animation.INDEFINITE);
        return driver;
    }

    @Override
    protected void freeResources() {

        swirlAnimations.forEach(ParticlesSwirlAnimation::dispose);

        final int particleCount = particles.size();
        if (particleCount > 0) {
            for (var particle : particles) {
                particle.dispose();
            }
            particles.clear();
            Logger.info("Disposed {} particles", particleCount);
        }
        particlesGroup.getChildren().clear();
    }

    public void triggerEnergizerExplosion(Point3D center) {
        requireNonNull(center);
        Logger.info("Trigger energizer explosion at point {}", center);
        for (int i = 0; i < config.explosion().particleCount(); ++i) {
            final EnergizerParticle3D p = particlePool.getEntry();
            p.setPosition(new Vector3f(center.getX(), center.getY(), center.getZ()));
            p.setVelocity(randomParticleVelocity(config.explosion()));
            p.setState(FragmentState.FLYING);
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
                case FLYING       -> updateStateFlying(particle);
                case ATTRACTED    -> updateStateAttracted(particle);
                case OUT_OF_VIEW  -> {}
            }
        }
    }

    private void updateStateFlying(EnergizerParticle3D particle) {
        particle.fly(config.explosion.gravity());
        if (particle.collidesWith(floor3D)) {
            onParticleLandedOnFloor(particle);
        }
        else if (isParticleOutsideView(particle)) {
            onParticleLeftView(particle);
        }
    }

    private boolean isParticleOutsideView(EnergizerParticle3D particle) {
        return particle.position().z() >= 50; // positive z direction points down!
    }
    
    private void onParticleLeftView(EnergizerParticle3D particle) {
        releaseParticle(particle);
        particle.setState(FragmentState.OUT_OF_VIEW);
    }

    private void updateStateAttracted(EnergizerParticle3D particle) {
        final Vector3f target = swirlBaseCenters.get(particle.targetSwirlIndex());
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
        final byte ghostID = randomGhostID();
        final byte targetSwirlIndex = switch (ghostID) {
            case CYAN_GHOST_BASHFUL -> 0;
            case RED_GHOST_SHADOW, PINK_GHOST_SPEEDY -> 1;
            case ORANGE_GHOST_POKEY -> 2;
            default -> throw new IllegalArgumentException("Illegal ghost ID: " + ghostID);
        };

        particle.shape().setMaterial(ghostDressMaterials.get(ghostID));

        particle.setTargetSwirlIndex(targetSwirlIndex);
        particle.setSize(config.attraction().particleSize());

        final double z = floorSurfaceZ() - 0.5 * particle.size();
        particle.setPosition(new Vector3f(particle.position().x(), particle.position().y(), z));

        final Vector3f swirlCenter = swirlBaseCenters.get(targetSwirlIndex);
        final float speed = randomFloat(config.attraction().particleMinSpeed, config.attraction().particleMaxSpeed);
        final Vector3f velocity = swirlCenter.sub(particle.position()).setToLength(speed);
        particle.setVelocity(velocity);

        particle.setState(FragmentState.ATTRACTED);
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

    private double floorSurfaceZ() {
        return floor3D.getTranslateZ() - 0.5 * floor3D.getDepth();
    }
}