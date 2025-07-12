/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.ui.ActionBindingMap;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.Node;

import static de.amr.pacmanfx.ui.GameUI.theUI;

public interface PacManGames_View extends GameEventListener {
    Node rootNode();
    ObservableStringValue title();
    ActionBindingMap actionBindingMap();
    default void handleKeyboardInput(GameContext gameContext) { actionBindingMap().runMatchingAction(theUI(), gameContext); }
}
