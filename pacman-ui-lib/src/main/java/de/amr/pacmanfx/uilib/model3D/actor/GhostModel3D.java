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

	private static final String OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/ghost.obj";
    private static final String ID_DRESS = "Group.Dress";
	private static final String ID_EYEBALLS = "Group.Eyeballs";
	private static final String ID_PUPILS = "Group.Pupils";

    private final Model3D model3D;

	private GhostModel3D() {
		try {
			model3D = Model3D.importObj(getClass().getResource(OBJ_FILE));
			// fail fast
			dressMesh();
			eyeballsMesh();
			pupilsMesh();
		} catch (Model3DException x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public void dispose() {
		model3D.dispose();
	}

	public Mesh dressMesh() {
		return model3D.meshOrFail(ID_DRESS);
	}

	public Mesh eyeballsMesh() {
		return model3D.meshOrFail(ID_EYEBALLS);
	}

	public Mesh pupilsMesh() {
		return model3D.meshOrFail(ID_PUPILS);
	}
}
