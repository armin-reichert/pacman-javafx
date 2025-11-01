/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import javafx.scene.layout.Region;

public interface StartPage {
    Region layoutRoot();
    default void onEnter(GameUI ui) {}
    default void onExit(GameUI ui) {}
    String title();
}
