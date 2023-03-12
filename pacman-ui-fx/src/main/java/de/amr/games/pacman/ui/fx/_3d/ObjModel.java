package de.amr.games.pacman.ui.fx._3d;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;

/**
 * A 3D-model imported from a Wavefront .obj file.
 * 
 * @author Armin Reichert
 */
public class ObjModel {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private Map<String, Mesh> meshes;
	private Map<String, PhongMaterial> materials;

	public ObjModel(URL url) {
		if (url == null) {
			throw new Model3DException("OBJ model cannot be created: URL is null");
		}
		ObjModelImporter importer = new ObjModelImporter();
		try {
			importer.read(url);
			meshes = new HashMap<>();
			for (var entry : importer.getNamedMeshViews().entrySet()) {
				meshes.put(entry.getKey(), entry.getValue().getMesh());
			}
			materials = importer.getNamedMaterials();
			LOG.info("3D model loaded, URL='%s'", url);
			for (var entry : meshes.entrySet()) {
				LOG.info("Mesh id=%s, value=%s", entry.getKey(), entry.getValue());
			}
		} catch (ImportException e) {
			throw new Model3DException("Error loading 3D model, URL='%s': %s", url, e.getMessage());
		} finally {
			importer.close();
		}
	}

	public ObjModel(String relPath) {
		this(ResourceMgr.urlFromRelPath(relPath));
	}

	public Mesh mesh(String name) {
		if (meshes.containsKey(name)) {
			return meshes.get(name);
		}
		throw new Model3DException("No mesh with name %s found", name);
	}

	public PhongMaterial material(String name) {
		if (materials.containsKey(name)) {
			return materials.get(name);
		}
		throw new Model3DException("No material with name %s found", name);
	}
}