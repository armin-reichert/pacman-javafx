/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.model.GameVariant;
import javafx.scene.Node;
import org.tinylog.Logger;

import static de.amr.games.pacman.ui.Globals.THE_UI;

public interface StartPage {
    Node root();

    default void onEnter(GameVariant gameVariant) {
        Logger.info("{} selected", getClass().getSimpleName());
        THE_UI.init(gameVariant);
        root().requestFocus();
    }

    default void onExit(GameVariant gameVariant) {
        Logger.info("{} deselected", getClass().getSimpleName());
    }
}
