/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Destroyable;
import de.amr.pacmanfx.lib.Vector3f;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Random;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomFloat;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

public abstract class SquirtingAnimation extends Transition implements Destroyable {

    record Range(float from, float to) {}

    private static final float MEAN_PARTICLE_RADIUS = 0.3f;

    private static final Range PARTICLE_VELOCITY_XY = new Range(-0.2f, 0.2f);
    private static final Range PARTICLE_VELOCITY_Z  = new Range(-3f, -0.5f);

    private static final Vector3f GRAVITY = new Vector3f(0, 0, 0.1f);

    public static class Particle extends Sphere {
        private Vector3f velocity = Vector3f.ZERO;

        public Particle(Material material, double radius, Point3D origin) {
            super(radius);
            setMaterial(material);
            setTranslateX(origin.getX());
            setTranslateY(origin.getY());
            setTranslateZ(origin.getZ());
        }

        public void setVelocity(double x, double y, double z) {
            velocity = new Vector3f(x, y, z);
        }

        public void move() {
            setTranslateX(getTranslateX() + velocity.x());
            setTranslateY(getTranslateY() + velocity.y());
            setTranslateZ(getTranslateZ() + velocity.z());
            velocity = velocity.add(GRAVITY);
        }
    }

    private final Group particlesGroup = new Group();

    public abstract boolean particleReachedEndPosition(Particle particle);

    public SquirtingAnimation(
        Duration duration,
        int minParticleCount, int maxParticleCount,
        Material particleMaterial,
        Point3D origin)
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
            double radius = rnd.nextGaussian(MEAN_PARTICLE_RADIUS, 0.25);
            var particle = new Particle(particleMaterial, radius, origin);
            particle.setVelocity(
                randomFloat(PARTICLE_VELOCITY_XY.from(), PARTICLE_VELOCITY_XY.to()),
                randomFloat(PARTICLE_VELOCITY_XY.from(), PARTICLE_VELOCITY_XY.to()),
                randomFloat(PARTICLE_VELOCITY_Z.from(), PARTICLE_VELOCITY_Z.to()));
            particle.setVisible(false);
            particlesGroup.getChildren().add(particle);
        }
        Logger.info("{} particles created", particlesGroup.getChildren().size());
    }

    public Group particlesGroup() {
        return particlesGroup;
    }

    @Override
    public void destroy() {
        particlesGroup.getChildren().forEach(p -> ((Particle)p).setMaterial(null));
        particlesGroup.getChildren().clear();
    }

    @Override
    public void play() {
        for (var particle : particlesGroup.getChildren()) {
            particle.setVisible(true);
        }
        super.play();
    }

    @Override
    protected void interpolate(double t) {
        for (var child : particlesGroup.getChildren()) {
            Particle particle = (Particle) child;
            if (particleReachedEndPosition(particle)) {
                particle.setScaleZ(0.02); // flat
            } else {
                particle.move();
            }
        }
    }
}