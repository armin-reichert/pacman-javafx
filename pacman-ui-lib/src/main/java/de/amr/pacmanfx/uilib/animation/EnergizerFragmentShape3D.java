/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;

/**
 * A small 3D particle used for the energizer explosion animation.
 * <p>
 * Each fragment is represented as a low‑poly {@link Shape3D} with its own
 * velocity vector. The fragment can move freely in 3D space and optionally
 * participate in special animation modes such as the "swirl" effect or
 * returning to the ghost house.
 * <p>
 * The fragment is lightweight and disposable. Calling {@link #dispose()}
 * releases its material reference so it can be garbage‑collected.
 */
public class EnergizerFragmentShape3D implements Disposable, EnergizerFragment {

    /** Number of subdivisions used for the sphere mesh. */
    private static final short SPHERE_DIVISIONS = 8;

    /** Whether this fragment is currently moving back toward the ghost house. */
    public boolean movingHome = false;

    /** Whether this fragment is currently part of the energizer swirl animation. */
    public boolean partOfSwirl = false;

    /**
     * Index of the ghost color associated with this fragment, or {@code -1}
     * if no color is associated.
     */
    public byte ghostColorIndex = -1;

    /** Target position inside the ghost house, used when {@link #movingHome} is true. */
    public Point3D houseTargetPosition;

    /** Current velocity of the fragment in 3D space. */
    public Vector3f velocity;

    private final Sphere shape;

    /**
     * Creates a new energizer fragment.
     *
     * @param radius   the sphere radius
     * @param material the material applied to the sphere
     * @param velocity the initial velocity vector
     * @param origin   the initial world position of the fragment
     */
    public EnergizerFragmentShape3D(double radius, Material material, Vector3f velocity, Point3D origin) {
        this.velocity = velocity;
        shape = new Sphere(radius, SPHERE_DIVISIONS);
        shape.setMaterial(material);
        shape.setTranslateX(origin.getX());
        shape.setTranslateY(origin.getY());
        shape.setTranslateZ(origin.getZ());
    }

    public Sphere shape() {
        return shape;
    }

    /**
     * Releases resources held by this fragment.
     * <p>
     * Currently this clears the material reference so the fragment can be
     * garbage‑collected. Additional cleanup may be added if needed.
     */
    @Override
    public void dispose() {
        shape.setMaterial(null);
    }

    /**
     * Applies gravity to the fragment and advances its position.
     *
     * @param gravity the gravity vector to add to the current velocity
     */
    @Override
    public void fly(Vector3f gravity) {
        move();
        velocity = velocity.add(gravity);
    }

    /**
     * Moves the fragment according to its current velocity.
     * <p>
     * This updates the translation coordinates in all three axes.
     */
    @Override
    public void move() {
        shape.setTranslateX(shape.getTranslateX() + velocity.x());
        shape.setTranslateY(shape.getTranslateY() + velocity.y());
        shape.setTranslateZ(shape.getTranslateZ() + velocity.z());
    }
}
