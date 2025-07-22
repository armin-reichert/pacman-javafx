/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;

import static de.amr.pacmanfx.uilib.Ufx.pauseSec;
import static java.util.Objects.requireNonNull;

/**
 * 3D pellet.
 */
public class Pellet3D implements Eatable3D {

    private static final PseudoClass PELLET3D_CLASS = PseudoClass.getPseudoClass("pellet3d");

    public static boolean isPellet3D(Node node) {
        return node.getPseudoClassStates().contains(PELLET3D_CLASS);
    }

    private Shape3D shape;

    public Pellet3D(Shape3D shape) {
        this.shape = requireNonNull(shape);
        shape.pseudoClassStateChanged(PELLET3D_CLASS, true);
        shape.setCache(true); // TODO does this bring an advantage at all?
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
        pauseSec(0.05, () -> {
            shape.setVisible(false);
            if (shape.getParent() instanceof Group group) {
                group.getChildren().remove(shape);
            }
        }).play();
    }

    @Override
    public String toString() {
        return String.format("[Pellet3D tile=%s]", tile());
    }
}