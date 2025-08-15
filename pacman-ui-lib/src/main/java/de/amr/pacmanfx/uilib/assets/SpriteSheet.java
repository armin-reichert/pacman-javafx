/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import de.amr.pacmanfx.lib.RectShort;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Sprite sheet interface.
 *
 * @param <SID> sprite ID enum type
 */
public interface SpriteSheet<SID extends Enum<SID>> {

    Image sourceImage();

    SpriteMap<SID> content();

    /**
     * @param x      region x-coordinate
     * @param y      region y-coordinate
     * @param width  region width
     * @param height region height
     * @return image for given region
     */
    default Image image(int x, int y, int width, int height) {
        var section = new WritableImage(width, height);
        section.getPixelWriter().setPixels(0, 0, width, height, sourceImage().getPixelReader(), x, y);
        return section;
    }

    default Image image(RectShort sprite) {
        return image(sprite.x(), sprite.y(), sprite.width(), sprite.height());
    }
}