/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.lib.UsefulFunctions.randomFloat;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

/**
 * Animation played when energizer explodes.
 */
public abstract class SquirtingAnimation extends Transition {

    public static class Particle extends Sphere {
        private float vx, vy, vz;

        public Particle(Material material, double radius, Point3D origin) {
            super(radius);
            setMaterial(material);
            setTranslateX(origin.getX());
            setTranslateY(origin.getY());
            setTranslateZ(origin.getZ());
        }

        public void setVelocity(double x, double y, double z) {
            vx = (float) x;
            vy = (float) y;
            vz = (float) z;
        }

        public void move(Point3D acceleration) {
            setTranslateX(getTranslateX() + vx);
            setTranslateY(getTranslateY() + vy);
            setTranslateZ(getTranslateZ() + vz);
            vx += (float) acceleration.getX();
            vy += (float) acceleration.getY();
            vz += (float) acceleration.getZ();
        }
    }

    private final Group embeddingParent;
    private final Group particleGroup = new Group();

    private static final float MIN_PARTICLE_RADIUS = 0.1f;
    private static final float MAX_PARTICLE_RADIUS =  1.0f;
    private static final Point3D MIN_PARTICLE_VELOCITY = new Point3D(-0.25f, -0.25f, -4.0f);
    private static final Point3D MAX_PARTICLE_VELOCITY = new Point3D(0.25f, 0.25f, -1.0f);
    private static final Point3D GRAVITY = new Point3D(0, 0, 0.1f);

    public SquirtingAnimation(
        Group embeddingParent,
        Duration duration,
        int minParticleCount, int maxParticleCount,
        Material particleMaterial,
        Point3D origin)
    {
        this.embeddingParent = requireNonNull(embeddingParent);
        setCycleDuration(duration);
        setOnFinished(e -> removeFromEmbeddingParent());
        statusProperty().addListener((py, ov, nv) -> {
            if (ov == Animation.Status.RUNNING) {
                removeFromEmbeddingParent();
            }
        });

        int numParticles = randomInt(minParticleCount, maxParticleCount + 1);
        for (int i = 0; i < numParticles; ++i) {
            float radius = randomFloat(MIN_PARTICLE_RADIUS, MAX_PARTICLE_RADIUS);
            var drop = new Particle(particleMaterial, radius, origin);
            drop.setVisible(false);
            drop.setVelocity(
                randomFloat((float) MIN_PARTICLE_VELOCITY.getX(), (float) MAX_PARTICLE_VELOCITY.getX()),
                randomFloat((float) MIN_PARTICLE_VELOCITY.getY(), (float) MAX_PARTICLE_VELOCITY.getY()),
                randomFloat((float) MIN_PARTICLE_VELOCITY.getZ(), (float) MAX_PARTICLE_VELOCITY.getZ()));
            particleGroup.getChildren().add(drop);
        }
        Logger.info("{} particles created", particleGroup.getChildren().size());
    }

    public Group node() {
        return particleGroup;
    }

    private void removeFromEmbeddingParent() {
        embeddingParent.getChildren().remove(particleGroup);
    }

    public void destroy() {
        particleGroup.getChildren().clear();
    }

    public abstract boolean particleShouldVanish(Particle particle);

    @Override
    protected void interpolate(double t) {
        if (t >= 1.0) {
            Logger.debug("Last interpolation frame t={}", t);
            return;
        }
        for (Node child : particleGroup.getChildren()) {
            var particle = (Particle) child;
            particle.setVisible(true);
            if (particleShouldVanish(particle)) {
                particle.setVelocity(0, 0, 0);
                particle.setScaleZ(0.1);
            } else {
                particle.move(GRAVITY);
            }
        }
    }
}