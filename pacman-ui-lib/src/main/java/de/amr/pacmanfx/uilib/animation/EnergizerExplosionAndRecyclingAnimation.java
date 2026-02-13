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
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the particles are sucked in by 3 swirls inside the ghost house where they accumulate
 * to colored ghost shapes.
 */
public class EnergizerExplosionAndRecyclingAnimation extends RegisteredAnimation {

    public static final float SWIRL_RADIUS = 7;
    public static final float SWIRL_HEIGHT = 12;
    public static final float SWIRL_RISING_SPEED = 0.5f;
    public static final float SWIRL_ROTATION_SEC = 1.0f;

    // Time includes movement of particles to the ghost house after the explosion
    private static final Duration TOTAL_DURATION = Duration.seconds(30);

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
    private List<EnergizerFragment> particles;

    private class ParticlesMovement extends Transition {

        private final List<EnergizerFragment> trash = new ArrayList<>();

        public ParticlesMovement() {
            setCycleDuration(TOTAL_DURATION);
            setOnFinished(_ -> {
                trash.clear();
                for (EnergizerFragment particle : particles) {
                    if (!particle.partOfSwirl) {
                        trash.add(particle);
                    }
                }
                Platform.runLater(() -> {
                    trash.forEach(EnergizerFragment::dispose);
                    trash.clear();
                });
            });
        }

        @Override
        protected void interpolate(double t) {
            trash.clear();
            for (EnergizerFragment particle : particles) {
                if (particle.movingHome) {
                    boolean homePositionReached = moveHome(particle);
                    if (homePositionReached) {
                        particleReachedHome(particle, swirls);
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
                        landedOnFloor(particle);
                        particle.movingHome = true;
                    }
                    else if (particle.getTranslateZ() > PARTICLE_REMOVAL_Z) {
                        // if particle fell over world border, remove it at some z position under floor level
                        trash.add(particle);
                    }
                }
            }
            if (!trash.isEmpty()) {
                Platform.runLater(() -> {
                    particles.removeAll(trash);
                    particlesGroup.getChildren().removeAll(trash);
                    trash.forEach(EnergizerFragment::dispose);
                    Logger.debug("{} particles disposed, t={}", trash.size(), t);
                    trash.clear();
                });
            }
        }

        private void landedOnFloor(EnergizerFragment particle) {
            particle.ghostColorIndex = randomByte(0, 4);
            particle.setMaterial(ghostDressMaterials.get(particle.ghostColorIndex));
            particle.setRadius(PARTICLE_RADIUS_RETURNING_HOME);
            particle.setTranslateZ(-particle.getRadius()); // floor top is at z=0
            var swirlCenter = new Point3D(
                ghostRevivalPositionCenters[particle.ghostColorIndex].x(),
                ghostRevivalPositionCenters[particle.ghostColorIndex].y(),
                0); // floor top is at z=0
            particle.houseTargetPosition = randomPointOnLateralSurface(swirlCenter, SWIRL_RADIUS, SWIRL_HEIGHT);

            float speed = randomFloat(PARTICLE_SPEED_MOVING_HOME_MIN, PARTICLE_SPEED_MOVING_HOME_MAX);
            particle.velocity = new Vector3f(
                (float) (particle.houseTargetPosition.getX() - particle.getTranslateX()),
                (float) (particle.houseTargetPosition.getY() - particle.getTranslateY()),
                0)
                .normalized()
                .mul(speed);
        }

        private boolean moveHome(EnergizerFragment particle) {
            // if target reached, move particle to its column group
            double distXY = Math.hypot(
                particle.getTranslateX() - particle.houseTargetPosition.getX(),
                particle.getTranslateY() - particle.houseTargetPosition.getY());
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

        private void particleReachedHome(EnergizerFragment particle, List<Group> swirls) {
            Group targetSwirl = swirls.get(swirlIndex(particle.ghostColorIndex));
            particle.setTranslateX(particle.houseTargetPosition.getX() - targetSwirl.getTranslateX());
            particle.setTranslateY(particle.houseTargetPosition.getY() - targetSwirl.getTranslateY());
            particle.setTranslateZ(particle.houseTargetPosition.getZ());
            particle.velocity = new Vector3f(0, 0, -SWIRL_RISING_SPEED);
            Platform.runLater(() -> {
                if (particlesGroup != null) {
                    particlesGroup.getChildren().remove(particle);
                    targetSwirl.getChildren().add(particle);
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

        private void moveInsideSwirl(EnergizerFragment particle) {
            particle.move();
            if (particle.getTranslateZ() < -SWIRL_HEIGHT) {
                particle.setTranslateZ(0);
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
                for (EnergizerFragment particle : particles) {
                    particle.velocity = Vector3f.ZERO;
                }
            }
        }

        private void createAndAddParticles(Material particleMaterial, Point3D origin) {
            particles = new ArrayList<>();
            for (int i = 0; i < PARTICLE_COUNT; ++i) {
                double radius = randomParticleRadius();
                Vector3f velocity = randomParticleVelocity();
                EnergizerFragment particle = new EnergizerFragment(radius, particleMaterial, velocity, origin);
                particle.setVisible(true);
                particles.add(particle);
            }
            particlesGroup.getChildren().setAll(particles);
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

    public EnergizerExplosionAndRecyclingAnimation(
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
        var particlesMovement = new ParticlesMovement();
        particlesMovement.setDelay(Duration.millis(200));
        return particlesMovement;
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
            for (EnergizerFragment particle : particles) {
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

    private boolean particleTouchesFloor(EnergizerFragment particle) {
        final double r = particle.getRadius(), cx = particle.getTranslateX(), cy = particle.getTranslateY();
        if (cx + r < 0 || cx - r > floorSize.x()) return false;
        if (cy + r < 0 || cy - r > floorSize.y()) return false;
        return particle.getTranslateZ() >= 0;
    }
}