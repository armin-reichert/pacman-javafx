package de.amr.games.pacman.ui.fx._2d.entity.mspacman;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx._2d.entity.common.Renderable2D;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D_MsPacMan;
import javafx.scene.canvas.GraphicsContext;

/**
 * The heart displayed in Ms. Pac-Man intermission scene 1.
 * 
 * @author Armin Reichert
 */
public class Heart2D implements Renderable2D {

	private final GameEntity heart;
	private final Rendering2D_MsPacMan rendering;

	public Heart2D(GameEntity heart, Rendering2D_MsPacMan rendering) {
		this.heart = heart;
		this.rendering = rendering;
	}

	@Override
	public void render(GraphicsContext gc) {
		rendering.renderEntity(gc, heart, rendering.getHeart());
	}
}