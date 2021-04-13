package de.amr.games.pacman.ui.fx.entities._2d.pacman;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_PacMan;
import javafx.scene.canvas.GraphicsContext;

public class Nail2D implements Renderable2D<Rendering2D_PacMan> {

	private final Rendering2D_PacMan rendering;
	private final GameEntity nail;

	public Nail2D(GameEntity nail, Rendering2D_PacMan rendering) {
		this.rendering = rendering;
		this.nail = nail;
	}

	@Override
	public void render(GraphicsContext g) {
		rendering.renderEntity(g, nail, rendering.getNail());
	}
}