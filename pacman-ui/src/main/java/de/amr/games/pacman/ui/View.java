/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.event.GameEventListener;
import javafx.scene.Node;

public interface View extends GameActionProvider, GameEventListener {
    Node node();
    void onTick();
}
