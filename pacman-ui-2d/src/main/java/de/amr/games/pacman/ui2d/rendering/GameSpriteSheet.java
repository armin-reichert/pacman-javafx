/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.ui2d.util.SpriteSheet;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public interface GameSpriteSheet extends SpriteSheet {

    SpriteArea livesCounterSprite();
    SpriteArea highlightedMaze(int mapNumber);
    SpriteArea bonusSymbolSprite(byte symbol);
    SpriteArea bonusValueSprite(byte symbol);
    SpriteArea ghostFacingRight(byte id);
}
