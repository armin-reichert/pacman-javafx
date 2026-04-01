/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import javafx.scene.SubScene;
import javafx.scene.input.ScrollEvent;

import java.util.Optional;

/**
 * Common interface of all game scenes (2D and 3D).
 */
public interface GameScene extends GameEventListener, Disposable {

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
    default GameContext gameContext() {
        return ui().gameContext();
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

    void onEmbed(GameUI ui);

    /**
     * Called when a key combination has been pressed inside this game scene. By default, the first matching action
     * defined in the action bindings is executed.
     */
    default void onKeyboardInput() {
        actionBindings().findMatchingAction(GameUI.KEYBOARD).ifPresent(action -> action.executeIfEnabled(ui()));
    }

    /**
     * Called when a scroll event (mouse wheel event) has been triggered inside this game scene
     * @param scrollEvent the scroll event
     */
    default void onScroll(ScrollEvent scrollEvent) {}

    @Override
    default void onStopAllSounds(StopAllSoundsEvent e) {
        ui().soundManager().stopAll();
    }

    /**
     * @param game the current game
     * @return context menu provided by this game scene which is merged into the view's context menu
     */
    default Optional<GameUI_ContextMenu> supplyContextMenu(Game game) {
        return Optional.empty();
    }
}