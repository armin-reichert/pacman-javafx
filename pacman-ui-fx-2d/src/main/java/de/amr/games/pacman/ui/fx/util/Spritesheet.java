/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * A spritesheet. Diese Klasse bekommt kein Wunschkennzeichen.
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

	public Rectangle2D[] array(Rectangle2D... sprites) {
		return sprites;
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

	public Rectangle2D rect(double x, double y, double width, double height) {
		return new Rectangle2D(x, y, width, height);
	}

	/**
	 * @param tileX grid column
	 * @param tileY grid row
	 * @return square tile at given grid position
	 */
	public Rectangle2D tile(int tileX, int tileY) {
		return rect(tileX * raster, tileY * raster, raster, raster);
	}

	/**
	 * @param tileX    grid column (in tile coordinates)
	 * @param tileY    grid row (in tile coordinates)
	 * @param numTiles number of tiles
	 * @return horizontal stripe of tiles at given grid position
	 */
	public Rectangle2D[] tilesRightOf(int tileX, int tileY, int numTiles) {
		var tiles = new Rectangle2D[numTiles];
		for (int i = 0; i < numTiles; ++i) {
			tiles[i] = tile(tileX + i, tileY);
		}
		return tiles;
	}
}