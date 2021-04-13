package de.amr.games.pacman.ui.fx.entities._2d.pacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_PacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * The naked Blinky from the third intermission scene in Pac-Man.
 * 
 * @author Armin Reichert
 */
public class BlinkyNaked2D implements Renderable2D<Rendering2D_PacMan> {

	private final Rendering2D_PacMan rendering;
	private final Ghost blinky;
	private TimedSequence<Rectangle2D> animation;

	public BlinkyNaked2D(Ghost blinky, Rendering2D_PacMan rendering) {
		this.rendering = rendering;
		this.blinky = blinky;
		animation = rendering.createBlinkyNakedAnimation();
	}

	public TimedSequence<Rectangle2D> getAnimation() {
		return animation;
	}

	@Override
	public void render(GraphicsContext g) {
		rendering.renderEntity(g, blinky, animation.animate());
	}
}