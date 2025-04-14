/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.level;

import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Scale;

import static de.amr.games.pacman.Globals.assertNonNegative;
import static de.amr.games.pacman.uilib.Ufx.doAfterSec;
import static java.util.Objects.requireNonNull;

/**
 * 3D pellet.
 *
 * @author Armin Reichert
 */
public class Pellet3D implements Eatable3D {

    private final Shape3D shape;

    public Pellet3D(Shape3D shape, double radius) {
        this.shape = requireNonNull(shape);
        assertNonNegative(radius, "Pellet3D radius must be positive but is %f");
        var bounds = shape.getBoundsInLocal();
        var max = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        var scaling = new Scale(2 * radius / max, 2 * radius / max, 2 * radius / max);
        shape.getTransforms().add(scaling);
    }

    @Override
    public Shape3D shape3D() {
        return shape;
    }

    @Override
    public void onEaten() {
        doAfterSec(0.05, () -> shape.setVisible(false)).play();
    }

    @Override
    public String toString() {
        return String.format("[Pellet3D tile=%s]", tile());
    }
}