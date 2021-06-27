package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.function.IntSupplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 2D representation of the score or the high score.
 * 
 * @author Armin Reichert
 */
public class GameScore2D implements Renderable2D {

	private final Rendering2D rendering;
	private IntSupplier pointsSupplier;
	private IntSupplier levelSupplier;

	private int x;
	private int y;
	private Color titleColor = Color.WHITE;
	private Color pointsColor = Color.YELLOW;
	private String title = "SCORE";
	private boolean showPoints = true;

	public GameScore2D(Rendering2D rendering) {
		this.rendering = rendering;
	}

	public void setPointsSupplier(IntSupplier pointsSupplier) {
		this.pointsSupplier = pointsSupplier;
	}

	public void setLevelSupplier(IntSupplier levelSupplier) {
		this.levelSupplier = levelSupplier;
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

	public void setLeftUpperCorner(V2i tile) {
		x = t(tile.x);
		y = t(tile.y);
	}

	public void setTitleColor(Color titleColor) {
		this.titleColor = titleColor;
	}

	public void setPointsColor(Color pointsColor) {
		this.pointsColor = pointsColor;
	}

	public void setShowPoints(boolean showPoints) {
		this.showPoints = showPoints;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}