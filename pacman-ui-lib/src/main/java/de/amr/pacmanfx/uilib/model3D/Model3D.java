/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.objimport.ObjFileData;
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

    private ObjFileData data;

    public Model3D(URL objFileURL) {
        requireNonNull(objFileURL);
        data = ObjFileData.fromFile(objFileURL, StandardCharsets.UTF_8);
    }

    @Override
    public void dispose() {
        data.meshMap().clear();
        data.materialLibsList().clear();
        data = null;
    }

    public Map<String, TriangleMesh> meshesByName() {
        return data.meshMap();
    }

    public List<Map<String, Material>> materialLibs() { return data.materialLibsList(); }

    public TriangleMesh mesh(String name) {
        requireNonNull(name);
        if (meshesByName().containsKey(name)) {
            return meshesByName().get(name);
        }
        throw new Model3DException("No mesh with name %s found", name);
    }
}