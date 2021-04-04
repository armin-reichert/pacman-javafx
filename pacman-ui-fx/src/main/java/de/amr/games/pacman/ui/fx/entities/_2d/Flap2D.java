package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * The film flap used at the beginning of the Ms. Pac-Man intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap2D extends Renderable2D {

	private final Flap flap;
	private TimedSequence<Rectangle2D> animation;
	private Font font;

	public Flap2D(Flap flap, GameRendering2D rendering) {
		super(rendering);
		this.flap = flap;
		animation = rendering.createFlapAnimation();
		setFont(rendering.getScoreFont()); // TODO
	}

	public TimedSequence<Rectangle2D> getAnimation() {
		return animation;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	@Override
	public void render(GraphicsContext g) {
		if (flap.visible) {
			Rectangle2D sprite = animation.animate();
			renderEntity(g, flap, sprite);
			g.setFont(font);
			g.setFill(Color.rgb(222, 222, 225));
			g.fillText(String.valueOf(flap.sceneNumber), flap.position.x + sprite.getWidth() - 25, flap.position.y + 18);
			g.fillText(flap.sceneTitle, flap.position.x + sprite.getWidth(), flap.position.y);
		}
	}
}