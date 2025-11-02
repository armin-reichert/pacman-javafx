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
import org.tinylog.Logger;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
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
        Instant start = Instant.now();
        objData = ObjFileImporter.importObjFile(url, StandardCharsets.UTF_8);
        Duration duration = Duration.between(start, Instant.now());
        Logger.info("OBJ file imported ({} millis). URL: '{}'", duration.toMillis(), url);
        for (TriangleMesh mesh : objData.triangleMeshMap.values()) {
            try {
                ObjFileImporter.validateTriangleMesh(mesh);
            } catch (AssertionError error) {
                Logger.error("Invalid OBJ file data: {}, URL: '{}'", error.getMessage(), url);
            }
        }
    }

    @Override
    public void dispose() {
        objData.triangleMeshMap.clear();
        objData.materialMapsList.clear();
        objData = null;
    }

    /**
     * @return (unmodifiable) map from mesh names to triangle meshes contained in OBJ file
     */
    public Map<String, TriangleMesh> meshesByName() {
        return Collections.unmodifiableMap(objData.triangleMeshMap);
    }

    /**
     * @return (unmodifiable) list of material maps defined in OBJ file
     */
    public List<Map<String, Material>> materialLibs() {
        return Collections.unmodifiableList(objData.materialMapsList);
    }

    /**
     * @param name mesh name as specified in OBJ file
     * @return triangle mesh with given name
     * @throws Model3DException if mesh with this name does not exist
     */
    public TriangleMesh mesh(String name) {
        requireNonNull(name);
        if (objData.triangleMeshMap.containsKey(name)) {
            return objData.triangleMeshMap.get(name);
        }
        throw new Model3DException("No mesh with name %s found", name);
    }
}