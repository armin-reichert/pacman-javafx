/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.Ufx.defaultPhongMaterial;

public class PacBodyNoEyes extends Group implements Disposable {

    private MeshView headMeshView;
    private MeshView palateMeshView;
    private PhongMaterial headMaterial;
    private PhongMaterial palateMaterial;

    public PacBodyNoEyes(PacManModel3DRepository model3DRepository, double size, Color headColor, Color palateColor) {
        headMaterial = defaultPhongMaterial(headColor);
        headMeshView = new MeshView(model3DRepository.pacHeadMesh());
        headMeshView.setMaterial(headMaterial);

        palateMaterial = defaultPhongMaterial(palateColor);
        palateMeshView = new MeshView(model3DRepository.pacPalateMesh());
        palateMeshView.setMaterial(palateMaterial);

        var bounds = headMeshView.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
        Stream.of(headMeshView, palateMeshView).forEach(node -> node.getTransforms().add(centeredOverOrigin));

        getChildren().addAll(headMeshView, palateMeshView);

        // TODO check/fix Pac-Man mesh position and rotation in OBJ file
        getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        var rootBounds = getBoundsInLocal();
        getTransforms().add(new Scale(size / rootBounds.getWidth(), size / rootBounds.getHeight(), size / rootBounds.getDepth()));
    }

    @Override
    public void dispose() {
        getChildren().clear();
        if (headMeshView != null) {
            headMeshView.setMaterial(null);
            headMeshView.setMesh(null);
            headMeshView = null;
        }
        headMaterial = null;
        if (palateMeshView != null) {
            palateMeshView.setMaterial(null);
            palateMeshView.setMesh(null);
            palateMeshView = null;
        }
        palateMaterial = null;
    }
}
