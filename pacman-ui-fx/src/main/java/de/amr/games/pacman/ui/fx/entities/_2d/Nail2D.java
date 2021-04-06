package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.scene.canvas.GraphicsContext;

public class Nail2D extends Renderable2D {

	private final GameEntity nail;

	public Nail2D(GameEntity nail, GameRendering2D rendering) {
		super(rendering);
		this.nail = nail;
	}

	public void render(GraphicsContext g) {
		renderEntity(g, nail, rendering.getNail());
	}
}