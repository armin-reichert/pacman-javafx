/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.uilib.objimport.Model3D;
import de.amr.pacmanfx.uilib.objimport.Model3DException;
import javafx.scene.shape.Mesh;

public class GhostModel3D implements Disposable {

    private static class LazyThreadSafeSingletonHolder {
        static final GhostModel3D SINGLETON = new GhostModel3D();
    }

    public static GhostModel3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    private static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	private static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
	private static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

    private final Model3D model3D;
	private final Mesh dressMesh;
    private final Mesh eyeballsMesh;
    private final Mesh pupilsMesh;

	private GhostModel3D() {
		try {
			model3D = Model3D.importObjFile(getClass().getResource("/de/amr/pacmanfx/uilib/model3D/ghost.obj"));
            dressMesh = model3D.mesh(MESH_ID_GHOST_DRESS).orElseThrow();
            eyeballsMesh = model3D.mesh(MESH_ID_GHOST_EYEBALLS).orElseThrow();
            pupilsMesh = model3D.mesh(MESH_ID_GHOST_PUPILS).orElseThrow();
		} catch (Model3DException x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public void dispose() {
		model3D.dispose();
	}

	public Mesh dressMesh() {
		return dressMesh;
	}

	public Mesh eyeballsMesh() {
		return eyeballsMesh;
	}

	public Mesh pupilsMesh() {
		return pupilsMesh;
	}
}
