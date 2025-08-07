/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.objimport.ObjFileData;
import de.amr.pacmanfx.uilib.objimport.ObjFileImporter;
import javafx.scene.paint.Material;
import javafx.scene.shape.TriangleMesh;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A 3D-model imported from a Wavefront .obj file.
 */
public class Model3D implements Disposable {

    private ObjFileData objData;

    /**
     * @param url URL addressing an OBJ file (Wavefront .obj file format)
     */
    public Model3D(URL url) {
        requireNonNull(url);
        objData = ObjFileImporter.importObjFile(url, StandardCharsets.UTF_8);
    }

    @Override
    public void dispose() {
        objData.meshMap().clear();
        objData.materialLibsList().clear();
        objData = null;
    }

    public Map<String, TriangleMesh> meshesByName() {
        return objData.meshMap();
    }

    public List<Map<String, Material>> materialLibs() { return objData.materialLibsList(); }

    public TriangleMesh mesh(String name) {
        requireNonNull(name);
        if (meshesByName().containsKey(name)) {
            return meshesByName().get(name);
        }
        throw new Model3DException("No mesh with name %s found", name);
    }
}