/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.uilib.ActionBindingsProvider;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.scene.layout.Region;

import static de.amr.pacmanfx.ui.PacManGames_Env.theKeyboard;

//TODO Maybe this should become a FX scene?
public interface PacManGames_View extends ActionBindingsProvider, GameEventListener {
    @Override
    default Keyboard keyboard() { return theKeyboard(); }
    Region layoutRoot();
    void update();
    default void draw() {}
    default StringExpression title() {
        return Bindings.createStringBinding(() -> getClass().getSimpleName());
    }
}
