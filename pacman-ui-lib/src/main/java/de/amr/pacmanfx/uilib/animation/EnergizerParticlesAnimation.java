/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.math.Vector3f;
import de.amr.pacmanfx.uilib.model3D.ArcadeHouse3D;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
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
    private static final float SWIRL_RISING_SPEED = 0.5f;

    private static final Duration PARTICLE_SWARM_MOVEMENT_DELAY = Duration.millis(200);
    // Time includes movement of particles to the ghost house after the explosion
    private static final Duration PARTICLE_SWARM_DROPPERS_DISPOSAL_TIME = Duration.seconds(15);

    private static final short PARTICLE_COUNT = 500;

    private static final float PARTICLE_MEAN_RADIUS_UNSCALED = 0.2f;
    private static final float PARTICLE_RADIUS_RETURNING_HOME = 0.15f;

    private static final float PARTICLE_SPEED_EXPLODING_XY_MIN = 0.0f;
    private static final float PARTICLE_SPEED_EXPLODING_XY_MAX = 1.0f;

    private static final float PARTICLE_SPEED_EXPLODING_Z_MIN = 2;
    private static final float PARTICLE_SPEED_EXPLODING_Z_MAX = 8;

    private static final float PARTICLE_SPEED_MOVING_HOME_MIN = 0.4f;
    private static final float PARTICLE_SPEED_MOVING_HOME_MAX = 0.8f;

    private static final int PARTICLE_REMOVAL_Z = 50;

    private final Box floor3D;

    private Vector3f gravity = Vector3f.ZERO;
    private Point3D origin;
    private List<ArcadeHouse3D.SwirlAnimation> swirlAnimations;
    private Group particlesGroupContainer;
    private Group particleShapesGroup = new Group();
    private Material particleMaterial;
    private List<PhongMaterial> ghostDressMaterials;
    private List<AbstractEnergizerFragment> particles;
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
                    case AbstractEnergizerFragment.FragmentState.FLYING -> {
                        particle.fly(gravity);
                        if (particle.collidesWith(floor3D)) {
                            onParticleLandedOnFloor(particle);
                            particle.setState(AbstractEnergizerFragment.FragmentState.ATTRACTED);
                        } else if (particle.shape().getTranslateZ() > PARTICLE_REMOVAL_Z) {
                            // if particle fell over world border, remove it at some z position under floor level
                            particlesToDispose.add(particle);
                        }
                    }
                    case AbstractEnergizerFragment.FragmentState.ATTRACTED -> {
                        final boolean homeReached = moveTowardsTargetPosition(particle);
                        if (homeReached) {
                            onParticleReachedTargetPosition(particle);
                            particle.setState(AbstractEnergizerFragment.FragmentState.INSIDE_SWIRL);
                        }
                    }
                    case AbstractEnergizerFragment.FragmentState.INSIDE_SWIRL -> moveInsideSwirl(particle);
                }
            }
        }

        // When a particle lands on the maze floor, it is resized to a uniform size and gets attracted by a randomly
        // assigned swirl inside the ghost house. Once it reaches its target position on the swirls surface, it is
        // integrated into the swirl and moves forever on the surface.
        private void onParticleLandedOnFloor(AbstractEnergizerFragment particle) {
            // Select random ghost ID and assign target swirl where particle will be finally end
            final byte[] ghostIDs = {
                Globals.RED_GHOST_SHADOW,
                Globals.PINK_GHOST_SPEEDY,
                Globals.CYAN_GHOST_BASHFUL,
                Globals.ORANGE_GHOST_POKEY
            };
            final byte ghostID = ghostIDs[randomInt(0, ghostIDs.length)];
            final int swirlIndex = switch (ghostID) {
                case Globals.CYAN_GHOST_BASHFUL -> 0;
                case Globals.RED_GHOST_SHADOW, Globals.PINK_GHOST_SPEEDY -> 1;
                case Globals.ORANGE_GHOST_POKEY -> 2;
                default -> throw new IllegalArgumentException("Illegal ghost ID: " + ghostID);
            };

            particle.shape().setMaterial(ghostDressMaterials.get(ghostID));
            particle.shape().setTranslateZ(-0.5 * particle.size()); // put particle on floor surface

            // Set uniform size for particles returning to house
            particle.setSize(2 * PARTICLE_RADIUS_RETURNING_HOME);

            final Group targetSwirlGroup = swirlAnimations.get(swirlIndex).swirlGroup();
            particle.setTargetSwirlGroup(targetSwirlGroup);

            final var swirlCenter = new Point3D(targetSwirlGroup.getTranslateX(), targetSwirlGroup.getTranslateY(), 0);
            particle.setTargetPosition(randomPointOnLateralSwirlSurface(swirlCenter));

            final float speed = randomFloat(PARTICLE_SPEED_MOVING_HOME_MIN, PARTICLE_SPEED_MOVING_HOME_MAX);
            particle.setVelocity(new Vector3f(
                (float) (particle.targetPosition().getX() - particle.shape().getTranslateX()),
                (float) (particle.targetPosition().getY() - particle.shape().getTranslateY()),
                0
            ).normalized().mul(speed));
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
            Platform.runLater(() -> {
                final Point3D targetPosition = particle.targetPosition();
                final Shape3D particleShape = particle.shape();
                if (particleShapesGroup != null) {
                    particleShapesGroup.getChildren().remove(particleShape);
                    particle.targetSwirlGroup().getChildren().add(particleShape);
                    // Set position relative to swirl group
                    particleShape.setTranslateX(targetPosition.getX() - particle.targetSwirlGroup().getTranslateX());
                    particleShape.setTranslateY(targetPosition.getY() - particle.targetSwirlGroup().getTranslateY());
                    particleShape.setTranslateZ(targetPosition.getZ());
                    particle.setVelocity(new Vector3f(0, 0, -SWIRL_RISING_SPEED));
                }
            });
        }

        private void moveInsideSwirl(AbstractEnergizerFragment particle) {
            particle.move();
            if (particle.shape().getTranslateZ() < -SWIRL_HEIGHT) {
                particle.shape().setTranslateZ(0);
            }
        }

        @Override
        public void play() {
            createAndAddParticles(particleMaterial, origin);
            super.play();
        }

        @Override
        public void stop() {
            super.stop();
            if (particles != null) {
                for (AbstractEnergizerFragment particle : particles) {
                    particle.setVelocity(Vector3f.ZERO);
                }
            }
        }

        private void createAndAddParticles(Material particleMaterial, Point3D origin) {
            particles = new ArrayList<>();
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
        Point3D origin,
        List<ArcadeHouse3D.SwirlAnimation> swirlAnimations,
        Group particlesGroupContainer,
        Material particleMaterial,
        List<PhongMaterial> ghostDressMaterials,
        Box floor3D)
    {
        super(animationRegistry, "Energizer_Explosion");

        this.origin = requireNonNull(origin);
        this.swirlAnimations = requireNonNull(swirlAnimations);
        this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
        this.particleMaterial = requireNonNull(particleMaterial);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.floor3D = requireNonNull(floor3D);
        particlesGroupContainer.getChildren().add(particleShapesGroup);
    }

    public void setGravity(Vector3f gravity) {
        this.gravity = gravity;
    }

    @Override
    protected Animation createAnimationFX() {
        final var swarmMovement = new ParticleSwarmMovement(PARTICLE_SWARM_DROPPERS_DISPOSAL_TIME);
        swarmMovement.setDelay(PARTICLE_SWARM_MOVEMENT_DELAY);
        swarmMovement.setOnFinished(_ -> {
            // Particles that did not make it into the swirl will be disposed
            for (AbstractEnergizerFragment particle : particles) {
                if (particle.state() == AbstractEnergizerFragment.FragmentState.ATTRACTED) {
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
        if (origin != null) {
            origin = null;
        }
        if (swirlAnimations != null) {
            swirlAnimations = null;
        }
        if (particles != null) {
            for (AbstractEnergizerFragment particle : particles) {
                particle.dispose();
            }
            Logger.info("Disposed {} particles", particles.size());
            particles.clear();
            particles = null;
        }
        if (particleShapesGroup != null) {
            particleShapesGroup.getChildren().clear();
            particleShapesGroup = null;
        }
        if (particlesGroupContainer != null) {
            particlesGroupContainer.getChildren().remove(particleShapesGroup);
            particlesGroupContainer = null;
        }
        if (particleMaterial != null) {
            particleMaterial = null;
        }
        if (ghostDressMaterials != null) {
            ghostDressMaterials = null;
        }
    }
}