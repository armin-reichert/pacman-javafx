/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.Globals;
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

    private static final Vector3f SWIRL_RISING_VELOCITY = new Vector3f(0, 0, -0.5f);

    private static final float PARTICLE_MEAN_RADIUS_UNSCALED = 0.15f;

    private static final float PARTICLE_SIZE_WHEN_RETURNING_HOME = 0.4f;

    private static final float PARTICLE_SPEED_EXPLODING_XY_MIN = 0.0f;
    private static final float PARTICLE_SPEED_EXPLODING_XY_MAX = 0.4f;

    private static final float PARTICLE_SPEED_EXPLODING_Z_MIN = 2;
    private static final float PARTICLE_SPEED_EXPLODING_Z_MAX = 8;

    private static final float PARTICLE_SPEED_MOVING_HOME_MIN = 0.3f;
    private static final float PARTICLE_SPEED_MOVING_HOME_MAX = 0.6f;

    private static final float PARTICLE_ROTATION_SPEED = 0.05f;

    private static final int PARTICLE_OUT_OF_WORLD_Z = 50;

    private static final byte[] GHOST_IDS = {
        Globals.RED_GHOST_SHADOW,
        Globals.PINK_GHOST_SPEEDY,
        Globals.CYAN_GHOST_BASHFUL,
        Globals.ORANGE_GHOST_POKEY
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
        super(animationRegistry, "Energizer_Explosion");

        this.swirlBaseCenters = requireNonNull(swirlBaseCenters);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.floor3D = requireNonNull(floor3D);
        this.particlesGroup = requireNonNull(particlesGroup);

        setFactory(() -> new Transition() {
            {
                setCycleDuration(Duration.INDEFINITE);
            }

            @Override
            protected void interpolate(double frac) {
                for (EnergizerParticle particle : particles) {
                    switch (particle.state()) {
                        case FLYING       -> doFly(particle);
                        case ATTRACTED    -> doAttract(particle);
                        case INSIDE_SWIRL -> doMoveInsideSwirl(particle);
                        case OUT_OF_WORLD -> {}
                    }
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

    private void doFly(EnergizerParticle particle) {
        particle.fly(gravity);
        if (particle.collidesWith(floor3D)) {
            onParticleLandedOnFloor(particle);
        }
        else if (particle.position().z() > PARTICLE_OUT_OF_WORLD_Z) {
            particle.shape().setVisible(false);
            particle.setVelocity(Vector3f.ZERO);
            particle.setState(FragmentState.OUT_OF_WORLD);
        }
    }

    private void doAttract(EnergizerParticle particle) {
        final boolean homeReached = moveParticleTowardsTarget(particle);
        if (homeReached) {
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
            case Globals.CYAN_GHOST_BASHFUL -> 0;
            case Globals.RED_GHOST_SHADOW, Globals.PINK_GHOST_SPEEDY -> 1;
            case Globals.ORANGE_GHOST_POKEY -> 2;
            default -> throw new IllegalArgumentException("Illegal ghost ID: " + ghostID);
        };
        final var swirlCenter = swirlBaseCenters.get(targetSwirlIndex);
        final float speed = randomFloat(PARTICLE_SPEED_MOVING_HOME_MIN, PARTICLE_SPEED_MOVING_HOME_MAX);

        if (particle instanceof BallEnergizerParticle ball) {
            particlesGroup.getChildren().remove(particle.shape());
            ball.changeMeshResolution(MESH_DIVISIONS_LOW);
            particlesGroup.getChildren().add(particle.shape());
        }
        particle.setSize(PARTICLE_SIZE_WHEN_RETURNING_HOME);
        final double z = floorSurfaceZ() - 0.5 * particle.size();
        particle.setPosition(new Vector3f(particle.position().x(), particle.position().y(), z));
        particle.setTargetSwirlIndex(targetSwirlIndex);
        particle.setTargetPosition(randomPointOnLateralSwirlSurface(swirlCenter));
        particle.setVelocity(new Vector3f(
            particle.targetPosition().x() - particle.position().x(),
            particle.targetPosition().y() - particle.position().y(),
            0)
            .normalized().mul(speed));

        particle.setState(FragmentState.ATTRACTED);

        particle.shape().setMaterial(ghostDressMaterials.get(ghostID));
    }

    private Vector3f randomPointOnLateralSwirlSurface(Vector3f swirlCenter) {
        final double angle = Math.toRadians(randomInt(0, 360));
        return new Vector3f(
            swirlCenter.x() + SWIRL_RADIUS * Math.cos(angle),
            swirlCenter.y() + SWIRL_RADIUS * Math.sin(angle),
            randomFloat(0, SWIRL_HEIGHT)
        );
    }

    private boolean moveParticleTowardsTarget(EnergizerParticle particle) {
        final double xyDist = Math.hypot(
            particle.position().x() - particle.targetPosition().x(),
            particle.position().y() - particle.targetPosition().y()
        );
        final boolean targetReached = xyDist < particle.velocity().length();
        if (!targetReached) {
            particle.move();
        }
        return targetReached;
    }

    private void onParticleReachedTarget(EnergizerParticle particle) {
        particle.setState(FragmentState.INSIDE_SWIRL);
        particle.setAngle(Math.toRadians(randomInt(0, 360)));
        particle.setVelocity(SWIRL_RISING_VELOCITY);
        updateSwirlPosition(particle);
    }

    private void doMoveInsideSwirl(EnergizerParticle particle) {
        particle.move();
        final Vector3f pos = particle.position();
        if (pos.z() < -SWIRL_HEIGHT) {
            // reached top of swirl: move to bottom of floor
            particle.setPosition(new Vector3f(pos.x(), pos.y(), floorSurfaceZ() - 0.5 * particle.size()));
        }
        // Rotate on swirl border
        particle.setAngle(particle.angle() + PARTICLE_ROTATION_SPEED);
        if (particle.angle() > Math.TAU) {
            particle.setAngle(particle.angle() - Math.TAU);
        }
        updateSwirlPosition(particle);
    }

    private void updateSwirlPosition(EnergizerParticle particle) {
        final var swirlBaseCenter = swirlBaseCenters.get(particle.targetSwirlIndex());
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