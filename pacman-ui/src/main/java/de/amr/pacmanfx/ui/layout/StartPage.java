/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import javafx.scene.layout.Region;
import org.tinylog.Logger;

public interface StartPage {
    Region layoutRoot();
    default void onEnter() { Logger.trace("{} onExit", getClass().getSimpleName()); }
    default void onExit() {
        Logger.trace("{} onExit", getClass().getSimpleName());
    }
    String currentGameVariant();
}
