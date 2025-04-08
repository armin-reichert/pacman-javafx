/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.uilib.Ufx;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.tinylog.Logger;

import static de.amr.games.pacman.ui.Globals.THE_ASSETS;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public interface StartPage {
    Region layoutRoot();
    default void requestFocus() { layoutRoot().requestFocus(); }
    default void onEnter() { Logger.info("{} onExit", getClass().getSimpleName()); }
    default void onExit() {
        Logger.info("{} onExit", getClass().getSimpleName());
    }
    GameVariant currentGameVariant();
}
