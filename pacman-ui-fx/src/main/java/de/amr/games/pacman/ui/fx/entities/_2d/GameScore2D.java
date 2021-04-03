package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.function.IntSupplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameScore2D extends Renderable2D {

	private IntSupplier pointsSupplier;
	private IntSupplier levelSupplier;

	private V2i tile = V2i.NULL;
	private Color titleColor = Color.WHITE;
	private Color pointsColor = Color.YELLOW;
	private String title = "SCORE";
	private boolean showPoints = true;

	public GameScore2D(GameRendering2D rendering) {
		super(rendering);
	}

	public void setPointsSupplier(IntSupplier pointsSupplier) {
		this.pointsSupplier = pointsSupplier;
	}

	public void setLevelSupplier(IntSupplier levelSupplier) {
		this.levelSupplier = levelSupplier;
	}

	@Override
	public void render(GraphicsContext gc) {
		gc.save();
		gc.translate(tile.x * TS, tile.y * TS);
		gc.setFont(rendering.getScoreFont());
		gc.translate(0, 2);
		gc.setFill(titleColor);
		gc.fillText(title, 0, 0);
		gc.translate(0, 1);
		if (showPoints) {
			gc.setFill(pointsColor);
			gc.translate(0, t(1));
			gc.fillText(String.format("%08d", pointsSupplier.getAsInt()), 0, 0);
			gc.setFill(Color.LIGHTGRAY);
			gc.fillText(String.format("L%02d", levelSupplier.getAsInt()), t(8), 0);
		}
		gc.restore();
	}

	public void setLeftUpperCorner(V2i tile) {
		this.tile = tile;
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