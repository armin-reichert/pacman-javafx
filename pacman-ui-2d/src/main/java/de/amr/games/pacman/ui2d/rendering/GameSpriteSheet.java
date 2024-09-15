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

    Image getFlashingMazeImage();

    SpriteArea getFullMazeSprite();

    SpriteArea getEmptyMazeSprite();

    SpriteArea livesCounterSprite();

    SpriteArea highlightedMaze(int mapNumber);

    Image getFlashingMazesImage();

    SpriteArea emptyMaze(int mapNumber);

    SpriteArea filledMaze(int mapNumber);

    SpriteArea[] clapperboardSprites();

    SpriteArea bonusSymbolSprite(byte symbol);

    SpriteArea bonusValueSprite(byte symbol);

    SpriteArea ghostFacingRight(byte id);

    SpriteArea getEnergizerSprite();
}
