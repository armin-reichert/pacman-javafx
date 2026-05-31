/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

/**
 * Central interface for the Pac-Man FX user interface layer.
 */
public interface GameUI {

    GameUI_View view();

    GameUI_ServiceFacade services();

    GameUI_Life life();
}
