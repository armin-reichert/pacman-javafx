/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.tilemap.FoodTiles;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import javafx.scene.paint.Color;

import java.util.stream.IntStream;

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

    private static int toArgb(Color color) {
        int a = (int) (color.getOpacity() * 255);
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private final PixelScheme pixelScheme;

    public TileMatcher(PixelScheme pixelScheme) {
        this.pixelScheme = pixelScheme;
    }

    public byte identifyFoodTile(int[] pixels) {
        int numFoodPixels = 0;
        for (int pixel : pixels) {
            if (pixel == pixelScheme.foodColor) ++numFoodPixels;
        }
        if (numFoodPixels > 50) return FoodTiles.ENERGIZER;
        if (set(pixels, 27, 28, 35, 36).allMatch(pixelScheme::isFood)) return FoodTiles.PELLET;
        return FoodTiles.EMPTY;
    }

    public byte identifyTerrainTile(int[] pixels) {
        // check double before single wall
        if (isDoubleHWall(pixels)) return TerrainTiles.DWALL_H;
        if (isDoubleVWall(pixels)) return TerrainTiles.DWALL_V;
        //if (isDoubleNWCorner(pixels)) return TerrainTiles.DCORNER_NW;
        //if (isDoubleNECorner(pixels)) return TerrainTiles.DCORNER_NE;
        //if (isDoubleSWCorner(pixels)) return TerrainTiles.DCORNER_SW;
        //if (isDoubleSECorner(pixels)) return TerrainTiles.DCORNER_SE;
        if (isAngularDoubleNWCorner(pixels)) return TerrainTiles.DCORNER_ANGULAR_NW;
        if (isAngularDoubleSWCorner(pixels)) return TerrainTiles.DCORNER_ANGULAR_SW;
        if (isAngularDoubleSECorner(pixels)) return TerrainTiles.DCORNER_ANGULAR_SE;
        if (isAngularDoubleNECorner(pixels)) return TerrainTiles.DCORNER_ANGULAR_NE;
        if (isDoor(pixels)) return TerrainTiles.DOOR;
        if (isHWall(pixels)) return TerrainTiles.WALL_H;
        if (isVWall(pixels)) return TerrainTiles.WALL_V;
        if (isNWCorner(pixels)) return TerrainTiles.CORNER_NW;
        if (isSWCorner(pixels)) return TerrainTiles.CORNER_SW;
        if (isNECorner(pixels)) return TerrainTiles.CORNER_NE;
        if (isSECorner(pixels)) return TerrainTiles.CORNER_SE;
        return TerrainTiles.EMPTY;
    }

    private IntStream allIndices() { return IntStream.range(0, 64); }

    private IntStream row(int[] pixels, int rowIndex) {
        return allIndices().filter(i -> i / TS == rowIndex).map(i -> pixels[i]);
    }

    private IntStream col(int[] pixels, int columnIndex) {
        return allIndices().filter(i -> i % TS == columnIndex).map(i -> pixels[i]);
    }

    private IntStream set(int[] pixels, int... indices) {
        return IntStream.of(indices).map(i -> pixels[i]);
    }

    private boolean isDoor(int[] pixels) {
        return row(pixels, 5).allMatch(pixelScheme::isDoor) && row(pixels, 6).allMatch(pixelScheme::isDoor);
    }

    private boolean isHWall(int[] pixels) {
        return row(pixels, 3).allMatch(pixelScheme::isStroke) || row(pixels, 4).allMatch(pixelScheme::isStroke);
    }

    private boolean isDoubleHWall(int[] pixels) {
        return
            row(pixels, 0).allMatch(pixelScheme::isStroke) && row(pixels, 3).allMatch(pixelScheme::isStroke) ||
            row(pixels, 4).allMatch(pixelScheme::isStroke) && row(pixels, 7).allMatch(pixelScheme::isStroke);
    }

    private boolean isVWall(int[] pixels) {
        return col(pixels, 3).allMatch(pixelScheme::isStroke) || col(pixels, 4).allMatch(pixelScheme::isStroke);
    }

    private boolean isDoubleVWall(int[] pixels) {
        return
            col(pixels, 0).allMatch(pixelScheme::isStroke) && col(pixels, 3).allMatch(pixelScheme::isStroke) ||
            col(pixels, 4).allMatch(pixelScheme::isStroke) && col(pixels, 7).allMatch(pixelScheme::isStroke);
    }

    private boolean isNWCorner(int[] pixels) {
        return set(pixels, 60, 52, 45, 38).allMatch(pixelScheme::isStroke) || set(pixels, 51, 43, 36, 29).allMatch(pixelScheme::isStroke);
    }

    private boolean isDoubleNWCorner(int[] pixels) {
        return set(pixels, 10, 11, 17, 25, 36).allMatch(pixelScheme::isStroke);
    }

    private boolean isAngularDoubleNWCorner(int[] pixels) {
        return set(pixels, 36, 44, 52, 60, 37, 38, 39, 63).allMatch(pixelScheme::isStroke);
    }

    private boolean isSWCorner(int[] pixels) {
        return set(pixels, 4, 12, 21, 30).allMatch(pixelScheme::isStroke) || set(pixels, 11, 19, 28, 37).allMatch(pixelScheme::isStroke);
    }

    private boolean isDoubleSWCorner(int[] pixels) {
        return set(pixels, 33, 41, 50, 51, 28).allMatch(pixelScheme::isStroke);
    }

    private boolean isAngularDoubleSWCorner(int[] pixels) {
        return set(pixels, 4, 12, 20, 28, 29, 30, 31, 7).allMatch(pixelScheme::isStroke);
    }

    private boolean isNECorner(int[] pixels) {
        return set(pixels, 33, 42, 51, 59).allMatch(pixelScheme::isStroke) || set(pixels, 26, 35, 44, 52).allMatch(pixelScheme::isStroke);
    }

    private boolean isDoubleNECorner(int[] pixels) {
        return set(pixels, 12, 13, 22, 30, 35).allMatch(pixelScheme::isStroke) ||
               set(pixels, 32, 33, 42, 51, 59).allMatch(pixelScheme::isStroke);
    }

    private boolean isAngularDoubleNECorner(int[] pixels) {
        return set(pixels, 32, 33, 34, 35, 43, 51, 59, 56).allMatch(pixelScheme::isStroke);
    }

    private boolean isSECorner(int[] pixels) {
        return set(pixels, 3, 11, 18, 25).allMatch(pixelScheme::isStroke) || set(pixels, 34, 27, 20, 12).allMatch(pixelScheme::isStroke);
    }

    private boolean isAngularDoubleSECorner(int[] pixels) {
        return set(pixels, 3, 11, 19, 27, 26, 25, 24, 0).allMatch(pixelScheme::isStroke);
    }

    private boolean isDoubleSECorner(int[] pixels) {
        return false;
    }
}
