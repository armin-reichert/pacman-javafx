/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.GameVariant;
import javafx.scene.Node;
import org.tinylog.Logger;

public interface StartPage {
    Node root();
    default void requestFocus() { root().requestFocus(); }
    default void onEnter() { Logger.info("{} onExit", getClass().getSimpleName()); }
    default void onExit() {
        Logger.info("{} onExit", getClass().getSimpleName());
    }
    GameVariant currentGameVariant();
}
