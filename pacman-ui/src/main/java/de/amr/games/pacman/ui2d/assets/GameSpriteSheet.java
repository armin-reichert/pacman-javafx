/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.assets;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Sprite sheet interface for all game variants.
 *
 * @author Armin Reichert
 */
public interface GameSpriteSheet {

    static RectArea[] rectAreas(RectArea... areas) {
        return areas;
    }

    static ImageArea imageArea(Image sourceImage, int x, int y, int width, int height) {
        return new ImageArea(sourceImage, new RectArea(x, y, width, height));
    }

    default ImageArea imageArea(int x, int y, int width, int height) {
        return imageArea(sourceImage(), x, y, width, height);
    }

    RectArea NO_SPRITE  = RectArea.PIXEL;

    Image sourceImage();

    /**
     * @param area rectangular region
     * @return image copy of region
     */
    default Image subImage(RectArea area) {
        return subImage(area.x(), area.y(), area.width(), area.height());
    }

    /**
     * @param x      region x-coordinate
     * @param y      region y-coordinate
     * @param width  region width
     * @param height region height
     * @return image copy of region
     */
    default Image subImage(int x, int y, int width, int height) {
        var subImage = new WritableImage(width, height);
        subImage.getPixelWriter().setPixels(0, 0, width, height, sourceImage().getPixelReader(), x, y);
        return subImage;
    }

    // Game-related stuff

    RectArea[] pacMunchingSprites(Direction dir);
    RectArea[] pacDyingSprites();

    RectArea[] ghostEyesSprites(Direction dir);
    RectArea[] ghostFlashingSprites();
    RectArea[] ghostFrightenedSprites();
    RectArea[] ghostNumberSprites();
    RectArea[] ghostNormalSprites(byte id, Direction dir);

    RectArea livesCounterSprite();
    RectArea bonusSymbolSprite(byte symbol);
    RectArea bonusValueSprite(byte symbol);
}