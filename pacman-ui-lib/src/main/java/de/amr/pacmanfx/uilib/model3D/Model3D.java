/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.objimport.ObjImporter;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Scale;

import java.io.IOException;
import java.net.URISyntaxException;
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

    public static Scale scaled(Node node, double size) {
        var bounds = node.getBoundsInLocal();
        return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
    }

    private final Map<String, TriangleMesh> meshesByName = new HashMap<>();
    private final Map<String, PhongMaterial> materials = new HashMap<>();

    public Model3D(URL objFileURL) throws IOException, URISyntaxException {
        requireNonNull(objFileURL);
        var importer = new ObjImporter(objFileURL.toExternalForm());
        for (String meshName : importer.getMeshNames()) {
            TriangleMesh mesh = importer.getMesh(meshName);
            ObjImporter.validateTriangleMesh(mesh);
            meshesByName.put(meshName, mesh);
        }
        for (var materialLibrary : importer.materialLibraries()) {
            materialLibrary.forEach((materialName, material) -> materials.put(materialName, (PhongMaterial) material));
        }
    }

    public void destroy() {
        meshesByName.clear();
        materials.clear();
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
}