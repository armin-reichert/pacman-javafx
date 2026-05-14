/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.List;

import static java.util.Objects.requireNonNull;

public interface Model3DHelper {

    static MeshView createMeshView(Mesh mesh, PhongMaterial material) {
        requireNonNull(mesh);
        requireNonNull(material);
        final MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    static void centerOverOrigin(Node master, List<Node> slaves) {
        requireNonNull(master);
        requireNonNull(slaves);
        final Bounds b = master.getBoundsInLocal();
        final var centerTranslate = new Translate(-b.getCenterX(), -b.getCenterY(), -b.getCenterZ());
        master.getTransforms().add(centerTranslate);
        slaves.stream().map(Node::getTransforms).forEach(tf -> tf.add(centerTranslate));
    }

    static <T extends Node> T scale(T shape, float size) {
        requireNonNull(shape);
        final Bounds b = shape.getBoundsInLocal();
        shape.getTransforms().add(new Scale(size / b.getWidth(), size / b.getHeight(), size / b.getDepth()));
        return shape;
    }

    Rotate PAC_ORIENTATION_ADJUSTMENT = new Rotate(270, Rotate.X_AXIS);

    /**
     * Rotates Pac-Man / the used ghost to the orientation required by the 3D play scene.
     */
    static <T extends Node> T adjustOrientation(T node) {
        requireNonNull(node);
        node.getTransforms().add(PAC_ORIENTATION_ADJUSTMENT);
        return node;
    }
}
