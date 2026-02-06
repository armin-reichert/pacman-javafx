/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

/**
 * Implemented by scene camera perspectives (camera controller).
 *
 * @param <C> "context" of this camera perspective, e.g. the game context or game level.
 */
public interface Perspective<C> {

    /**
     * Called when this perspective (camera controller) is applied to the camera of a scene.
     */
    void startControlling();

    /**
     * Called when this perspective (camera controller) is detached from the camera of a scene.
     */
    default void stopControlling() {}

    /**
     * Called on every frame to update the camera within the given game context, e.g. to follow the Pac-Man in the
     * current game level.

     * @param context the camera context
     */
    void update(C context);
}