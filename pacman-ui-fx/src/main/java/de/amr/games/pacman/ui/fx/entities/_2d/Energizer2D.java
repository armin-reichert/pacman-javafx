package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 2D representation of a blinking energizer.
 * 
 * @author Armin Reichert
 */
public class Energizer2D implements Renderable2D {

	private int x;
	private int y;
	private TimedSequence<Boolean> blinkingAnimation;

	public void setTile(V2i tile) {
		x = t(tile.x);
		y = t(tile.y);
	}

	public void setBlinkingAnimation(TimedSequence<Boolean> blinkingAnimation) {
		this.blinkingAnimation = blinkingAnimation;
	}

	public TimedSequence<Boolean> getBlinkingAnimation() {
		return blinkingAnimation;
	}

	@Override
	public void render(GraphicsContext g) {
		if (!blinkingAnimation.frame()) {
			g.setFill(Color.BLACK); // assuming black maze background
			g.fillRect(x, y, TS, TS);
		}
	}
}