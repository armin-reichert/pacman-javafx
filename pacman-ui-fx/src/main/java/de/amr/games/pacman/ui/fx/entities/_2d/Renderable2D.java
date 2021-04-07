package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Base class of 2D representations.
 * 
 * @author Armin Reichert
 */
public abstract class Renderable2D<RENDERING extends GameRendering2D> {

	protected final RENDERING rendering;

	public Renderable2D(RENDERING rendering) {
		this.rendering = rendering;
	}

	public abstract void render(GraphicsContext g);

	protected void renderEntity(GraphicsContext g, GameEntity entity, Rectangle2D sprite) {
		if (entity.visible && rendering != null && rendering.spritesheet != null && sprite != null) {
			renderSpriteCentered(g, entity.position.x, entity.position.y, sprite);
		}
	}

	protected void renderSpriteCentered(GraphicsContext g, double x, double y, Rectangle2D sprite) {
		if (rendering != null && rendering.spritesheet != null && sprite != null) {
			g.drawImage(rendering.spritesheet, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
					x - sprite.getWidth() / 2 + HTS, y - sprite.getHeight() / 2 + HTS, sprite.getWidth(), sprite.getHeight());
		}
	}
}