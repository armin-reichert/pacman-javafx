/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import javafx.scene.PerspectiveCamera;

/**
 * Implemented by scene camera perspectives (camera controller).
 */
public interface Perspective {

    /**
     * Called when this perspective (camera controller) is applied to the camera of a scene.
     *
     * @param camera the camera of some scene
     */
    void apply(PerspectiveCamera camera);

    /**
     * Called when this perspective (camera controller) is detached from the camera of a scene.
     *
     * @param camera the camera of some scene
     */
    default void detach(PerspectiveCamera camera) {}

    /**
     * Called on every frame to update the camera within the given game context, e.g. to follow the Pac-Man in the
     * current game level.
     *
     * @param camera the camera of some scene
     * @param gameContext the game context
     */
    void update(PerspectiveCamera camera, GameContext gameContext);
}