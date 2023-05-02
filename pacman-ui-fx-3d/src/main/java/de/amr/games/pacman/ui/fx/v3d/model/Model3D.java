package de.amr.games.pacman.ui.fx.v3d.model;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.v3d.objimport.ObjImporter;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * A 3D-model imported from a Wavefront .obj file.
 * <p>
 * Uses the importer code from Oracle's JFX3DViewer sample project.
 * 
 * @author Armin Reichert
 */
public class Model3D {

	public static Translate centerOverOrigin(Node node) {
		var bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	public static Scale scale(Node node, double size) {
		var bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	public static MeshView meshView(Node tree, String id) {
		requireNonNull(tree);
		requireNonNull(id);

		var cssID = cssID(id);
		var node = tree.lookup("#" + cssID);
		if (node == null) {
			throw new IllegalArgumentException("No mesh view with ID '%s' found".formatted(cssID));
		}
		if (node instanceof MeshView meshView) {
			return meshView;
		}
		throw new IllegalArgumentException(
				"Node with CSS ID '%s' is not a MeshView but a %s".formatted(cssID, node.getClass()));
	}

	protected static String cssID(String id) {
		// TODO what else need to be escaped?
		return id.replace('.', '-');
	}

	private Map<String, Mesh> meshes = new HashMap<>();
	private Map<String, PhongMaterial> materials = new HashMap<>();

	public Model3D(URL url) {
		if (url == null) {
			throw new Model3DException("3D model cannot be created: URL is null");
		}
		var urlString = url.toExternalForm();
		int lastSlash = urlString.lastIndexOf('/');
		var fileName = urlString.substring(lastSlash + 1);
		Logger.trace("*** Load 3D model from file '{}'. URL: {}", fileName, url);
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
			Logger.trace(contentReport());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String contentReport() {
		var sb = new StringBuilder();
		sb.append("Model content:\n");
		sb.append("\tMeshes:\n");
		for (var entry : meshes.entrySet()) {
			sb.append("\t\t'%s': %s\n".formatted(entry.getKey(), entry.getValue()));
		}
		sb.append("\tMaterials:\n");
		for (var entry : materials.entrySet()) {
			sb.append("\t\t'%s': %s\n".formatted(entry.getKey(), entry.getValue()));
		}
		return sb.toString();
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