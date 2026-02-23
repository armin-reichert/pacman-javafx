/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.geometry.Point3D;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;

import static java.util.Objects.requireNonNull;

/**
 * Base class for 3D particle fragments used in the energizer explosion animation.
 * <p>
 * A particle is represented by a concrete {@link Shape3D} instance and moves freely in
 * three-dimensional space according to its current velocity. Subclasses define the
 * concrete geometry (e.g., sphere, box, mesh) and how its size is interpreted.
 * <p>
 * During the explosion sequence, a particle may transition through several motion states:
 * <ul>
 *   <li><b>FLYING</b> – free movement under its velocity and optional gravity</li>
 *   <li><b>ATTRACTED</b> – movement toward a target position (e.g., the ghost house)</li>
 *   <li><b>INSIDE_SWIRL</b> – circular or spiral motion inside the ghost-house swirl</li>
 *   <li><b>OUT_OF_WORLD</b> – particle has left the valid simulation space and can be removed</li>
 * </ul>
 * <p>
 * This class stores only the simulation state of a particle. Rendering is delegated to
 * the {@link Shape3D} returned by {@link #shape()}. Particles are lightweight and can be
 * disposed via {@link #dispose()} when no longer needed.
 * <p>
 * Subclasses must implement {@link #shape()}, {@link #size()}, and {@link #setSize(double)}
 * to define the concrete geometry.
 */
public abstract class EnergizerParticle implements Disposable {

    /**
     * Tests whether a sphere intersects an axis-aligned bounding box (AABB).
     * <p>
     * The algorithm:
     * <ol>
     *   <li>Clamp the sphere center to the AABB.</li>
     *   <li>Compute the squared distance between the sphere center and the clamped point.</li>
     *   <li>Intersection occurs if the squared distance is less than or equal to {@code radius²}.</li>
     * </ol>
     *
     * @param sphereCenter the sphere center in the same coordinate system as the AABB
     * @param radius       the sphere radius
     * @param boxMin       minimum AABB corner
     * @param boxMax       maximum AABB corner
     * @return {@code true} if the sphere intersects the AABB
     */
    public static boolean intersectsSphereAABB(Point3D sphereCenter, double radius, Point3D boxMin, Point3D boxMax) {

        final double x = Math.clamp(sphereCenter.getX(), boxMin.getX(), boxMax.getX());
        final double y = Math.clamp(sphereCenter.getY(), boxMin.getY(), boxMax.getY());
        final double z = Math.clamp(sphereCenter.getZ(), boxMin.getZ(), boxMax.getZ());

        final double dx = sphereCenter.getX() - x;
        final double dy = sphereCenter.getY() - y;
        final double dz = sphereCenter.getZ() - z;

        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    /**
     * Motion states of a particle during the explosion animation.
     */
    public enum FragmentState {
        /** Free flight under velocity and gravity. */
        FLYING,
        /** Movement toward a target position (e.g., ghost house). */
        ATTRACTED,
        /** Circular or spiral motion inside the ghost-house swirl. */
        INSIDE_SWIRL,
        /** Particle has left the simulation space and should be removed. */
        OUT_OF_WORLD
    }

    private Shape3D shape3D;

    /** Current motion state of this particle. */
    private FragmentState state = FragmentState.FLYING;

    /** Index used by the swirl system to assign a swirl target or orbit slot. */
    private int targetSwirlIndex;

    /** Target position used in {@link FragmentState#INSIDE_SWIRL}. */
    private Point3D targetPosition;

    /** Current velocity of the particle in 3D space. */
    private Vector3f velocity = Vector3f.ZERO;

    /** Current angular position of the particle (used for swirl motion). */
    private double angle;

    protected EnergizerParticle(Shape3D shape3D) {
        this.shape3D = requireNonNull(shape3D);
    }

    /**
     * Returns the 3D shape representing this particle.
     *
     * @return the particle's {@link Shape3D}
     */
    public Shape3D shape() {
        return shape3D;
    }

    protected void setShape3D(Shape3D shape3D) {
        this.shape3D = requireNonNull(shape3D);
    }

    /**
     * Sets the angular position of this particle.
     *
     * @param angle angle in radians
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * @return the current angular position of this particle in radians
     */
    public double angle() {
        return angle;
    }

    /**
     * Sets the visual size of this particle. The meaning of “size” depends on the concrete subclass
     * (e.g., sphere diameter, box edge length).
     *
     * @param size the new size value
     */
    public abstract void setSize(double size);

    /**
     * Returns the visual size of this particle. The meaning of “size” depends on the concrete subclass.
     *
     * @return the particle size
     */
    public abstract double size();

    /**
     * Sets the particle's velocity.
     *
     * @param velocity a velocity vector
     */
    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    /**
     * @return the current velocity of this particle
     */
    public Vector3f velocity() {
        return velocity;
    }

    /**
     * Sets the particle's motion state.
     *
     * @param state the new state (non-null)
     */
    public void setState(FragmentState state) {
        this.state = requireNonNull(state);
    }

    /**
     * @return the current motion state of this particle
     */
    public FragmentState state() {
        return state;
    }

    /**
     * @return the swirl target index used by the swirl system
     */
    public int targetSwirlIndex() {
        return targetSwirlIndex;
    }

    /**
     * Sets the swirl target index.
     *
     * @param index the new index
     */
    public void setTargetSwirlIndex(int index) {
        this.targetSwirlIndex = index;
    }

    /**
     * @return the target position inside the ghost house, or {@code null} if not set
     */
    public Point3D targetPosition() {
        return targetPosition;
    }

    /**
     * Sets the target position inside the ghost house.
     *
     * @param point the target position (may be {@code null})
     */
    public void setTargetPosition(Point3D point) {
        targetPosition = point;
    }

    /**
     * Checks whether this particle intersects the given box.
     * <p>
     * The particle is approximated as a sphere with radius {@code size() / 2}.
     * The box is treated as an axis-aligned bounding box (AABB) in parent coordinates.
     *
     * @param box the box to test against
     * @return {@code true} if the particle intersects the box
     */
    public boolean collidesWith(Box box) {
        final Shape3D shape = shape();

        // Approximate particle center (translation only)
        final Point3D shapeCenter = new Point3D(
            shape.getTranslateX(),
            shape.getTranslateY(),
            shape.getTranslateZ()
        );

        // Box center in parent coordinates
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

        return intersectsSphereAABB(shapeCenter, 0.5 * size(), boxMin, boxMax);
    }

    /**
     * Applies gravity to the particle and advances its position.
     * Gravity is added to the current velocity, then {@link #move()} is invoked.
     *
     * @param gravity the gravity vector (non-null)
     */
    public void fly(Vector3f gravity) {
        requireNonNull(gravity);
        velocity = velocity.add(gravity);
        move();
    }

    /**
     * Moves the particle according to its current velocity by updating the translation
     * coordinates of its underlying {@link Shape3D}.
     */
    public void move() {
        final Shape3D shape = shape();
        shape.setTranslateX(shape.getTranslateX() + velocity.x());
        shape.setTranslateY(shape.getTranslateY() + velocity.y());
        shape.setTranslateZ(shape.getTranslateZ() + velocity.z());
    }
}
