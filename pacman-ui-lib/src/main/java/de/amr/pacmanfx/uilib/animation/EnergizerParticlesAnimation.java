/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.Vector3f;
import de.amr.pacmanfx.uilib.animation.EnergizerParticle.FragmentState;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the explosion particles that land on the floor are sucked in by 3 swirls
 * inside the ghost house where they accumulate to colored ghost shapes.
 * <p>Particles falling off from the maze are hidden at a certain height below the maze.</p>
 */
public class EnergizerParticlesAnimation extends ManagedAnimation {

    public record Config(
        ExplosionConfig explosion,
        AttractionConfig attraction,
        SwirlConfig swirl)
    {}

    public record ExplosionConfig(
        int particleCount,
        float particleMeanRadius,
        float particleMinSpeedXY,
        float particleMaxSpeedXY,
        float particleMinSpeedZ,
        float particleMaxSpeedZ)
    {}

    public record AttractionConfig(
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
        new ExplosionConfig(400, 0.2f, 0, 0.4f, 2, 8),
        new AttractionConfig(0.4f, 0.3f, 0.5f),
        new SwirlConfig(6, 12, 0.3f, 0.05f)
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

    private Vector3f gravity = Vector3f.ZERO;

    private final List<EnergizerParticle> particles = new ArrayList<>();
    private final Group particlesGroup;

    private final Queue<EnergizerParticle> pool = new ArrayDeque<>();

    public EnergizerParticlesAnimation(
        Config config,
        AnimationRegistry animationRegistry,
        List<Vector3f> swirlBaseCenters,
        List<PhongMaterial> ghostDressMaterials,
        Box floor3D,
        Group particlesGroup)
    {
        super(animationRegistry, "Energizers_ParticlesAnimation");

        this.config = requireNonNull(config);
        this.swirlBaseCenters = requireNonNull(swirlBaseCenters);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.floor3D = requireNonNull(floor3D);
        this.particlesGroup = requireNonNull(particlesGroup);

        setFactory(this::createAnimationDriver);
    }

    private Animation createAnimationDriver() {
        final var loop = new Timeline(
            new KeyFrame(FRAME_DURATION, _ -> {
                for (EnergizerParticle particle : particles) {
                    updateParticleState(particle);
                }
            })
        );
        loop.setCycleCount(Animation.INDEFINITE);
        return loop;
    }

    @Override
    protected void freeResources() {
        final int particleCount = particles.size();
        if (particleCount > 0) {
            for (var particle : particles) {
                particle.dispose();
            }
            particles.clear();
            Logger.info("Disposed {} particles", particleCount);
        }
        particlesGroup.getChildren().clear();
        pool.clear();
    }

    public void setGravity(Vector3f gravity) {
        this.gravity = gravity;
    }

    public void showExplosion(Vector3f origin) {
        for (int i = 0; i < config.explosion().particleCount(); ++i) {
            final EnergizerParticle particle = obtainParticle(origin);
            particles.add(particle);
            particlesGroup.getChildren().add(particle.shape());
        }
    }

    private EnergizerParticle obtainParticle(Vector3f origin) {
        EnergizerParticle particle = pool.poll();
        if (particle == null) {
            particle = createExplosionParticle(origin);
        } else {
            Logger.debug("Particle obtained from pool! Pool size={}", pool.size());
        }
        return particle;
    }

    private void releaseParticle(EnergizerParticle particle) {
        particlesGroup.getChildren().remove(particle.shape());
        pool.offer(particle);
        particle.reset();
        particle.shape().setVisible(false);
        Logger.debug("Particle released! Pool size={}", pool.size());
    }

    private EnergizerParticle createExplosionParticle(Vector3f origin) {
        final PhongMaterial material = ghostDressMaterials.get(randomInt(0, 4));
        final double radius = Math.clamp(RND.nextGaussian(2, 0.1), 0.5, 4) * config.explosion().particleMeanRadius();
        final var particle = new SphericalEnergizerParticle(radius, material, origin, SphericalEnergizerParticle.Resolution.HIGH);
        particle.setVelocity(randomParticleVelocity(config.explosion()));
        particle.setState(FragmentState.FLYING);
        return particle;
    }

    private Vector3f randomParticleVelocity(ExplosionConfig cfg) {
        final int xDir = RND.nextBoolean() ? -1 : 1;
        final int yDir = RND.nextBoolean() ? -1 : 1;
        return new Vector3f(
            xDir * randomFloat(cfg.particleMinSpeedXY(), cfg.particleMaxSpeedXY()),
            yDir * randomFloat(cfg.particleMinSpeedXY(), cfg.particleMaxSpeedXY()),
            -randomFloat(cfg.particleMinSpeedZ(), cfg.particleMaxSpeedZ())
        );
    }

    private void updateParticleState(EnergizerParticle particle) {
        switch (particle.state()) {
            case FLYING       -> updateStateFlying(particle);
            case ATTRACTED    -> updateStateAttracted(particle);
            case INSIDE_SWIRL -> updateStateInsideSwirl(particle);
            case OUT_OF_VIEW  -> {}
        }
    }
    
    private void updateStateFlying(EnergizerParticle particle) {
        particle.fly(gravity);
        if (particle.collidesWith(floor3D)) {
            onParticleLandedOnFloor(particle);
        }
        else if (isParticleOutsideView(particle)) {
            onParticleLeftView(particle);
        }
    }

    private boolean isParticleOutsideView(EnergizerParticle particle) {
        return particle.position().z() >= 50; // positive z direction points down!
    }
    
    private void onParticleLeftView(EnergizerParticle particle) {
        releaseParticle(particle);
        particle.setState(FragmentState.OUT_OF_VIEW);
    }

    private void updateStateAttracted(EnergizerParticle particle) {
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
    private void onParticleLandedOnFloor(EnergizerParticle particle) {
        final byte ghostID = randomGhostID();
        final byte targetSwirlIndex = switch (ghostID) {
            case CYAN_GHOST_BASHFUL -> 0;
            case RED_GHOST_SHADOW, PINK_GHOST_SPEEDY -> 1;
            case ORANGE_GHOST_POKEY -> 2;
            default -> throw new IllegalArgumentException("Illegal ghost ID: " + ghostID);
        };

        if (particle instanceof SphericalEnergizerParticle ball) {
            particlesGroup.getChildren().remove(particle.shape());
            ball.setResolution(SphericalEnergizerParticle.Resolution.LOW);
            particlesGroup.getChildren().add(particle.shape());
        }
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

    private boolean moveParticleTowardsTarget(EnergizerParticle particle, Vector3f target) {
        final double dist = particle.position().euclideanDist(target);
        final boolean targetReached = dist <= particle.velocity().length();
        if (!targetReached) {
            particle.move();
        }
        return targetReached;
    }

    private void onParticleReachedTarget(EnergizerParticle particle) {
        particle.setAngle(Math.toRadians(randomInt(0, 360)));
        particle.setVelocity(new Vector3f(0, 0, -config.swirl().upwardsSpeed()));
        updateParticlePositionOnSwirlSurface(particle);
        particle.setState(FragmentState.INSIDE_SWIRL);
    }

    private void updateStateInsideSwirl(EnergizerParticle particle) {
        particle.move();
        final Vector3f pos = particle.position();
        if (pos.z() < -config.swirl().height()) {
            // reached top of swirl: move to bottom of floor
            particle.setPosition(new Vector3f(pos.x(), pos.y(), floorSurfaceZ() - 0.5 * particle.size()));
        }
        // Rotate on swirl surface
        particle.setAngle(particle.angle() + config.swirl().rotationSpeed());
        if (particle.angle() > Math.TAU) {
            particle.setAngle(particle.angle() - Math.TAU);
        }
        updateParticlePositionOnSwirlSurface(particle);
    }

    private void updateParticlePositionOnSwirlSurface(EnergizerParticle particle) {
        final Vector3f swirlBaseCenter = swirlBaseCenters.get(particle.targetSwirlIndex());
        final var pos = new Vector3f(
            swirlBaseCenter.x() + config.swirl().radius() * Math.cos(particle.angle()),
            swirlBaseCenter.y() + config.swirl().radius() * Math.sin(particle.angle()),
            particle.position().z()
        );
        particle.setPosition(pos);
    }

    private double floorSurfaceZ() {
        return floor3D.getTranslateZ() - 0.5 * floor3D.getDepth();
    }
}