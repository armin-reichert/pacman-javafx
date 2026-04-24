/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.uilib.objimport.ObjFileParser;
import de.amr.pacmanfx.uilib.objimport.TriangleMeshBuilder;
import javafx.scene.shape.MeshView;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GhostModel3D implements Disposable {

    private static class LazyThreadSafeSingletonHolder {
        static final GhostModel3D SINGLETON = new GhostModel3D();
    }

    public static GhostModel3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

	private static final String OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/ghost.obj";

	private static final String ID_PUPILS   = "Object.Sphere.1.Group.Pupils";
    private static final String ID_EYEBALLS = "Object.Sphere.2.Group.Eyeballs";
    private static final String ID_DRESS    = "Object.Sphere.3.Group.Dress";

	private final Map<String, MeshView> meshViews;

	private GhostModel3D() {
		try {
			final URL url = getClass().getResource(OBJ_FILE);
			final var parser = new ObjFileParser(url, StandardCharsets.UTF_8);
			final ObjFileParser.ObjModel objModel = parser.parse();
            final var builder = new TriangleMeshBuilder(objModel);
			meshViews = builder.buildMeshViewsByGroup();
			// access meshes, fail fast
            dress();
			eyeballs();
			pupils();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public void dispose() {
		meshViews.clear();
	}

	private MeshView meshViewOrFail(String name) {
		final MeshView meshView = meshViews.get(name);
		if (meshView != null) {
			return meshView;
		}
		throw new IllegalArgumentException("Mesh view for name %s does not exist".formatted(name));
	}

	public MeshView dress() {
		return meshViewOrFail(ID_DRESS);
	}

	public MeshView eyeballs() {
		return meshViewOrFail(ID_EYEBALLS);
	}

	public MeshView pupils() {
		return meshViewOrFail(ID_PUPILS);
	}
}
