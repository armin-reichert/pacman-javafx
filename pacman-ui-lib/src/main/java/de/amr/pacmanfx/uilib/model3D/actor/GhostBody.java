/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;

public class GhostBody extends Group implements DisposableGraphicsObject {

    public GhostBody(
        Mesh dressMesh, Mesh pupilsMesh, Mesh eyeballsMesh,
        double size, Color dressColor, double rotateY)
    {
        final MeshView dressMeshView = new MeshView(dressMesh);
        dressMeshView.setMaterial(coloredPhongMaterial(dressColor));

        final MeshView pupilsMeshView = new MeshView(pupilsMesh);
        pupilsMeshView.setMaterial(coloredPhongMaterial(Color.BLUE));

        final MeshView eyeballsMeshView = new MeshView(eyeballsMesh);
        eyeballsMeshView.setMaterial(coloredPhongMaterial(Color.WHITE));

        final var dressGroup = new Group(dressMeshView);
        final var eyesGroup = new Group(pupilsMeshView, eyeballsMeshView);
        getChildren().addAll(dressGroup, eyesGroup);

        final Bounds dressBounds = dressMeshView.getBoundsInLocal();
        final Bounds bounds = getBoundsInLocal();
        final var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());

        dressMeshView.getTransforms().add(centeredOverOrigin);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        getTransforms().add(new Rotate(270, Rotate.X_AXIS));
        getTransforms().add(new Rotate(rotateY, Rotate.Y_AXIS));
        getTransforms().add(new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth()));
    }

    @Override
    public void dispose() {
        cleanupGroup(this, true);
    }
}