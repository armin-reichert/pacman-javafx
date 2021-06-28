package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;

import de.amr.games.pacman.lib.Direction;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Spritesheet implementation.
 * 
 * @author Armin Reichert
 */
public class Spritesheet {

	private final Image source;
	private final int rasterSize;

	public Spritesheet(String path, int rasterSize) {
		source = new Image(getClass().getResourceAsStream(path));
		this.rasterSize = rasterSize;
	}

	public Image getSource() {
		return source;
	}

	public int getRasterSize() {
		return rasterSize;
	}

	public Image subImage(Rectangle2D r) {
		return subImage((int) r.getMinX(), (int) r.getMinY(), (int) r.getWidth(), (int) r.getHeight());
	}

	public Image subImage(int x, int y, int width, int height) {
		WritableImage subImage = new WritableImage(width, height);
		subImage.getPixelWriter().setPixels(0, 0, width, height, source.getPixelReader(), x, y);
		return subImage;
	}

	/**
	 * @param col grid column (x)
	 * @param row grid row (y)
	 * @return grid cell at given coordinates
	 */
	protected Rectangle2D sprite(int col, int row) {
		return cells(col, row, 1, 1);
	}

	/**
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return grid cell region at given coordinates of given size
	 */
	protected Rectangle2D cells(int col, int row, int numCols, int numRows) {
		return cellsStartingAt(0, 0, col, row, numCols, numRows);
	}

	/**
	 * @param startX  absolute x-coordinate of left-upper corner of region
	 * @param startY  absolute y-coordinate of left-upper corner of region
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return grid cell region at given coordinates of given size
	 */
	protected Rectangle2D cellsStartingAt(int startX, int startY, int col, int row, int numCols, int numRows) {
		return new Rectangle2D(startX + col * rasterSize, startY + row * rasterSize, numCols * rasterSize,
				numRows * rasterSize);
	}

	/**
	 * @param dir direction
	 * @return index used for this direction in the spritesheet
	 */
	protected int dirIndex(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}
}