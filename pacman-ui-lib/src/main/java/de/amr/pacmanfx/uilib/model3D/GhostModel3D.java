/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.objimport.ObjFileContent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;

public class GhostModel3D implements Disposable {
	private static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	private static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
	private static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

	private final ObjFileContent model3D;

	public GhostModel3D() {
		model3D = Models3D.loadModelFromObjFile("/de/amr/pacmanfx/uilib/model3D/ghost.obj");
	}

	@Override
	public void dispose() {
		model3D.dispose();
	}

	public Mesh dressMesh() {
		return Model3D.mesh(model3D, MESH_ID_GHOST_DRESS);
	}

	public Mesh eyeballsMesh() {
		return Model3D.mesh(model3D, MESH_ID_GHOST_EYEBALLS);
	}

	public Mesh pupilsMesh() {
		return Model3D.mesh(model3D, MESH_ID_GHOST_PUPILS);
	}

	public GhostBody createGhostBody(double size, Color dressColor, double rotateY) {
		return new GhostBody(dressMesh(), pupilsMesh(), eyeballsMesh(), size, dressColor, rotateY);
	}
}
