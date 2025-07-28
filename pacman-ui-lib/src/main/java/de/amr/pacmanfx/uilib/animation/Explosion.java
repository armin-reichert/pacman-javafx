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
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;

import static de.amr.pacmanfx.lib.UsefulFunctions.randomFloat;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

public class Explosion extends ManagedAnimation {

    private static final short PARTICLE_DIVISIONS = 8;
    private static final short PARTICLE_COUNT_MIN = 200;
    private static final short PARTICLE_COUNT_MAX = 400;
    private static final float PARTICLE_MEAN_RADIUS_UNSCALED = .15f;
    private static final float PARTICLE_VELOCITY_XY_MIN = -0.6f;
    private static final float PARTICLE_VELOCITY_XY_MAX =  0.6f;
    private static final float PARTICLE_VELOCITY_Z_MIN  = -4.5f;
    private static final float PARTICLE_VELOCITY_Z_MAX  = -1.5f;
    private static final float GRAVITY_Z = 0.1f;

    private static class MutableVec3f {
        public float x, y, z;

        public MutableVec3f(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private class Particle extends Sphere {

        private MutableVec3f velocity;

        public Particle(double radius, Material material, MutableVec3f velocity, Point3D origin) {
            super(radius, PARTICLE_DIVISIONS);
            this.velocity = velocity;
            setMaterial(material);
            Translate position = new Translate(origin.getX(), origin.getY(), origin.getZ());
            getTransforms().setAll(position);
        }

        public void move() {
            Translate position = (Translate) getTransforms().getFirst();
            position.setX(position.getX() + velocity.x);
            position.setY(position.getY() + velocity.y);
            position.setZ(position.getZ() + velocity.z);
            velocity.z += GRAVITY_Z;
        }

        public void vanish(double t) {
            setScaleZ(0.02);
            setScaleX(1-t);
            setScaleY(1-t);

        }
    }

    private class ParticlesMovement extends Transition {
        public ParticlesMovement() {
            setCycleDuration(Duration.seconds(5));
        }

        @Override
        protected void interpolate(double t) {
            for (int i = 0; i < particleCount; ++i) {
                Translate translate = (Translate) particles[i].getTransforms().getFirst();
                Point3D position = new Point3D(translate.getX(), translate.getY(), translate.getZ());
                if (particleAtEndPosition.test(position)) {
                    particles[i].vanish(t);
                } else {
                    particles[i].move();
                }
            }
        }

        @Override
        public void play() {
            for (int i = 0; i < particleCount; ++i) {
                particles[i].setVisible(true);
            }
            super.play();
        }
    }

    private final Group particlesGroupContainer;
    private Group particlesGroup = new Group();
    private Predicate<Point3D> particleAtEndPosition;

    private int particleCount;
    private Particle[] particles;

    public Explosion(
        AnimationRegistry animationRegistry,
        Point3D origin,
        Group particlesGroupContainer,
        Material particleMaterial,
        Predicate<Point3D> particleAtEndPosition) {

        super(animationRegistry, "Energizer_Explosion");

        this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
        this.particleAtEndPosition = requireNonNull(particleAtEndPosition);

        Random rnd = new Random();
        var stopWatch = new StopWatch();

        particleCount = randomInt(PARTICLE_COUNT_MIN, PARTICLE_COUNT_MAX + 1);
        particles = new Particle[particleCount];
        for (int i = 0; i < particleCount; ++i) {
            double radius = randomRadius(rnd);
            var velocity = new MutableVec3f(
                randomFloat(PARTICLE_VELOCITY_XY_MIN, PARTICLE_VELOCITY_XY_MAX),
                randomFloat(PARTICLE_VELOCITY_XY_MIN, PARTICLE_VELOCITY_XY_MAX),
                randomFloat(PARTICLE_VELOCITY_Z_MIN, PARTICLE_VELOCITY_Z_MAX)
            );
            particles[i] = new Particle(radius, particleMaterial, velocity, origin);
            particles[i].setVisible(false);
        }
        Logger.info("{} particles created in {0.000} milliseconds", particleCount, stopWatch.passedMillis());

        stopWatch.reset();
        particlesGroup.getChildren().addAll(Arrays.copyOfRange(particles, 0, particleCount));
        Logger.info("Adding {} particles to the scene graph took {0.000} milliseconds", particleCount, stopWatch.passedMillis());

        animationFX = new Explosion.ParticlesMovement();
        animationFX.setDelay(Duration.millis(200));
        animationFX.setOnFinished(e -> disposeParticles());
    }

    private double randomRadius(Random rnd) {
        double scaling = rnd.nextGaussian(2, 0.1);
        scaling = Math.clamp(scaling, 0.5, 4);
        return scaling * PARTICLE_MEAN_RADIUS_UNSCALED;
    }

    @Override
    protected Animation createAnimationFX() {
        return animationFX;
    }

    @Override
    public void playFromStart() {
        super.playFromStart();
        particlesGroupContainer.getChildren().add(particlesGroup);
    }

    @Override
    protected void freeResources() {
        disposeParticles();
        particlesGroupContainer.getChildren().remove(particlesGroup);
        particlesGroup = null;
        particleAtEndPosition = null;
    }

    private void disposeParticles() {
        if (particleCount != 0) {
            for (int i = 0; i < particleCount; ++i) {
                particles[i].setMaterial(null);
            }
            Logger.info("Disposed {} particles", particleCount);
            particles = null;
            particleCount = 0;
            particlesGroup.getChildren().clear();
        }
    }
}