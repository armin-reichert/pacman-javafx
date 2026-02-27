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
 *
 * <p>A particle consists of:
 * <ul>
 *   <li>a concrete {@link Shape3D} instance used purely for rendering, and</li>
 *   <li>a simulation state (position, velocity, angle, motion state) that determines how the particle moves.</li>
 * </ul>
 *
 * <p>The particle moves freely in three-dimensional space according to its velocity. Subclasses define
 * the concrete geometry (e.g., sphere, box, mesh) and how its visual size is interpreted. The geometry
 * may be replaced at runtime (for example, to change mesh resolution); the particle automatically
 * re-applies its current position to the new shape.
 *
 * <p>During the explosion sequence, a particle may transition through several motion states:
 * <ul>
 *   <li><b>FLYING</b> – free movement under velocity and optional gravity</li>
 *   <li><b>ATTRACTED</b> – movement toward a target position (e.g., the ghost house)</li>
 *   <li><b>INSIDE_SWIRL</b> – circular or spiral motion inside the ghost-house swirl</li>
 *   <li><b>OUT_OF_WORLD</b> – particle has left the valid simulation space and can be removed</li>
 * </ul>
 *
 * <p>The particle’s position is maintained independently of the shape’s transforms. The shape’s
 * translation is updated whenever the particle moves or when the geometry is replaced.
 *
 * <p>Subclasses must implement {@link #size()} and {@link #setSize(double)} to define how the visual
 * size of the particle is interpreted.
 */
public abstract class EnergizerParticle implements Disposable {

    /**
     * Tests whether a sphere intersects an axis-aligned bounding box (AABB).
     *
     * <p>The algorithm:
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
        OUT_OF_VIEW
    }

    /** The render shape of this particle. */
    private Shape3D shape3D;

    /** Current motion state of this particle. */
    private FragmentState state = FragmentState.FLYING;

    /** Index used by the swirl system to assign a swirl target or orbit slot. */
    private byte targetSwirlIndex;

    /** Current position (center) of the particle in 3D space. */
    private Vector3f position;

    /** Current velocity of the particle in 3D space. */
    private Vector3f velocity;

    /** Current angular position of the particle (used for swirl motion). */
    private float angle;

    /**
     * Creates a particle with the given shape and initial position.
     *
     * @param shape3D         the render shape (non-null)
     * @param initialPosition the initial simulation position (non-null)
     */
    protected EnergizerParticle(Shape3D shape3D, Vector3f initialPosition) {
        this.shape3D = requireNonNull(shape3D);
        setPosition(initialPosition);
        setVelocity(Vector3f.ZERO);
    }

    /**
     * Creates a particle at the origin with the given shape.
     *
     * @param shape3D the render shape (non-null)
     */
    protected EnergizerParticle(Shape3D shape3D) {
        this(shape3D, Vector3f.ZERO);
    }

    public void reset() {
        state = FragmentState.FLYING;
        targetSwirlIndex = -1;
        position = Vector3f.ZERO;
        velocity = Vector3f.ZERO;
    }

    /**
     * Returns the 3D shape used to render this particle.
     *
     * @return the particle's {@link Shape3D}
     */
    public Shape3D shape() {
        return shape3D;
    }

    /**
     * Replaces the particle’s render shape. The new shape is positioned at the particle’s
     * current simulation position.
     *
     * @param shape3D the new render shape (non-null)
     */
    protected void setShape3D(Shape3D shape3D) {
        this.shape3D = requireNonNull(shape3D);
        updateShapeTranslate();
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
    public Vector3f position() {
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
     * Sets the particle’s motion state.
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

    /**
     * Checks whether this particle intersects the given box.
     *
     * <p>The particle is approximated as a sphere with radius {@code size() / 2}.
     * The box is treated as an axis-aligned bounding box (AABB) in parent coordinates.
     *
     * @param box the box to test against
     * @return {@code true} if the particle intersects the box
     */
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

        return intersectsSphereAABB(shapeCenter, 0.5 * size(), boxMin, boxMax);
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
        shape3D.setTranslateX(position.x());
        shape3D.setTranslateY(position.y());
        shape3D.setTranslateZ(position.z());
    }
}
