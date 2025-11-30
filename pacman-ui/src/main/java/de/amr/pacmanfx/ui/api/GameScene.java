/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.ScrollEvent;

import java.util.List;

/**
 * Common interface of all game scenes (2D and 3D).
 */
public interface GameScene extends GameEventListener {

    GameUI ui();

    default GameContext context() {
        return ui().context();
    }

    ActionBindingsManager actionBindings();

    default void handleKeyboardInput() {
        actionBindings().matchingAction(ui().keyboard()).ifPresent(
            gameAction -> gameAction.executeIfEnabled(ui()));
    }

    default void handleScrollEvent(ScrollEvent e) {}

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

    @Override
    default void onStopAllSounds(GameEvent event) { ui().soundManager().stopAll(); }

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
}