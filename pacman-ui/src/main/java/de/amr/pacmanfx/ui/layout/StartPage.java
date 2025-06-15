/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameAction;
import javafx.scene.layout.Region;
import org.tinylog.Logger;

public interface StartPage {
    Region layoutRoot();
    default void requestFocus() { layoutRoot().requestFocus(); }
    default void onEnter() { Logger.trace("{} onExit", getClass().getSimpleName()); }
    default void onExit() {
        Logger.trace("{} onExit", getClass().getSimpleName());
    }
    String currentGameVariant();
}
