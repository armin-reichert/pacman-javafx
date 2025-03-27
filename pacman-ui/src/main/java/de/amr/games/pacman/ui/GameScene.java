/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;

import java.util.List;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;

/**
 * Common interface of all game scenes (2D and 3D).
 *
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener, GameActionProvider {

    default <GAME extends GameModel> GAME game() { return THE_GAME_CONTROLLER.game(); }
    default GameState gameState() { return THE_GAME_CONTROLLER.state(); }

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
    Vector2f sizeInPx();

    /**
     * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
     *
     * @param oldScene scene that was displayed before this scene
     */
    default void onSceneVariantSwitch(GameScene oldScene) {}

    /**
     * @param e event associated with opening of context menu
     * @return menu items provided by this game scene which are merged into the final context menu
     */
    default List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) { return List.of(); }

    /**
     * @return scene name as used by logging output
     */
    default String displayName() {
        return "%s (%s)".formatted(getClass().getSimpleName(), THE_GAME_CONTROLLER.selectedGameVariant());
    }
}