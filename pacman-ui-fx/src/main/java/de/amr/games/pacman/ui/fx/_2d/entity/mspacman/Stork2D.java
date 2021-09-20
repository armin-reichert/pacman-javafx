package de.amr.games.pacman.ui.fx._2d.entity.mspacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.entities.Stork;
import de.amr.games.pacman.ui.fx._2d.entity.common.Renderable2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of the stork flying through intermission scene 3 of Ms.
 * Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Stork2D implements Renderable2D {

	private final Stork stork;
	private final Rendering2D_MsPacMan rendering;
	public final TimedSequence<Rectangle2D> animation;

	public Stork2D(Stork stork, Rendering2D_MsPacMan rendering) {
		this.stork = stork;
		this.rendering = rendering;
		animation = rendering.createStorkFlyingAnimation();
	}

	@Override
	public void render(GraphicsContext g) {
		rendering.renderEntity(g, stork, animation.animate());
	}
}