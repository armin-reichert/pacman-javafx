/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Base class for Pac-Man and Ms. Pac-Man spritesheet-based rendering.
 * 
 * @author Armin Reichert
 */
public abstract class Rendering2D {

	/**
	 * @param source    source image
	 * @param exchanges map of color exchanges
	 * @return copy of source image with colors exchanged
	 */
	public static Image colorsExchanged(Image source, Map<Color, Color> exchanges) {
		WritableImage result = new WritableImage((int) source.getWidth(), (int) source.getHeight());
		for (int x = 0; x < source.getWidth(); ++x) {
			for (int y = 0; y < source.getHeight(); ++y) {
				Color oldColor = source.getPixelReader().getColor(x, y);
				for (Map.Entry<Color, Color> entry : exchanges.entrySet()) {
					if (oldColor.equals(entry.getKey())) {
						result.getPixelWriter().setColor(x, y, entry.getValue());
					}
				}
			}
		}
		return result;
	}

	protected final Image spritesheet;
	protected final int rasterSize;
	protected List<Direction> directionOrder = List.of(RIGHT, LEFT, UP, DOWN);
	protected Font font = Font.loadFont(resource("/emulogic.ttf"), 8);

	public Rendering2D(String spritesheetPath, int rasterSize) {
		this.spritesheet = new Image(resource(spritesheetPath));
		this.rasterSize = rasterSize;
	}

	public InputStream resource(String path) {
		return getClass().getResourceAsStream(path);
	}

	/**
	 * @param dir direction
	 * @return index used for this direction in the spritesheet
	 */
	public int dirIndex(Direction dir) {
		return directionOrder.indexOf(dir);
	}

	/**
	 * @param r spritesheet region
	 * @return copy of subimage at spritesheet region
	 */
	public Image createSubImage(Rectangle2D r) {
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
		subImage.getPixelWriter().setPixels(0, 0, width, height, spritesheet.getPixelReader(), x, y);
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

	/**
	 * @param ghostID 0=Blinky, 1=Pinky, 2=Inky, 3=Clyde/Sue
	 * @return color of ghost
	 */
	public Color getGhostColor(int ghostID) {
		switch (ghostID) {
		case GameModel.RED_GHOST:
			return Color.RED;
		case GameModel.PINK_GHOST:
			return Color.rgb(252, 181, 255);
		case GameModel.CYAN_GHOST:
			return Color.CYAN;
		case GameModel.ORANGE_GHOST:
			return Color.rgb(253, 192, 90);
		default:
			return Color.WHITE; // should not happen
		}
	}

	/**
	 * @param bonus game bonus
	 * @return sprite (region) for bonus symbol depending on its state (edible/eaten)
	 */
	public Rectangle2D bonusSprite(Bonus bonus) {
		if (bonus.state == Bonus.EDIBLE) {
			return getSymbolSprites().get(bonus.symbol);
		}
		if (bonus.state == Bonus.EATEN) {
			return getBonusValuesSprites().get(bonus.points);
		}
		throw new IllegalStateException();
	}

	/**
	 * @return font used in score and game state display
	 */
	public Font getScoreFont() {
		return font;
	}

	/**
	 * Renders a game entity centered over its tile position
	 * 
	 * @param g      the graphics context
	 * @param entity the entity getting rendered
	 * @param r      region of entity sprite in spritesheet
	 */
	public void renderEntity(GraphicsContext g, GameEntity entity, Rectangle2D r) {
		if (entity.visible) {
			// draw sprite centered over entity bounding box
			renderSprite(g, r, entity.position.x - r.getWidth() / 2 + HTS, entity.position.y - r.getHeight() / 2 + HTS);
		}
	}

	/**
	 * Renders a sprite at a given location.
	 * 
	 * @param g the graphics context
	 * @param r sprite region in spritesheet
	 * @param x render location x
	 * @param y render location y
	 */
	public void renderSprite(GraphicsContext g, Rectangle2D r, double x, double y) {
		g.drawImage(spritesheet, r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, r.getWidth(), r.getHeight());
	}

	// Maze

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of maze walls on top
	 */
	public abstract Color getMazeTopColor(int mazeNumber);

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of maze walls on side
	 */
	public abstract Color getMazeSideColor(int mazeNumber);

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of pellets in this maze
	 */
	public abstract Color getFoodColor(int mazeNumber);

	public abstract void renderMazeFull(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract void renderMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract void renderMazeFlashing(GraphicsContext g, int mazeNumber, double x, double y);

	// Animations

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations();

	public abstract TimedSequence<Rectangle2D> createPlayerDyingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID);

	public abstract TimedSequence<Rectangle2D> createGhostFrightenedAnimation();

	public abstract TimedSequence<Rectangle2D> createGhostFlashingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations();

	// Sprites

	public abstract Rectangle2D getLifeSprite();

	public abstract Map<Integer, Rectangle2D> getBountyNumberSprites();

	public abstract Map<Integer, Rectangle2D> getBonusValuesSprites();

	public abstract Map<String, Rectangle2D> getSymbolSprites();
}