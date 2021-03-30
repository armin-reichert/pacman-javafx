package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.animation.TimedSequence;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Energizer2D {

	private final V2i tile;
	private TimedSequence<Boolean> blinkingAnimation = TimedSequence.pulse().frameDuration(15);

	public Energizer2D(V2i tile) {
		this.tile = tile;
	}

	public TimedSequence<Boolean> getBlinkingAnimation() {
		return blinkingAnimation;
	}

	public void render(GraphicsContext g) {
		if (!blinkingAnimation.animate()) {
			g.setFill(Color.BLACK);
			g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
		}
	}
}