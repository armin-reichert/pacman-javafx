package de.amr.games.pacman.ui.fx._3d.model;

import static de.amr.games.pacman.lib.Logging.log;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

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
		ObjModelImporter importer = new ObjModelImporter();
		URL url = getClass().getResource(path);
		if (url == null) {
			log("Loading 3D model failed: could not access resource using path '%s'", path);
			throw new RuntimeException("3D model loading failed");
		}
		try {
			importer.read(url);
			meshViews = importer.getNamedMeshViews();
			materials = importer.getNamedMaterials();
			log("Loading 3D model '%s' succeeded", path);
		} catch (ImportException e) {
			log("Loading 3D model '%s' failed", path);
			throw new RuntimeException("3D model loading failed", e);
		} finally {
			importer.close();
		}
	}

	public MeshView createMeshView(String name) {
		return new MeshView(meshViews.get(name).getMesh());
	}

	public PhongMaterial getMaterial(String name) {
		return materials.get(name);
	}
}