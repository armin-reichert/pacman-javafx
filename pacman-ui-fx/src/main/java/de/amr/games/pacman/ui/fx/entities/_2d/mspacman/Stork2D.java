package de.amr.games.pacman.ui.fx.entities._2d.mspacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.Stork;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of the stork flying through intermission scene 3 of Ms.
 * Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Stork2D implements Renderable2D<Rendering2D_MsPacMan> {

	private final Rendering2D_MsPacMan rendering;
	private final Stork stork;
	private TimedSequence<Rectangle2D> animation;

	public Stork2D(Stork stork, Rendering2D_MsPacMan rendering) {
		this.rendering = rendering;
		this.stork = stork;
		animation = rendering.createStorkFlyingAnimation();
	}

	public TimedSequence<Rectangle2D> getAnimation() {
		return animation;
	}

	@Override
	public void render(GraphicsContext g) {
		rendering.renderEntity(g, stork, animation.animate());
	}
}