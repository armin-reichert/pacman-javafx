/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

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

    /** Shared 3D model instance for Pac-Man. */
    public static final PacManModel3D PAC_MAN_MODEL = new PacManModel3D();

    /** Shared 3D model instance for ghosts. */
    public static final GhostModel3D GHOST_MODEL = new GhostModel3D();

    /** Shared 3D model instance for pellets. */
    public static final PelletModel3D PELLET_MODEL = new PelletModel3D();
}
