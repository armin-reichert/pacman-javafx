/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.ui2d.util.SpriteSheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public interface GameSpriteSheet extends SpriteSheet {

    Image getFlashingMazeImage();

    Rectangle2D getFullMazeSprite();

    Rectangle2D getEmptyMazeSprite();

    Rectangle2D livesCounterSprite();

    Rectangle2D highlightedMaze(int mapNumber);

    Image getFlashingMazesImage();

    Rectangle2D emptyMaze(int mapNumber);

    Rectangle2D filledMaze(int mapNumber);

    Rectangle2D[] clapperboardSprites();

    Rectangle2D bonusSymbolSprite(byte symbol);

    Rectangle2D bonusValueSprite(byte symbol);

    Rectangle2D ghostFacingRight(byte id);

    Rectangle2D getEnergizerSprite();
}
