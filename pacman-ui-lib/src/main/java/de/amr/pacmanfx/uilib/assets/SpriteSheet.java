/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.assets;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Sprite sheet interface.
 *
 * @param <K> key type for accessing sprites by key
 */
public interface SpriteSheet<K extends Named> {

    static RectShort spriteOrDefault(RectShort[] sprites, int index) {
        if (0 <= index && index < sprites.length) {
            return sprites[index];
        }
        return RectShort.NULL_RECTANGLE;
    }

    /**
     * @return the sprite sheet image
     */
    Image sourceImage();

    SpriteMap<K> spriteMap();

    /**
     * @param key sprite sequence key
     * @return array of rectangular sprite sheet areas where sprites are located
     */
    default RectShort[] findSpriteSequence(K key) {
        return spriteMap().spriteSequence(key);
    }

    default RectShort findSprite(K key) {
        return findSpriteSequence(key)[0];
    }

    /**
     * @param key sprite key
     * @return image cropped from sprite sheet for given sprite
     */
    default Image image(K key) {
        return image(findSprite(key));
    }

    /**
     * @param x      x-coordinate of rectangular area
     * @param y      y-coordinate of rectangular area
     * @param width  width of rectangular area
     * @param height height of rectangular area
     * @return image cropped from sprite sheet for given area
     */
    default Image image(int x, int y, int width, int height) {
        final var image = new WritableImage(width, height);
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
}