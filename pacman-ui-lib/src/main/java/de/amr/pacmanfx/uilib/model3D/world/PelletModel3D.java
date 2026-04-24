/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.world;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.uilib.objimport.ObjFileParser;
import de.amr.pacmanfx.uilib.objimport.TriangleMeshBuilder;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PelletModel3D implements Disposable {

    private static class LazyThreadSafeSingletonHolder {
        static final PelletModel3D SINGLETON = new PelletModel3D();
    }

    public static PelletModel3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

	private static final String OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/pellet.obj";
	private static final String ID_PELLET = "Material.Pellet";

    private final Map<String, MeshView> meshesByMaterialName;

	private PelletModel3D() {
        try {
            URL url = getClass().getResource(OBJ_FILE);
            ObjFileParser parser = new ObjFileParser(url, StandardCharsets.UTF_8);
            TriangleMeshBuilder builder = new TriangleMeshBuilder(parser.objModel(), parser.materialLibsMap());
            meshesByMaterialName = builder.buildMeshViewsByMaterial();
            pelletMesh(); // fail fast
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

	public Mesh pelletMesh() {
		return meshOrFail(ID_PELLET);
	}
}
