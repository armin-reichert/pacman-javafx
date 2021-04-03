package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Heart2D extends Renderable2D {

	private final GameEntity heart;
	private Rectangle2D image;

	public Heart2D(GameEntity heart) {
		this.heart = heart;
	}

	@Override
	public void setRendering(GameRendering2D rendering) {
		super.setRendering(rendering);
		setImage(rendering.getHeart());
	}

	public void setImage(Rectangle2D image) {
		this.image = image;
	}

	@Override
	public void render(GraphicsContext gc) {
		renderEntity(gc, heart, image);
	}
}