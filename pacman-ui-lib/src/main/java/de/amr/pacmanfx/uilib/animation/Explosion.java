/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.Vec3f;
import de.amr.pacmanfx.lib.Vector2f;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.effect.Bloom;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Random;
import java.util.function.Predicate;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomFloat;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

public class Explosion extends ManagedAnimation {

    private static final Duration EXPLOSION_DURATION = Duration.seconds(30);

    private static final short PARTICLE_DIVISIONS = 8;
    private static final short PARTICLE_COUNT_MIN = 200;
    private static final short PARTICLE_COUNT_MAX = 400;
    private static final float PARTICLE_MEAN_RADIUS_UNSCALED = .15f;
    private static final float PARTICLE_SPEED_XY_MIN = 0.05f;
    private static final float PARTICLE_SPEED_XY_MAX = 0.5f;
    private static final float PARTICLE_SPEED_Z_MIN = 2;
    private static final float PARTICLE_SPEED_Z_MAX = 8;
    private static final float PARTICLE_SPEED_MOVING_HOME_MIN = 0.4f;
    private static final float PARTICLE_SPEED_MOVING_HOME_MAX = 0.8f;

    private static final float GRAVITY_Z = 0.18f;

    public static class Particle extends Sphere {

        private boolean flying = true;
        private boolean glowing = false;
        private boolean movingIntoHouse = false;
        private Point3D targetPosition;

        private Vec3f velocity;

        public Particle(double radius, Material material, Vec3f velocity, Point3D origin) {
            super(radius, PARTICLE_DIVISIONS);
            this.velocity = velocity;
            setMaterial(material);
            Translate translate = new Translate(origin.getX(), origin.getY(), origin.getZ());
            getTransforms().add(translate);
        }

        public Point3D position() {
            Translate translate = (Translate) getTransforms().getFirst();
            return new Point3D(translate.getX(), translate.getY(), translate.getZ());
        }

        public void moveWithGravity() {
            move();
            velocity.z += GRAVITY_Z;
        }

        public void move() {
            Translate translate = (Translate) getTransforms().getFirst();
            translate.setX(translate.getX() + velocity.x);
            translate.setY(translate.getY() + velocity.y);
            translate.setZ(translate.getZ() + velocity.z);
        }

        public void setZ(double z) {
            Translate translate = (Translate) getTransforms().getFirst();
            translate.setZ(z);
        }
    }

    private final Random rnd = new Random();
    private Material particleMaterial;
    private Material[] ghostDressMaterials;
    private final Point3D origin;
    private final Vector2f[] ghostRevivalPositions;
    private final Group particlesGroupContainer;
    private final Group particlesGroup = new Group();
    private final Predicate<Particle> particleTouchesFloor;
    private Particle[] particles;

    private class ParticlesMovement extends Transition {

        public ParticlesMovement() {
            setCycleDuration(EXPLOSION_DURATION);
        }

        @Override
        protected void interpolate(double t) {
            for (Particle particle : particles) {
                if (particle.flying) {
                    if (particleTouchesFloor.test(particle)) {
                        particle.flying = false;
                        particle.setZ(-particle.getRadius()); // just on floor
                        particle.setRadius(0.2); //TODO make something more intelligent
                    }
                }
                if (particle.flying) {
                    particle.moveWithGravity();
                    // if falling under certain height, start glowing etc.
                    if (particle.velocity.z > 0 && particle.position().getZ() > -20) {
                        startGlowing(particle);
                        setDebrisMaterial(particle);
                    }
                } else {
                    moveIntoHouse(particle);
                }
            }
        }

        private void startGlowing(Particle particle) {
            if (!particle.glowing) {
                Bloom bloom = new Bloom();
                particle.setEffect(bloom);
                particle.glowing = true;
            }
        }

        private void setDebrisMaterial(Particle particle) {
            int index = randomInt(0, 4);
            particle.setMaterial(ghostDressMaterials[index]);
        }

        private void moveIntoHouse(Particle particle) {
            if (!particle.movingIntoHouse) {
                Point3D position = particle.position();
                int personality = rnd.nextInt(4);
                Vector2f revivalPosition = ghostRevivalPositions[personality];
                float minX = revivalPosition.x() + 1, maxX = revivalPosition.x() + TS - 1;
                float minY = revivalPosition.y() + 1, maxY = revivalPosition.y() + TS - 1;
                particle.targetPosition = new Point3D(
                        minX + rnd.nextDouble(maxX - minX),
                        minY + rnd.nextDouble(maxY - minY),
                        position.getZ()
                );
                float speed = rnd.nextFloat(PARTICLE_SPEED_MOVING_HOME_MIN, PARTICLE_SPEED_MOVING_HOME_MAX);
                particle.velocity = new Vec3f(
                    (float) (particle.targetPosition.getX() - position.getX()),
                    (float) (particle.targetPosition.getY() - position.getY()),
                    0
                ).normalize().multiply(speed);
                particle.setMaterial(ghostDressMaterials[personality]);
                particle.movingIntoHouse = true;
            }
            if (particle.position().distance(particle.targetPosition) < PARTICLE_SPEED_MOVING_HOME_MAX) {
                particle.velocity = Vec3f.ZERO;
            } else {
                particle.move();
            }
        }

        @Override
        public void play() {
            replaceParticles(particleMaterial, origin);
            super.play();
        }

        private void replaceParticles(Material particleMaterial, Point3D origin) {
            var stopWatch = new StopWatch();
            int particleCount = randomInt(PARTICLE_COUNT_MIN, PARTICLE_COUNT_MAX + 1);
            particles = new Particle[particleCount];
            for (int i = 0; i < particleCount; ++i) {
                double radius = randomParticleRadius();
                Vec3f velocity = randomParticleVelocity();
                particles[i] = new Particle(radius, particleMaterial, velocity, origin);
                particles[i].setVisible(true);
            }
            particlesGroup.getChildren().setAll(particles);
            Logger.info("{} particles created in {0.000} milliseconds", particleCount, stopWatch.passedMillis());
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
                xDir * randomFloat(PARTICLE_SPEED_XY_MIN, PARTICLE_SPEED_XY_MAX),
                yDir * randomFloat(PARTICLE_SPEED_XY_MIN, PARTICLE_SPEED_XY_MAX),
                -randomFloat(PARTICLE_SPEED_Z_MIN, PARTICLE_SPEED_Z_MAX)
            );
        }
    }

    public Explosion(
        AnimationRegistry animationRegistry,
        Point3D origin,
        Vector2f[] ghostRevivalPositions,
        Group particlesGroupContainer,
        Material particleMaterial,
        Material[] ghostDressMaterials,
        Predicate<Particle> particleTouchesFloor) {

        super(animationRegistry, "Energizer_Explosion");
        this.origin = requireNonNull(origin);
        this.ghostRevivalPositions = requireNonNull(ghostRevivalPositions);
        this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
        this.particleMaterial = requireNonNull(particleMaterial);
        this.ghostDressMaterials = requireNonNull(ghostDressMaterials);
        this.particleTouchesFloor = requireNonNull(particleTouchesFloor);
        particlesGroupContainer.getChildren().add(particlesGroup);
    }

    @Override
    protected Animation createAnimationFX() {
        var particlesMovement = new Explosion.ParticlesMovement();
        particlesMovement.setDelay(Duration.millis(200));
        return particlesMovement;
    }

    @Override
    protected void freeResources() {
        if (particles == null) return;
        for (Particle particle : particles) {
            particle.setMaterial(null);
        }
        Logger.info("Disposed {} particles", particles.length);
        particles = null;
        particlesGroup.getChildren().clear();
        particlesGroupContainer.getChildren().remove(particlesGroup);
        particleMaterial = null;
        ghostDressMaterials = null;
    }
}