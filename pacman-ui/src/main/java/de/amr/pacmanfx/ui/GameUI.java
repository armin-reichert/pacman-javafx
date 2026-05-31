/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.view.GameUI_View;

/**
 * Central interface for the Pac-Man FX user interface layer.
 */
public interface GameUI extends GameUI_Life {

    GameUI_View view();

    GameUI_ServicesAccess access();
}
