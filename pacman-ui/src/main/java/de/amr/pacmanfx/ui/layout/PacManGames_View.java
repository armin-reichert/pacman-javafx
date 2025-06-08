/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.uilib.ActionBindingsSupport;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.scene.layout.Region;

import static de.amr.pacmanfx.ui.PacManGames_Env.theKeyboard;

public interface PacManGames_View extends ActionBindingsSupport, GameEventListener {
    @Override
    default Keyboard keyboard() { return theKeyboard(); }
    Region layoutRoot();
    default StringExpression titleBinding() {
        return Bindings.createStringBinding(() -> getClass().getSimpleName());
    }
}
