package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Energizer2D extends Renderable2D {

	private final V2i tile;
	private final TimedSequence<Boolean> blinkingAnimation;

	public Energizer2D(V2i tile, TimedSequence<Boolean> blinkingAnimation) {
		this.tile = tile;
		this.blinkingAnimation = blinkingAnimation;
	}

	public TimedSequence<Boolean> getBlinkingAnimation() {
		return blinkingAnimation;
	}

	@Override
	public void render(GraphicsContext g) {
		if (!blinkingAnimation.frame()) {
			g.setFill(Color.BLACK); // TODO could be other color
			g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
		}
	}
}