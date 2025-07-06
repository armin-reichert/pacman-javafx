/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.objimport.ObjImporter;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A 3D-model imported from a Wavefront .obj file.
 * <p>
 * Uses an adapted version of the OBJ importer from Oracle's JFX3DViewer sample project.
 */
public class Model3D {

    private final Map<String, TriangleMesh> triangleMeshMap = new HashMap<>();
    private final Map<String, PhongMaterial> materialMap = new HashMap<>();

    public Model3D(URL objFileURL) throws IOException, URISyntaxException {
        requireNonNull(objFileURL);
        var importer = new ObjImporter(objFileURL.toExternalForm());
        for (String meshName : importer.getMeshNames()) {
            TriangleMesh mesh = importer.getMesh(meshName);
            ObjImporter.validateTriangleMesh(mesh);
            triangleMeshMap.put(meshName, mesh);
        }
        for (var materialLibrary : importer.materialLibraries()) {
            materialLibrary.forEach((materialName, material) -> materialMap.put(materialName, (PhongMaterial) material));
        }
    }

    public void destroy() {
        triangleMeshMap.clear();
        materialMap.clear();
    }

    public Map<String, TriangleMesh> meshesByName() {
        return Collections.unmodifiableMap(triangleMeshMap);
    }

    public Map<String, PhongMaterial> materials() {
        return Collections.unmodifiableMap(materialMap);
    }

    public TriangleMesh mesh(String name) {
        requireNonNull(name);
        if (triangleMeshMap.containsKey(name)) {
            return triangleMeshMap.get(name);
        }
        throw new Model3DException("No mesh with name %s found", name);
    }

    public PhongMaterial material(String name) {
        requireNonNull(name);
        if (materialMap.containsKey(name)) {
            return materialMap.get(name);
        }
        throw new Model3DException("No material with name %s found", name);
    }
}