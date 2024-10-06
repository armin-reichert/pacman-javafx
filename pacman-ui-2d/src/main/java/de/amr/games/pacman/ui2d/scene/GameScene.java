/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;

/**
 * Common interface of all game scenes (2D and 3D).
 *
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

    /**
     * Called when the scene becomes the current one.
     */
    void init();

    /**
     * Called when the scene needs to be updated.
     */
    void update();

    /**
     * Called when the scene ends and gets replaced by another scene.
     */
    void end();

    /**
     * Draws the scene content using the given renderer (2D-only.
     * @param renderer world renderer (for current game variant)
     */
    default void draw(GameWorldRenderer renderer) {}

    /**
     * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
     */
    default void onSceneVariantSwitch(GameScene oldScene) {}

    /**
     * Handles user input like pressed keys.
     */
    default void handleInput() {}
}