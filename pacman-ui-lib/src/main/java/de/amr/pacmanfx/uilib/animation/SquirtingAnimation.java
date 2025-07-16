/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Destroyable;
import de.amr.pacmanfx.lib.Vector3f;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomFloat;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

/**
 * Animation played when energizer explodes.
 */
public abstract class SquirtingAnimation extends Transition implements Destroyable {

    private static final float MIN_PARTICLE_RADIUS = 0.1f;
    private static final float MAX_PARTICLE_RADIUS =  1.0f;
    private static final Vector3f MIN_PARTICLE_VELOCITY = new Vector3f(-0.25f, -0.25f, -4.0f);
    private static final Vector3f MAX_PARTICLE_VELOCITY = new Vector3f(0.25f, 0.25f, -1.0f);
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

    private Group embeddingParent;
    private Group particleGroup = new Group();

    public abstract boolean particleShouldVanish(Particle particle);

    public SquirtingAnimation(
        Group embeddingParent,
        Duration duration,
        int minParticleCount, int maxParticleCount,
        Material particleMaterial,
        Point3D origin)
    {
        this.embeddingParent = requireNonNull(embeddingParent);
        requireNonNull(duration);
        requireNonNegativeInt(minParticleCount);
        requireNonNegativeInt(maxParticleCount);
        requireNonNull(particleMaterial);
        requireNonNull(origin);

        setCycleDuration(duration);
        setOnFinished(e -> embeddingParent.getChildren().remove(particleGroup));
        statusProperty().addListener(this::handleStatusChange);

        int numParticles = randomInt(minParticleCount, maxParticleCount + 1);
        for (int i = 0; i < numParticles; ++i) {
            float radius = randomFloat(MIN_PARTICLE_RADIUS, MAX_PARTICLE_RADIUS);
            var particle = new Particle(particleMaterial, radius, origin);
            particle.setVisible(false);
            particle.setVelocity(
                randomFloat(MIN_PARTICLE_VELOCITY.x(), MAX_PARTICLE_VELOCITY.x()),
                randomFloat(MIN_PARTICLE_VELOCITY.y(), MAX_PARTICLE_VELOCITY.y()),
                randomFloat(MIN_PARTICLE_VELOCITY.z(), MAX_PARTICLE_VELOCITY.z()));
            particleGroup.getChildren().add(particle);
        }
        embeddingParent.getChildren().add(particleGroup);
        Logger.info("{} particles created", particleGroup.getChildren().size());
    }

    private void handleStatusChange(ObservableValue<? extends Animation.Status> property, Animation.Status oldStatus, Animation.Status newStatus) {
        if (oldStatus == Animation.Status.RUNNING) {
            embeddingParent.getChildren().remove(particleGroup);
        }
    }

    @Override
    public void destroy() {
        setOnFinished(null);
        statusProperty().removeListener(this::handleStatusChange);
        for (Node child : particleGroup.getChildren()) {
            if (child instanceof Sphere sphere) {
                sphere.setMaterial(null);
            }
        }
        particleGroup.getChildren().clear();
        particleGroup = null;
        embeddingParent = null;
    }

    @Override
    protected void interpolate(double t) {
        if (particleGroup != null) {
            for (Node child : particleGroup.getChildren()) {
                var particle = (Particle) child;
                particle.setVisible(true);
                if (particleShouldVanish(particle)) {
                    particle.setVelocity(0, 0, 0);
                    particle.setScaleZ(0.1);
                } else {
                    particle.move();
                }
            }
        }
    }
}