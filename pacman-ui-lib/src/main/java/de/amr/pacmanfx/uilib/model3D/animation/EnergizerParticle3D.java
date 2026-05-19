/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.geometry.Point3D;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;

import static java.util.Objects.requireNonNull;

/**
 * Base class for 3D particle fragments used in the energizer explosion animation.
 */
public class EnergizerParticle3D {

    public enum ParticleState {
        /** Flight under velocity and gravity. */
        FLYING_THROUGH_AIR,
        /** Movement toward a target position (e.g., ghost house). */
        ATTRACTED_BY_SWIRL,
        /** Particle has left the simulation space and should be removed. */
        OUT_OF_WORLD
    }

    private final Sphere sphere;

    private ParticleState state = ParticleState.FLYING_THROUGH_AIR;

    private byte targetSwirlIndex;

    private Vector3f position;

    private Vector3f velocity;

    private float angle;

    public EnergizerParticle3D(double radius, PhongMaterial material, Vector3f initialPosition) {
        sphere = new Sphere(radius, 8);
        sphere.setMaterial(material);
        setPosition(initialPosition);
        setVelocity(Vector3f.ZERO);
    }

    public void reset() {
        state = ParticleState.FLYING_THROUGH_AIR;
        targetSwirlIndex = -1;
        position = Vector3f.ZERO;
        velocity = Vector3f.ZERO;
    }

    public Sphere shape() {
        return sphere;
    }

    /**
     * Sets the angular position of this particle.
     *
     * @param angle angle in radians
     */
    public void setAngle(double angle) {
        this.angle = (float) angle;
    }

    /**
     * @return the current angular position of this particle in radians
     */
    public float angle() {
        return angle;
    }

    /**
     * Returns the particle’s current simulation position.
     *
     * @return the particle position
     */
    public Vector3f pos() {
        return position;
    }

    /**
     * Sets the particle’s simulation position and updates the shape’s translation.
     *
     * @param point the new position (non-null)
     */
    public void setPosition(Vector3f point) {
        this.position = requireNonNull(point);
        updateShapeTranslate();
    }

    /**
     * Sets the particle’s velocity.
     *
     * @param velocity a velocity vector (non-null)
     */
    public void setVelocity(Vector3f velocity) {
        this.velocity = requireNonNull(velocity);
    }

    /**
     * @return the current velocity of this particle
     */
    public Vector3f velocity() {
        return velocity;
    }

    /**
     * Sets the particle’s motion state.
     *
     * @param state the new state (non-null)
     */
    public void setState(ParticleState state) {
        this.state = requireNonNull(state);
    }

    /**
     * @return the current motion state of this particle
     */
    public ParticleState state() {
        return state;
    }

    /**
     * @return the swirl target index used by the swirl system
     */
    public byte targetSwirlIndex() {
        return targetSwirlIndex;
    }

    /**
     * Sets the swirl target index.
     *
     * @param index the new index
     */
    public void setTargetSwirlIndex(byte index) {
        this.targetSwirlIndex = index;
    }

    public boolean collidesWith(Box box) {
        final Point3D shapeCenter = new Point3D(position.x(), position.y(), position.z());

        final Point3D boxOrigin = box.localToParent(Point3D.ZERO);

        final Point3D boxMin = new Point3D(
            boxOrigin.getX() - 0.5 * box.getWidth(),
            boxOrigin.getY() - 0.5 * box.getHeight(),
            boxOrigin.getZ() - 0.5 * box.getDepth()
        );

        final Point3D boxMax = new Point3D(
            boxOrigin.getX() + 0.5 * box.getWidth(),
            boxOrigin.getY() + 0.5 * box.getHeight(),
            boxOrigin.getZ() + 0.5 * box.getDepth()
        );

        return Ufx.intersectsSphereAABB(shapeCenter, sphere.getRadius(), boxMin, boxMax);
    }

    /**
     * Applies gravity to the particle by adding the gravity vector to its velocity,
     * then moves the particle.
     *
     * @param gravity the gravity vector (non-null)
     */
    public void fly(Vector3f gravity) {
        requireNonNull(gravity);
        velocity = velocity.add(gravity);
        move();
    }

    /**
     * Moves the particle according to its current velocity by integrating its position
     * and updating the shape’s translation.
     */
    public void move() {
        position = position.add(velocity);
        updateShapeTranslate();
    }

    private void updateShapeTranslate() {
        sphere.setTranslateX(position.x());
        sphere.setTranslateY(position.y());
        sphere.setTranslateZ(position.z());
    }
}
