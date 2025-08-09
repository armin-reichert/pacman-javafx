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
    Node rootNode();
    default Optional<? extends StringExpression> title() { return Optional.empty(); }
    ActionBindingManager actionBindingMap();
    default void handleKeyboardInput(GameUI ui) {
        actionBindingMap().matchingAction(ui.theKeyboard()).ifPresent(ui::runAction);
    }
}
