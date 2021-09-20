package de.amr.games.pacman.ui.fx._2d.entity.pacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.fx._2d.entity.common.Renderable2D;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * The big Pac-Man from the first intermission scene in Pac-Man.
 * 
 * @author Armin Reichert
 */
public class BigPacMan2D implements Renderable2D {

	private final Rendering2D_PacMan rendering;
	private final Pac pacMan;
	public final TimedSequence<Rectangle2D> munchingAnimation;

	public BigPacMan2D(Pac pacMan, Rendering2D_PacMan rendering) {
		this.pacMan = pacMan;
		this.rendering = rendering;
		munchingAnimation = rendering.createBigPacManMunchingAnimation();
	}

	@Override
	public void render(GraphicsContext g) {
		Rectangle2D sprite = munchingAnimation.animate();
		// lift it up such that it sits on the ground instead of being vertically
		// centered to the ground
		g.save();
		g.translate(0, -sprite.getHeight() / 2 + 8);
		rendering.renderEntity(g, pacMan, sprite);
		g.restore();
	}
}