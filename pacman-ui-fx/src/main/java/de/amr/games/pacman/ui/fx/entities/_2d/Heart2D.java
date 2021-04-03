package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Heart2D extends Renderable2D {

	private final GameEntity heart;
	private Rectangle2D image;

	public Heart2D(GameEntity heart, GameRendering2D rendering) {
		super(rendering);
		this.heart = heart;
		image = rendering.getHeart();
	}

	@Override
	public void render(GraphicsContext gc) {
		renderEntity(gc, heart, image);
	}
}