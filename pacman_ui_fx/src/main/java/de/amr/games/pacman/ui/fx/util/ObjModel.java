package de.amr.games.pacman.ui.fx.util;

import java.net.URL;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private Map<String, MeshView> meshViews;
	private Map<String, PhongMaterial> materials;

	public ObjModel(URL url) {
		if (url == null) {
			throw new Model3DException("OBJ model cannot be created: URL is null");
		}
		ObjModelImporter importer = new ObjModelImporter();
		try {
			importer.read(url);
			meshViews = importer.getNamedMeshViews();
			materials = importer.getNamedMaterials();
			LOGGER.info("3D model loaded successfully (URL='%s')", url);
		} catch (ImportException e) {
			throw new Model3DException("Error loading 3D model (URL='%s'): %s", url, e.getMessage());
		} finally {
			importer.close();
		}
	}

	public MeshView createMeshView(String name) {
		if (meshViews.containsKey(name)) {
			return new MeshView(meshViews.get(name).getMesh());
		}
		throw new Model3DException("No mesh with name %s found", name);
	}

	public PhongMaterial getMaterial(String name) {
		return materials.get(name);
	}
}