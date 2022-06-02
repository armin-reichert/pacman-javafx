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

	Rectangle2D getBountyNumberSprite(int number);

	Rectangle2D getBonusValueSprite(int number);

	Rectangle2D getLifeSprite();

	Rectangle2D getSymbolSprite(int symbol);

	SpriteAnimationMap<Direction> createPlayerMunchingAnimations();

	SpriteAnimation createPlayerDyingAnimation();

	SpriteAnimationMap<Direction> createGhostKickingAnimationMap(int ghostID);

	SpriteAnimation createGhostFrightenedAnimation();

	SpriteAnimation createGhostFlashingAnimation();

	SpriteAnimationMap<Direction> createEyesAnimationMap();

	default void drawEntity(GraphicsContext g, Entity e, Rectangle2D sprite) {
		if (e.visible) {
			drawSpriteCentered(g, sprite, e.position.x, e.position.y);
		}
	}

	default void drawSpriteCentered(GraphicsContext g, Rectangle2D s, double x, double y) {
		drawSprite(g, s, x + HTS - s.getWidth() / 2, y + HTS - s.getHeight() / 2);
	}

	default void drawSprite(GraphicsContext g, Rectangle2D s, double x, double y) {
		g.drawImage(spritesheet().getImage(), s.getMinX(), s.getMinY(), s.getWidth(), s.getHeight(), x, y, s.getWidth(),
				s.getHeight());
	}

	void drawCopyright(GraphicsContext g, int x, int y);

	int mazeNumber(int levelNumber);

	Color getFoodColor(int mazeNumber);

	void drawMazeFull(GraphicsContext g, int mazeNumber, double x, double y);

	void drawMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y);

	void drawMazeBright(GraphicsContext g, int mazeNumber, double x, double y);
}