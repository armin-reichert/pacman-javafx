/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.startpages;

import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.runtime.action.GameActionContext;
import javafx.scene.layout.Pane;
import org.tinylog.Logger;

/**
 * Represents a single start page in the application's start‑view system.
 */
public interface StartPage {

    Pane rootPane();

    void setActionContext(GameActionContext actionContext);

    void onEnter();

    default void onExit() {
        Logger.info("Exit start page {}", this);
    }

    void onInput(Input input);

    String title();
}
