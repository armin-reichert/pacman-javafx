package de.amr.games.pacman.ui.fx.rendering;

import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.ui.animation.TimedSequence;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Flap2D extends GameEntity2D {

	private final Flap flap;
	private TimedSequence<Rectangle2D> animation;
	private Font font;

	public Flap2D(Flap flap) {
		this.flap = flap;
	}

	@Override
	public void setRendering(GameRendering2D rendering) {
		super.setRendering(rendering);
		setAnimation(rendering.createFlapAnimation());
		setFont(rendering.getScoreFont());
	}

	public void setAnimation(TimedSequence<Rectangle2D> animation) {
		this.animation = animation;
	}

	public TimedSequence<Rectangle2D> getAnimation() {
		return animation;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public Flap getFlap() {
		return flap;
	}

	public void render(GraphicsContext g) {
		if (flap.visible) {
			Rectangle2D sprite = animation.animate();
			render(g, flap, sprite);
			g.setFont(font);
			g.setFill(Color.rgb(222, 222, 225));
			g.fillText(flap.sceneNumber + "", (int) flap.position.x + sprite.getWidth() - 25, (int) flap.position.y + 18);
			g.fillText(flap.sceneTitle, (int) flap.position.x + sprite.getWidth(), (int) flap.position.y);
		}
	}
}