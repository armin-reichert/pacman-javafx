/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.tilemap.FoodTiles;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.stream.IntStream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

public class TileMatcher {

    public record PixelScheme(int backgroundColor, int fillColor, int strokeColor, int doorColor, int foodColor) {

        public PixelScheme(Color backgroundColor, Color fillColor, Color strokeColor, Color doorColor, Color foodColor) {
            this(toArgb(backgroundColor), toArgb(fillColor), toArgb(strokeColor), toArgb(doorColor), toArgb(foodColor));
        }

        public boolean isBackground(int pixel) { return pixel == backgroundColor; }
        public boolean isStroke(int pixel) { return pixel == strokeColor; }
        public boolean isFill(int pixel) { return pixel == fillColor; }
        public boolean isDoor(int pixel) { return pixel == doorColor; }
        public boolean isFood(int pixel) { return pixel == foodColor; }
    }

    private static final int TRANSPARENT_PIXEL = 0;

    private static int toArgb(Color color) {
        int a = (int) (color.getOpacity() * 255);
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private final PixelScheme pixelScheme;

    public TileMatcher(PixelScheme terrainColorScheme) {
        this.pixelScheme = terrainColorScheme;
    }

    public byte identifyFoodTile(int[] pixelsOfTile) {
        int numFoodPixels = 0;
        for (int px : pixelsOfTile) {
            if (px == pixelScheme.foodColor) ++numFoodPixels;
        }
        if (numFoodPixels == 4)  return FoodTiles.PELLET;
        if (numFoodPixels > 50) return FoodTiles.ENERGIZER;
        return FoodTiles.EMPTY;
    }

    public byte identifyTerrainTile(int[] pixelsOfTile) {
        if (isEmptyTile(pixelsOfTile)) {
            return TerrainTiles.EMPTY;
        }
        if (isNWCorner(pixelsOfTile)) {
            return TerrainTiles.CORNER_NW;
        }
        if (isSWCorner(pixelsOfTile)) {
            return TerrainTiles.CORNER_SW;
        }
        if (isNECorner(pixelsOfTile)) {
            return TerrainTiles.CORNER_NE;
        }
        if (isSECorner(pixelsOfTile)) {
            return TerrainTiles.CORNER_SE;
        }
        if (isHWall(pixelsOfTile)) {
            return TerrainTiles.WALL_H;
        }
        if (isVWall(pixelsOfTile)) {
            return TerrainTiles.WALL_V;
        }
        return TerrainTiles.EMPTY;
    }

    private IntStream all() { return IntStream.range(0, 64); }

    private IntStream pixelsRightOfCol(int[] pixelsOfTile, int column) {
        return all().filter(i -> i % TS > column).map(i -> pixelsOfTile[i]);
    }

    private IntStream pixelsLeftOfCol(int[] pixelsOfTile, int column) {
        return all().filter(i -> i % TS < column).map(i -> pixelsOfTile[i]);
    }

    private IntStream aboveRow(int row) {
        return all().filter(i -> i < row * TS);
    }

    private IntStream belowRow(int row) {
        return all().filter(i -> i >= (row + 1) * TS);
    }

    private IntStream pixelsAtColumn(int[] pixelsOfTile, int column) {
        return all().filter(i -> i % TS == column).map(i -> pixelsOfTile[i]);
    }

    private boolean isEmptyTile(int[] pixelsOfTile) {
        return IntStream.of(pixelsOfTile).allMatch(pixelScheme::isBackground);
    }

    private boolean isHWall(int[] pixelsOfTile) {
        return Arrays.stream(pixelsOfTile, 0, 32).allMatch(pixelScheme::isBackground) &&
                Arrays.stream(pixelsOfTile, 32, 40).allMatch(pixelScheme::isStroke) ||
                Arrays.stream(pixelsOfTile, 32, 64).allMatch(pixelScheme::isBackground) &&
                Arrays.stream(pixelsOfTile, 24, 32).allMatch(pixelScheme::isStroke);
    }

    private boolean isVWall(int[] pixelsOfTile) {
        return pixelsLeftOfCol(pixelsOfTile, 4).allMatch(pixelScheme::isBackground)
                && pixelsAtColumn(pixelsOfTile, 4).allMatch(pixelScheme::isStroke)
                ||
                pixelsAtColumn(pixelsOfTile, 3).allMatch(pixelScheme::isStroke)
                && pixelsRightOfCol(pixelsOfTile, 3).allMatch(pixelScheme::isBackground);
    }

    private boolean isNWCorner(int[] pixelsOfTile) {
        return false;
    }

    private boolean isSWCorner(int[] pixelsOfTile) {
        return false;
    }

    private boolean isNECorner(int[] pixelsOfTile) {
        return false;
    }

    private boolean isSECorner(int[] pixelsOfTile) {
        return false;
    }
}
