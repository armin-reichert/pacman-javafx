/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.geometry.Point3D;
import javafx.scene.shape.Shape3D;

import static java.util.Objects.requireNonNull;

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
public abstract class EnergizerFragmentShape3D implements Disposable, EnergizerFragment {

    public enum FragmentState { FLYING, ATTRACTED_BY_HOUSE, INSIDE_SWIRL }

    private FragmentState state = FragmentState.FLYING;

    /**
     * Index of the ghost color associated with this fragment, or {@code -1}
     * if no color is associated.
     */
    private byte ghostColorIndex = -1;

    /** Target position inside the ghost house, used when state is {@link FragmentState#INSIDE_SWIRL}. */
    private Point3D houseTargetPosition;

    /** Current velocity of the fragment in 3D space. */
    private Vector3f velocity = Vector3f.ZERO;

    public abstract Shape3D shape();

    public void setVelocity(Vector3f velocity) {
        this.velocity = requireNonNull(velocity);
    }

    public Vector3f velocity() {
        return velocity;
    }

    public void setState(FragmentState state) {
        this.state = requireNonNull(state);
    }

    public FragmentState state() {
        return state;
    }

    public byte ghostColorIndex() {
        return ghostColorIndex;
    }

    public void setGhostColorIndex(byte index) {
        this.ghostColorIndex = Validations.requireValidGhostPersonality(index);
    }

    public Point3D houseTargetPosition() {
        return houseTargetPosition;
    }

    public void setHouseTargetPosition(Point3D point3D) {
        this.houseTargetPosition = point3D;
    }

    /**
     * Applies gravity to the fragment and advances its position.
     *
     * @param gravity the gravity vector to add to the current velocity
     */
    @Override
    public void fly(Vector3f gravity) {
        requireNonNull(gravity);
        velocity = velocity.add(gravity);
        move();
    }

    /**
     * Moves the fragment according to its current velocity.
     * <p>
     * This updates the translation coordinates in all three axes.
     */
    @Override
    public void move() {
        final Shape3D shape = shape();
        shape.setTranslateX(shape.getTranslateX() + velocity.x());
        shape.setTranslateY(shape.getTranslateY() + velocity.y());
        shape.setTranslateZ(shape.getTranslateZ() + velocity.z());
    }
}
