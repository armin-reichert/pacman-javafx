/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.uilib.objimport.ObjFileParser;
import de.amr.pacmanfx.uilib.objimport.TriangleMeshBuilder;
import javafx.scene.shape.Mesh;
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
			URL url = getClass().getResource(OBJ_FILE);
			ObjFileParser parser = new ObjFileParser(url, StandardCharsets.UTF_8);
			TriangleMeshBuilder builder = new TriangleMeshBuilder(parser.objModel(), parser.materialLibsMap());
			meshesByMaterialName = builder.buildMeshViewsByMaterial();
			dressMesh();
			eyeballsMesh();
			pupilsMesh();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public void dispose() {
		meshesByMaterialName.clear();
	}

	private Mesh meshOrFail(String materialName) {
		final MeshView meshView = meshesByMaterialName.get(materialName);
		if (meshView != null) {
			return meshView.getMesh();
		}
		throw new IllegalArgumentException("Mesh view for material %s does not exist".formatted(materialName));
	}

	public Mesh dressMesh() {
		return meshOrFail(ID_DRESS);
	}

	public Mesh eyeballsMesh() {
		return meshOrFail(ID_EYEBALLS);
	}

	public Mesh pupilsMesh() {
		return meshOrFail(ID_PUPILS);
	}
}
