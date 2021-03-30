package de.amr.games.pacman.ui.fx.rendering;

import de.amr.games.pacman.model.common.GameEntity;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Heart2D extends GameEntity2D {

	private final GameEntity heart;
	private Rectangle2D image;

	public Heart2D(GameEntity heart) {
		this.heart = heart;
	}

	public void setImage(Rectangle2D image) {
		this.image = image;
	}

	public void render(GraphicsContext gc) {
		render(gc, heart, image);
	}
}