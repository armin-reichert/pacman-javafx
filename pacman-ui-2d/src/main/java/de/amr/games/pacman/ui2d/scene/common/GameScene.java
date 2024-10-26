/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.ui2d.GameActionProvider;
import de.amr.games.pacman.ui2d.GameContext;

/**
 * Common interface of all game scenes (2D and 3D).
 *
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener, GameActionProvider {

    /**
     * @return the game scene context
     */
    GameContext context();

    /**
     * Sets the game scene context.
     *
     * @param context the game scene context
     */
    void setGameContext(GameContext context);

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
     * Hook method where actions are bound to keyboard combinations.
     */
    void bindActions();

    /**
     * @return (unscaled) scene size in pixels e.g. 224x288
     */
    Vector2f size();

    /**
     * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
     */
    default void onSceneVariantSwitch(GameScene oldScene) {}
}