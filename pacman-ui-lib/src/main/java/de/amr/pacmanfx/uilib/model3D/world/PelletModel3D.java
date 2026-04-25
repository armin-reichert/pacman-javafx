/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.world;

import de.amr.pacmanfx.uilib.objimport.ObjFileParser;
import de.amr.pacmanfx.uilib.model3D.TriangleMeshBuilder;
import javafx.scene.shape.MeshView;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PelletModel3D {

    private static class LazyThreadSafeSingletonHolder {
        static final PelletModel3D SINGLETON = new PelletModel3D();
    }

    public static PelletModel3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

	private static final String OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/pellet.obj";

    private static final String ID_PELLET = "Object.Pellet.Group.anon.0";

    private final Map<String, MeshView> meshViews;

	private PelletModel3D() {
        try {
            final URL url = getClass().getResource(OBJ_FILE);
            final var parser = new ObjFileParser(url, StandardCharsets.UTF_8);
            final var builder = new TriangleMeshBuilder(parser.parse());
            meshViews = builder.buildMeshViewsByGroup();
            pellet(); // fail fast
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

    public MeshView pellet() {
        return meshViewOrFail(ID_PELLET);
    }

    private MeshView meshViewOrFail(String name) {
        final MeshView meshView = meshViews.get(name);
        if (meshView != null) {
            return meshView;
        }
        throw new IllegalArgumentException("Mesh view for name %s does not exist".formatted(name));
    }
}
