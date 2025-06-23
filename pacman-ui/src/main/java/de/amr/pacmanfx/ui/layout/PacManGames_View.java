/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.ui.ActionBindingSupport;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.Node;

import static de.amr.pacmanfx.ui.PacManGames.theUI;

public interface PacManGames_View extends ActionBindingSupport, GameEventListener {
    Node rootNode();
    ObservableStringValue title();
    default void handleKeyboardInput() { runMatchingAction(theUI()); }
}
