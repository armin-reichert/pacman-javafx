package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.Stork;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Stork2D extends Renderable2D {

	private final Stork stork;
	private TimedSequence<Rectangle2D> animation;

	public Stork2D(Stork stork) {
		this.stork = stork;
	}

	@Override
	public void setRendering(GameRendering2D rendering) {
		super.setRendering(rendering);
		setAnimation(rendering.createStorkFlyingAnimation());
	}

	public TimedSequence<Rectangle2D> getAnimation() {
		return animation;
	}

	public void setAnimation(TimedSequence<Rectangle2D> animation) {
		this.animation = animation;
	}

	@Override
	public void render(GraphicsContext gc) {
		Rectangle2D sprite = animation.animate();
		renderEntity(gc, stork, sprite);
	}
}