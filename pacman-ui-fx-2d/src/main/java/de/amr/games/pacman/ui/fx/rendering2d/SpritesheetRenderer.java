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

import de.amr.games.pacman.model.actors.Entity;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Common rendering functionality for renderers using a spritesheet.
 * 
 * @author Armin Reichert
 */
public abstract class SpritesheetRenderer {

	protected final Spritesheet spritesheet;

	protected SpritesheetRenderer(Spritesheet spritesheet) {
		this.spritesheet = spritesheet;
	}

	public Spritesheet spritesheet() {
		return spritesheet;
	}

	public Image image(Rectangle2D region) {
		return spritesheet.subImage(region);
	}

	/**
	 * Draws a region from the spritesheet, no need for separate image copy.
	 * 
	 * @param g graphics context
	 * @param r rectangular spritesheet region
	 * @param x x position
	 * @param y y position
	 */
	public void drawSprite(GraphicsContext g, Rectangle2D r, double x, double y) {
		if (r != null) {
			g.drawImage(spritesheet.source(), r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, r.getWidth(),
					r.getHeight());
		}
	}

	public void drawSpriteCenteredOverBox(GraphicsContext g, Rectangle2D r, double x, double y) {
		if (r != null) {
			double dx = HTS - r.getWidth() / 2;
			double dy = HTS - r.getHeight() / 2;
			drawSprite(g, r, x + dx, y + dy);
		}
	}

	public void drawEntitySprite(GraphicsContext g, Entity entity, Rectangle2D r) {
		if (entity.isVisible()) {
			drawSpriteCenteredOverBox(g, r, entity.position().x(), entity.position().y());
		}
	}

}