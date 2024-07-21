/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.model;

import de.amr.games.pacman.ui3d.objimport.ObjImporter;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A 3D-model imported from a Wavefront .obj file.
 * <p>
 * Uses the importer code from Oracle's JFX3DViewer sample project.
 *
 * @author Armin Reichert
 */
public class Model3D {

    public static Translate centeredOverOrigin(Node node) {
        var bounds = node.getBoundsInLocal();
        return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
    }

    public static Scale scaled(Node node, double size) {
        var bounds = node.getBoundsInLocal();
        return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
    }

    public static MeshView meshView(Node tree, String id) {
        requireNonNull(tree);
        requireNonNull(id);
        var cssID = toCSS_ID(id);
        var node = tree.lookup("#" + cssID);
        if (node == null) {
            throw new IllegalArgumentException("No mesh view with ID '%s' found".formatted(cssID));
        }
        if (node instanceof MeshView meshView) {
            return meshView;
        }
        throw new IllegalArgumentException("Node with CSS ID '%s' is no MeshView but a %s".formatted(cssID, node.getClass()));
    }

    public static String toCSS_ID(String id) {
        // TODO what else need to be escaped?
        return id.replace('.', '-');
    }

    private final Map<String, Mesh> meshesByName = new HashMap<>();
    private final Map<String, PhongMaterial> materials = new HashMap<>();

    public Model3D(URL objFileURL) {
        if (objFileURL == null) {
            throw new Model3DException("3D model cannot be created: URL is null");
        }
        String url = objFileURL.toExternalForm();
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        Logger.info("Loading 3D model from OBJ file '{}'. URL: {}", fileName, objFileURL);
        try {
            var importer = new ObjImporter(url);
            for (String meshName : importer.getMeshNames()) {
                TriangleMesh mesh = importer.getMesh(meshName);
                ObjImporter.validateTriangleMesh(mesh);
                meshesByName.put(meshName, mesh);
                Logger.info("Mesh ID: '{}'", meshName);
            }
            for (var materialMap : importer.materialLibrary()) {
                for (var entry : materialMap.entrySet()) {
                    materials.put(entry.getKey(), (PhongMaterial) entry.getValue());
                }
            }
            Logger.trace(contentAsText(objFileURL));
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public String contentAsText(URL url) {
        var sb = new StringBuilder();
        sb.append("3D model loaded from URL ").append(url).append("\n");
        sb.append("\tMeshes:\n");
        for (var entry : meshesByName.entrySet()) {
            sb.append("\t\t'%s': %s%n".formatted(entry.getKey(), entry.getValue()));
        }
        sb.append("\tMaterials:\n");
        for (var entry : materials.entrySet()) {
            sb.append("\t\t'%s': %s%n".formatted(entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }

    public Mesh mesh(String name) {
        if (meshesByName.containsKey(name)) {
            return meshesByName.get(name);
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