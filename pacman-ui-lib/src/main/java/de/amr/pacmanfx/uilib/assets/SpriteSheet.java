/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import de.amr.pacmanfx.lib.math.RectShort;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Sprite sheet interface.
 *
 * @param <SID> sprite ID enum type
 */
public interface SpriteSheet<SID extends Enum<SID>> {

    /**
     * @return the sprite sheet image
     */
    Image sourceImage();

    /**
     * @param id a sprite ID
     * @return the rectangular area in the sprite sheet where this sprite is located
     */
    RectShort sprite(SID id);

    /**
     * @param id a sprite sequence ID
     * @return array of rectangular sprite sheet areas where sprites are located
     */
    RectShort[] spriteSequence(SID id);

    /**
     * @param x      x-coordinate of rectangular area
     * @param y      y-coordinate of rectangular area
     * @param width  width of rectangular area
     * @param height height of rectangular area
     * @return image cropped from sprite sheet for given area
     */
    default Image image(int x, int y, int width, int height) {
        var image = new WritableImage(width, height);
        image.getPixelWriter().setPixels(0, 0, width, height, sourceImage().getPixelReader(), x, y);
        return image;
    }

    /**
     * @param sprite rectangular area in sprite sheet
     * @return image cropped from sprite sheet for given area
     */
    default Image image(RectShort sprite) {
        return image(sprite.x(), sprite.y(), sprite.width(), sprite.height());
    }

    default Image image(SID id) {
        return image(sprite(id));
    }
}