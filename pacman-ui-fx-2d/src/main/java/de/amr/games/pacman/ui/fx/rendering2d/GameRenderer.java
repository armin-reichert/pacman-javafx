/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx.rendering2d;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Common interface for the Pac-Man and Ms. Pac-Man game renderers.
 * 
 * @author Armin Reichert
 */
public abstract class GameRenderer {

	protected final Spritesheet spritesheet;

	protected GameRenderer(Spritesheet spritesheet) {
		checkNotNull(spritesheet);
		this.spritesheet = spritesheet;
	}

	public Spritesheet spritesheet() {
		return spritesheet;
	}

	/**
	 * @return sprite used in lives counter
	 */
	public abstract Rectangle2D livesCounterSprite();

	/**
	 * @return sprite showing ghost value (200, 400, 800, 1600)
	 */
	public abstract Rectangle2D ghostValueSprite(int index);

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol (cherries, strawberry, ...)
	 */
	public abstract Rectangle2D bonusSymbolSprite(int symbol);

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol value (100, 300, ...)
	 */
	public abstract Rectangle2D bonusValueSprite(int symbol);

	public void drawSprite(GraphicsContext g, Image source, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			g.drawImage(source, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), x, y, sprite.getWidth(), sprite.getHeight());
		}
	}

	/**
	 * Draws a sprite at the given position. The position specifies the left-upper corner.
	 * 
	 * @param g graphics context
	 * @param sprite sprite (may be null)
	 * @param x x position
	 * @param y y position
	 */
	public void drawSprite(GraphicsContext g, Rectangle2D sprite, double x, double y) {
		drawSprite(g, spritesheet().source(), sprite, x, y);
	}

	/**
	 * Draws a sprite centered over a one "square tile" large box (bounding box of creature). The position specifies the
	 * left-upper corner of the bounding box. Note that the sprites for Pac-Man and the ghosts are 16 pixels wide but the
	 * bounding box is only 8 pixels (one square tile) wide.
	 * 
	 * @param g graphics context
	 * @param sprite spritesheet region (may be null)
	 * @param x x coordinate of left-upper corner of bounding box
	 * @param y y coordinate of left-upper corner of bounding box
	 */
	public void drawSpriteOverBoundingBox(GraphicsContext g, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			drawSprite(g, sprite, x + HTS - sprite.getWidth() / 2, y + HTS - sprite.getHeight() / 2);
		}
	}

	/**
	 * Draws the sprite over the bounding box of the given entity (if visible).
	 * 
	 * @param g      graphics context
	 * @param entity an entity like Pac-Man or a ghost
	 * @param sprite      the sprite
	 */
	protected void drawEntitySprite(GraphicsContext g, Entity entity, Rectangle2D sprite) {
		checkNotNull(entity);
		if (entity.isVisible()) {
			drawSpriteOverBoundingBox(g, sprite, entity.position().x(), entity.position().y());
		}
	}

	public void drawWishDirIndicator(GraphicsContext g, Pac pac) {
		g.setFill(Color.RED);
		float r = 4;
		var center = pac.center().plus(pac.wishDir().vector().toFloatVec().scaled(8f)).minus(r, r);
		g.fillOval(center.x(), center.y(), 2 * r, 2 * r);
	}

	public abstract AnimationMap createPacAnimations(Pac pac);

	public abstract AnimationMap createGhostAnimations(Ghost ghost);

	public abstract AnimationMap createWorldAnimations(World world);
}