/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameUI;
import javafx.scene.layout.Region;

public interface StartPage {
    Region layoutRoot();
    String currentGameVariant();
    default void onEnter(GameUI ui) {}
    default void onExit(GameUI ui) {}
}
