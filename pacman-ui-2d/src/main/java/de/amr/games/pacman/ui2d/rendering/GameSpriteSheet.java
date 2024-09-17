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

    SpriteArea livesCounterSprite();
    SpriteArea bonusSymbolSprite(byte symbol);
    SpriteArea bonusValueSprite(byte symbol);
}
