/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.Vector3f;
import de.amr.pacmanfx.uilib.animation.EnergizerParticle.FragmentState;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Globals.ORANGE_GHOST_POKEY;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the explosion particles that land on the floor are sucked in by 3 swirls
 * inside the ghost house where they accumulate to colored ghost shapes.
 * <p>Particles falling off from the maze are destroyed after a certain time.</p>
 */
public class EnergizerParticlesAnimation extends ManagedAnimation {

    private static final short EXPLOSION_PARTICLE_COUNT = 400;

    private static final int MESH_DIVISIONS_HIGH = 8;
    private static final int MESH_DIVISIONS_LOW  = 4;

    private static final float SWIRL_RADIUS = 7;
    private static final float SWIRL_HEIGHT = 12;

    private static final Vector3f SWIRL_RISING_VELOCITY = new Vector3f(0, 0, -0.3f);

    private static final float PARTICLE_MEAN_RADIUS_UNSCALED = 0.15f;

    private static final float PARTICLE_SIZE_ATTRACTED = 0.4f;

    private static final float PARTICLE_SPEED_EXPLODING_XY_MIN = 0.0f;
    private static final float PARTICLE_SPEED_EXPLODING_XY_MAX = 0.4f;

    private static final float PARTICLE_SPEED_EXPLODING_Z_MIN = 2;
    private static final float PARTICLE_SPEED_EXPLODING_Z_MAX = 8;

    private static final float PARTICLE_MIN_SPEED_ATTRACTED = 0.3f;
    private static final float PARTICLE_MAX_SPEED_ATTRACTED = 0.6f;

    private static final float SWIRL_ROTATION_SPEED = 0.05f;

    private static final int PARTICLE_OUT_OF_VIEW_Z = 50;

    private static final byte[] GHOST_IDS = {
        RED_GHOST_SHADOW,
        PINK_GHOST_SPEEDY,
        CYAN_GHOST_BASHFUL,
        ORANGE_GHOST_POKEY
    };

    private static byte randomGhostID() {
        return GHOST_IDS[randomInt(0, GHOST_IDS.length)];
    }

    private final List<Vector3f> swirlBaseCenters;
    private final Box floor3D;
    private final List<PhongMaterial> ghostDressMaterials;

    private Vector3f gravity = Vector3f.ZERO;

    private final List<EnergizerParticle> particles = new ArrayList<>();
    private final Group particlesGroup;

    public EnergizerParticlesAnimation(
        AnimationRegistry animationRegistry,
        List<Vector3f> swirlBaseCenters,
        List<PhongMaterial> ghostDressMaterials,
        Box floor3D,
        Group particlesGroup)
    {
        super(animationRegistry, "Energizers_ParticlesAnimation");

        this.swirlBaseCenters = requireNonNull(swirlBaseCenters);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.floor3D = requireNonNull(floor3D);
        this.particlesGroup = requireNonNull(particlesGroup);

        setFactory(() -> new Transition(60) {
            {
                setCycleDuration(Duration.INDEFINITE);
            }

            @Override
            protected void interpolate(double frac) {
                for (EnergizerParticle particle : particles) {
                    updateParticleState(particle);
                }
            }
        });
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
    }

    public void setGravity(Vector3f gravity) {
        this.gravity = gravity;
    }

    public void showExplosion(Vector3f origin) {
        for (int i = 0; i < EXPLOSION_PARTICLE_COUNT; ++i) {
            final PhongMaterial material = ghostDressMaterials.get(randomInt(0, 4));
            final var particle = new BallEnergizerParticle(randomParticleRadius(), material, origin, MESH_DIVISIONS_HIGH);
            particle.setVelocity(randomParticleVelocity());
            particle.setState(FragmentState.FLYING);
            particle.shape().setVisible(true);
            particles.add(particle);
            particlesGroup.getChildren().add(particle.shape());
        }
    }

    private double randomParticleRadius() {
        final double scaling = Math.clamp(RND.nextGaussian(2, 0.1), 0.5, 4);
        return scaling * PARTICLE_MEAN_RADIUS_UNSCALED;
    }

    private Vector3f randomParticleVelocity() {
        final int xDir = RND.nextBoolean() ? -1 : 1;
        final int yDir = RND.nextBoolean() ? -1 : 1;
        return new Vector3f(
            xDir * randomFloat(PARTICLE_SPEED_EXPLODING_XY_MIN, PARTICLE_SPEED_EXPLODING_XY_MAX),
            yDir * randomFloat(PARTICLE_SPEED_EXPLODING_XY_MIN, PARTICLE_SPEED_EXPLODING_XY_MAX),
            -randomFloat(PARTICLE_SPEED_EXPLODING_Z_MIN, PARTICLE_SPEED_EXPLODING_Z_MAX)
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
        else if (particle.position().z() >= PARTICLE_OUT_OF_VIEW_Z) {
            onParticleLeftView(particle);
        }
    }
    
    private void onParticleLeftView(EnergizerParticle particle) {
        particle.shape().setVisible(false);
        particle.setVelocity(Vector3f.ZERO);
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

        if (particle instanceof BallEnergizerParticle ball) {
            particlesGroup.getChildren().remove(particle.shape());
            ball.changeMeshResolution(MESH_DIVISIONS_LOW);
            particlesGroup.getChildren().add(particle.shape());
        }
        particle.shape().setMaterial(ghostDressMaterials.get(ghostID));

        particle.setTargetSwirlIndex(targetSwirlIndex);
        particle.setSize(PARTICLE_SIZE_ATTRACTED);

        final double z = floorSurfaceZ() - 0.5 * particle.size();
        particle.setPosition(new Vector3f(particle.position().x(), particle.position().y(), z));

        final Vector3f swirlCenter = swirlBaseCenters.get(targetSwirlIndex);
        final float speed = randomFloat(PARTICLE_MIN_SPEED_ATTRACTED, PARTICLE_MAX_SPEED_ATTRACTED);
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
        particle.setVelocity(SWIRL_RISING_VELOCITY);
        updateParticlePositionOnSwirlSurface(particle);
        particle.setState(FragmentState.INSIDE_SWIRL);
    }

    private void updateStateInsideSwirl(EnergizerParticle particle) {
        particle.move();
        final Vector3f pos = particle.position();
        if (pos.z() < -SWIRL_HEIGHT) {
            // reached top of swirl: move to bottom of floor
            particle.setPosition(new Vector3f(pos.x(), pos.y(), floorSurfaceZ() - 0.5 * particle.size()));
        }
        // Rotate on swirl surface
        particle.setAngle(particle.angle() + SWIRL_ROTATION_SPEED);
        if (particle.angle() > Math.TAU) {
            particle.setAngle(particle.angle() - Math.TAU);
        }
        updateParticlePositionOnSwirlSurface(particle);
    }

    private void updateParticlePositionOnSwirlSurface(EnergizerParticle particle) {
        final Vector3f swirlBaseCenter = swirlBaseCenters.get(particle.targetSwirlIndex());
        final var pos = new Vector3f(
            swirlBaseCenter.x() + SWIRL_RADIUS * Math.cos(particle.angle()),
            swirlBaseCenter.y() + SWIRL_RADIUS * Math.sin(particle.angle()),
            particle.position().z()
        );
        particle.setPosition(pos);
    }

    private double floorSurfaceZ() {
        return floor3D.getTranslateZ() - 0.5 * floor3D.getDepth();
    }
}