/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import static de.amr.games.pacman.ui2d.rendering.RectangularArea.rect;

/**
 * A sprite sheet (bekommt kein Wunschkennzeichen!)
 *
 * @author Armin Reichert
 */
public interface SpriteSheet {

    RectangularArea[] pacMunchingSprites(Direction dir);
    RectangularArea[] pacDyingSprites();
    RectangularArea[] blinkyNakedSprites();
    RectangularArea[] blinkyPatchedSprites();
    RectangularArea[] blinkyDamagedSprites();
    RectangularArea[] blinkyStretchedSprites();
    RectangularArea[] bigPacManSprites();

    RectangularArea[] msPacManDyingSprites();
    RectangularArea[] msPacManMunchingSprites(Direction dir);
    RectangularArea[] pacManMunchingSprites(Direction dir);
    RectangularArea[] clapperboardSprites();
    RectangularArea heartSprite();
    RectangularArea blueBagSprite();
    RectangularArea juniorPacSprite();

    RectangularArea[] ghostEyesSprites(Direction dir);
    RectangularArea[] ghostFlashingSprites();
    RectangularArea[] ghostFrightenedSprites();
    RectangularArea[] ghostNumberSprites();
    RectangularArea[] ghostNormalSprites(byte id, Direction dir);

    RectangularArea livesCounterSprite();
    RectangularArea bonusSymbolSprite(byte symbol);
    RectangularArea bonusValueSprite(byte symbol);

    int tileSize();

    /**
     * @param n number of tiles
     * @return pixels taken by tiles
     */
    default int tiles(int n) {
        return n * tileSize();
    }

    default RectangularArea[] rectArray(RectangularArea... areas) {
        return areas;
    }

    /**
     * @param offsetX  x-offset in spritesheet source
     * @param tileX    grid column (in tile coordinates)
     * @param tileY    grid row (in tile coordinates)
     * @param numTiles number of tiles
     * @return horizontal stripe of tiles at given grid position
     */
    default RectangularArea[] tilesRightOf(int offsetX, int tileX, int tileY, int numTiles) {
        var tiles = new RectangularArea[numTiles];
        for (int i = 0; i < numTiles; ++i) {
            tiles[i] = rect(offsetX + tiles(tileX + i), tiles(tileY), tileSize(), tileSize());
        }
        return tiles;
    }

    Image sourceImage();

    /**
     * @param r rectangular region
     * @return image copy of region
     */
    default Image subImage(RectangularArea r) {
        return subImage(r.x(), r.y(), r.width(), r.height());
    }

    /**
     * @param x      region x-coordinate
     * @param y      region y-coordinate
     * @param width  region width
     * @param height region height
     * @return image copy of region
     */
    default Image subImage(int x, int y, int width, int height) {
        var image = new WritableImage(width, height);
        image.getPixelWriter().setPixels(0, 0, width, height, sourceImage().getPixelReader(), x, y);
        return image;
    }
}