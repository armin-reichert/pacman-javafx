/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.RectShort;

public interface HUD_SymbolSpriteProvider {

    RectShort[] bonusSymbolSprites();

    RectShort livesCounterSymbol();
}
