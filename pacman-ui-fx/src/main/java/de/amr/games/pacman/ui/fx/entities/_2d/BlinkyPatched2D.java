package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Blinky with his dress patched. Used in the third intermission scene in Pac-Man.
 * 
 * @author Armin Reichert
 */
public class BlinkyPatched2D extends Renderable2D {

	private final Ghost blinky;
	private TimedSequence<Rectangle2D> animation;

	public BlinkyPatched2D(Ghost blinky, GameRendering2D rendering) {
		super(rendering);
		this.blinky = blinky;
		animation = rendering.createBlinkyPatchedAnimation();
	}

	public TimedSequence<Rectangle2D> getAnimation() {
		return animation;
	}

	@Override
	public void render(GraphicsContext g) {
		renderEntity(g, blinky, animation.animate());
	}
}