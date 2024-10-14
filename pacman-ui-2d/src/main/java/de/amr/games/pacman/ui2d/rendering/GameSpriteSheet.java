/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.stream.IntStream;

/**
 * Sprite sheet interface for all game variants.
 *
 * @author Armin Reichert
 */
public interface GameSpriteSheet {

    static RectArea[] rectArray(RectArea... areas) {
        return areas;
    }

    RectArea NO_SPRITE  = RectArea.PIXEL;
    RectArea[] NO_SPRITES = IntStream.range(0, 10).mapToObj(i -> NO_SPRITE).toArray(RectArea[]::new);

    // Common
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

    // Ms. Pac-Man only
    default RectArea[] pacManMunchingSprites(Direction dir) { return NO_SPRITES; }
    default RectArea[] clapperboardSprites() { return NO_SPRITES; }
    default SpriteAnimation createStorkFlyingAnimation()  { return null; }

    // Pac-Man only
    default RectArea ghostFacingRight(byte ghostID) {
        return NO_SPRITE;
    }
    default RectArea[] blinkyNakedSprites() { return NO_SPRITES; }
    default RectArea[] blinkyPatchedSprites() { return NO_SPRITES; }
    default RectArea[] blinkyDamagedSprites() { return NO_SPRITES; }
    default RectArea[] blinkyStretchedSprites() { return NO_SPRITES; }
    default RectArea[] bigPacManSprites() { return NO_SPRITES; }

    Image sourceImage();

    /**
     * @param r rectangular region
     * @return image copy of region
     */
    default Image subImage(RectArea r) {
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