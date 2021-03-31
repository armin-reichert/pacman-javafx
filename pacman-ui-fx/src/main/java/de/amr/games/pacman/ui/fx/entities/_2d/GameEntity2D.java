package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GameEntity2D {

	protected GameRendering2D rendering;

	protected void render(GraphicsContext g, GameEntity entity, Rectangle2D sprite) {
		if (entity.visible && rendering != null && rendering.spritesheet != null && sprite != null) {
			g.drawImage(rendering.spritesheet, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
					entity.position.x - sprite.getWidth() / 2 + HTS, entity.position.y - sprite.getHeight() / 2 + HTS,
					sprite.getWidth(), sprite.getHeight());
		}
	}

	public void setRendering(GameRendering2D rendering) {
		this.rendering = rendering;
	}

	public Image getSpritesheet() {
		return rendering.spritesheet;
	}
}