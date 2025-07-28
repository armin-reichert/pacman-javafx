/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.StopWatch;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static de.amr.pacmanfx.lib.UsefulFunctions.randomFloat;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

public class Explosion extends ManagedAnimation {

    private static final short PARTICLE_DIVISIONS = 8;
    private static final short PARTICLE_COUNT_MIN = 150;
    private static final short PARTICLE_COUNT_MAX = 300;
    private static final float PARTICLE_MEAN_RADIUS_UNSCALED = .15f;
    private static final float PARTICLE_VELOCITY_XY_MIN = -0.6f;
    private static final float PARTICLE_VELOCITY_XY_MAX = 0.6f;
    private static final float PARTICLE_VELOCITY_Z_MIN  = -4.5f;
    private static final float PARTICLE_VELOCITY_Z_MAX  = -1.5f;
    private static final float GRAVITY_Z = 0.1f;

    public class Particle extends Sphere implements Disposable {
        private float vx, vy, vz;

        public Particle(double radius) {
            super(radius, PARTICLE_DIVISIONS);
            setMaterial(particleMaterial);
        }

        public void setVelocity(float x, float y, float z) {
            vx = x;
            vy = y;
            vz = z;
        }

        @Override
        public void dispose() {
            setMaterial(null);
        }
    }

    private class ParticlesMovement extends Transition {

        public ParticlesMovement() {
            Random rnd = new Random();
            setOnFinished(e -> disposeParticles());
            setCycleDuration(Duration.seconds(5));
            int numParticles = randomInt(PARTICLE_COUNT_MIN, PARTICLE_COUNT_MAX + 1);
            for (int i = 0; i < numParticles; ++i) {
                double scaling = Math.clamp(rnd.nextGaussian(2, 0.1), 0.5, 4);
                double radius = scaling * PARTICLE_MEAN_RADIUS_UNSCALED;
                var particle = new Particle(radius);
                particle.setVelocity(
                    randomFloat(PARTICLE_VELOCITY_XY_MIN, PARTICLE_VELOCITY_XY_MAX),
                    randomFloat(PARTICLE_VELOCITY_XY_MIN, PARTICLE_VELOCITY_XY_MAX),
                    randomFloat(PARTICLE_VELOCITY_Z_MIN, PARTICLE_VELOCITY_Z_MAX));
                particle.setTranslateX(origin.getTranslateX());
                particle.setTranslateY(origin.getTranslateY());
                particle.setTranslateZ(origin.getTranslateZ());
                particle.setVisible(false);
                particlesGroup.getChildren().add(particle);
            }
            Logger.info("{} particles created", particlesGroup.getChildren().size());
        }

        @Override
        protected void interpolate(double t) {
            for (Node child : particlesGroup.getChildren()) {
                Particle particle = (Particle) child;
                if (particleReachedEndPosition.test(particle)) {
                    // show flat and shrink over time
                    particle.setScaleZ(0.02);
                    particle.setScaleX(1-t);
                    particle.setScaleY(1-t);
                } else {
                    // move and fall to floor
                    particle.setTranslateX(particle.getTranslateX() + particle.vx);
                    particle.setTranslateY(particle.getTranslateY() + particle.vy);
                    particle.setTranslateZ(particle.getTranslateZ() + particle.vz);
                    particle.vz += GRAVITY_Z;
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

    private final Group particlesGroupContainer;
    private Group particlesGroup = new Group();
    private Predicate<Explosion.Particle> particleReachedEndPosition;
    private Node origin;
    private Material particleMaterial;

    public Explosion(
        AnimationRegistry animationRegistry,
        Node origin,
        Group particlesGroupContainer,
        Material particleMaterial,
        Predicate<Explosion.Particle> particleReachedEndPosition)
    {
        super(animationRegistry, "Energizer_Explosion");
        this.origin = requireNonNull(origin);
        this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
        this.particleMaterial = requireNonNull(particleMaterial);
        this.particleReachedEndPosition = requireNonNull(particleReachedEndPosition);

        var stopWatch = new StopWatch();
        animationFX = new Explosion.ParticlesMovement();
        Logger.info("Particles transition created in {} milliseconds", stopWatch.passedTime().toMillis());
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
        particleMaterial = null;
        origin = null;
        particleReachedEndPosition = null;
    }

    private void disposeParticles() {
        if (particlesGroup.getChildren().isEmpty()) return;
        List<Particle> particles = particlesGroup.getChildren().stream()
                .filter(Particle.class::isInstance).map(Particle.class::cast)
                .toList();
        particles.forEach(Particle::dispose);
        Logger.info("Disposed {} particles", particles.size());
        particlesGroup.getChildren().clear();
    }
}
