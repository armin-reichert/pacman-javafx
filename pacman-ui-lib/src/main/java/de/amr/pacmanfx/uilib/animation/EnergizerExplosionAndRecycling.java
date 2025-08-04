/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.Vec3f;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.uilib.model3D.ArcadeHouse3D;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.RandomNumberSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * When an energizer explodes, the particles are sucked in by 3 swirls inside the ghost house where they accumulate
 * to colored ghost shapes.
 */
public class EnergizerExplosionAndRecycling extends ManagedAnimation {

    public static final float SWIRL_RADIUS = 7;
    public static final float SWIRL_HEIGHT = 12;
    public static final float SWIRL_RISING_SPEED = 0.5f;
    public static final float SWIRL_ROTATION_SEC = 1.0f;

    // Time includes movement of particles to the ghost house after the explosion
    private static final Duration TOTAL_DURATION = Duration.seconds(30);

    // Use low-resolution mesh for particle
    private static final short PARTICLE_DIVISIONS = 8;

    private static final short PARTICLE_COUNT = 500;

    private static final float PARTICLE_MEAN_RADIUS_UNSCALED = 0.2f;
    private static final float PARTICLE_RADIUS_RETURNING_HOME = 0.15f;

    private static final float PARTICLE_SPEED_EXPLODING_XY_MIN = 0.0f;
    private static final float PARTICLE_SPEED_EXPLODING_XY_MAX = 1.0f;

    private static final float PARTICLE_SPEED_EXPLODING_Z_MIN = 2;
    private static final float PARTICLE_SPEED_EXPLODING_Z_MAX = 8;

    private static final float PARTICLE_SPEED_MOVING_HOME_MIN = 0.4f;
    private static final float PARTICLE_SPEED_MOVING_HOME_MAX = 0.8f;

    private static final float GRAVITY_Z = 0.18f;

    public static class Particle extends Sphere implements Disposable {
        public boolean moving_home = false;
        public boolean part_of_swirl = false;
        public byte ghost_personality = -1;
        public Point3D houseTargetPosition;
        public Vec3f velocity;

        public Particle(double radius, Material material, Vec3f velocity, Point3D origin) {
            super(radius, PARTICLE_DIVISIONS);
            this.velocity = velocity;
            setMaterial(material);
            setTranslateX(origin.getX());
            setTranslateY(origin.getY());
            setTranslateZ(origin.getZ());
            setEffect(randomGlow());
        }

        private Glow randomGlow() {
            return new Glow(0.5 + randomFloat(0, 0.5f));
        }

        @Override
        public void dispose() {
            setMaterial(null);
        }

        public Point3D center() {
            return new Point3D(getTranslateX(), getTranslateY(), getTranslateZ());
        }

        public void fly() {
            move();
            velocity.z += GRAVITY_Z;
        }

        public void move() {
            setTranslateX(getTranslateX() + velocity.x);
            setTranslateY(getTranslateY() + velocity.y);
            setTranslateZ(getTranslateZ() + velocity.z);
        }
    }

    private final ArcadeHouse3D house3D;
    private Point3D origin;
    private Vector2f[] ghostRevivalPositionCenters;
    private Group particlesGroupContainer;
    private Group particlesGroup = new Group();
    private Predicate<Particle> particleTouchesFloor;

    private Material particleMaterial;
    private Material[] ghostDressMaterials;
    private List<Particle> particles;

    private class ParticlesMovement extends Transition {

        private final List<Particle> trash = new ArrayList<>();

        public ParticlesMovement() {
            setCycleDuration(TOTAL_DURATION);
            setOnFinished(e -> {
                trash.clear();
                for (Particle particle : particles) {
                    if (!particle.part_of_swirl) {
                        trash.add(particle);
                    }
                }
                Platform.runLater(() -> {
                    trash.forEach(Particle::dispose);
                    trash.clear();
                });
            });
        }

        @Override
        protected void interpolate(double t) {
            trash.clear();
            for (Particle particle : particles) {
                if (particle.moving_home) {
                    boolean homePositionReached = moveHome(particle);
                    if (homePositionReached) {
                        arrivedAtTargetPosition(particle);
                        particle.moving_home = false;
                        particle.part_of_swirl = true;
                    }
                }
                else if (particle.part_of_swirl) {
                    moveInsideSwirl(particle);
                }
                else {
                    particle.fly();
                    if (particleTouchesFloor.test(particle)) {
                        landedOnFloor(particle);
                        particle.moving_home = true;
                    }
                    else if (particle.center().getZ() > 50) {
                        // if particle fell over world border, remove it at some z position under floor level
                        trash.add(particle);
                    }
                }
            }
            if (!trash.isEmpty()) {
                Platform.runLater(() -> {
                    particles.removeAll(trash);
                    particlesGroup.getChildren().removeAll(trash);
                    trash.forEach(Particle::dispose);
                    Logger.info("{} particles disposed, t={}", trash.size(), t);
                    trash.clear();
                });
            }
        }

        private void landedOnFloor(Particle particle) {
            particle.ghost_personality = randomByte(0, 4);
            particle.setMaterial(ghostDressMaterials[particle.ghost_personality]);
            particle.setRadius(PARTICLE_RADIUS_RETURNING_HOME);
            particle.setTranslateZ(-particle.getRadius()); // floor top is at z=0
            var swirlCenter = new Point3D(
                ghostRevivalPositionCenters[particle.ghost_personality].x(),
                ghostRevivalPositionCenters[particle.ghost_personality].y(),
                0); // floor top is at z=0
            particle.houseTargetPosition = randomPointOnLateralSurface(swirlCenter, SWIRL_RADIUS, SWIRL_HEIGHT);

            float speed = randomFloat(PARTICLE_SPEED_MOVING_HOME_MIN, PARTICLE_SPEED_MOVING_HOME_MAX);
            Point3D center = particle.center();
            particle.velocity.x = (float) (particle.houseTargetPosition.getX() - center.getX());
            particle.velocity.y = (float) (particle.houseTargetPosition.getY() - center.getY());
            particle.velocity.z = 0;
            particle.velocity.normalize().multiply(speed);
        }

        private boolean moveHome(Particle particle) {
            Point3D center = particle.center();
            // if target reached, move particle to its column group
            double distXY = Math.hypot(
                center.getX() - particle.houseTargetPosition.getX(),
                center.getY() - particle.houseTargetPosition.getY());
            boolean homePositionReached = distXY < particle.velocity.magnitude();
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

        private void arrivedAtTargetPosition(Particle particle) {
            Group swirl = house3D.particleSwirls().get(swirlIndex(particle.ghost_personality));
            particle.setTranslateX(particle.houseTargetPosition.getX() - swirl.getTranslateX());
            particle.setTranslateY(particle.houseTargetPosition.getY() - swirl.getTranslateY());
            particle.setTranslateZ(particle.houseTargetPosition.getZ());
            particle.velocity.x = particle.velocity.y = 0;
            particle.velocity.z = -SWIRL_RISING_SPEED;
            Platform.runLater(() -> {
                particlesGroup.getChildren().remove(particle);
                swirl.getChildren().add(particle);
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

        private void moveInsideSwirl(Particle particle) {
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
                for (Particle particle : particles) {
                    particle.velocity = Vec3f.ZERO;
                }
            }
        }

        private void createAndAddParticles(Material particleMaterial, Point3D origin) {
            particles = new ArrayList<>();
            for (int i = 0; i < PARTICLE_COUNT; ++i) {
                double radius = randomParticleRadius();
                Vec3f velocity = randomParticleVelocity();
                Particle particle = new Particle(radius, particleMaterial, velocity, origin);
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

        private Vec3f randomParticleVelocity() {
            int xDir = RND.nextBoolean() ? -1 : 1;
            int yDir = RND.nextBoolean() ? -1 : 1;
            return new Vec3f(
                xDir * randomFloat(PARTICLE_SPEED_EXPLODING_XY_MIN, PARTICLE_SPEED_EXPLODING_XY_MAX),
                yDir * randomFloat(PARTICLE_SPEED_EXPLODING_XY_MIN, PARTICLE_SPEED_EXPLODING_XY_MAX),
                -randomFloat(PARTICLE_SPEED_EXPLODING_Z_MIN, PARTICLE_SPEED_EXPLODING_Z_MAX)
            );
        }
    }

    public EnergizerExplosionAndRecycling(
        AnimationRegistry animationRegistry,
        Point3D origin,
        ArcadeHouse3D house3D,
        Vector2f[] ghostRevivalPositionCenters,
        Group particlesGroupContainer,
        Material particleMaterial,
        Material[] ghostDressMaterials,
        Predicate<Particle> particleTouchesFloor) {

        super(animationRegistry, "Energizer_Explosion");
        this.origin = requireNonNull(origin);
        this.house3D = requireNonNull(house3D);
        this.ghostRevivalPositionCenters = requireNonNull(ghostRevivalPositionCenters);
        this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
        this.particleMaterial = requireNonNull(particleMaterial);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.particleTouchesFloor = requireNonNull(particleTouchesFloor);
        particlesGroupContainer.getChildren().add(particlesGroup);
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
            for (Particle particle : particles) {
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
        if (particleTouchesFloor != null) {
            particleTouchesFloor = null;
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
}