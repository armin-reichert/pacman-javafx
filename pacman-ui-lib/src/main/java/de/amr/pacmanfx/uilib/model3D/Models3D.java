/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.objimport.Model3D;
import de.amr.pacmanfx.uilib.objimport.ObjFileImporter;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
     * Loads a {@link Model3D} from a Wavefront .obj file located on the classpath.
     *
     * @param rm resource manager for creating URL from resource path
     * @param objFilePath the classpath-relative path to the OBJ file
     * @return the loaded model
     * @throws Model3DException if the resource cannot be found or loaded
     */
    public static Model3D createFromObjFile(ResourceManager rm, String objFilePath) throws Model3DException {
        final URL url = rm.url(objFilePath);
        if (url == null) {
            throw new Model3DException("Could not access OBJ file at path '%s'".formatted(objFilePath));
        }
        return loadWavefrontObjFile(url);
    }

    public static Model3D loadWavefrontObjFile(URL modelURL) throws Model3DException {
        try {
            final Model3D content = ObjFileImporter.importObjFile(modelURL, StandardCharsets.UTF_8);
            if (content == null) {
                throw new Model3DException("Could not load OBJ file");
            }
            for (TriangleMesh mesh : content.meshMap().values()) {
                try {
                    ObjFileImporter.validateTriangleMesh(mesh);
                } catch (AssertionError error) {
                    Logger.error("Invalid OBJ file data: {}, URL: '{}'", error.getMessage(), modelURL);
                }
            }
            return content;
        } catch (IOException x) {
            throw new Model3DException("Could not load OBJ file", x);
        }
    }

    /** Shared 3D model instance for Pac-Man. */
    public static final PacManModel3D PAC_MAN_MODEL = new PacManModel3D();

    /** Shared 3D model instance for ghosts. */
    public static final GhostModel3D GHOST_MODEL = new GhostModel3D();

    /** Shared 3D model instance for pellets. */
    public static final PelletModel3D PELLET_MODEL = new PelletModel3D();
}
