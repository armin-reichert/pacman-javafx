/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.startpages;

import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.layout.Pane;
import org.tinylog.Logger;

/**
 * Represents a single start page in the application's start‑view system.
 */
public interface StartPage {

    Pane rootPane();

    void connect(Game game);

    void onEnter();

    default void onExit() {
        Logger.info("Exit start page {}", this);
    }

    String title();
}
