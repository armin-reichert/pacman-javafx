/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;

public class PacBodyNoEyes extends Group implements DisposableGraphicsObject {

    public PacBodyNoEyes(PacConfig pacConfig, Mesh headMesh, Mesh palateMesh) {
        final PhongMaterial headMaterial = coloredPhongMaterial(pacConfig.colors().head());
        final var headMeshView = new MeshView(headMesh);
        headMeshView.setMaterial(headMaterial);

        final PhongMaterial palateMaterial = coloredPhongMaterial(pacConfig.colors().palate());
        final var palateMeshView = new MeshView(palateMesh);
        palateMeshView.setMaterial(palateMaterial);

        final var bounds = headMeshView.getBoundsInLocal();
        final var centeredOverOrigin = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
        Stream.of(headMeshView, palateMeshView).forEach(node -> node.getTransforms().add(centeredOverOrigin));

        getChildren().addAll(headMeshView, palateMeshView);

        // TODO check/fix Pac-Man mesh position and rotation in OBJ file
        getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        final var extent = getBoundsInLocal();
        final float size = pacConfig.size3D();
        getTransforms().add(new Scale(size / extent.getWidth(), size / extent.getHeight(), size / extent.getDepth()));
    }

    @Override
    public void dispose() {
        cleanupGroup(this, true);
    }
}