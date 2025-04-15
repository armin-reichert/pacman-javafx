/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib.assets;

import de.amr.games.pacman.lib.RectArea;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Sprite sheet interface for all game variants.
 *
 * @author Armin Reichert
 */
public interface SpriteSheet {

    static RectArea[] rectAreaArray(RectArea... areas) {
        return areas;
    }

    RectArea NO_SPRITE  = RectArea.PIXEL;

    Image sourceImage();

    /**
     * @param r rectangular region
     * @return image copy of region
     */
    default Image crop(RectArea r) {
        return crop(r.x(), r.y(), r.width(), r.height());
    }

    /**
     * @param x      region x-coordinate
     * @param y      region y-coordinate
     * @param width  region width
     * @param height region height
     * @return image copy of region
     */
    default Image crop(int x, int y, int width, int height) {
        var section = new WritableImage(width, height);
        section.getPixelWriter().setPixels(0, 0, width, height, sourceImage().getPixelReader(), x, y);
        return section;
    }
}