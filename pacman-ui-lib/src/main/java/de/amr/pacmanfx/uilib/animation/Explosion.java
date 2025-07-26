/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Disposable;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Random;
import java.util.function.Predicate;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomFloat;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

public class Explosion extends ManagedAnimation {

    private record FloatRange(float from, float to) {}

    private static final Duration DURATION = Duration.seconds(4);

    private static final byte PARTICLE_DIVISIONS = 8;
    private static final short PARTICLE_COUNT_MIN = 128;
    private static final short PARTICLE_COUNT_MAX = 512;
    private static final float PARTICLE_MEAN_RADIUS = .4f;
    private static final FloatRange PARTICLE_VELOCITY_XY = new FloatRange(-0.2f, 0.2f);
    private static final FloatRange PARTICLE_VELOCITY_Z  = new FloatRange(-4.5f, -1.5f);
    private static final float GRAVITY_Z = 0.1f;

    public static class Particle extends Sphere implements Disposable {
        private float vx, vy, vz;

        public Particle(Material material, double radius) {
            super(radius, PARTICLE_DIVISIONS);
            setMaterial(material);
        }

        public void setVelocity(float x, float y, float z) {
            vx = x;
            vy = y;
            vz = z;
        }

        public void move() {
            setTranslateX(getTranslateX() + vx);
            setTranslateY(getTranslateY() + vy);
            setTranslateZ(getTranslateZ() + vz);
            vz += GRAVITY_Z;
        }

        @Override
        public void dispose() {
            setMaterial(null);
        }
    }

    private final Group particlesGroupContainer;
    private Group particlesGroup = new Group();
    private Predicate<Particle> particleReachedEndPosition;
    private Node origin;
    private Material particleMaterial;

    private class ParticlesTransition extends Transition implements Disposable{

        ParticlesTransition(
            Duration duration,
            int minParticleCount,
            int maxParticleCount,
            Material particleMaterial,
            Node origin)
        {
            requireNonNull(duration);
            requireNonNegativeInt(minParticleCount);
            requireNonNegativeInt(maxParticleCount);
            requireNonNull(particleMaterial);
            requireNonNull(origin);

            setCycleDuration(duration);
            int numParticles = randomInt(minParticleCount, maxParticleCount + 1);
            Random rnd = new Random();
            for (int i = 0; i < numParticles; ++i) {
                double scaling = Math.clamp(rnd.nextGaussian(1, 0.2), 0.5, 2);
                double radius = scaling * PARTICLE_MEAN_RADIUS;
                var particle = new Particle(particleMaterial, radius);
                particle.setVelocity(
                    randomFloat(PARTICLE_VELOCITY_XY.from(), PARTICLE_VELOCITY_XY.to()),
                    randomFloat(PARTICLE_VELOCITY_XY.from(), PARTICLE_VELOCITY_XY.to()),
                    randomFloat(PARTICLE_VELOCITY_Z.from(), PARTICLE_VELOCITY_Z.to()));
                particle.setTranslateX(origin.getTranslateX());
                particle.setTranslateY(origin.getTranslateY());
                particle.setTranslateZ(origin.getTranslateZ());
                particle.setVisible(false);
                particlesGroup.getChildren().add(particle);
            }
            Logger.info("{} particles created", particlesGroup.getChildren().size());
        }

        @Override
        public void dispose() {
            particlesGroup.getChildren().stream()
                .filter(Particle.class::isInstance)
                .map(Particle.class::cast)
                .forEach(Particle::dispose);
        }

        @Override
        protected void interpolate(double t) {
            for (var child : particlesGroup.getChildren()) {
                Particle particle = (Particle) child;
                if (particleReachedEndPosition.test(particle)) {
                    particle.setScaleZ(0.02); // flat
                } else {
                    particle.move();
                }
            }
        }

        @Override
        public void play() {
            for (var particle : particlesGroup.getChildren()) {
                particle.setVisible(true);
            }
            super.play();
        }
    }

    public Explosion(
        AnimationRegistry animationRegistry,
        Node origin,
        Group particlesGroupContainer,
        Material particleMaterial,
        Predicate<Particle> particleReachedEndPosition)
    {
        super(animationRegistry, "Energizer_Explosion");
        this.origin = requireNonNull(origin);
        this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
        this.particleMaterial = requireNonNull(particleMaterial);
        this.particleReachedEndPosition = requireNonNull(particleReachedEndPosition);
    }

    @Override
    protected Animation createAnimation() {
        return new ParticlesTransition(DURATION, PARTICLE_COUNT_MIN, PARTICLE_COUNT_MAX, particleMaterial, origin);
    }

    @Override
    public void playFromStart() {
        super.playFromStart();
        particlesGroupContainer.getChildren().add(particlesGroup);
    }

    @Override
    public void stop() {
        super.stop();
        disposeParticles();
    }

    @Override
    protected void freeResources() {
        disposeParticles();
        particlesGroupContainer.getChildren().remove(particlesGroup);
        particlesGroup = null;
        particleMaterial = null;
        origin = null;
        particleReachedEndPosition = null;
    }

    private void disposeParticles() {
        if (particlesGroup.getChildren().isEmpty()) return;
        long count = particlesGroup.getChildren().stream().filter(Particle.class::isInstance).count();
        particlesGroup.getChildren().stream().filter(Particle.class::isInstance).map(Particle.class::cast).forEach(Particle::dispose);
        Logger.info("Disposed {} particles", count);
        particlesGroup.getChildren().clear();
    }
}
