package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class BlinkyPatched2D extends GameEntity2D {

	private final Ghost blinky;
	private TimedSequence<Rectangle2D> animation;

	public BlinkyPatched2D(Ghost blinky) {
		this.blinky = blinky;
	}

	@Override
	public void setRendering(GameRendering2D rendering) {
		super.setRendering(rendering);
		animation = rendering.createBlinkyPatchedAnimation();
	}

	public void render(GraphicsContext g) {
		render(g, blinky, animation.animate());
	}
}
