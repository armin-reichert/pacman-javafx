package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class BigPacMan2D extends Renderable2D {

	private final Pac pacMan;
	private TimedSequence<Rectangle2D> munchingAnimation;

	public BigPacMan2D(Pac pacMan, GameRendering2D rendering) {
		super(rendering);
		this.pacMan = pacMan;
		munchingAnimation = rendering.createBigPacManMunchingAnimation();
	}

	public TimedSequence<Rectangle2D> getMunchingAnimation() {
		return munchingAnimation;
	}

	@Override
	public void render(GraphicsContext g) {
		Rectangle2D sprite = munchingAnimation.animate();
		g.save();
		g.translate(0, -sprite.getHeight() / 2 + 8);
		renderEntity(g, pacMan, sprite);
		g.restore();
	}
}