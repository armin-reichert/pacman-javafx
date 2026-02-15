/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.math.Vector3f;
import de.amr.pacmanfx.uilib.animation.AbstractEnergizerFragment.FragmentState;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.lib.math.RandomNumberSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the explosion particles that land on the floor are sucked in by 3 swirls
 * inside the ghost house where they accumulate to colored ghost shapes.
 */
public class EnergizerParticlesAnimation extends RegisteredAnimation {

    private static final float SWIRL_RADIUS = 7;
    private static final float SWIRL_HEIGHT = 12;

    private static final Vector3f SWIRL_RISING_VELOCITY = new Vector3f(0, 0, -0.5f);

    // Time includes movement of particles to the ghost house after the explosion
    private static final Duration PARTICLE_SWARM_DROPPERS_DISPOSAL_TIME = Duration.seconds(15);

    private static final short PARTICLE_COUNT = 500;

    private static final float PARTICLE_MEAN_RADIUS_UNSCALED = 0.2f;
    private static final float PARTICLE_SIZE_WHEN_RETURNING_HOME = 0.4f;

    private static final float PARTICLE_SPEED_EXPLODING_XY_MIN = 0.0f;
    private static final float PARTICLE_SPEED_EXPLODING_XY_MAX = 1.0f;

    private static final float PARTICLE_SPEED_EXPLODING_Z_MIN = 2;
    private static final float PARTICLE_SPEED_EXPLODING_Z_MAX = 8;

    private static final float PARTICLE_SPEED_MOVING_HOME_MIN = 0.4f;
    private static final float PARTICLE_SPEED_MOVING_HOME_MAX = 0.8f;

    private static final int PARTICLE_REMOVAL_Z = 50;

    private static final byte[] GHOST_IDS = {
        Globals.RED_GHOST_SHADOW,
        Globals.PINK_GHOST_SPEEDY,
        Globals.CYAN_GHOST_BASHFUL,
        Globals.ORANGE_GHOST_POKEY
    };

    private final Point3D origin;
    private final Box floor3D;
    private final List<Group> swirlGroups;
    private final Material particleMaterial;
    private final List<PhongMaterial> ghostDressMaterials;

    private Vector3f gravity = Vector3f.ZERO;

    private Group particleShapesGroup = new Group();
    private final List<AbstractEnergizerFragment> particles = new ArrayList<>();
    private final Set<AbstractEnergizerFragment> particlesToDispose = new HashSet<>();

    private class ParticleSwarmMovement extends Transition {

        public ParticleSwarmMovement(Duration duration) {
            setCycleCount(1);
            setCycleDuration(duration);
        }

        @Override
        protected void interpolate(double t) {
            for (AbstractEnergizerFragment particle : particles) {
                switch (particle.state()) {
                    case FragmentState.FLYING -> {
                        particle.fly(gravity);
                        if (particle.collidesWith(floor3D)) {
                            onParticleLandedOnFloor(particle);
                        } else if (particle.shape().getTranslateZ() > PARTICLE_REMOVAL_Z) {
                            // if particle fell over world border, remove it at some z position under floor level
                            particlesToDispose.add(particle);
                        }
                    }
                    case FragmentState.ATTRACTED -> {
                        final boolean homeReached = moveTowardsTargetPosition(particle);
                        if (homeReached) {
                            onParticleReachedTargetPosition(particle);
                        }
                    }
                    case FragmentState.INSIDE_SWIRL -> moveInsideSwirl(particle);
                }
            }
        }

        /* When a particle lands on the maze floor, it is resized to a uniform size and gets attracted by a randomly
         * assigned swirl inside the ghost house. Once it reaches its target position (on the swirl surface), it is
         * integrated into the swirl and moves forever on the swirl surface.
         */
        private void onParticleLandedOnFloor(AbstractEnergizerFragment particle) {
            // Select random ghost ID and assign target swirl located at that ghost's revival position in the house
            final byte ghostID = GHOST_IDS[randomInt(0, GHOST_IDS.length)];
            final int swirlIndex = switch (ghostID) {
                case Globals.CYAN_GHOST_BASHFUL -> 0;
                case Globals.RED_GHOST_SHADOW, Globals.PINK_GHOST_SPEEDY -> 1;
                case Globals.ORANGE_GHOST_POKEY -> 2;
                default -> throw new IllegalArgumentException("Illegal ghost ID: " + ghostID);
            };

            particle.shape().setMaterial(ghostDressMaterials.get(ghostID));

            // Set uniform size for particles returning to house
            particle.setSize(PARTICLE_SIZE_WHEN_RETURNING_HOME);
            // Put particle on floor surface
            particle.shape().setTranslateZ(floorSurfaceZ() - 0.5 * particle.size());

            final Group targetSwirlGroup = swirlGroups.get(swirlIndex);
            particle.setTargetSwirlIndex(swirlIndex);

            final var swirlCenter = new Point3D(targetSwirlGroup.getTranslateX(), targetSwirlGroup.getTranslateY(), 0);
            particle.setTargetPosition(randomPointOnLateralSwirlSurface(swirlCenter));

            final float speed = randomFloat(PARTICLE_SPEED_MOVING_HOME_MIN, PARTICLE_SPEED_MOVING_HOME_MAX);
            particle.setVelocity(new Vector3f(
                (float) (particle.targetPosition().getX() - particle.shape().getTranslateX()),
                (float) (particle.targetPosition().getY() - particle.shape().getTranslateY()),
                0
            ).normalized().mul(speed));

            particle.setState(FragmentState.ATTRACTED);
        }

        private boolean moveTowardsTargetPosition(AbstractEnergizerFragment particle) {
            final double distXY = Math.hypot(
                particle.shape().getTranslateX() - particle.targetPosition().getX(),
                particle.shape().getTranslateY() - particle.targetPosition().getY());
            final boolean targetPositionReached = distXY < particle.velocity().length();
            if (!targetPositionReached) {
                particle.move();
            }
            return targetPositionReached;
        }

        private Point3D randomPointOnLateralSwirlSurface(Point3D swirlCenter) {
            final double angle = Math.toRadians(randomInt(0, 360));
            return new Point3D(
                swirlCenter.getX() + SWIRL_RADIUS * Math.cos(angle),
                swirlCenter.getY() + SWIRL_RADIUS * Math.sin(angle),
                randomFloat(0, SWIRL_HEIGHT)
            );
        }

        private void onParticleReachedTargetPosition(AbstractEnergizerFragment particle) {
            final Point3D targetPosition = particle.targetPosition();
            final Shape3D particleShape = particle.shape();
            final Group targetSwirlGroup = swirlGroups.get(particle.targetSwirlIndex());
            if (particleShapesGroup != null) {
                particleShapesGroup.getChildren().remove(particleShape);
                targetSwirlGroup.getChildren().add(particleShape);
                // Set position relative to swirl group
                particleShape.setTranslateX(targetPosition.getX() - targetSwirlGroup.getTranslateX());
                particleShape.setTranslateY(targetPosition.getY() - targetSwirlGroup.getTranslateY());
                particleShape.setTranslateZ(targetPosition.getZ());
                particle.setVelocity(SWIRL_RISING_VELOCITY);
                particle.setState(FragmentState.INSIDE_SWIRL);
            }
            else Logger.error("Particle shapes group is NULL");
        }

        private void moveInsideSwirl(AbstractEnergizerFragment particle) {
            particle.move();
            if (particle.shape().getTranslateZ() < -SWIRL_HEIGHT) {
                particle.shape().setTranslateZ(floorSurfaceZ() - 0.5 * particle.size());
            }
        }

        private double floorSurfaceZ() {
            return floor3D.getTranslateZ() - 0.5 * floor3D.getDepth();
        }

        @Override
        public void play() {
            createAndAddParticles(origin);
            super.play();
        }

        @Override
        public void stop() {
            super.stop();
            for (AbstractEnergizerFragment particle : particles) {
                particle.setVelocity(Vector3f.ZERO);
            }
        }

        private void createAndAddParticles(Point3D origin) {
            particles.clear();
            particleShapesGroup.getChildren().clear();
            for (int i = 0; i < PARTICLE_COUNT; ++i) {
                final var particle = new BallEnergizerFragment(randomParticleRadius(), particleMaterial, origin);
                particle.setVelocity(randomParticleVelocity());
                particle.shape().setVisible(true);
                particles.add(particle);
                particleShapesGroup.getChildren().add(particle.shape());
            }
        }

        private double randomParticleRadius() {
            double scaling = RND.nextGaussian(2, 0.1);
            scaling = Math.clamp(scaling, 0.5, 4);
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
    }

    public EnergizerParticlesAnimation(
        AnimationRegistry animationRegistry,
        Point3D energizerCenter,
        List<Group> swirlGroups,
        Material particleMaterial,
        List<PhongMaterial> ghostDressMaterials,
        Box floor3D)
    {
        super(animationRegistry, "Energizer_Explosion");

        this.origin = requireNonNull(energizerCenter);
        this.swirlGroups = requireNonNull(swirlGroups);
        this.particleMaterial = requireNonNull(particleMaterial);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.floor3D = requireNonNull(floor3D);
    }

    public void setGravity(Vector3f gravity) {
        this.gravity = gravity;
    }

    public Group particleShapesGroup() {
        return particleShapesGroup;
    }

    @Override
    protected Animation createAnimationFX() {
        final var swarmMovement = new ParticleSwarmMovement(PARTICLE_SWARM_DROPPERS_DISPOSAL_TIME);
        swarmMovement.setOnFinished(_ -> {
            // Particles that did not make it into the swirl will be disposed
            for (AbstractEnergizerFragment particle : particles) {
                if (particle.state() != FragmentState.INSIDE_SWIRL) {
                    particlesToDispose.add(particle);
                }
            }
            Logger.info("{} particles will be disposed", particlesToDispose.size());
            particlesToDispose.forEach(AbstractEnergizerFragment::dispose);
            particles.removeAll(particlesToDispose);
            particleShapesGroup.getChildren().removeAll(particlesToDispose.stream().map(AbstractEnergizerFragment::shape).toList());
            particlesToDispose.clear();
        });
        return swarmMovement;
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
        if (particleShapesGroup != null) {
            particleShapesGroup.getChildren().clear();
            particleShapesGroup = null;
            Logger.info("Disposed particle shapes group");
        }
        final int particlesToDisposeCount = particlesToDispose.size();
        if (particlesToDisposeCount > 0) {
            for (var particle : particlesToDispose) {
                particle.dispose();
            }
            particlesToDispose.clear();
            Logger.info("Disposed {} particles that should have been disposed before", particlesToDisposeCount);
        }
    }
}