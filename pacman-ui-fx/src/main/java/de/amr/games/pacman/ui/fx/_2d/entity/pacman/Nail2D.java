package de.amr.games.pacman.ui.fx._2d.entity.pacman;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx._2d.entity.common.Renderable2D;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D_PacMan;
import javafx.scene.canvas.GraphicsContext;

public class Nail2D implements Renderable2D {

	private final GameEntity nail;
	private final Rendering2D_PacMan rendering;

	public Nail2D(GameEntity nail, Rendering2D_PacMan rendering) {
		this.nail = nail;
		this.rendering = rendering;
	}

	@Override
	public void render(GraphicsContext g) {
		rendering.renderEntity(g, nail, rendering.getNail());
	}
}