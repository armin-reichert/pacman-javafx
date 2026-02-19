/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.objimport.Model3D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;

public class GhostModel3D implements Disposable {

	private static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	private static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
	private static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

	private final Model3D model3D;

	public GhostModel3D() {
		try {
			model3D = Models3D.createFromObjFile(this::getClass, "/de/amr/pacmanfx/uilib/model3D/ghost.obj");
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

	public GhostBody createGhostBody(double size, Color dressColor, double rotateY) {
		return new GhostBody(dressMesh(), pupilsMesh(), eyeballsMesh(), size, dressColor, rotateY);
	}
}
