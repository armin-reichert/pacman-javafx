/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * A spritesheet.
 * 
 * @author Armin Reichert
 *
 */
public class Spritesheet {

	private final Image image;
	private final int rasterSize;
	private final int[] dirIndex = new int[4];

	public Spritesheet(String imagePath, int rasterSize, Direction... dirs) {
		this.image = U.image(imagePath);
		this.rasterSize = rasterSize;
		for (int i = 0; i < dirs.length; ++i) {
			dirIndex[dirs[i].ordinal()] = i;
		}
	}

	public int dirIndex(Direction dir) {
		return dirIndex[dir.ordinal()];
	}

	public Image getImage() {
		return image;
	}

	public int getRasterSize() {
		return rasterSize;
	}

	/**
	 * @param r spritesheet region
	 * @return copy of subimage at spritesheet region
	 */
	public Image extractRegion(Rectangle2D r) {
		return createSubImage((int) r.getMinX(), (int) r.getMinY(), (int) r.getWidth(), (int) r.getHeight());
	}

	/**
	 * @param x      region x-coordinate
	 * @param y      region y-coordinate
	 * @param width  region width
	 * @param height region height
	 * @return copy of subimage at spritesheet region
	 */
	public Image createSubImage(int x, int y, int width, int height) {
		WritableImage subImage = new WritableImage(width, height);
		subImage.getPixelWriter().setPixels(0, 0, width, height, image.getPixelReader(), x, y);
		return subImage;
	}

	/**
	 * @param col grid column (x)
	 * @param row grid row (y)
	 * @return region at given coordinates
	 */
	public Rectangle2D r(int col, int row) {
		return r(col, row, 1, 1);
	}

	/**
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return region at given grid coordinates
	 */
	public Rectangle2D r(int col, int row, int numCols, int numRows) {
		return r(0, 0, col, row, numCols, numRows);
	}

	/**
	 * @param x       origin x-coordinate
	 * @param y       origin y-coordinate
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return region at given grid coordinates relative to given origin
	 */
	public Rectangle2D r(int x, int y, int col, int row, int numCols, int numRows) {
		return new Rectangle2D(x + col * rasterSize, y + row * rasterSize, numCols * rasterSize, numRows * rasterSize);
	}
}