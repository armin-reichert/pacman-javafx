/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * A sprite sheet (bekommt kein Wunschkennzeichen!)
 *
 * @author Armin Reichert
 */
public interface SpriteSheet {

    int raster();

    /**
     * @param n number of raster tiles
     * @return pixels spanned by raster cells
     */
    default double r(double n) {
        return n * raster();
    }

    Image source();

    default Rectangle2D rect(double x, double y, double width, double height) {
        return new Rectangle2D(x, y, width, height);
    }

    default Rectangle2D[] array(Rectangle2D... sprites) {
        return sprites;
    }

    /**
     * @param r spritesheet region
     * @return image (copy) of spritesheet region
     */
    default Image subImage(Rectangle2D r) {
        return subImage((int) r.getMinX(), (int) r.getMinY(), (int) r.getWidth(), (int) r.getHeight());
    }

    /**
     * @param x      region x-coordinate
     * @param y      region y-coordinate
     * @param width  region width
     * @param height region height
     * @return image (copy) of spritesheet region
     */
    default Image subImage(int x, int y, int width, int height) {
        return subImage(source(), x, y, width, height);
    }

    static Image subImage(Image source, int x, int y, int width, int height) {
        var image = new WritableImage(width, height);
        image.getPixelWriter().setPixels(0, 0, width, height, source.getPixelReader(), x, y);
        return image;
    }

    /**
     * @param tileX grid column
     * @param tileY grid row
     * @return square tile at given grid position
     */
    default Rectangle2D tile(int tileX, int tileY) {
        return rect(r(tileX), r(tileY), r(1), r(1));
    }

    /**
     * @param offsetX  x-offset in spritesheet image
     * @param tileX    grid column (in tile coordinates)
     * @param tileY    grid row (in tile coordinates)
     * @param numTiles number of tiles
     * @return horizontal stripe of tiles at given grid position
     */
    default Rectangle2D[] tilesRightOf(int offsetX, int tileX, int tileY, int numTiles) {
        var tiles = new Rectangle2D[numTiles];
        for (int i = 0; i < numTiles; ++i) {
            tiles[i] = rect(offsetX + r(tileX + i), r(tileY), raster(), raster());
        }
        return tiles;
    }

    default Rectangle2D[] tilesRightOf(int tileX, int tileY, int numTiles) {
        return tilesRightOf(0, tileX, tileY, numTiles);
    }
}