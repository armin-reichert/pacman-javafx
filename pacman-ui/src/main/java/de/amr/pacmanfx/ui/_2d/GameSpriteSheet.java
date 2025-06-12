/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

public interface GameSpriteSheet extends SpriteSheet {
    RectArea bonusSymbolSprite(byte symbol);
    RectArea bonusValueSprite(byte symbol);
}