/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.scene.Group;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;

public class PacBody extends Group implements DisposableGraphicsObject {

    // TODO Adapt mesh position and rotation in OBJ file
    public PacBody(PacConfig pacConfig, Mesh headMesh, Mesh eyesMesh, Mesh palateMesh) {
        final var head = new MeshView(headMesh);
        head.setMaterial(coloredPhongMaterial(pacConfig.colors().head()));

        final var eyes = new MeshView(eyesMesh);
        eyes.setMaterial(coloredPhongMaterial(pacConfig.colors().eyes()));

        final var palate = new MeshView(palateMesh);
        palate.setMaterial((coloredPhongMaterial(pacConfig.colors().palate())));

        getChildren().addAll(head, eyes, palate);

        final var headBounds = head.getBoundsInLocal();
        final var centeredOverOrigin = new Translate(-headBounds.getCenterX(), -headBounds.getCenterY(), -headBounds.getCenterZ());
        Stream.of(head, eyes, palate).forEach(mv -> mv.getTransforms().add(centeredOverOrigin));

        getTransforms().add(new Rotate(90,  Rotate.X_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        final var bounds = getBoundsInLocal();
        final float size = pacConfig.size3D();
        final var scaleToSize = new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
        getTransforms().add(scaleToSize);
    }

    @Override
    public void dispose() {
        cleanupGroup(this, true);
    }
}