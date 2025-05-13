/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.uilib.ActionProvider;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.scene.layout.Region;

import static de.amr.pacmanfx.ui.PacManGamesEnv.theKeyboard;

public interface View extends ActionProvider, GameEventListener {
    @Override
    default Keyboard keyboard() { return theKeyboard(); }
    Region layoutRoot();
    void update();
    default void draw() {}
    default StringExpression title() {
        return Bindings.createStringBinding(() -> getClass().getSimpleName());
    }
}
