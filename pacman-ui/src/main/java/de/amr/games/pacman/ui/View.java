/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.event.GameEventListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.scene.layout.Region;

public interface View extends ActionProvider, GameEventListener {
    Region layoutRoot();
    void onTick();
    default StringExpression title() {
        return Bindings.createStringBinding(() -> getClass().getSimpleName());
    }
}
