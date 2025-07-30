/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.ui.ActionBindingManager;
import de.amr.pacmanfx.ui.GameUI;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.Node;

public interface PacManGames_View extends GameEventListener {
    Node rootNode();

    ObservableStringValue title();

    ActionBindingManager actionBindingMap();

    default void handleKeyboardInput(GameUI ui) {
        actionBindingMap().matchingAction(ui.theKeyboard()).ifPresent(ui::runAction);
    }
}
