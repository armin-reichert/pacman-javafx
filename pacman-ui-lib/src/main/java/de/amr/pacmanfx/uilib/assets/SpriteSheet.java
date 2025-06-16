/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import de.amr.pacmanfx.lib.Sprite;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Sprite sheet interface.
 *
 * @param <SID> sprite ID type
 */
public interface SpriteSheet<SID> {
    Image sourceImage();
    Map<SID, Object> spriteMap();

    default Sprite sprite(SID id) {
        requireNonNull(id);
        if (!spriteMap().containsKey(id)) {
            throw new IllegalArgumentException("Unknown sprite ID '%s'".formatted(id));
        }
        Object value = spriteMap().get(id);
        if (value == null) {
            throw new IllegalArgumentException("Sprite value is null for id '%s'".formatted(id));
        }
        if (value instanceof Sprite) {
            return (Sprite) value;
        }
        throw new IllegalArgumentException("Value stored in sprite map for id '%s' is no sprite but of type %s"
            .formatted(id, value.getClass()));
    }

    default Sprite[] spriteSeq(SID id) {
        requireNonNull(id);
        if (!spriteMap().containsKey(id)) {
            throw new IllegalArgumentException("Unknown sprite ID '%s'".formatted(id));
        }
        Object value = spriteMap().get(id);
        if (value == null) {
            throw new IllegalArgumentException("Sprite value is null for id '%s'".formatted(id));
        }
        if (value instanceof Sprite[]) {
            return (Sprite[]) value;
        }
        throw new IllegalArgumentException("Value stored in sprite map for id '%s' is no sprite array but of type %s"
            .formatted(id, value.getClass()));
    }

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

    default Image image(Sprite sprite) {
        return image(sprite.x(), sprite.y(), sprite.width(), sprite.height());
    }
}