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

    private static final String ID_DRESS = "Material.Dress";
	private static final String ID_EYEBALLS = "Material.Eyeballs";
	private static final String ID_PUPILS = "Material.Pupils";

	private final Map<String, MeshView> meshesByMaterialName;

	private GhostModel3D() {
		try {
			final URL url = getClass().getResource(OBJ_FILE);
			final var parser = new ObjFileParser(url, StandardCharsets.UTF_8);
			final var builder = new TriangleMeshBuilder(parser.objModel(), parser.materialLibsMap());
			meshesByMaterialName = builder.buildMeshViewsByMaterial();
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
		meshesByMaterialName.clear();
	}

	private MeshView meshViewOrFail(String materialName) {
		final MeshView meshView = meshesByMaterialName.get(materialName);
		if (meshView != null) {
			return meshView;
		}
		throw new IllegalArgumentException("Mesh view for material %s does not exist".formatted(materialName));
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
