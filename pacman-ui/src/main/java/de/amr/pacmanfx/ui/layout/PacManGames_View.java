/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.ui.ActionBindingSupport;
import javafx.beans.binding.StringExpression;
import javafx.scene.layout.Region;

import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;

public interface PacManGames_View extends ActionBindingSupport, GameEventListener {
    Region container();
    StringExpression titleBinding();
    default void handleKeyboardInput() { runMatchingAction(theUI()); }
}
