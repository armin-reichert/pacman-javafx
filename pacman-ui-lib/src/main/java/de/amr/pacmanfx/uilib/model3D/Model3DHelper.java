/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import static java.util.Objects.requireNonNull;

public interface Model3DHelper {

    static MeshView createMeshView(Mesh mesh, PhongMaterial material) {
        requireNonNull(mesh);
        requireNonNull(material);
        final MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    static Translate translateToOrigin(Node node) {
        requireNonNull(node);
        final Bounds b = node.getBoundsInLocal();
        return new Translate(-b.getCenterX(), -b.getCenterY(), -b.getCenterZ());
    }

    static Scale scaleTo(Node node, float size) {
        requireNonNull(node);
        final Bounds b = node.getBoundsInLocal();
        return new Scale(size / b.getWidth(), size / b.getHeight(), size / b.getDepth());
    }
}
