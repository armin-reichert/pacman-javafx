/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.objimport.ObjFileContent;
import de.amr.pacmanfx.uilib.objimport.ObjFileImporter;
import javafx.scene.paint.Material;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * API for accessing 3D model data imported from a Wavefront .obj file.
 */
public class Model3D implements Disposable {

    private ObjFileContent objFileContent;

    protected Model3D() {
    }

    /**
     * @param url URL addressing an OBJ file (Wavefront .obj file format)
     */
    public Model3D(URL url) {
        requireNonNull(url);
        loadModel(url);
    }

    protected void loadModel(URL url) {
        Ufx.measureDuration("Import OBJ file '%s'".formatted(url), () -> {
            objFileContent = ObjFileImporter.importObjFile(url, StandardCharsets.UTF_8);
        });
        if (objFileContent == null) {
            Logger.error("Import OBJ file '{}' failed!");
            throw new RuntimeException("OBJ import failed!");
        }

        for (TriangleMesh mesh : objFileContent.triangleMeshMap.values()) {
            try {
                ObjFileImporter.validateTriangleMesh(mesh);
            } catch (AssertionError error) {
                Logger.error("Invalid OBJ file data: {}, URL: '{}'", error.getMessage(), url);
            }
        }
    }

    @Override
    public void dispose() {
        objFileContent.dispose();
    }

    /**
     * @return (unmodifiable) map from mesh names to triangle meshes contained in OBJ file
     */
    public Map<String, TriangleMesh> meshMap() {
        return Collections.unmodifiableMap(objFileContent.triangleMeshMap);
    }

    /**
     * @param meshName mesh name as specified in OBJ file
     * @return triangle mesh with given name
     * @throws Model3DException if mesh with this name does not exist
     */
    public TriangleMesh mesh(String meshName) {
        requireNonNull(meshName);
        if (objFileContent.triangleMeshMap.containsKey(meshName)) {
            return objFileContent.triangleMeshMap.get(meshName);
        }
        throw new Model3DException("No mesh with name '%s' found", meshName);
    }

    /**
     * @return (unmodifiable) list of material maps defined in OBJ file
     */
    public List<Map<String, Material>> materialLibs() {
        return Collections.unmodifiableList(objFileContent.materialMapsList);
    }
}