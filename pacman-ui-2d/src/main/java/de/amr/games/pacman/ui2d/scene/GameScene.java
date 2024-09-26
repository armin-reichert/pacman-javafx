/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.event.GameEventListener;
import javafx.scene.Node;

/**
 * Common interface of all game scenes (2D and 3D).
 *
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

    /**
     * Called when the scene becomes the current one.
     */
    default void init() {}

    /**
     * Called when the scene needs to be updated.
     */
    void update();

    /**
     * Called when the scene ends and gets replaced by another scene.
     */
    default void end() {}

    /**
     * @return the root of the game scene (used to embed the scene into the scene graph)
     */
    Node root();

    /**
     * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
     */
    default void onSceneVariantSwitch(GameScene oldScene) {}

    /**
     * Handles user input like pressed keys.
     */
    default void handleInput() {}
}