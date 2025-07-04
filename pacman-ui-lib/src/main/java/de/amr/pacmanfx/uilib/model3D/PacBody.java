/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.Ufx;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.stream.Stream;

public class PacBody extends Group {

    public PacBody(Model3DRepository model3DRepository, double size, Color headColor, Color eyesColor, Color palateColor) {
        var headMeshView = new MeshView(model3DRepository.pacHeadMesh());
        headMeshView.setMaterial(Ufx.coloredPhongMaterial(headColor));

        var eyesMeshView = new MeshView(model3DRepository.pacEyesMesh());
        eyesMeshView.setMaterial(Ufx.coloredPhongMaterial(eyesColor));

        var palateMeshView = new MeshView(model3DRepository.pacPalateMesh());
        palateMeshView.setMaterial(Ufx.coloredPhongMaterial(palateColor));

        getChildren().addAll(headMeshView, eyesMeshView, palateMeshView);

        var headBounds = headMeshView.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-headBounds.getCenterX(), -headBounds.getCenterY(), -headBounds.getCenterZ());
        Stream.of(headMeshView, eyesMeshView, palateMeshView).forEach(node -> node.getTransforms().add(centeredOverOrigin));

        // TODO check/fix Pac-Man mesh position and rotation in OBJ file
        getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        var bounds = getBoundsInLocal();
        getTransforms().add(new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth()));
    }

    public void destroy() {
        //TODO
        getChildren().clear();
    }
}
