/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib;

import de.amr.pacmanfx.lib.math.RectShort;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Map;
import java.util.function.BiPredicate;

public final class UfxImages {

    private UfxImages() {}


    /**
     * Recolors pixels in the source image according to the given color mapping.
     * Only exact color matches are replaced.
     *
     * @param source  the source image
     * @param changes a map from original colors to replacement colors
     * @return a new image with applied color changes
     */
    public static Image recolorImage(Image source, Map<Color, Color> changes) {
        WritableImage target = new WritableImage((int) source.getWidth(), (int) source.getHeight());
        PixelReader sourceReader = source.getPixelReader();
        PixelWriter targetWriter = target.getPixelWriter();
        for (int x = 0; x < source.getWidth(); ++x) {
            for (int y = 0; y < source.getHeight(); ++y) {
                final Color color = sourceReader.getColor(x, y);
                final Color newColor = changes.get(color);
                if (newColor != null) {
                    targetWriter.setColor(x, y, newColor);
                }
            }
        }
        return target;
    }

    /**
     * Converts the given image to greyscale using simple RGB averaging.
     *
     * @param source the source image (may be {@code null})
     * @return a greyscale version of the image, or {@code null} if the source is null
     */
    public static Image imageToGreyscale(Image source) {
        if (source == null) {
            return null;
        }
        int width = (int) source.getWidth(), height = (int) source.getHeight();
        WritableImage target = new WritableImage(width, height);
        PixelReader r = source.getPixelReader();
        PixelWriter w = target.getPixelWriter();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                Color color = r.getColor(x, y);
                double g = (color.getRed() + color.getBlue() + color.getGreen()) / 3.0;
                w.setColor(x, y, Color.gray(g));
            }
        }
        return target;
    }

    /**
     * Extracts a rectangular region from the source image.
     *
     * @param sourceImage the source image
     * @param r           the region to extract
     * @return a new image containing the specified region
     */
    public static Image subImage(Image sourceImage, RectShort r) {
        return subImage(sourceImage, r.x(), r.y(), r.width(), r.height());
    }

    /**
     * Extracts a rectangular region from the source image.
     *
     * @param sourceImage the source image
     * @param x           region x-coordinate
     * @param y           region y-coordinate
     * @param width       region width
     * @param height      region height
     * @return a new image containing the specified region
     */
    public static Image subImage(Image sourceImage, int x, int y, int width, int height) {
        var section = new WritableImage(width, height);
        section.getPixelWriter().setPixels(0, 0, width, height, sourceImage.getPixelReader(), x, y);
        return section;
    }

    /**
     * Replaces specific fill, stroke, and pellet colors in the source image.
     * Logs warnings if expected colors are not found.
     *
     * @param sourceImage the source image
     * @param fillFrom    original fill color
     * @param strokeFrom  original stroke color
     * @param pelletFrom  original pellet color
     * @param fillTo      replacement fill color
     * @param strokeTo    replacement stroke color
     * @param pelletTo    replacement pellet color
     * @return a recolored image
     */
    public static Image replaceImageColors(
        Image sourceImage,
        Color fillFrom, Color strokeFrom, Color pelletFrom,
        Color fillTo, Color strokeTo, Color pelletTo) {

        WritableImage targetImage = new WritableImage((int) sourceImage.getWidth(), (int) sourceImage.getHeight());
        PixelReader sourceReader = sourceImage.getPixelReader();
        PixelWriter targetWriter = targetImage.getPixelWriter();
        boolean fillColorFound = false, strokeColorFound = false, pelletColorFound = false;
        for (int x = 0; x < sourceImage.getWidth(); ++x) {
            for (int y = 0; y < sourceImage.getHeight(); ++y) {
                final Color color = sourceReader.getColor(x, y);
                if (color.equals(fillFrom)) {
                    fillColorFound = true;
                    targetWriter.setColor(x, y, fillTo);
                }
                else if (color.equals(strokeFrom)) {
                    strokeColorFound = true;
                    targetWriter.setColor(x, y, strokeTo);
                }
                else if (color.equals(pelletFrom)) {
                    pelletColorFound = true;
                    targetWriter.setColor(x, y, pelletTo);
                }
            }
        }
        if (!fillColorFound) {
            Logger.warn("Fill color {} not found in image", fillFrom);
        }
        if (!strokeColorFound) {
            Logger.warn("Stroke color {} not found in image", strokeFrom);
        }
        if (!pelletColorFound) {
            Logger.warn("Pellet color {} not found in image", pelletFrom);
        }
        return targetImage;
    }

    /**
     * Recolors pixels inside a masked area according to the given predicate.
     *
     * @param image            the source image
     * @param insideMaskedArea predicate determining whether a pixel is inside the mask
     * @param maskColor        the replacement color
     * @return a new image with masked pixels recolored
     */
    public static Image recolorPixels(Image image, BiPredicate<Integer, Integer> insideMaskedArea, Color maskColor) {
        WritableImage result = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter w = result.getPixelWriter();
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                if (insideMaskedArea.test(x, y)) {
                    w.setColor(x, y, maskColor);
                } else {
                    w.setColor(x, y, image.getPixelReader().getColor(x, y));
                }
            }
        }
        return result;
    }
}
