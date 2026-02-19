/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.assets.ResourceManager;

import java.net.URL;

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
    public static Model3D loadModelFromObjFile(String objFilePath) {
        final ResourceManager resourceManager = () -> Models3D.class;
        final URL url = resourceManager.url(objFilePath);

        if (url == null) {
            throw new IllegalArgumentException(
                "Could not access 3D model resource at path '%s'".formatted(objFilePath)
            );
        }

        try {
            return new Model3D(url);
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
}
