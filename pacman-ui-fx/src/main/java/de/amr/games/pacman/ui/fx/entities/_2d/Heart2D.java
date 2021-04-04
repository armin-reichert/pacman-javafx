package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * The heart displayed in Ms. Pac-Man intermission scene 1.
 * 
 * @author Armin Reichert
 */
public class Heart2D extends Renderable2D {

	private final GameEntity heart;

	public Heart2D(GameEntity heart, GameRendering2D rendering) {
		super(rendering);
		this.heart = heart;
	}

	@Override
	public void render(GraphicsContext gc) {
		renderEntity(gc, heart, rendering.getHeart());
	}
}