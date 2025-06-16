/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import javafx.scene.layout.Region;

public interface StartPage {
    Region layoutRoot();
    String currentGameVariant();
    void onEnter();
    void onExit();
}
