/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.Vec3f;
import de.amr.pacmanfx.lib.Vector2f;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.effect.Bloom;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static de.amr.pacmanfx.lib.UsefulFunctions.randomFloat;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

public class EnergizerExplosionAndRecycling extends ManagedAnimation {

    public static final float PARTICLE_COLUMN_RADIUS = 4;
    public static final float PARTICLE_COLUMN_HEIGHT = 12;

    // Time includes movement of particles to the ghost house after the explosion
    private static final Duration TOTAL_DURATION = Duration.seconds(30);

    private static final short PARTICLE_DIVISIONS = 8;

    private static final short PARTICLE_COUNT_MIN = 250;
    private static final short PARTICLE_COUNT_MAX = 500;

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
        public byte personality = -1;
        public boolean debris = false;
        public boolean landed = false;
        public boolean movingHome = false;
        public boolean removed = false;
        public Point3D housePosition;
        public Vec3f velocity;

        public Particle(double radius, Material material, Vec3f velocity, Point3D origin) {
            super(radius, PARTICLE_DIVISIONS);
            this.velocity = velocity;
            setMaterial(material);
            setTranslateX(origin.getX());
            setTranslateY(origin.getY());
            setTranslateZ(origin.getZ());
        }

        @Override
        public void dispose() {
            setMaterial(null);
            setTranslateX(0);
            setTranslateY(0);
            setTranslateZ(0);
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

    private final Random rnd = new Random();

    private Point3D origin;
    private Vector2f[] ghostRevivalPositionCenters;
    private Group particlesGroupContainer;
    private Group particlesGroup = new Group();
    private Predicate<Particle> particleTouchesFloor;
    private Group[] particleColumns;

    private Material particleMaterial;
    private Material[] ghostDressMaterials;
    private Particle[] particles;

    private class ParticlesMovement extends Transition {

        public ParticlesMovement() {
            setCycleDuration(TOTAL_DURATION);
        }

        @Override
        protected void interpolate(double t) {
            List<Particle> particlesToRemove = new ArrayList<>();
            for (Particle particle : particles) {
                if (particle.removed) continue;

                if (particle.landed) {
                    moveHome(particle);
                }
                else {
                    particle.fly();
                    // if falling under certain height, become debris, change color to one of ghost dress colors
                    // Note: falling means moving to positive z direction!
                    if (particle.velocity.z > 0 && particle.center().getZ() > -20) {
                        becomeDebris(particle);
                    }
                    if (particleTouchesFloor.test(particle)) {
                        particle.setRadius(PARTICLE_RADIUS_RETURNING_HOME);
                        particle.setTranslateZ(-particle.getRadius());
                        particle.landed = true;
                    }
                    // if felt outside world, remove it at some level
                    if (!particle.landed && particle.center().getZ() > 100) {
                        particle.velocity = null;
                        particle.setMaterial(null);
                        particle.removed = true;
                        particlesToRemove.add(particle);
                        Logger.debug(() -> "%s removed (felt outside), z=%.2f".formatted(particle, particle.getTranslateZ()));
                    }
                }
            }
            if (!particlesToRemove.isEmpty()) {
                particlesGroup.getChildren().removeAll(particlesToRemove);
                Logger.info("{} particles removed at t={}", particlesToRemove.size(), t);
                particlesToRemove.forEach(Particle::dispose);
                particlesToRemove.clear();
            }
        }

        private void becomeDebris(Particle particle) {
            if (particle.debris) return;
            particle.setMaterial(ghostDressMaterials[randomInt(0, 4)]);
            Bloom bloom = new Bloom();
            bloom.setThreshold(0.5);
            particle.setEffect(bloom);
            particle.debris = true;
        }

        /**
         * @param baseCenter center of base circle
         * @param r radius
         * @param h height
         * @return random point on lateral surface of cylinder
         */
        public Point3D randomPointOnLateralSurface(Point3D baseCenter, double r, double h) {
            double angle = Math.toRadians(rnd.nextInt(360));
            return new Point3D(
                baseCenter.getX() + r * Math.cos(angle),
                baseCenter.getY() + r * Math.sin(angle),
                rnd.nextDouble(h)
            );
        }

        private void moveHome(Particle particle) {
            Point3D particleCenter = particle.center();
            if (!particle.movingHome) {
                // first time: compute particle "personality", target point and velocity
                particle.personality = (byte) rnd.nextInt(4);
                particle.setMaterial(ghostDressMaterials[particle.personality]);

                Point3D baseCenter = new Point3D(
                    ghostRevivalPositionCenters[particle.personality].x(),
                    ghostRevivalPositionCenters[particle.personality].y(),
                    0); // floor top is at z=0!
                particle.housePosition = randomPointOnLateralSurface(baseCenter, 6, 10);

                float speed = rnd.nextFloat(PARTICLE_SPEED_MOVING_HOME_MIN, PARTICLE_SPEED_MOVING_HOME_MAX);
                particle.velocity.x = (float) (particle.housePosition.getX() - particleCenter.getX());
                particle.velocity.y = (float) (particle.housePosition.getY() - particleCenter.getY());
                particle.velocity.z = 0;
                particle.velocity.normalize().multiply(speed);

                particle.movingHome = true;
            }
            // if target reached, move particle to its column group
            double distXY = Math.hypot(
                particleCenter.getX() - particle.housePosition.getX(),
                particleCenter.getY() - particle.housePosition.getY());
            if (distXY < particle.velocity.magnitude()) {
                particle.velocity = Vec3f.ZERO;
                particlesGroup.getChildren().remove(particle); //TODO collect and remove?
                particleColumns[columnIndex(particle.personality)].getChildren().add(particle);
                particle.setTranslateX(PARTICLE_COLUMN_RADIUS * Math.cos(rnd.nextDouble(Math.TAU)));
                particle.setTranslateY(PARTICLE_COLUMN_RADIUS * Math.sin(rnd.nextDouble(Math.TAU)));
                particle.setTranslateZ(-rnd.nextDouble(PARTICLE_COLUMN_HEIGHT));
            } else {
                particle.move();
            }
        }

        private int columnIndex(int personality) {
            return switch (personality) {
                case Globals.CYAN_GHOST_BASHFUL -> 0;
                case Globals.RED_GHOST_SHADOW, Globals.PINK_GHOST_SPEEDY -> 1;
                case Globals.ORANGE_GHOST_POKEY -> 2;
                default -> throw new IllegalArgumentException();
            };
        }

        @Override
        public void play() {
            createAndAddParticles(particleMaterial, origin);
            super.play();
        }

        private void createAndAddParticles(Material particleMaterial, Point3D origin) {
            int particleCount = randomInt(PARTICLE_COUNT_MIN, PARTICLE_COUNT_MAX + 1);
            particles = new Particle[particleCount];
            for (int i = 0; i < particleCount; ++i) {
                double radius = randomParticleRadius();
                Vec3f velocity = randomParticleVelocity();
                particles[i] = new Particle(radius, particleMaterial, velocity, origin);
                particles[i].setVisible(true);
            }
            particlesGroup.getChildren().setAll(particles);
        }

        private double randomParticleRadius() {
            double scaling = rnd.nextGaussian(2, 0.1);
            scaling = Math.clamp(scaling, 0.5, 4);
            return scaling * PARTICLE_MEAN_RADIUS_UNSCALED;
        }

        private Vec3f randomParticleVelocity() {
            int xDir = rnd.nextBoolean() ? -1 : 1;
            int yDir = rnd.nextBoolean() ? -1 : 1;
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
        Vector2f[] ghostRevivalPositionCenters,
        Group particlesGroupContainer,
        Group[] particleColumns,
        Material particleMaterial,
        Material[] ghostDressMaterials,
        Predicate<Particle> particleTouchesFloor) {

        super(animationRegistry, "Energizer_Explosion");
        this.origin = requireNonNull(origin);
        this.ghostRevivalPositionCenters = requireNonNull(ghostRevivalPositionCenters);
        this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
        this.particleColumns = requireNonNull(particleColumns);
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
            Logger.info("Disposed {} particles", particles.length);
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