/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.math.Vector3f;
import de.amr.pacmanfx.uilib.animation.AbstractEnergizerParticle.FragmentState;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
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

    private static final float PARTICLE_SIZE_WHEN_RETURNING_HOME = 0.25f;

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

    private final List<Point3D> swirlBaseCenters;
    private final Box floor3D;
    private final List<PhongMaterial> ghostDressMaterials;

    private Vector3f gravity = Vector3f.ZERO;

    private final List<AbstractEnergizerParticle> particles = new ArrayList<>();
    private final Group particlesGroup;

    public EnergizerParticlesAnimation(
        AnimationRegistry animationRegistry,
        List<Point3D> swirlBaseCenters,
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
                for (AbstractEnergizerParticle particle : particles) {
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

    public void showExplosion(Point3D origin) {
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

    private void doFly(AbstractEnergizerParticle particle) {
        particle.fly(gravity);
        if (particle.collidesWith(floor3D)) {
            onParticleLandedOnFloor(particle);
        }
        else if (particle.shape().getTranslateZ() > PARTICLE_OUT_OF_WORLD_Z) {
            particle.shape().setVisible(false);
            particle.setVelocity(Vector3f.ZERO);
            particle.setState(FragmentState.OUT_OF_WORLD);
        }
    }

    private void doAttract(AbstractEnergizerParticle particle) {
        final boolean homeReached = moveParticleTowardsTarget(particle);
        if (homeReached) {
            onParticleReachedTarget(particle);
        }
    }

    /* When a particle lands on the maze floor, it is resized to a uniform size and gets attracted by a randomly
     * assigned swirl inside the ghost house. Once it reaches its target position (on the swirl surface), it is
     * integrated into the swirl and moves forever on the swirl surface.
     */
    private void onParticleLandedOnFloor(AbstractEnergizerParticle particle) {
        final byte ghostID = randomGhostID();
        final int targetSwirlIndex = switch (ghostID) {
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
        particle.setTargetSwirlIndex(targetSwirlIndex);
        particle.setTargetPosition(randomPointOnLateralSwirlSurface(swirlCenter));
        particle.setVelocity(new Vector3f(
                (float) (particle.targetPosition().getX() - particle.shape().getTranslateX()),
                (float) (particle.targetPosition().getY() - particle.shape().getTranslateY()),
                0
        ).normalized().mul(speed));

        particle.setState(FragmentState.ATTRACTED);

        particle.shape().setMaterial(ghostDressMaterials.get(ghostID));
        particle.shape().setTranslateZ(floorSurfaceZ() - 0.5 * particle.size());
    }

    private Point3D randomPointOnLateralSwirlSurface(Point3D swirlCenter) {
        final double angle = Math.toRadians(randomInt(0, 360));
        return new Point3D(
                swirlCenter.getX() + SWIRL_RADIUS * Math.cos(angle),
                swirlCenter.getY() + SWIRL_RADIUS * Math.sin(angle),
                randomFloat(0, SWIRL_HEIGHT)
        );
    }

    private boolean moveParticleTowardsTarget(AbstractEnergizerParticle particle) {
        final double distXY = Math.hypot(
                particle.shape().getTranslateX() - particle.targetPosition().getX(),
                particle.shape().getTranslateY() - particle.targetPosition().getY());
        final boolean targetReached = distXY < particle.velocity().length();
        if (!targetReached) {
            particle.move();
        }
        return targetReached;
    }

    private void onParticleReachedTarget(AbstractEnergizerParticle particle) {
        particle.setState(FragmentState.INSIDE_SWIRL);
        // Place particle at random position on swirl base radius
        particle.setAngle(Math.toRadians(randomInt(0, 360)));
        particle.setVelocity(SWIRL_RISING_VELOCITY);
        updateParticleSwirlPosition(particle);
    }

    private void doMoveInsideSwirl(AbstractEnergizerParticle particle) {
        particle.move();
        if (particle.shape().getTranslateZ() < -SWIRL_HEIGHT) {
            particle.shape().setTranslateZ(floorSurfaceZ() - 0.5 * particle.size());
        }
        particle.setAngle(particle.angle() + PARTICLE_ROTATION_SPEED);
        if (particle.angle() > Math.TAU) {
            particle.setAngle(particle.angle() - Math.TAU);
        }
        updateParticleSwirlPosition(particle);
    }

    private void updateParticleSwirlPosition(AbstractEnergizerParticle particle) {
        final var center = swirlBaseCenters.get(particle.targetSwirlIndex());
        particle.shape().setTranslateX(center.getX() + SWIRL_RADIUS * Math.cos(particle.angle()));
        particle.shape().setTranslateY(center.getY() + SWIRL_RADIUS * Math.sin(particle.angle()));
    }

    private double floorSurfaceZ() {
        return floor3D.getTranslateZ() - 0.5 * floor3D.getDepth();
    }


}