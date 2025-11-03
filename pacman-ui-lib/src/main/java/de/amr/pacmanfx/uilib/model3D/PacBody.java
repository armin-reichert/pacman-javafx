/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.Ufx.defaultPhongMaterial;

public class PacBody extends Group implements Disposable {

    private PhongMaterial headMaterial;
    private PhongMaterial eyesMaterial;
    private PhongMaterial palateMaterial;

    private MeshView headMeshView;
    private MeshView eyesMeshView;
    private MeshView palateMeshView;

    public PacBody(
        double size,
        Mesh headMesh, Color headColor,
        Mesh eyesMesh, Color eyesColor,
        Mesh palateMesh, Color palateColor)
    {
        headMaterial = defaultPhongMaterial(headColor);
        headMeshView = new MeshView(headMesh);
        headMeshView.setMaterial(headMaterial);

        eyesMaterial = defaultPhongMaterial(eyesColor);
        eyesMeshView = new MeshView(eyesMesh);
        eyesMeshView.setMaterial(eyesMaterial);

        palateMaterial = defaultPhongMaterial(palateColor);
        palateMeshView = new MeshView(palateMesh);
        palateMeshView.setMaterial(palateMaterial);

        getChildren().addAll(headMeshView, eyesMeshView, palateMeshView);

        var headBounds = headMeshView.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-headBounds.getCenterX(), -headBounds.getCenterY(), -headBounds.getCenterZ());
        Stream.of(headMeshView, eyesMeshView, palateMeshView)
            .map(MeshView::getTransforms)
            .forEach(tf -> tf.add(centeredOverOrigin));

        // TODO check/fix Pac-Man mesh position and rotation in OBJ file
        getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        var bounds = getBoundsInLocal();
        getTransforms().add(new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth()));
    }

    @Override
    public void dispose() {
        getChildren().clear();
        if (headMeshView != null) {
            headMeshView.setMesh(null);
            headMeshView.setMaterial(null);
            headMeshView = null;
        }
        headMaterial = null;
        if (eyesMeshView != null) {
            eyesMeshView.setMesh(null);
            eyesMeshView.setMaterial(null);
            eyesMeshView = null;
        }
        eyesMaterial = null;
        if (palateMeshView != null) {
            palateMeshView.setMesh(null);
            palateMeshView.setMaterial(null);
            palateMeshView = null;
        }
        palateMaterial = null;
    }
}