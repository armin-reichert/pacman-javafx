/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import javafx.scene.shape.Mesh;

public class PelletModel3D implements Disposable {
	private static final String MESH_ID_PELLET = "Pellet";

	private final Model3D model3D;

	public PelletModel3D() {
		model3D = Models3D.loadModelFromObjFile("/de/amr/pacmanfx/uilib/model3D/pellet.obj");
	}

	@Override
	public void dispose() {
		model3D.dispose();
	}

	public Mesh mesh() {
		return model3D.mesh(MESH_ID_PELLET);
	}
}
