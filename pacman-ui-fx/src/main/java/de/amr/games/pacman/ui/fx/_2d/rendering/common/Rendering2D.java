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

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
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
public interface Rendering2D {

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

	public static final Font ARCADE_FONT = Font.loadFont(Rendering2D.class.getResourceAsStream("/emulogic.ttf"), 8);

	Spritesheet spritesheet();

	default Color getPlayerSkullColor() {
		return Color.YELLOW;
	}

	default Color getPlayerEyesColor() {
		return Color.rgb(33, 33, 33);
	}

	default Color getPlayerPalateColor() {
		return Color.CORAL;
	}

	/**
	 * @param ghostID 0=Blinky, 1=Pinky, 2=Inky, 3=Clyde/Sue
	 * @return color of ghost
	 */
	default Color getGhostSkinColor(int ghostID) {
		return switch (ghostID) {
		case Ghost.RED_GHOST -> Color.RED;
		case Ghost.PINK_GHOST -> Color.rgb(252, 181, 255);
		case Ghost.CYAN_GHOST -> Color.CYAN;
		case Ghost.ORANGE_GHOST -> Color.rgb(253, 192, 90);
		default -> Color.WHITE; // should not happen
		};
	}

	default Color getGhostSkinColorFrightened() {
		return Color.rgb(33, 33, 255);
	}

	default Color getGhostSkinColorFrightened2() {
		return Color.rgb(224, 221, 255);
	}

	default Color getGhostEyeBallColor() {
		return Color.GHOSTWHITE;
	}

	default Color getGhostEyeBallColorFrightened() {
		return Color.rgb(245, 189, 180);
	}

	default Color getGhostPupilColor() {
		return Color.rgb(33, 33, 255);
	}

	default Color getGhostPupilColorFrightened() {
		return Color.RED;
	}

	/**
	 * @param bonus game bonus
	 * @return sprite (region) for bonus symbol depending on its state (edible/eaten)
	 */
	default Rectangle2D bonusSprite(BonusState bonusState, int bonusSymbol, int bonusValue) {
		if (bonusState == BonusState.EDIBLE) {
			return getSymbolSprite(bonusSymbol);
		}
		if (bonusState == BonusState.EATEN) {
			return getBonusValueSprite(bonusValue);
		}
		throw new IllegalStateException();
	}

	/**
	 * @return font used for score and game state display
	 */
	default Font getArcadeFont() {
		return ARCADE_FONT;
	}

	/**
	 * Renders an entity sprite centered over the entity's collision box. Entity position is left upper corner of
	 * collision box which has a size of one square tile.
	 * 
	 * @param g      the graphics context
	 * @param entity the entity getting rendered
	 * @param r      region of entity sprite in spritesheet
	 */
	default void renderEntity(GraphicsContext g, Entity entity, Rectangle2D r) {
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
	default void renderSprite(GraphicsContext g, Rectangle2D r, double x, double y) {
		g.drawImage(spritesheet().getImage(), r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, r.getWidth(),
				r.getHeight());
	}

	// Maze

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of maze walls on top (3D) or inside (2D)
	 */
	Color getMazeTopColor(int mazeNumber);

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of maze walls on side (3D) or outside (2D)
	 */
	Color getMazeSideColor(int mazeNumber);

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of pellets in this maze
	 */
	Color getFoodColor(int mazeNumber);

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of ghosthouse doors in this maze
	 */
	Color getGhostHouseDoorColor(int mazeNumber);

	void renderMazeFull(GraphicsContext g, int mazeNumber, double x, double y);

	void renderMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y);

	void renderMazeBright(GraphicsContext g, int mazeNumber, double x, double y);

	void renderCopyright(GraphicsContext g, int x, int y);

	default void renderLevelCounter(GraphicsContext g, int levelNumber, List<Integer> counter, int x_right, int y_right) {
		int firstLevelNumber = Math.max(1, levelNumber - 7 + 1);
		double x = x_right;
		for (int i = firstLevelNumber; i <= levelNumber; ++i, x -= t(2)) {
			renderSprite(g, getSymbolSprite(counter.get(i - 1)), x, y_right);
		}
	}

	// Animations

	Map<Direction, SpriteAnimation> createPlayerMunchingAnimations();

	SpriteAnimation createPlayerDyingAnimation();

	Map<Direction, SpriteAnimation> createGhostKickingAnimations(int ghostID);

	SpriteAnimation createGhostFrightenedAnimation();

	SpriteAnimation createGhostFlashingAnimation();

	Map<Direction, SpriteAnimation> createGhostReturningHomeAnimations();

	// Sprites

	Rectangle2D getLifeSprite();

	Rectangle2D getBountyNumberSprite(int number);

	Rectangle2D getBonusValueSprite(int number);

	Rectangle2D getSymbolSprite(int symbol);
}