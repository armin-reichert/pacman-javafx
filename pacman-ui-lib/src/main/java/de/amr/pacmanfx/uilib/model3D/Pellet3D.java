/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Scale;

import static de.amr.pacmanfx.uilib.Ufx.pauseSec;
import static java.util.Objects.requireNonNull;

/**
 * 3D pellet.
 */
public class Pellet3D implements Eatable3D {

    private Shape3D shape;

    public Pellet3D(Shape3D shape, Scale scale) {
        this.shape = requireNonNull(shape);
        shape.getTransforms().add(scale);
    }

    public void destroy() {
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
        // small delay for better visualization
        pauseSec(0.05, () -> shape.setVisible(false)).play();
    }

    @Override
    public String toString() {
        return String.format("[Pellet3D tile=%s]", tile());
    }
}