package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import de.amr.games.pacman.model.common.GameEntity;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class GameEntity2D {

	protected Image spritesheet;

	protected void render(GraphicsContext g, GameEntity entity, Rectangle2D sprite) {
		if (entity.visible && spritesheet != null && sprite != null) {
			g.drawImage(spritesheet, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
					entity.position.x - sprite.getWidth() / 2 + HTS, entity.position.y - sprite.getHeight() / 2 + HTS,
					sprite.getWidth(), sprite.getHeight());
		}
	}

	public void setRendering(GameRendering2D rendering) {
		setSpritesheet(rendering.spritesheet);
	}

	public void setSpritesheet(Image spritesheet) {
		this.spritesheet = spritesheet;
	}
}