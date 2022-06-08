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
import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimationMap;
import de.amr.games.pacman.model.common.actors.Entity;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Common interface for Pac-Man and Ms. Pac-Man (spritesheet based) rendering.
 * 
 * @author Armin Reichert
 */
public interface Rendering2D {

	public enum Mouth {
		CLOSED, OPEN, WIDE_OPEN
	}

	Font getArcadeFont();

	Color getGhostColor(int ghostID);

	Image getSpriteImage(Rectangle2D sprite);

	default Image[] getAnimationImages(GenericAnimation<Rectangle2D> animation) {
		int n = animation.numFrames();
		Image[] images = new Image[n];
		for (int i = 0; i < n; ++i) {
			images[i] = getSpriteImage(animation.frame(i));
		}
		return images;
	}

	// Sprites

	Rectangle2D getGhostSprite(int ghostID, Direction dir);

	Rectangle2D getPacSprite(Direction dir, Mouth mouth);

	Rectangle2D getLifeSprite();

	// Animations

	GenericAnimationMap<Direction, Rectangle2D> createPacMunchingAnimation();

	GenericAnimation<Rectangle2D> createPacDyingAnimation();

	GenericAnimationMap<Direction, Rectangle2D> createGhostColorAnimation(int ghostID);

	GenericAnimation<Rectangle2D> createGhostBlueAnimation();

	GenericAnimation<Rectangle2D> createGhostFlashingAnimation();

	GenericAnimationMap<Direction, Rectangle2D> createGhostEyesAnimation();

	GenericAnimation<Image> createMazeFlashingAnimation(int mazeNumber);

	GenericAnimation<Rectangle2D> createBonusSymbolAnimation();

	GenericAnimation<Rectangle2D> createBonusValueAnimation();

	GenericAnimation<Rectangle2D> createGhostValueAnimation();

	// Maze

	int mazeNumber(int levelNumber);

	Color getFoodColor(int mazeNumber);

	Image getMazeFullImage(int mazeNumber);

	Image getMazeEmptyImage(int mazeNumber);

	// Drawing

	/**
	 * Draws the entity's sprite centered over the entity's collision box. The collision box is a square with left upper
	 * corner defined by the entity position and side length of one tile size. Respects the current visibility of the
	 * entity.
	 * 
	 * @param g      graphics context
	 * @param entity entity
	 * @param s      entity sprite (region in spritesheet)
	 */
	default void drawEntity(GraphicsContext g, Entity entity, Rectangle2D sprite) {
		if (entity.visible) {
			drawSpriteCenteredOverBox(g, sprite, entity.position.x, entity.position.y);
		}
	}

	/**
	 * Draws the sprite defined by the given spritesheet region centered of the the one square tile box with left upper
	 * corner defined by the given coordinates.
	 * 
	 * @param g graphics context
	 * @param s sprite region in spritesheet
	 * @param x left upper corner of the box (one square tile)
	 * @param y left upper corner of the box
	 */
	default void drawSpriteCenteredOverBox(GraphicsContext g, Rectangle2D s, double x, double y) {
		double dx = HTS - s.getWidth() / 2, dy = HTS - s.getHeight() / 2;
		drawSprite(g, s, x + dx, y + dy);
	}

	/**
	 * Draws sprite (region) using spritesheet.
	 * 
	 * @param g graphics context
	 * @param s sprite (region in spritesheet)
	 * @param x left upper corner x
	 * @param y left upper corner y
	 */
	void drawSprite(GraphicsContext g, Rectangle2D s, double x, double y);

	/**
	 * Draws the copyright text and image. Used in several scenes so put this here.
	 * 
	 * @param g graphics context
	 * @param x left upper corner x
	 * @param y left upper corner y
	 */
	void drawCopyright(GraphicsContext g);
}