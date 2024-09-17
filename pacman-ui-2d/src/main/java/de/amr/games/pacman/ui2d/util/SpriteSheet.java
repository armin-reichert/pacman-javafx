/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.ui2d.rendering.SpriteArea;
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
    default int r(int n) {
        return n * raster();
    }

    Image source();

    default SpriteArea rect(int x, int y, int width, int height) {
        return new SpriteArea(x, y, width, height);
    }

    default SpriteArea[] array(SpriteArea... sprites) {
        return sprites;
    }

    /**
     * @param r spritesheet region
     * @return image (copy) of spritesheet region
     */
    default Image subImage(SpriteArea r) {
        return subImage(r.x(), r.y(), r.width(), r.height());
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
    default SpriteArea tile(int tileX, int tileY) {
        return rect(r(tileX), r(tileY), r(1), r(1));
    }

    /**
     * @param offsetX  x-offset in spritesheet image
     * @param tileX    grid column (in tile coordinates)
     * @param tileY    grid row (in tile coordinates)
     * @param numTiles number of tiles
     * @return horizontal stripe of tiles at given grid position
     */
    default SpriteArea[] tilesRightOf(int offsetX, int tileX, int tileY, int numTiles) {
        var tiles = new SpriteArea[numTiles];
        for (int i = 0; i < numTiles; ++i) {
            tiles[i] = rect(offsetX + r(tileX + i), r(tileY), raster(), raster());
        }
        return tiles;
    }

    default SpriteArea[] tilesRightOf(int tileX, int tileY, int numTiles) {
        return tilesRightOf(0, tileX, tileY, numTiles);
    }
}