package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

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

	private V2i tile;
	private TimedSequence<Boolean> blinkingAnimation;

	public void setTile(V2i tile) {
		this.tile = tile;
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
			g.setFill(Color.BLACK); // TODO rendering should provide this color
			g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
		}
	}
}