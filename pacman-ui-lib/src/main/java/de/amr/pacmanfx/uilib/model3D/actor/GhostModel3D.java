/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.objparser.ObjFileParser;
import de.amr.objparser.ObjModel;
import de.amr.meshbuilder.MeshBuilder;
import javafx.scene.shape.MeshView;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GhostModel3D {

    private static class LazyThreadSafeSingletonHolder {
        static final GhostModel3D SINGLETON = new GhostModel3D();
    }

    public static GhostModel3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

	private static final String OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/ghost.obj";

	private static final String GROUP_ID_PUPILS   = "Object.Sphere.1.Group.Pupils";
    private static final String GROUP_ID_EYEBALLS = "Object.Sphere.2.Group.Eyeballs";
    private static final String GROUP_ID_DRESS    = "Object.Sphere.3.Group.Dress";

	private final Map<String, MeshView> meshViewsForGroups;

	private GhostModel3D() {
        final URL url = getClass().getResource(OBJ_FILE);
        if (url == null) {
            throw new RuntimeException("3D model cannot be loaded from file: " + OBJ_FILE);
        }
        final var parser = new ObjFileParser(url, StandardCharsets.UTF_8);
		try {
			final ObjModel objModel = parser.parse();
			meshViewsForGroups = MeshBuilder.build(objModel, MeshBuilder.BuildMode.BY_GROUP);
			// access meshes, fail fast
            dress();
			eyeballs();
			pupils();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	private MeshView meshViewOrFail(String name) {
		final MeshView meshView = meshViewsForGroups.get(name);
		if (meshView != null) {
			return meshView;
		}
		throw new IllegalArgumentException("Mesh view for name %s does not exist".formatted(name));
	}

	public MeshView dress() {
		return meshViewOrFail(GROUP_ID_DRESS);
	}

	public MeshView eyeballs() {
		return meshViewOrFail(GROUP_ID_EYEBALLS);
	}

	public MeshView pupils() {
		return meshViewOrFail(GROUP_ID_PUPILS);
	}
}
