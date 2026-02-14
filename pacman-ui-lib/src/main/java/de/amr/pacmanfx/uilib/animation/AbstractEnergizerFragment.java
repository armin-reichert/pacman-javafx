/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;

import static java.util.Objects.requireNonNull;

/**
 * Base class for 3D particle fragments used in the energizer explosion animation.
 * <p>
 * A fragment is represented by a {@link Shape3D} and moves freely in 3D space
 * according to its velocity vector. Subclasses define the concrete geometry
 * and size of the fragment. The fragment may participate in special animation
 * modes such as being attracted to the ghost house or swirling inside it.
 * <p>
 * Fragments are lightweight and disposable. Subclasses must implement
 * {@link #shape()}, {@link #size()} and {@link #setSize(double)}.
 */
public abstract class AbstractEnergizerFragment implements Disposable {

    /**
     * Tests whether a sphere intersects an axis-aligned bounding box (AABB).
     * <p>
     * This uses the standard algorithm:
     * <ol>
     *   <li>Clamp the sphere center to the AABB.</li>
     *   <li>Compute the squared distance to the clamped point.</li>
     *   <li>Intersection occurs if the distance is less than or equal to the squared radius.</li>
     * </ol>
     *
     * @param sphereCenter the sphere center in the same coordinate system as the AABB
     * @param radius       the sphere radius
     * @param boxMin       the minimum AABB corner
     * @param boxMax       the maximum AABB corner
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
     * The animation state of a fragment.
     * <ul>
     *   <li>{@code FLYING}: The fragment moves freely according to its velocity.</li>
     *   <li>{@code ATTRACTED}: The fragment is pulled toward the ghost house.</li>
     *   <li>{@code INSIDE_SWIRL}: The fragment moves inside the ghost-house swirl effect.</li>
     * </ul>
     */
    public enum FragmentState { FLYING, ATTRACTED, INSIDE_SWIRL }

    /** Current animation state of the fragment. */
    private FragmentState state = FragmentState.FLYING;

    private Group targetSwirlGroup;

    /**
     * Target position inside the ghost house. Used when the fragment is in {@link FragmentState#INSIDE_SWIRL}.
     */
    private Point3D targetPosition;

    /** Current velocity of the fragment in 3D space. Immutable vector. */
    private Vector3f velocity = Vector3f.ZERO;

    /**
     * Returns the 3D shape representing this fragment.
     *
     * @return the fragment's {@link Shape3D}
     */
    public abstract Shape3D shape();

    /**
     * Sets the fragment's visual size. The meaning of “size” is defined by the concrete subclass
     * (e.g., diameter of a sphere).
     *
     * @param size the new size value
     */
    public abstract void setSize(double size);

    /**
     * Returns the fragment's visual size. The meaning of “size” is defined by the concrete subclass
     * (e.g., diameter of a sphere).
     *
     * @return the fragment size
     */
    public abstract double size();

    /**
     * Sets the fragment's velocity.
     *
     * @param velocity a non-null velocity vector
     */
    public void setVelocity(Vector3f velocity) {
        this.velocity = requireNonNull(velocity);
    }

    /**
     * @return the current velocity of this fragment
     */
    public Vector3f velocity() {
        return velocity;
    }

    /**
     * Sets the fragment's animation state.
     *
     * @param state the new state (non-null)
     */
    public void setState(FragmentState state) {
        this.state = requireNonNull(state);
    }

    /**
     * @return the current animation state of this fragment
     */
    public FragmentState state() {
        return state;
    }

    public Group targetSwirlGroup() {
        return targetSwirlGroup;
    }

    public void setTargetSwirlGroup(Group targetSwirlGroup) {
        this.targetSwirlGroup = targetSwirlGroup;
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
     * Checks whether this fragment intersects the given box.
     * <p>
     * The fragment is approximated as a sphere with radius {@code size() / 2}.
     * The box is treated as an axis-aligned bounding box (AABB) in the fragment's
     * parent coordinate system. The box center is obtained via
     * {@link Box#localToParent(Point3D)} to account for parent transforms.
     *
     * @param box the box to test against
     * @return {@code true} if the fragment intersects the box, {@code false} otherwise
     */
    public boolean collidesWith(Box box) {
        final Shape3D shape = shape();

        //TODO use localToParent(Point3D.ZERO) here too?
        final Point3D shapeCenter = new Point3D(shape.getTranslateX(), shape.getTranslateY(), shape.getTranslateZ());

        // Box center in parent coordinates (accounts for parent transforms)
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
     * Applies gravity to the fragment and advances its position.
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
     * Moves the fragment according to its current velocity by updating the
     * translation coordinates of its underlying {@link Shape3D}.
     */
    public void move() {
        final Shape3D shape = shape();
        shape.setTranslateX(shape.getTranslateX() + velocity.x());
        shape.setTranslateY(shape.getTranslateY() + velocity.y());
        shape.setTranslateZ(shape.getTranslateZ() + velocity.z());
    }
}
