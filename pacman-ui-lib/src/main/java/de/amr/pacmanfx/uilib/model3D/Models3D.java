/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.assets.ResourceManager;
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
 * Central access point for loading and providing 3D models used by the UI layer.
 * <p>
 * This class offers:
 * <ul>
 *   <li>a utility method for loading OBJ-based models from the classpath</li>
 *   <li>pre-instantiated shared model instances for Pac-Man, ghosts, and pellets</li>
 * </ul>
 * <p>
 * The class is non-instantiable and acts purely as a static model registry.
 */
public abstract class Models3D {

    /** Prevent instantiation. */
    private Models3D() {}

    /**
     * Loads a {@link Model3D} from an OBJ file located on the classpath.
     * <p>
     * The resource path is resolved relative to the {@code Models3D} package.
     *
     * @param objFilePath the classpath-relative path to the OBJ file
     * @return the loaded model
     * @throws IllegalArgumentException if the resource cannot be found or loaded
     */
    public static ObjFileContent loadModelFromObjFile(String objFilePath) {
        final ResourceManager resourceManager = () -> Models3D.class;
        final URL url = resourceManager.url(objFilePath);

        if (url == null) {
            throw new IllegalArgumentException(
                "Could not access 3D model resource at path '%s'".formatted(objFilePath)
            );
        }

        try {
            return loadObjFile(url);
        } catch (Exception x) {
            throw new IllegalArgumentException(
                "Could not load 3D model from URL '%s'".formatted(url), x
            );
        }
    }

    /** Shared 3D model instance for Pac-Man. */
    public static final PacManModel3D PAC_MAN_MODEL = new PacManModel3D();

    /** Shared 3D model instance for ghosts. */
    public static final GhostModel3D GHOST_MODEL = new GhostModel3D();

    /** Shared 3D model instance for pellets. */
    public static final PelletModel3D PELLET_MODEL = new PelletModel3D();

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
