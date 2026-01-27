/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.event.GameEventListener;
import javafx.scene.Node;

import java.util.Optional;
import java.util.function.Supplier;

public interface GameUI_View extends GameEventListener {
    /**
     * @return the action bindings manager for this view
     */
    ActionBindingsManager actionBindingsManager();

    /**
     * Looks up a matching game action for the current key input. If found the action is executed (if enabled).
     *
     * @param ui the game UI
     */
    default void onKeyboardInput(GameUI ui) {
        actionBindingsManager().matchingAction(GameUI.KEYBOARD).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }

    /**
     * @return the root node of this view in the scene graph
     */
    Node root();

    /**
     * @return a title expression for this view. If empty, the default main scene title is used.
     */
    default Optional<Supplier<String>> titleSupplier() {
        return Optional.empty();
    }

    void onEnter();

    void onExit();

    default void render() {}
}