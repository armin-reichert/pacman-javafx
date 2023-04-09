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
import javafx.scene.shape.MeshView;

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
			throw new Model3DException("3D model cannot be created: URL is null");
		}
		var urlString = url.toExternalForm();
		int lastSlash = urlString.lastIndexOf('/');
		var fileName = urlString.substring(lastSlash + 1);
		LOG.info("*** Load 3D model from file '%s'. URL: %s", fileName, url);
		try {
			var importer = new ObjImporter(url.toExternalForm());
			for (var meshName : importer.getMeshNames()) {
				var mesh = importer.getMesh(meshName);
				ObjImporter.validateTriangleMesh(mesh);
				meshes.put(meshName, mesh);
			}
			for (var materialMap : importer.materialLibrary()) {
				for (var entry : materialMap.entrySet()) {
					materials.put(entry.getKey(), (PhongMaterial) entry.getValue());
				}
			}
			dump(LOG);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Model3D(String relPath) {
		this(ResourceMgr.urlFromRelPath(relPath));
	}

	public void dump(Logger log) {
		log.info("Model content:");
		log.info("\tMeshes:");
		for (var entry : meshes.entrySet()) {
			log.trace("\t\t'%s': %s", entry.getKey(), entry.getValue());
		}
		log.info("\tMaterials:");
		for (var entry : materials.entrySet()) {
			log.trace("\t\t'%s': %s", entry.getKey(), entry.getValue());
		}
	}

	public Mesh mesh(String name) {
		if (meshes.containsKey(name)) {
			return meshes.get(name);
		}
		throw new Model3DException("No mesh with name %s found", name);
	}

	public MeshView meshView(String name) {
		return new MeshView(mesh(name));
	}

	public PhongMaterial material(String name) {
		if (materials.containsKey(name)) {
			return materials.get(name);
		}
		throw new Model3DException("No material with name %s found", name);
	}
}