/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.StopWatch;
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

import static de.amr.pacmanfx.lib.UsefulFunctions.randomFloat;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

public class Explosion extends ManagedAnimation {

    private static final Duration EXPLOSION_DURATION = Duration.seconds(30);

    private static final short PARTICLE_DIVISIONS = 8;
    private static final short PARTICLE_COUNT_MIN = 200;
    private static final short PARTICLE_COUNT_MAX = 400;
    private static final float PARTICLE_MEAN_RADIUS_UNSCALED = .15f;
    private static final float PARTICLE_VELOCITY_XY_MIN = -2f;
    private static final float PARTICLE_VELOCITY_XY_MAX =  2f;
    private static final float PARTICLE_VELOCITY_Z_MIN  = -8f;
    private static final float PARTICLE_VELOCITY_Z_MAX  = -2f;
    private static final float GRAVITY_Z = 0.18f;

    public static class Velocity {
        public float x, y, z;

        public Velocity(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class Particle extends Sphere {

        private boolean flying = true;
        private boolean glowing = false;

        private final Velocity velocity;

        public Particle(double radius, Material material, Velocity velocity, Point3D origin) {
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

        public void move() {
            Translate translate = (Translate) getTransforms().getFirst();
            translate.setX(translate.getX() + velocity.x);
            translate.setY(translate.getY() + velocity.y);
            translate.setZ(translate.getZ() + velocity.z);
            velocity.z += GRAVITY_Z;
        }
    }

    private Material particleMaterial;
    private Material[] debrisMaterial = new Material[5];
    private final Point3D origin;
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
                    particle.move();
                    // if falling under certain height, start glowing etc.
                    if (particle.velocity.z > 0 && particle.position().getZ() > -20) {
                        startGlowing(particle);
                        setRandomDebrisMaterial(particle);
                    }
                } else {
                    setRandomDebrisMaterial(particle);
                }
            }
        }

        private void startGlowing(Particle particle) {
            if (!particle.glowing) {
                Bloom bloom = new Bloom();
                particle.setEffect(bloom);
            }
        }

        private void setRandomDebrisMaterial(Particle particle) {
            particle.setMaterial(debrisMaterial[randomInt(0, debrisMaterial.length)]);
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
                Velocity velocity = randomParticleVelocity();
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

        private Velocity randomParticleVelocity() {
            return new Velocity(
                randomFloat(PARTICLE_VELOCITY_XY_MIN, PARTICLE_VELOCITY_XY_MAX),
                randomFloat(PARTICLE_VELOCITY_XY_MIN, PARTICLE_VELOCITY_XY_MAX),
                randomFloat(PARTICLE_VELOCITY_Z_MIN, PARTICLE_VELOCITY_Z_MAX)
            );
        }
    }

    public Explosion(
        AnimationRegistry animationRegistry,
        Point3D origin,
        Group particlesGroupContainer,
        Material particleMaterial,
        Predicate<Particle> particleAtEndPosition) {

        super(animationRegistry, "Energizer_Explosion");
        this.origin = requireNonNull(origin);
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