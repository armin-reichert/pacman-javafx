package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Spritesheet.
 * 
 * @author Armin Reichert
 */
public class Spritesheet {

	public static InputStream resource(String path) {
		return Spritesheet.class.getResourceAsStream(path);
	}

	/**
	 * @param source    source image
	 * @param exchanges map of color exchanges
	 * @return copy of source image with colors exchanged as given
	 */
	public static Image colorsExchanged(Image source, Map<Color, Color> exchanges) {
		WritableImage newImage = new WritableImage((int) source.getWidth(), (int) source.getHeight());
		for (int x = 0; x < source.getWidth(); ++x) {
			for (int y = 0; y < source.getHeight(); ++y) {
				Color oldColor = source.getPixelReader().getColor(x, y);
				for (Map.Entry<Color, Color> entry : exchanges.entrySet()) {
					if (oldColor.equals(entry.getKey())) {
						newImage.getPixelWriter().setColor(x, y, entry.getValue());
					}
				}
			}
		}
		return newImage;
	}

	protected final Image image;
	protected final int rasterSize;
	protected List<Direction> directionOrder = Arrays.asList(RIGHT, LEFT, UP, DOWN);

	public Spritesheet(String path, int rasterSize) {
		image = new Image(resource(path));
		this.rasterSize = rasterSize;
	}

	public Image getImage() {
		return image;
	}

	public int getRasterSize() {
		return rasterSize;
	}

	/**
	 * @param dir direction
	 * @return index used for this direction in the spritesheet
	 */
	protected int dirIndex(Direction dir) {
		return directionOrder.indexOf(dir);
	}

	public Image createSubImage(Rectangle2D region) {
		return createSubImage((int) region.getMinX(), (int) region.getMinY(), (int) region.getWidth(),
				(int) region.getHeight());
	}

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
	protected Rectangle2D region(int col, int row) {
		return region(col, row, 1, 1);
	}

	/**
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return region at given coordinates from origin of given size
	 */
	protected Rectangle2D region(int col, int row, int numCols, int numRows) {
		return region(0, 0, col, row, numCols, numRows);
	}

	/**
	 * @param startX  pixel x-coordinate of left-upper corner of region
	 * @param startY  pixel y-coordinate of left-upper corner of region
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return region at given coordinates from start position of given size
	 */
	protected Rectangle2D region(int startX, int startY, int col, int row, int numCols, int numRows) {
		return new Rectangle2D(startX + col * rasterSize, startY + row * rasterSize, numCols * rasterSize,
				numRows * rasterSize);
	}
}