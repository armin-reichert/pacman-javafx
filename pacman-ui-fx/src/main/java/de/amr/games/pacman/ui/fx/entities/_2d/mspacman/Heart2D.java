package de.amr.games.pacman.ui.fx.entities._2d.mspacman;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import javafx.scene.canvas.GraphicsContext;

/**
 * The heart displayed in Ms. Pac-Man intermission scene 1.
 * 
 * @author Armin Reichert
 */
public class Heart2D implements Renderable2D<Rendering2D_MsPacMan> {

	private final Rendering2D_MsPacMan rendering;
	private final GameEntity heart;

	public Heart2D(GameEntity heart, Rendering2D_MsPacMan rendering) {
		this.rendering = rendering;
		this.heart = heart;
	}

	@Override
	public void render(GraphicsContext gc) {
		rendering.renderEntity(gc, heart, rendering.getHeart());
	}
}