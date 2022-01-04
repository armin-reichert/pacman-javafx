package de.amr.games.pacman.ui.fx._3d.entity;

import java.util.Collections;
import java.util.Map;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.lib.Logging;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

/**
 * A 3D-model imported from a Wavefront .obj file.
 * 
 * @author Armin Reichert
 */
public class ObjModel {

	public Map<String, MeshView> meshViews = Collections.emptyMap();
	public Map<String, PhongMaterial> materials = Collections.emptyMap();

	public ObjModel(String path) {
		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(getClass().getResource(path));
			meshViews = objImporter.getNamedMeshViews();
			materials = objImporter.getNamedMaterials();
			Logging.log("3D model '%s' loaded successfully", path);
		} catch (ImportException e) {
			Logging.log("3D model '%s' loading failed", path);
			e.printStackTrace();
		} finally {
			objImporter.close();
		}
	}

	public MeshView createMeshView(String name) {
		return new MeshView(meshViews.get(name).getMesh());
	}

	public PhongMaterial getMaterial(String name) {
		return materials.get(name);
	}
}