/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib.model3D;

import de.amr.games.pacman.uilib.objimport.ObjImporter;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Scale;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A 3D-model imported from a Wavefront .obj file.
 * <p>
 * Uses the importer code from Oracle's JFX3DViewer sample project.
 *
 * @author Armin Reichert
 */
public class Model3D {

    public static Scale scaled(Node node, double size) {
        var bounds = node.getBoundsInLocal();
        return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
    }

    public static Stream<MeshView> allMeshViewsUnder(Node root) {
        return root.lookupAll("*").stream().filter(MeshView.class::isInstance).map(MeshView.class::cast);
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

    private final String url;
    private final Map<String, TriangleMesh> meshesByName = new HashMap<>();
    private final Map<String, PhongMaterial> materials = new HashMap<>();

    public Model3D(URL objFileURL) throws IOException, URISyntaxException {
        url = requireNonNull(objFileURL).toExternalForm();
        readMeshesAndMaterials(new ObjImporter(url));
    }

    public Model3D(File objFile) throws IOException, URISyntaxException {
        this(objFile.toURI().toURL());
    }

    public String url() {
        return url;
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

    public TriangleMesh mesh(String name) {
        requireNonNull(name);
        if (meshesByName.containsKey(name)) {
            return meshesByName.get(name);
        }
        throw new Model3DException("No mesh with name %s found", name);
    }

    public PhongMaterial material(String name) {
        requireNonNull(name);
        if (materials.containsKey(name)) {
            return materials.get(name);
        }
        throw new Model3DException("No material with name %s found", name);
    }

    private void readMeshesAndMaterials(ObjImporter importer) {
        for (String meshName : importer.getMeshNames()) {
            TriangleMesh mesh = importer.getMesh(meshName);
            ObjImporter.validateTriangleMesh(mesh);
            meshesByName.put(meshName, mesh);
        }
        for (var materialLibrary : importer.materialLibrary()) {
            materialLibrary.forEach((materialName, material) -> materials.put(materialName, (PhongMaterial) material));
        }
    }
}