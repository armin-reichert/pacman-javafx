/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.uilib.ActionBindingsSupport;
import javafx.beans.binding.StringExpression;
import javafx.scene.layout.Region;

public interface PacManGames_View extends ActionBindingsSupport, GameEventListener {
    Region container();
    StringExpression titleBinding();
}
