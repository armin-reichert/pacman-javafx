package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.function.IntSupplier;

import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Common;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 2D representation of the score or the high score.
 * 
 * @author Armin Reichert
 */
public class GameScore2D implements Renderable2D {

	private final Rendering2D_Common rendering;

	public int x;
	public int y;
	public IntSupplier pointsSupplier;
	public IntSupplier levelSupplier;
	public Color titleColor = Color.WHITE;
	public Color pointsColor = Color.YELLOW;
	public String title = "SCORE";
	public boolean showPoints = true;

	public GameScore2D(Rendering2D_Common rendering) {
		this.rendering = rendering;
	}

	@Override
	public void render(GraphicsContext g) {
		g.save();
		g.translate(x, y);
		g.setFont(rendering.getScoreFont());
		g.translate(0, 2);
		g.setFill(titleColor);
		g.fillText(title, 0, 0);
		g.translate(0, 1);
		if (showPoints) {
			g.setFill(pointsColor);
			g.translate(0, t(1));
			g.fillText(String.format("%08d", pointsSupplier.getAsInt()), 0, 0);
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", levelSupplier.getAsInt()), t(8), 0);
		}
		g.restore();
	}
}