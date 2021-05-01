package de.amr.games.pacman.ui.fx.entities._2d.pacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_PacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Blinky with his dress patched. Used in the third intermission scene in
 * Pac-Man.
 * 
 * @author Armin Reichert
 */
public class BlinkyPatched2D implements Renderable2D {

	private final Rendering2D_PacMan rendering;
	private final Ghost blinky;
	private TimedSequence<Rectangle2D> animation;

	public BlinkyPatched2D(Ghost blinky, Rendering2D_PacMan rendering) {
		this.rendering = rendering;
		this.blinky = blinky;
		animation = rendering.createBlinkyPatchedAnimation();
	}

	public TimedSequence<Rectangle2D> getAnimation() {
		return animation;
	}

	@Override
	public void render(GraphicsContext g) {
		rendering.renderEntity(g, blinky, animation.animate());
	}
}