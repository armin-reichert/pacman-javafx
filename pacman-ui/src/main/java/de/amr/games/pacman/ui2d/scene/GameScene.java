/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.action.GameActionProvider;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Common interface of all game scenes (2D and 3D).
 *
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener, GameActionProvider {

    Font DEBUG_FONT = Font.font("Sans", FontWeight.BOLD, 20);

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
     * @return (unscaled) scene size in pixels e.g. 224x288
     */
    Vector2f size();

    /**
     * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
     */
    default void onSceneVariantSwitch(GameScene oldScene) {}

    default List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) { return List.of(); }
}