/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import javafx.scene.layout.Region;
import org.tinylog.Logger;

public interface GameUI_StartPage {

    Region layoutRoot();

    void init(GameUI ui);

    default void onEnterStartPage(GameUI ui) {
        Logger.info("Enter start page {}", this);
    }

    default void onExitStartPage(GameUI ui) {
        Logger.info("Exit start page {}", this);
    }

    String title();
}
