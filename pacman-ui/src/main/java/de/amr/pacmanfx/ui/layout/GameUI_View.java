/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.GameUI;
import javafx.scene.Node;

import java.util.Optional;
import java.util.function.Supplier;

public interface GameUI_View extends GameEventListener {
    /**
     * @return the root node of this view in the scene graph
     */
    Node root();

    /**
     * @return the action bindings manager for this view
     */
    ActionBindingsManager actionBindingsManager();

    /**
     * @return a title expression for this view. If empty, the default main scene title is used.
     */
    default Optional<Supplier<String>> titleSupplier() { return Optional.empty(); }

    /**
     * Looks up a matching game action for the current key input. If found the action is executed (if enabled).
     *
     * @param ui the game UI
     */
    default void handleKeyboardInput(GameUI ui) {
        actionBindingsManager().matchingAction(ui.keyboard()).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }
}
