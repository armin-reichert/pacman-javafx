/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.stream.IntStream;

import static de.amr.games.pacman.ui2d.rendering.RectangularArea.rect;

/**
 * A sprite sheet (bekommt kein Wunschkennzeichen!)
 *
 * @author Armin Reichert
 */
public interface SpriteSheet {

    RectangularArea   NO_SPRITE  = RectangularArea.PIXEL;
    RectangularArea[] NO_SPRITES = IntStream.range(0, 10).mapToObj(i -> NO_SPRITE).toArray(RectangularArea[]::new);

    // Common
    RectangularArea[] pacMunchingSprites(Direction dir);
    RectangularArea[] pacDyingSprites();

    RectangularArea[] ghostEyesSprites(Direction dir);
    RectangularArea[] ghostFlashingSprites();
    RectangularArea[] ghostFrightenedSprites();
    RectangularArea[] ghostNumberSprites();
    RectangularArea[] ghostNormalSprites(byte id, Direction dir);

    RectangularArea livesCounterSprite();
    RectangularArea bonusSymbolSprite(byte symbol);
    RectangularArea bonusValueSprite(byte symbol);

    // Ms. Pac-Man variants
    default RectangularArea[] pacManMunchingSprites(Direction dir) { return NO_SPRITES; }
    default RectangularArea[] clapperboardSprites() { return NO_SPRITES; }
    default RectangularArea heartSprite() { return NO_SPRITE; }
    default RectangularArea blueBagSprite() { return NO_SPRITE; }
    default RectangularArea juniorPacSprite() { return NO_SPRITE; }
    default SpriteAnimation createStorkFlyingAnimation()  { return null; }

    // Pac-Man variants
    default RectangularArea[] blinkyNakedSprites() { return NO_SPRITES; }
    default RectangularArea[] blinkyPatchedSprites() { return NO_SPRITES; }
    default RectangularArea[] blinkyDamagedSprites() { return NO_SPRITES; }
    default RectangularArea[] blinkyStretchedSprites() { return NO_SPRITES; }
    default RectangularArea[] bigPacManSprites() { return NO_SPRITES; }

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