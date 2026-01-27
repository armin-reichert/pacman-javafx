/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import javafx.scene.layout.Region;
import org.tinylog.Logger;

public interface GameUI_StartPage {

    Region layoutRoot();

    void init(GameUI ui);

    void onEnterStartPage(GameUI ui);

    default void onExitStartPage(GameUI ui) {
        Logger.info("Exit start page {}", this);
    }

    String title();
}
