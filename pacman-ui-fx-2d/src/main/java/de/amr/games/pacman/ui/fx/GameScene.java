/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.event.GameEventListener;
//import de.amr.games.pacman.ui.fx.v3d.GamePageContextMenu;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

/**
 * Common interface of all game scenes (2D and 3D).
 *
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

    boolean isScoreVisible();

    void setScoreVisible(boolean visible);

    /**
     * Called when the scene becomes the current one.
     */
    default void init() {
        // empty default
    }

    /**
     * Called when the scene needs to be updated.
     */
    void update();

    /**
     * Called when the scene ends and gets replaced by another scene.
     */
    default void end() {
        // empty default
    }

    /**
     * @return the root of the game scene (used to embed the scene into the scene graph)
     */
    Node root();

    /**
     * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
     */
    default void onSceneVariantSwitch() {
        // empty default
    }

    /**
     * Handles keyboard input.
     */
    default void handleKeyboardInput() {
        // empty default
    }

    GameSceneContext context();

    void setContext(GameSceneContext context);
    public abstract void configureContextMenu(GameScene gameScene, GameSceneContext sceneContext, ObservableList<MenuItem> items);
}