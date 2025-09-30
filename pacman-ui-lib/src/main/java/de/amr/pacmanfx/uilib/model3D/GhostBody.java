/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import static de.amr.pacmanfx.uilib.Ufx.defaultPhongMaterial;

public class GhostBody extends Group implements Disposable {
    private PhongMaterial dressMaterial;
    private PhongMaterial pupilsMaterial;
    private PhongMaterial eyeballsMaterial;
    private MeshView dressMeshView;
    private MeshView pupilsMeshView;
    private MeshView eyeballsMeshView;

    public GhostBody(Model3DRepository model3DRepository, double size, Color dressColor, double rotateY) {
        dressMaterial = defaultPhongMaterial(dressColor);
        dressMeshView = new MeshView(model3DRepository.ghostDressMesh());
        dressMeshView.setMaterial(dressMaterial);
        Bounds dressBounds = dressMeshView.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dressMeshView.getTransforms().add(centeredOverOrigin);

        pupilsMaterial = defaultPhongMaterial(Color.BLUE);
        pupilsMeshView = new MeshView(model3DRepository.ghostPupilsMesh());
        pupilsMeshView.setMaterial(pupilsMaterial);

        eyeballsMaterial = defaultPhongMaterial(Color.WHITE);
        eyeballsMeshView = new MeshView(model3DRepository.ghostEyeballsMesh());
        eyeballsMeshView.setMaterial(eyeballsMaterial);

        var dressGroup = new Group(dressMeshView);
        var eyesGroup = new Group(pupilsMeshView, eyeballsMeshView);
        eyesGroup.getTransforms().add(centeredOverOrigin);
        getChildren().addAll(dressGroup, eyesGroup);

        getTransforms().add(new Rotate(270, Rotate.X_AXIS));
        getTransforms().add(new Rotate(rotateY, Rotate.Y_AXIS));
        Bounds bounds = getBoundsInLocal();
        getTransforms().add(new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth()));
    }

    @Override
    public void dispose() {
        getChildren().clear();
        if (dressMeshView != null) {
            dressMeshView.setMesh(null);
            dressMeshView.setMaterial(null);
            dressMeshView = null;
            dressMaterial = null;
        }
        if (pupilsMeshView != null) {
            pupilsMeshView.setMesh(null);
            pupilsMeshView.setMaterial(null);
            pupilsMeshView = null;
            pupilsMaterial = null;
        }
        if (eyeballsMeshView != null) {
            eyeballsMeshView.setMesh(null);
            eyeballsMeshView.setMaterial(null);
            eyeballsMeshView = null;
            eyeballsMaterial = null;
        }
    }
}
