/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the particles are sucked in by 3 swirls inside the ghost house where they accumulate
 * to colored ghost shapes.
 */
public class EnergizerExplosionAndSwirlAnimation extends RegisteredAnimation {

    public static final float SWIRL_RADIUS = 7;
    public static final float SWIRL_HEIGHT = 12;
    public static final float SWIRL_RISING_SPEED = 0.5f;
    public static final float SWIRL_ROTATION_SEC = 1.0f;

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

    private final List<Group> swirls;
    private final Vector2f floorSize;

    private Vector3f gravity = Vector3f.ZERO;
    private Point3D origin;
    private Vector2f[] ghostRevivalPositionCenters;
    private Group particlesGroupContainer;
    private Group particlesGroup = new Group();

    private Material particleMaterial;
    private List<PhongMaterial> ghostDressMaterials;
    private List<EnergizerFragmentShape3D> particles;
    private final Set<EnergizerFragmentShape3D> particlesToDispose = new HashSet<>();

    private class ParticleSwarmMovement extends Transition {

        public ParticleSwarmMovement(Duration duration) {
            setCycleCount(1);
            setCycleDuration(duration);
        }

        @Override
        protected void interpolate(double t) {
            for (EnergizerFragmentShape3D particle : particles) {
                if (particle.movingHome) {
                    final boolean homeReached = moveHome(particle);
                    if (homeReached) {
                        onParticleReachedHome(particle, swirls);
                        particle.movingHome = false;
                        particle.partOfSwirl = true;
                    }
                }
                else if (particle.partOfSwirl) {
                    moveInsideSwirl(particle);
                }
                else {
                    particle.fly(gravity);
                    if (particleTouchesFloor(particle)) {
                        onParticleLandedOnFloor(particle);
                        particle.movingHome = true;
                    }
                    else if (particle.shape().getTranslateZ() > PARTICLE_REMOVAL_Z) {
                        // if particle fell over world border, remove it at some z position under floor level
                        particlesToDispose.add(particle);
                    }
                }
            }
        }

        private void onParticleLandedOnFloor(EnergizerFragmentShape3D particle) {
            particle.ghostColorIndex = randomByte(0, 4);
            particle.shape().setMaterial(ghostDressMaterials.get(particle.ghostColorIndex));
            particle.shape().setRadius(PARTICLE_RADIUS_RETURNING_HOME);
            particle.shape().setTranslateZ(-particle.shape().getRadius()); // floor top is at z=0
            var swirlCenter = new Point3D(
                ghostRevivalPositionCenters[particle.ghostColorIndex].x(),
                ghostRevivalPositionCenters[particle.ghostColorIndex].y(),
                0); // floor top is at z=0
            particle.houseTargetPosition = randomPointOnLateralSurface(swirlCenter, SWIRL_RADIUS, SWIRL_HEIGHT);

            float speed = randomFloat(PARTICLE_SPEED_MOVING_HOME_MIN, PARTICLE_SPEED_MOVING_HOME_MAX);
            particle.velocity = new Vector3f(
                (float) (particle.houseTargetPosition.getX() - particle.shape().getTranslateX()),
                (float) (particle.houseTargetPosition.getY() - particle.shape().getTranslateY()),
                0
            ).normalized().mul(speed);
        }

        private boolean moveHome(EnergizerFragmentShape3D particle) {
            // if target reached, move particle to its column group
            double distXY = Math.hypot(
                particle.shape().getTranslateX() - particle.houseTargetPosition.getX(),
                particle.shape().getTranslateY() - particle.houseTargetPosition.getY());
            boolean homePositionReached = distXY < particle.velocity.length();
            if (!homePositionReached) {
                particle.move();
            }
            return homePositionReached;
        }

        /**
         * @param center center of cylinder base
         * @param r radius
         * @param h height
         * @return random point on lateral surface of cylinder
         */
        private Point3D randomPointOnLateralSurface(Point3D center, double r, double h) {
            double angle = Math.toRadians(randomInt(0, 360));
            return new Point3D(
                center.getX() + r * Math.cos(angle),
                center.getY() + r * Math.sin(angle),
                randomFloat(0, (float) h)
            );
        }

        private void onParticleReachedHome(EnergizerFragmentShape3D particle, List<Group> swirls) {
            Group targetSwirl = swirls.get(swirlIndex(particle.ghostColorIndex));
            particle.shape().setTranslateX(particle.houseTargetPosition.getX() - targetSwirl.getTranslateX());
            particle.shape().setTranslateY(particle.houseTargetPosition.getY() - targetSwirl.getTranslateY());
            particle.shape().setTranslateZ(particle.houseTargetPosition.getZ());
            particle.velocity = new Vector3f(0, 0, -SWIRL_RISING_SPEED);
            Platform.runLater(() -> {
                if (particlesGroup != null) {
                    particlesGroup.getChildren().remove(particle.shape());
                    targetSwirl.getChildren().add(particle.shape());
                }
            });
        }

        private int swirlIndex(int personality) {
            return switch (personality) {
                case CYAN_GHOST_BASHFUL -> 0;
                case RED_GHOST_SHADOW, PINK_GHOST_SPEEDY -> 1;
                case ORANGE_GHOST_POKEY -> 2;
                default -> throw new IllegalArgumentException("Illegal ghost personality: " + personality);
            };
        }

        private void moveInsideSwirl(EnergizerFragmentShape3D particle) {
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
                for (EnergizerFragmentShape3D particle : particles) {
                    particle.velocity = Vector3f.ZERO;
                }
            }
        }

        private void createAndAddParticles(Material particleMaterial, Point3D origin) {
            particles = new ArrayList<>();
            for (int i = 0; i < PARTICLE_COUNT; ++i) {
                double radius = randomParticleRadius();
                Vector3f velocity = randomParticleVelocity();
                EnergizerFragmentShape3D particle = new EnergizerFragmentShape3D(radius, particleMaterial, velocity, origin);
                particle.shape().setVisible(true);
                particles.add(particle);
            }
            particlesGroup.getChildren().setAll(particles.stream().map(EnergizerFragmentShape3D::shape).toList());
        }

        private double randomParticleRadius() {
            double scaling = RND.nextGaussian(2, 0.1);
            scaling = Math.clamp(scaling, 0.5, 4);
            return scaling * PARTICLE_MEAN_RADIUS_UNSCALED;
        }

        private Vector3f randomParticleVelocity() {
            int xDir = RND.nextBoolean() ? -1 : 1;
            int yDir = RND.nextBoolean() ? -1 : 1;
            return new Vector3f(
                xDir * randomFloat(PARTICLE_SPEED_EXPLODING_XY_MIN, PARTICLE_SPEED_EXPLODING_XY_MAX),
                yDir * randomFloat(PARTICLE_SPEED_EXPLODING_XY_MIN, PARTICLE_SPEED_EXPLODING_XY_MAX),
                -randomFloat(PARTICLE_SPEED_EXPLODING_Z_MIN, PARTICLE_SPEED_EXPLODING_Z_MAX)
            );
        }
    }

    public EnergizerExplosionAndSwirlAnimation(
        AnimationRegistry animationRegistry,
        Point3D origin,
        List<Group> swirls,
        Vector2f[] ghostRevivalPositionCenters,
        Group particlesGroupContainer,
        Material particleMaterial,
        List<PhongMaterial> ghostDressMaterials,
        Vector2f floorSize)
    {
        super(animationRegistry, "Energizer_Explosion");

        this.origin = requireNonNull(origin);
        this.swirls = requireNonNull(swirls);
        this.ghostRevivalPositionCenters = requireNonNull(ghostRevivalPositionCenters);
        this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
        this.particleMaterial = requireNonNull(particleMaterial);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.floorSize = requireNonNull(floorSize);
        particlesGroupContainer.getChildren().add(particlesGroup);
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
            for (EnergizerFragmentShape3D particle : particles) {
                if (!particle.partOfSwirl) {
                    particlesToDispose.add(particle);
                }
            }
            Logger.info("{} particles will be disposed", particlesToDispose.size());
            particlesToDispose.forEach(EnergizerFragmentShape3D::dispose);
            particles.removeAll(particlesToDispose);
            particlesGroup.getChildren().removeAll(particlesToDispose.stream().map(EnergizerFragmentShape3D::shape).toList());
            particlesToDispose.clear();
        });
        return swarmMovement;
    }

    @Override
    protected void freeResources() {
        if (origin != null) {
            origin = null;
        }
        if (ghostRevivalPositionCenters != null) {
            ghostRevivalPositionCenters = null;
        }
        if (particles != null) {
            for (EnergizerFragmentShape3D particle : particles) {
                particle.dispose();
            }
            Logger.info("Disposed {} particles", particles.size());
            particles.clear();
            particles = null;
        }
        if (particlesGroup != null) {
            particlesGroup.getChildren().clear();
            particlesGroup = null;
        }
        if (particlesGroupContainer != null) {
            particlesGroupContainer.getChildren().remove(particlesGroup);
            particlesGroupContainer = null;
        }
        if (particleMaterial != null) {
            particleMaterial = null;
        }
        if (ghostDressMaterials != null) {
            ghostDressMaterials = null;
        }
    }

    private boolean particleTouchesFloor(EnergizerFragmentShape3D particle) {
        final double r = particle.shape().getRadius(), cx = particle.shape().getTranslateX(), cy = particle.shape().getTranslateY();
        if (cx + r < 0 || cx - r > floorSize.x()) return false;
        if (cy + r < 0 || cy - r > floorSize.y()) return false;
        return particle.shape().getTranslateZ() >= 0;
    }
}