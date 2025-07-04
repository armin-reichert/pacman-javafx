/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * 3D pellet.
 */
public class Pellet3D implements Eatable3D {

    private Shape3D shape;
    private Animation hideAfterSmallDelay;

    public Pellet3D(Shape3D shape, double radius) {
        this.shape = requireNonNull(shape);
        requireNonNegative(radius, "Pellet3D radius must be positive but is %f");
        Bounds bounds = shape.getBoundsInLocal();
        double diameter = 2 * radius;
        double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        shape.getTransforms().add(new Scale(diameter / maxExtent, diameter / maxExtent, diameter / maxExtent));

        hideAfterSmallDelay = new PauseTransition(Duration.seconds(0.05));
        hideAfterSmallDelay.setOnFinished(e -> shape.setVisible(false));
    }

    public void destroy() {
        hideAfterSmallDelay.stop();
        hideAfterSmallDelay = null;
        if (shape instanceof MeshView meshView) {
            meshView.setMesh(null);
            shape = null;
        }
    }

    @Override
    public Shape3D shape3D() {
        return shape;
    }

    @Override
    public void onEaten() {
        hideAfterSmallDelay.play();
    }

    @Override
    public String toString() {
        return String.format("[Pellet3D tile=%s]", tile());
    }
}