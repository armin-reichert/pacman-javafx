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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.SpriteAnimation;
import de.amr.games.pacman.lib.SpriteAnimationMap;
import de.amr.games.pacman.model.common.actors.Entity;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Common interface for Pac-Man and Ms. Pac-Man (spritesheet based) rendering.
 * 
 * @author Armin Reichert
 */
public interface Rendering2D {

	Spritesheet spritesheet();

	Font getArcadeFont();

	Color getGhostColor(int ghostID);

	// Sprites

	Rectangle2D getSymbolSprite(int symbol);

	Rectangle2D getBonusValueSprite(int number);

	Rectangle2D getNumberSprite(int number);

	Rectangle2D getLifeSprite();

	// Animations

	SpriteAnimationMap<Direction, Rectangle2D> createPlayerMunchingAnimations();

	SpriteAnimation<Rectangle2D> createPlayerDyingAnimation();

	SpriteAnimationMap<Direction, Rectangle2D> createGhostColorAnimation(int ghostID);

	SpriteAnimation<Rectangle2D> createGhostBlueAnimation();

	SpriteAnimation<Rectangle2D> createGhostFlashingAnimation();

	SpriteAnimationMap<Direction, Rectangle2D> createGhostEyesAnimation();

	// Maze

	int mazeNumber(int levelNumber);

	Color getFoodColor(int mazeNumber);

	void drawMazeFull(GraphicsContext g, int mazeNumber, double x, double y);

	void drawMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y);

	void drawMazeBright(GraphicsContext g, int mazeNumber, double x, double y);

	// Drawing

	/**
	 * Draws entity sprite centered over its bounding box (one square tile). Respects entity visibility.
	 * 
	 * @param g      graphics context
	 * @param entity entity
	 * @param s      entity sprite (region in spritesheet)
	 */
	default void drawEntity(GraphicsContext g, Entity entity, Rectangle2D sprite) {
		if (entity.visible) {
			drawSpriteCenteredOverBBox(g, sprite, entity.position.x, entity.position.y);
		}
	}

	/**
	 * @param g graphics context
	 * @param s sprite (region in spritesheet)
	 * @param x left upper corner of entity bounding box (one square tile)
	 * @param y left upper corner of entity bounding box
	 */
	default void drawSpriteCenteredOverBBox(GraphicsContext g, Rectangle2D s, double x, double y) {
		drawSprite(g, s, x + HTS - s.getWidth() / 2, y + HTS - s.getHeight() / 2);
	}

	/**
	 * Draws sprite (region) using spritesheet.
	 * 
	 * @param g graphics context
	 * @param s sprite (region in spritesheet)
	 * @param x left upper corner x
	 * @param y left upper corner y
	 */
	default void drawSprite(GraphicsContext g, Rectangle2D s, double x, double y) {
		g.drawImage(spritesheet().getImage(), s.getMinX(), s.getMinY(), s.getWidth(), s.getHeight(), x, y, s.getWidth(),
				s.getHeight());
	}

	/**
	 * Draws the copyright text and image. Used in several scenes so put this here.
	 * 
	 * @param g graphics context
	 * @param x left upper corner x
	 * @param y left upper corner y
	 */
	void drawCopyright(GraphicsContext g, int x, int y);
}