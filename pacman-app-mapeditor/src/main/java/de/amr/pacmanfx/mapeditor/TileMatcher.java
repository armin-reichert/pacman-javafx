/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.tilemap.FoodTile;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;

public class TileMatcher {

    public record PixelScheme(int backgroundColor, int fillColor, int strokeColor, int doorColor, int foodColor) {

        public PixelScheme(Color backgroundColor, Color fillColor, Color strokeColor, Color doorColor, Color foodColor) {
            this(toArgb(backgroundColor), toArgb(fillColor), toArgb(strokeColor), toArgb(doorColor), toArgb(foodColor));
        }

        public boolean isBackgroundPixel(int pixel) { return pixel == backgroundColor; }
        public boolean isStrokePixel(int pixel) { return pixel == strokeColor; }
        public boolean isFillPixel(int pixel) { return pixel == fillColor; }
        public boolean isDoorPixel(int pixel) { return pixel == doorColor; }
        public boolean isFoodPixel(int pixel) { return pixel == foodColor; }

        private static int toArgb(Color color) {
            int a = (int) (color.getOpacity() * 255);
            int r = (int) (color.getRed() * 255);
            int g = (int) (color.getGreen() * 255);
            int b = (int) (color.getBlue() * 255);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    private final PixelScheme pixelScheme;

    public TileMatcher(Color backgroundColor, Color fillColor, Color strokeColor, Color doorColor, Color foodColor) {
        pixelScheme = new PixelScheme(backgroundColor, fillColor, strokeColor, doorColor, foodColor);
    }

    public byte matchFoodTile(int[] tilePixels) {
        if (isEnergizer(tilePixels)) return FoodTile.ENERGIZER.code();
        if (isPellet(tilePixels)) return FoodTile.PELLET.code();
        return FoodTile.EMPTY.code();
    }

    private boolean isEnergizer(int[] tilePixels) {
        return Arrays.stream(tilePixels).filter(pixelScheme::isFoodPixel).count() > 50;
    }

    private boolean isPellet(int[] tilePixels) {
        return subset(tilePixels, 27, 28, 35, 36).allMatch(pixelScheme::isFoodPixel)
                && row(tilePixels, 0).allMatch(pixelScheme::isBackgroundPixel)
                && row(tilePixels, 1).allMatch(pixelScheme::isBackgroundPixel);
    }

    public byte matchTerrainTile(int[] tilePixels) {
        if (matchesAngularDoubleNWCorner(tilePixels)) return DARC_NW.$;
        if (matchesAngularDoubleSWCorner(tilePixels)) return DARC_SW.$;
        if (matchesAngularDoubleSECorner(tilePixels)) return DARC_SE.$;
        if (matchesAngularDoubleNECorner(tilePixels)) return DARC_NE.$;
        //TODO: what if door and fill color are equal?
        if (matchesDoor(tilePixels))     return DOOR.$;
        if (matchesHWall(tilePixels))    return WALL_H.$;
        if (matchesVWall(tilePixels))    return WALL_V.$;
        if (matchesNWCorner(tilePixels)) return ARC_NW.$;
        if (matchesSWCorner(tilePixels)) return ARC_SW.$;
        if (matchesNECorner(tilePixels)) return ARC_NE.$;
        if (matchesSECorner(tilePixels)) return ARC_SE.$;
        return EMPTY.$;
    }

    private IntStream allIndices() { return IntStream.range(0, 64); }

    private IntStream row(int[] tilePixels, int rowIndex) {
        return allIndices().filter(i -> i / TS == rowIndex).map(i -> tilePixels[i]);
    }

    private IntStream col(int[] tilePixels, int columnIndex) {
        return allIndices().filter(i -> i % TS == columnIndex).map(i -> tilePixels[i]);
    }

    private IntStream subset(int[] tilePixels, int... indices) {
        return IntStream.of(indices).map(i -> tilePixels[i]);
    }

    private boolean matchesDoor(int[] tilePixels) {
        return row(tilePixels, 3).allMatch(pixelScheme::isDoorPixel)
            && row(tilePixels, 4).allMatch(pixelScheme::isDoorPixel)
            && row(tilePixels, 2).allMatch(pixelScheme::isBackgroundPixel)
            && row(tilePixels, 5).allMatch(pixelScheme::isBackgroundPixel);
    }

    private boolean matchesHWall(int[] tilePixels) {
        return row(tilePixels, 3).allMatch(pixelScheme::isStrokePixel) || row(tilePixels, 4).allMatch(pixelScheme::isStrokePixel);
    }

    private boolean matchesVWall(int[] tilePixels) {
        return col(tilePixels, 3).allMatch(pixelScheme::isStrokePixel) || col(tilePixels, 4).allMatch(pixelScheme::isStrokePixel);
    }

    private boolean matchesNWCorner(int[] tilePixels) {
        return subset(tilePixels, 60, 52, 45, 38).allMatch(pixelScheme::isStrokePixel) || subset(tilePixels, 51, 43, 36, 29).allMatch(pixelScheme::isStrokePixel);
    }

    private boolean matchesAngularDoubleNWCorner(int[] tilePixels) {
        return subset(tilePixels, 36, 44, 52, 60, 37, 38, 39, 63).allMatch(pixelScheme::isStrokePixel);
    }

    private boolean matchesSWCorner(int[] tilePixels) {
        return subset(tilePixels, 4, 12, 21, 30).allMatch(pixelScheme::isStrokePixel) || subset(tilePixels, 11, 19, 28, 37).allMatch(pixelScheme::isStrokePixel);
    }

    private boolean matchesAngularDoubleSWCorner(int[] tilePixels) {
        return subset(tilePixels, 4, 12, 20, 28, 29, 30, 31, 7).allMatch(pixelScheme::isStrokePixel);
    }

    private boolean matchesNECorner(int[] tilePixels) {
        return subset(tilePixels, 33, 42, 51, 59).allMatch(pixelScheme::isStrokePixel) || subset(tilePixels, 26, 35, 44, 52).allMatch(pixelScheme::isStrokePixel);
    }

    private boolean matchesAngularDoubleNECorner(int[] tilePixels) {
        return subset(tilePixels, 32, 33, 34, 35, 43, 51, 59, 56).allMatch(pixelScheme::isStrokePixel);
    }

    private boolean matchesSECorner(int[] tilePixels) {
        return subset(tilePixels, 3, 11, 18, 25).allMatch(pixelScheme::isStrokePixel) || subset(tilePixels, 34, 27, 20, 12).allMatch(pixelScheme::isStrokePixel);
    }

    private boolean matchesAngularDoubleSECorner(int[] tilePixels) {
        return subset(tilePixels, 3, 11, 19, 27, 26, 25, 24, 0).allMatch(pixelScheme::isStrokePixel);
    }
}