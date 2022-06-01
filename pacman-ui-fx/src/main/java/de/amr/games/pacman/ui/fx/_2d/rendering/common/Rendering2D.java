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

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
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

	// Sprites

	Rectangle2D getLifeSprite();

	Rectangle2D getBountyNumberSprite(int number);

	Rectangle2D getBonusValueSprite(int number);

	Rectangle2D getSymbolSprite(int symbol);

	/**
	 * Draws the entity's sprite centered over its collision box. The entity position is the left corner of the collision
	 * box which has a size of one square tile.
	 * 
	 * @param g      the graphics context
	 * @param e      the entity getting drawn
	 * @param sprite entity sprite (region) in spritesheet
	 */
	default void drawEntity(GraphicsContext g, Entity e, Rectangle2D sprite) {
		if (e.visible) {
			drawSprite(g, sprite, e.position.x + HTS - sprite.getWidth() / 2, e.position.y + HTS - sprite.getHeight() / 2);
		}
	}

	/**
	 * Renders a sprite at a given location.
	 * 
	 * @param g the graphics context
	 * @param s sprite (region)
	 * @param x left upper corner x
	 * @param y left upper corner y
	 */
	default void drawSprite(GraphicsContext g, Rectangle2D s, double x, double y) {
		g.drawImage(spritesheet().getImage(), s.getMinX(), s.getMinY(), s.getWidth(), s.getHeight(), x, y, s.getWidth(),
				s.getHeight());
	}

	/**
	 * Draws the copyright information. Not sure if this belongs here.
	 */
	void drawCopyright(GraphicsContext g, int x, int y);

	/**
	 * @param ghostID 0=Blinky, 1=Pinky, 2=Inky, 3=Clyde/Sue
	 * @return color of ghost as given in spritesheet
	 */
	Color getGhostColor(int ghostID);

	// Maze

	/**
	 * @param levelNumber 1-based game level number
	 * @return maze number (1, 2, ...) used in this level
	 */
	int mazeNumber(int levelNumber);

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of pellets in this maze
	 */
	Color getFoodColor(int mazeNumber);

	void drawMazeFull(GraphicsContext g, int mazeNumber, double x, double y);

	void drawMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y);

	void drawMazeBright(GraphicsContext g, int mazeNumber, double x, double y);

	// Animations

	Map<Direction, SpriteAnimation> createPlayerMunchingAnimations();

	SpriteAnimation createPlayerDyingAnimation();

	Map<Direction, SpriteAnimation> createGhostKickingAnimations(int ghostID);

	SpriteAnimation createGhostFrightenedAnimation();

	SpriteAnimation createGhostFlashingAnimation();

	Map<Direction, SpriteAnimation> createGhostReturningHomeAnimations();
}