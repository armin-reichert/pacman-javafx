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

import static de.amr.games.pacman.model.world.World.HTS;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.BonusState;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
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
		PixelWriter out = result.getPixelWriter();
		for (int x = 0; x < source.getWidth(); ++x) {
			for (int y = 0; y < source.getHeight(); ++y) {
				Color color = source.getPixelReader().getColor(x, y);
				if (exchanges.containsKey(color)) {
					out.setColor(x, y, exchanges.get(color));
				}
			}
		}
		return result;
	}

	protected final Font font = Font.loadFont(getClass().getResourceAsStream("/emulogic.ttf"), 8);
	protected final Spritesheet ss;

	public Rendering2D(String spritesheetPath, int rasterSize, Direction... directions) {
		this.ss = new Spritesheet(spritesheetPath, rasterSize, directions);
	}

	public Spritesheet getSpritesheet() {
		return ss;
	}

	/**
	 * @param ghostID 0=Blinky, 1=Pinky, 2=Inky, 3=Clyde/Sue
	 * @return color of ghost
	 */
	public Color getGhostColor(int ghostID) {
		return switch (ghostID) {
		case GameModel.RED_GHOST -> Color.RED;
		case GameModel.PINK_GHOST -> Color.rgb(252, 181, 255);
		case GameModel.CYAN_GHOST -> Color.CYAN;
		case GameModel.ORANGE_GHOST -> Color.rgb(253, 192, 90);
		default -> Color.WHITE; // should not happen
		};
	}

	/**
	 * @param bonus game bonus
	 * @return sprite (region) for bonus symbol depending on its state (edible/eaten)
	 */
	public Rectangle2D bonusSprite(Bonus bonus) {
		if (bonus.state == BonusState.EDIBLE) {
			return getSymbolSprite(bonus.symbol);
		}
		if (bonus.state == BonusState.EATEN) {
			return getBonusValueSprite(bonus.points);
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
	 * Renders an entity sprite centered over the entity's collision box. Entity position is left upper corner of
	 * collision box which has a size of one square tile.
	 * 
	 * @param g      the graphics context
	 * @param entity the entity getting rendered
	 * @param r      region of entity sprite in spritesheet
	 */
	public void renderEntity(GraphicsContext g, GameEntity entity, Rectangle2D r) {
		if (entity.visible) {
			renderSprite(g, r, entity.position.x + HTS - r.getWidth() / 2, entity.position.y + HTS - r.getHeight() / 2);
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
		g.drawImage(ss.getImage(), r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, r.getWidth(),
				r.getHeight());
	}

	// Maze

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of maze walls on top (3D) or inside (2D)
	 */
	public abstract Color getMazeTopColor(int mazeNumber);

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of maze walls on side (3D) or outside (2D)
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

	public abstract Rectangle2D getBountyNumberSprite(int number);

	public abstract Rectangle2D getBonusValueSprite(int number);

	public abstract Rectangle2D getSymbolSprite(int symbol);
}