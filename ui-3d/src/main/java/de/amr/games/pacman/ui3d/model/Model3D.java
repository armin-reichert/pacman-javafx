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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
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

    public static MeshView meshViewById(Node root, String id) {
        requireNonNull(root);
        requireNonNull(id);
        var cssID = toCSS_ID(id);
        var node = root.lookup("#" + cssID);
        if (node == null) {
            throw new IllegalArgumentException("No mesh view with ID '%s' found".formatted(cssID));
        }
        if (node instanceof MeshView meshView) {
            return meshView;
        }
        throw new IllegalArgumentException("Node with CSS ID '%s' is no MeshView but a %s".formatted(cssID, node.getClass()));
    }

    public static String toCSS_ID(String id) {
        // what else need to be escaped?
        return id.replace('.', '-');
    }

    private final Map<String, Mesh> meshesByName = new HashMap<>();
    private final Map<String, PhongMaterial> materials = new HashMap<>();

    public Model3D(URL objFileURL) {
        checkNotNull(objFileURL);
        Logger.info("Loading 3D OBJ model from URL: {}", objFileURL);
        try {
            importModel(new ObjImporter(objFileURL.toExternalForm()));
        } catch (Exception x) {
            Logger.error(x);
        }
    }

    public Model3D(File objFile) {
        checkNotNull(objFile);
        Logger.info("Loading 3D OBJ model from file '{}'", objFile);
        try (var in = new FileInputStream(objFile)) {
            importModel(new ObjImporter(in));
        } catch (Exception x) {
            Logger.error(x);
        }
    }

    private void importModel(ObjImporter importer) {
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

    public MeshView meshViewById(String name) {
        return new MeshView(mesh(name));
    }

    public PhongMaterial material(String name) {
        if (materials.containsKey(name)) {
            return materials.get(name);
        }
        throw new Model3DException("No material with name %s found", name);
    }
}