package de.amr.games.pacman.ui.fx._2d.entity.mspacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.entities.Flap;
import de.amr.games.pacman.ui.fx._2d.entity.Renderable2D;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D_MsPacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The film flap used at the beginning of the Ms. Pac-Man intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap2D implements Renderable2D {

	private final Flap flap;
	private Rendering2D_MsPacMan rendering;
	public final TimedSequence<Rectangle2D> animation;

	public Flap2D(Flap flap, Rendering2D_MsPacMan rendering) {
		this.flap = flap;
		this.rendering = rendering;
		animation = rendering.createFlapAnimation();
	}

	@Override
	public void render(GraphicsContext g) {
		if (flap.isVisible()) {
			Rectangle2D sprite = animation.animate();
			rendering.renderEntity(g, flap, sprite);
			g.setFont(rendering.getScoreFont());
			g.setFill(Color.rgb(222, 222, 225));
			g.fillText(String.valueOf(flap.sceneNumber), flap.position().x + sprite.getWidth() - 25,
					flap.position().y + 18);
			g.fillText(flap.sceneTitle, flap.position().x + sprite.getWidth(), flap.position().y);
		}
	}
}