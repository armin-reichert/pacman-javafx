/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.event.DefaultGameEventListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;

import java.util.List;

import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.ui.PacManGames.theUI;

/**
 * Common interface of all game scenes (2D and 3D).
 *
 * @author Armin Reichert
 */
public interface GameScene extends DefaultGameEventListener, ActionBindingSupport {

    /**
     * By default, the first matching game action is executed.
     */
    default void handleKeyboardInput() { runMatchingAction(theUI()); }

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
     * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
     *
     * @param scene2D 2D scene that was displayed before this scene
     */
    default void onSwitch_2D_3D(GameScene scene2D) {}

    /**
     * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
     *
     * @param scene3D 3D scene that was displayed before this scene
     */
    default void onSwitch_3D_2D(GameScene scene3D) {}

    /**
     * @param e event associated with opening of context menu
     * @param menu the context menu
     * @return menu items provided by this game scene which are merged into the final context menu
     */
    default List<MenuItem> supplyContextMenuItems(ContextMenuEvent e, ContextMenu menu) { return List.of(); }

    /**
     * @return scene name as used by logging output
     */
    default String displayName() {
        return "%s (%s)".formatted(getClass().getSimpleName(), theGameController().selectedGameVariant());
    }
}