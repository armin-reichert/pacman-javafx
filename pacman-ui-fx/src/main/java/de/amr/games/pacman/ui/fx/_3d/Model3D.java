package de.amr.games.pacman.ui.fx._3d;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx._3d.objimport.ObjImporter;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;

/**
 * A 3D-model imported from a Wavefront .obj file.
 * <p>
 * Uses the importer code from Oracle's JFX3DViewer sample project.
 * 
 * @author Armin Reichert
 */
public class Model3D {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private Map<String, Mesh> meshes = new HashMap<>();
	private Map<String, PhongMaterial> materials = new HashMap<>();

	public Model3D(URL url) {
		if (url == null) {
			throw new Model3DException("OBJ model cannot be created: URL is null");
		}
		try {
			var importer = new ObjImporter(url.toExternalForm());
			for (var meshName : importer.getMeshNames()) {
				var mesh = importer.getMesh(meshName);
				ObjImporter.validateTriangleMesh(mesh);
				meshes.put(meshName, mesh);
				var material = importer.getMaterial(meshName);
				materials.put(meshName, (PhongMaterial) material);
			}
			LOG.info("3D model loaded, URL='%s'", url);
			for (var entry : meshes.entrySet()) {
				LOG.trace("Mesh key=%s, value=%s", entry.getKey(), entry.getValue());
			}
			for (var entry : materials.entrySet()) {
				LOG.trace("Material key=%s, value=%s", entry.getKey(), entry.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Model3D(String relPath) {
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