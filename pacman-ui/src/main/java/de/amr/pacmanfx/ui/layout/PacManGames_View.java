/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.ui.ActionBindingManager;
import de.amr.pacmanfx.ui.GameUI;
import javafx.beans.binding.StringExpression;
import javafx.scene.Node;

import java.util.Optional;

public interface PacManGames_View extends GameEventListener {
    /**
     * @return the root node of this view
     */
    Node rootNode();

    /**
     * @return the action bindings defined for this view
     */
    ActionBindingManager actionBindingMap();

    /**
     * @return a title expression for this view. If empty, the default main scene title is used.
     */
    default Optional<? extends StringExpression> title() { return Optional.empty(); }

    /**
     * Looks up a matching game action for the current key input. If found the action is executed (if enabled).
     *
     * @param ui the game UI
     */
    default void handleKeyboardInput(GameUI ui) {
        actionBindingMap().matchingGameAction(ui.theKeyboard()).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }
}
