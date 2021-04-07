package de.amr.games.pacman.ui.fx.entities._2d.mspacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.Stork;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_MsPacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of the stork flying through intermission scene 3 of Ms.
 * Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Stork2D extends Renderable2D<GameRendering2D_MsPacMan> {

	private final Stork stork;
	private TimedSequence<Rectangle2D> animation;

	public Stork2D(Stork stork, GameRendering2D_MsPacMan rendering) {
		super(rendering);
		this.stork = stork;
		animation = rendering.createStorkFlyingAnimation();
	}

	public TimedSequence<Rectangle2D> getAnimation() {
		return animation;
	}

	@Override
	public void render(GraphicsContext g) {
		renderEntity(g, stork, animation.animate());
	}
}