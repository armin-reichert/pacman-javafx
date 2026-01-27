/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.Disposable;

import java.util.List;

/**
 * The map renderer needs a suitably colored maze image and a list of colored map images used to render the
 * flashing animation. In levels 28-31 of non-Arcade maps, the flashing animation uses differently colored map images.
 *
 * @param mapImage the image used when playing the level
 * @param flashingMapImages the images used for the flashing animation at the end of the level
 */
public record MapImageSet(ColorSchemedImage mapImage, List<ColorSchemedImage> flashingMapImages) implements Disposable {

    @Override
    public void dispose() {
        if (flashingMapImages != null) {
            flashingMapImages.clear();
        }
    }

    @Override
    public String toString() {
        return "ColoredSchemedImage{"
            + "mazeImage=" + mapImage
            + ", flashingMazeImages=" + flashingMapImages
            + "}";
    }
}
