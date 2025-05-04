/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.uilib.ActionProvider;
import de.amr.games.pacman.uilib.input.Keyboard;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.scene.layout.Region;

import static de.amr.games.pacman.ui.Globals.THE_KEYBOARD;

public interface View extends ActionProvider, GameEventListener {
    @Override
    default Keyboard keyboard() { return THE_KEYBOARD; }
    Region layoutRoot();
    void update();
    default void draw() {}
    default StringExpression title() {
        return Bindings.createStringBinding(() -> getClass().getSimpleName());
    }
}
