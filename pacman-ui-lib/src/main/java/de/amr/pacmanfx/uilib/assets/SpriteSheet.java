/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import de.amr.pacmanfx.lib.Sprite;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Sprite sheet interface.
 */
public interface SpriteSheet {
    Image sourceImage();

    /**
     * @param x      region x-coordinate
     * @param y      region y-coordinate
     * @param width  region width
     * @param height region height
     * @return image for given region
     */
    default Image subImage(int x, int y, int width, int height) {
        var section = new WritableImage(width, height);
        section.getPixelWriter().setPixels(0, 0, width, height, sourceImage().getPixelReader(), x, y);
        return section;
    }

    default Image subImage(Sprite sprite) {
        return subImage(sprite.x(), sprite.y(), sprite.width(), sprite.height());
    }
}