package de.amr.games.pacman.ui.fx.entities._2d.mspacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The film flap used at the beginning of the Ms. Pac-Man intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap2D implements Renderable2D {

	private Rendering2D_MsPacMan rendering;
	private final Flap flap;
	private TimedSequence<Rectangle2D> animation;

	public Flap2D(Flap flap, Rendering2D_MsPacMan rendering) {
		this.rendering = rendering;
		this.flap = flap;
		animation = rendering.createFlapAnimation();
	}

	public TimedSequence<Rectangle2D> getAnimation() {
		return animation;
	}

	@Override
	public void render(GraphicsContext g) {
		if (flap.visible) {
			Rectangle2D sprite = animation.animate();
			rendering.renderEntity(g, flap, sprite);
			g.setFont(rendering.getScoreFont());
			g.setFill(Color.rgb(222, 222, 225));
			g.fillText(String.valueOf(flap.sceneNumber), flap.position.x + sprite.getWidth() - 25, flap.position.y + 18);
			g.fillText(flap.sceneTitle, flap.position.x + sprite.getWidth(), flap.position.y);
		}
	}
}