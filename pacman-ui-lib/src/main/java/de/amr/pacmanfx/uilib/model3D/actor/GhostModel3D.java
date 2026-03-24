/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.model3D.Model3DException;
import de.amr.pacmanfx.uilib.objimport.Model3D;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;

public class GhostModel3D implements Disposable {

	private static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	private static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
	private static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

	private final Model3D model3D;

	public GhostModel3D() {
		try {
			model3D = Model3D.fromWavefrontFile(getClass().getResource("/de/amr/pacmanfx/uilib/model3D/ghost.obj"));
		} catch (Model3DException x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public void dispose() {
		model3D.dispose();
	}

	public Mesh dressMesh() {
		return model3D.mesh(MESH_ID_GHOST_DRESS).orElseThrow();
	}

	public Mesh eyeballsMesh() {
		return model3D.mesh(MESH_ID_GHOST_EYEBALLS).orElseThrow();
	}

	public Mesh pupilsMesh() {
		return model3D.mesh(MESH_ID_GHOST_PUPILS).orElseThrow();
	}

	public Group createGhostBody(double size, Color dressColor, double rotateY) {
        final Group body = new Group();

        final MeshView dressMeshView = new MeshView(dressMesh());
        dressMeshView.setMaterial(coloredPhongMaterial(dressColor));

        final MeshView pupilsMeshView = new MeshView(pupilsMesh());
        pupilsMeshView.setMaterial(coloredPhongMaterial(Color.BLUE));

        final MeshView eyeballsMeshView = new MeshView(eyeballsMesh());
        eyeballsMeshView.setMaterial(coloredPhongMaterial(Color.WHITE));

        final var dressGroup = new Group(dressMeshView);
        final var eyesGroup = new Group(pupilsMeshView, eyeballsMeshView);
        body.getChildren().addAll(dressGroup, eyesGroup);

        final Bounds dressBounds = dressMeshView.getBoundsInLocal();
        final Bounds bounds = body.getBoundsInLocal();
        final var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());

        dressMeshView.getTransforms().add(centeredOverOrigin);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        body.getTransforms().add(new Rotate(270, Rotate.X_AXIS));
        body.getTransforms().add(new Rotate(rotateY, Rotate.Y_AXIS));
        body.getTransforms().add(new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth()));

        return body;
    }

}
