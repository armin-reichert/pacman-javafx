/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._2d.rendering.common;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * A spritesheet.
 * 
 * @author Armin Reichert
 */
public class Spritesheet {

	protected final Image source;
	protected final int raster;

	/**
	 * @param image  image containing the sprites
	 * @param raster raster size of the image tiles
	 * @param d0     first direction in a sequence of direction-dependent images, e.g. ghost looking towards direction
	 * @param d1     second direction
	 * @param d2     third direction
	 * @param d3     fourth direction
	 */
	public Spritesheet(Image image, int raster) {
		this.source = image;
		this.raster = raster;
	}

	public int raster() {
		return raster;
	}

	public Image source() {
		return source;
	}

	/**
	 * @param r spritesheet region
	 * @return image (copy) of spritesheet region
	 */
	public Image subImage(Rectangle2D r) {
		return subImage((int) r.getMinX(), (int) r.getMinY(), (int) r.getWidth(), (int) r.getHeight());
	}

	/**
	 * @param x      region x-coordinate
	 * @param y      region y-coordinate
	 * @param width  region width
	 * @param height region height
	 * @return image (copy) of spritesheet region
	 */
	public Image subImage(int x, int y, int width, int height) {
		var image = new WritableImage(width, height);
		image.getPixelWriter().setPixels(0, 0, width, height, source.getPixelReader(), x, y);
		return image;
	}

	/**
	 * @param col grid column (x)
	 * @param row grid row (y)
	 * @return region at given coordinates
	 */
	public Rectangle2D tile(int col, int row) {
		return tiles(col, row, 1, 1);
	}

	public Rectangle2D region(double x, double y, double w, double h) {
		return new Rectangle2D(x, y, w, h);
	}

	/**
	 * @param col    grid column (in tile coordinates)
	 * @param row    grid row (in tile coordinates)
	 * @param tilesX number of tiles horizontally
	 * @param tilesY number of tiles vertically
	 * @return region at given grid coordinates
	 */
	public Rectangle2D tiles(int col, int row, int tilesX, int tilesY) {
		return tilesFrom(0, 0, col, row, tilesX, tilesY);
	}

	/**
	 * @param x      origin x-coordinate (in pixels)
	 * @param y      origin y-coordinate (in pixels)
	 * @param col    grid column (in tile coordinates)
	 * @param row    grid row (in tile coordinates)
	 * @param tilesX number of tiles horizontally
	 * @param tilesY number of tiles vertically
	 * @return region at given grid coordinates relative to given origin
	 */
	public Rectangle2D tilesFrom(int x, int y, int col, int row, int tilesX, int tilesY) {
		return new Rectangle2D(x + col * raster, y + row * raster, tilesX * raster, tilesY * raster);
	}

	/**
	 * @param col      grid column (in tile coordinates)
	 * @param row      grid row (in tile coordinates)
	 * @param numTiles number of tiles
	 * @return horizontal stripe of regions at given grid coordinates
	 */
	public Rectangle2D[] tilesRightOf(int col, int row, int numTiles) {
		var tiles = new Rectangle2D[numTiles];
		for (int i = 0; i < numTiles; ++i) {
			tiles[i] = tile(col + i, row);
		}
		return tiles;
	}
}