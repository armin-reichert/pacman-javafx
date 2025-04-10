/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.GameVariant;
import javafx.scene.layout.Region;
import org.tinylog.Logger;

public interface StartPage {
    Region layoutRoot();
    default void requestFocus() { layoutRoot().requestFocus(); }
    default void onEnter() { Logger.trace("{} onExit", getClass().getSimpleName()); }
    default void onExit() {
        Logger.trace("{} onExit", getClass().getSimpleName());
    }
    GameVariant currentGameVariant();
}
