package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class BigPacMan2D extends GameEntity2D {

	private final GameEntity entity;
	private TimedSequence<Rectangle2D> munchingAnimation;

	public BigPacMan2D(GameEntity entity) {
		this.entity = entity;
	}

	public TimedSequence<Rectangle2D> getMunchingAnimation() {
		return munchingAnimation;
	}

	@Override
	public void setRendering(GameRendering2D rendering) {
		super.setRendering(rendering);
		munchingAnimation = rendering.createBigPacManMunchingAnimation();
	}

	public void render(GraphicsContext g) {
		Rectangle2D sprite = munchingAnimation.animate();
		g.save();
		g.translate(0, -sprite.getHeight() / 2 + 8);
		render(g, entity, sprite);
		g.restore();
	}
}