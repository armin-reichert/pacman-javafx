/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.world;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.uilib.objimport.Model3D;
import javafx.scene.shape.Mesh;

public class PelletModel3D implements Disposable {

    private static class LazyThreadSafeSingletonHolder {
        static final PelletModel3D SINGLETON = new PelletModel3D();
    }

    public static PelletModel3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

	private static final String OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/pellet.obj";
	private static final String ID_PELLET = "Object.Pellet";

	private final Model3D model3D;

	private PelletModel3D() {
		try {
			model3D = Model3D.importObj(getClass().getResource(OBJ_FILE));
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public void dispose() {
		model3D.dispose();
	}

	public Mesh pelletMesh() {
		return model3D.meshOrFail(ID_PELLET);
	}
}
