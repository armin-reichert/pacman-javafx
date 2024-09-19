/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.ui2d.util.SpriteSheet;

/**
 * @author Armin Reichert
 */
public interface GameSpriteSheet extends SpriteSheet {

    RectangularArea livesCounterSprite();
    RectangularArea bonusSymbolSprite(byte symbol);
    RectangularArea bonusValueSprite(byte symbol);
}
