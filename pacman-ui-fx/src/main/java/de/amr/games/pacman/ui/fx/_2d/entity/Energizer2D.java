package de.amr.games.pacman.ui.fx._2d.entity;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.TimedSequence;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 2D representation of a blinking energizer.
 * 
 * @author Armin Reichert
 */
public class Energizer2D implements Renderable2D {

	public int x;
	public int y;
	public Color darkColor = Color.BLACK;
	public TimedSequence<Boolean> blinkingAnimation;

	@Override
	public void render(GraphicsContext g) {
		if (!blinkingAnimation.frame()) {
			g.setFill(darkColor);
			g.fillRect(x, y, TS, TS);
		}
	}
}