/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.House;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.effect.Bloom;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
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
    private static final float PARTICLE_SPEED_MOVING_HOME_MIN = 0.2f;
    private static final float PARTICLE_SPEED_MOVING_HOME_MAX = 0.4f;

    private static final float GRAVITY_Z = 0.18f;

    // As there is no *mutable* 3d vector class in standard JDK or JavaFX...
    public static class Vec3f {
        public float x, y, z;

        static Vec3f ZERO = new Vec3f(0, 0, 0);

        public Vec3f(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vec3f normalize() {
            float maga = (float) Math.sqrt(x * x + y * y + z * z);
            if (maga > 0) {
                return multiply(1f / maga);
            } else {
                x = y = z = 0; // Point3D.normalized() also return zero vector, so be it
            }
            return this;
        }

        public Vec3f multiply(float s) {
            x *= s;
            y *= s;
            z *= s;
            return this;
        }
    }

    public static class Particle extends Sphere {

        private boolean flying = true;
        private boolean glowing = false;
        private boolean movingIntoHouse = false;
        private Point3D houseTarget;

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
    }

    private final Random rnd = new Random();
    private Material particleMaterial;
    private Material[] debrisMaterial = new Material[5];
    private final Point3D origin;
    private final Vector2f[] ghostRevivalPositions;
    private final Group particlesGroupContainer;
    private final Group particlesGroup = new Group();
    private final Predicate<Particle> particleAtEndPosition;
    private Particle[] particles;

    private class ParticlesMovement extends Transition {

        public ParticlesMovement() {
            setCycleDuration(EXPLOSION_DURATION);
        }

        @Override
        protected void interpolate(double t) {
            for (Particle particle : particles) {
                if (particle.flying) {
                    if (particleAtEndPosition.test(particle)) {
                        particle.flying = false;
                        particle.setRadius(0.2); //TODO make something more intelligent
                    }
                }
                if (particle.flying) {
                    particle.moveWithGravity();
                    // if falling under certain height, start glowing etc.
                    if (particle.velocity.z > 0 && particle.position().getZ() > -20) {
                        startGlowing(particle);
                        setRandomDebrisMaterial(particle);
                    }
                } else {
                    setRandomDebrisMaterial(particle);
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

        private void setRandomDebrisMaterial(Particle particle) {
            particle.setMaterial(debrisMaterial[randomInt(0, debrisMaterial.length)]);
        }

        private void moveIntoHouse(Particle particle) {
            if (!particle.movingIntoHouse) {
                particle.houseTarget = randomHousePosition(particle);
                Point3D position = particle.position();
                float speed = rnd.nextFloat(PARTICLE_SPEED_MOVING_HOME_MIN, PARTICLE_SPEED_MOVING_HOME_MAX);
                particle.velocity = new Vec3f(
                    (float) (particle.houseTarget.getX() - position.getX()),
                    (float) (particle.houseTarget.getY() - position.getY()),
                    0
                ).normalize().multiply(speed);
                particle.movingIntoHouse = true;
            }
            if (particle.position().distance(particle.houseTarget) < PARTICLE_SPEED_MOVING_HOME_MAX) {
                particle.velocity = Vec3f.ZERO;
            } else {
                particle.move();
            }
        }

        private Point3D randomHousePosition(Particle particle) {
            Vector2f randomRevivalPosition = ghostRevivalPositions[rnd.nextInt(ghostRevivalPositions.length)];
            float minX = randomRevivalPosition.x(), maxX = randomRevivalPosition.x() + TS;
            float minY = randomRevivalPosition.y(), maxY = randomRevivalPosition.y() + TS;
            return new Point3D(
                minX + rnd.nextDouble(maxX - minX),
                minY + rnd.nextDouble(maxY - minY),
                particle.position().getZ()
            );
        }

        @Override
        public void play() {
            replaceParticles(particleMaterial, origin);
            super.play();
        }

        private void replaceParticles(Material particleMaterial, Point3D origin) {
            Random rnd = new Random();
            var stopWatch = new StopWatch();
            int particleCount = randomInt(PARTICLE_COUNT_MIN, PARTICLE_COUNT_MAX + 1);
            particles = new Particle[particleCount];
            for (int i = 0; i < particleCount; ++i) {
                double radius = randomParticleRadius(rnd);
                Vec3f velocity = randomParticleVelocity();
                particles[i] = new Particle(radius, particleMaterial, velocity, origin);
                particles[i].setVisible(true);
            }
            particlesGroup.getChildren().setAll(particles);
            Logger.info("{} particles created in {0.000} milliseconds", particleCount, stopWatch.passedMillis());
        }

        private double randomParticleRadius(Random rnd) {
            double scaling = rnd.nextGaussian(2, 0.1);
            scaling = Math.clamp(scaling, 0.5, 4);
            return scaling * PARTICLE_MEAN_RADIUS_UNSCALED;
        }

        private Vec3f randomParticleVelocity() {
            Random rnd = new Random();
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
        Predicate<Particle> particleAtEndPosition) {

        super(animationRegistry, "Energizer_Explosion");
        this.origin = requireNonNull(origin);
        this.ghostRevivalPositions = requireNonNull(ghostRevivalPositions);
        this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
        this.particleMaterial = requireNonNull(particleMaterial);
        this.particleAtEndPosition = requireNonNull(particleAtEndPosition);
        particlesGroupContainer.getChildren().add(particlesGroup);

        for (int i = 0; i < debrisMaterial.length; ++i) {
            PhongMaterial material = new PhongMaterial();
            double hue = (360.0 * i) / debrisMaterial.length;
            material.setDiffuseColor(Color.hsb(hue, 1, 1));
            material.setSpecularColor(Color.hsb(hue, 0.5, 1));
            debrisMaterial[i] = material;
        }
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
        debrisMaterial = null;
    }
}