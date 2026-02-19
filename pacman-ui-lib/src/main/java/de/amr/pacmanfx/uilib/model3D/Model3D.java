/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.objimport.ObjFileContent;
import de.amr.pacmanfx.uilib.objimport.ObjFileImporter;
import javafx.scene.paint.Material;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * API for accessing 3D model data imported from a Wavefront .obj file.
 */
public final class Model3D {

    private Model3D() {}

    public static ObjFileContent loadObjFile(URL modelURL) throws IOException {
        final ObjFileContent content = ObjFileImporter.importObjFile(modelURL, StandardCharsets.UTF_8);
        if (content == null) {
            Logger.error("Import OBJ file '{}' failed!");
            throw new Model3DException("OBJ import failed!");
        }
        for (TriangleMesh mesh : content.triangleMeshMap.values()) {
            try {
                ObjFileImporter.validateTriangleMesh(mesh);
            } catch (AssertionError error) {
                Logger.error("Invalid OBJ file data: {}, URL: '{}'", error.getMessage(), modelURL);
            }
        }
        return content;
    }

    /**
     * @return (unmodifiable) map from mesh names to triangle meshes contained in OBJ file
     */
    public static Map<String, TriangleMesh> meshMap(ObjFileContent objFileContent) {
        return Collections.unmodifiableMap(objFileContent.triangleMeshMap);
    }

    /**
     * @param meshName mesh name as specified in OBJ file
     * @return triangle mesh with given name
     * @throws Model3DException if mesh with this name does not exist
     */
    public static TriangleMesh mesh(ObjFileContent objFileContent, String meshName) {
        requireNonNull(meshName);
        if (objFileContent.triangleMeshMap.containsKey(meshName)) {
            return objFileContent.triangleMeshMap.get(meshName);
        }
        throw new Model3DException("No mesh with name '%s' found", meshName);
    }

    /**
     * @return (unmodifiable) list of material maps defined in OBJ file
     */
    public static List<Map<String, Material>> materialLibs(ObjFileContent objFileContent) {
        return Collections.unmodifiableList(objFileContent.materialMapsList);
    }
}