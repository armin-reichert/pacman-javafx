/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.model.Game;
import javafx.scene.SubScene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.ScrollEvent;

import java.util.List;
import java.util.Optional;

/**
 * Common interface of all game scenes (2D and 3D).
 */
public interface GameScene extends GameEventListener {

    /**
     * @return the game UI
     */
    GameUI ui();

    /**
     * @return (optional) JavaFX subscene associated with this game scene. 2D scenes without camera do not need one.
     */
    default Optional<SubScene> optSubScene() {
        return Optional.empty();
    }

    /**
     * @return the global context providing access to some global data as the currently selected game variant or the
     *         coin mechanism used by Arcade games
     */
    default GameContext context() {
        return ui().context();
    }

    /**
     * @return the action bindings defined for this game scene
     */
    ActionBindingsManager actionBindings();

    /**
     * Called when the scene becomes the current one.
     */
    void init(Game game);

    /**
     * Called when the scene needs to be updated.
     */
    void update(Game game);

    /**
     * Called when the scene ends and gets replaced by another scene.
     */
    void end(Game game);

    /**
     * Called when a key combination has been pressed inside this game scene. By default, the first matching action
     * defined in the action bindings is executed.
     */
    default void onKeyboardInput() {
        actionBindings().matchingAction(GameUI.KEYBOARD).ifPresent(action -> action.executeIfEnabled(ui()));
    }

    /**
     * Called when a scroll event (mouse wheel event) has been triggered inside this game scene
     * @param scrollEvent the scroll event
     */
    default void onScroll(ScrollEvent scrollEvent) {}

    @Override
    default void onStopAllSounds(GameEvent event) {
        ui().soundManager().stopAll();
    }

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
     * @param game the current game
     * @param menuEvent event associated with opening of context menu
     * @return menu items provided by this game scene which are merged into the final context menu
     */
    default List<MenuItem> supplyContextMenuItems(Game game, ContextMenuEvent menuEvent) {
        return List.of();
    }
}