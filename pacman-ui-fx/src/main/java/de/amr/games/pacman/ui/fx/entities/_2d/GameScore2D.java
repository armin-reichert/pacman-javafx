package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.function.IntSupplier;

import de.amr.games.pacman.lib.V2i;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GameScore2D extends GameEntity2D {

	private final IntSupplier pointsSupplier;
	private final IntSupplier levelSupplier;

	private V2i tile = V2i.NULL;
	private Font font = Font.font("Arial");
	private Color titleColor = Color.WHITE;
	private Color pointsColor = Color.YELLOW;
	private String title = "SCORE";
	private boolean showPoints = true;

	public GameScore2D(IntSupplier pointsSupplier, IntSupplier levelSupplier) {
		this.pointsSupplier = pointsSupplier;
		this.levelSupplier = levelSupplier;
	}

	public void render(GraphicsContext gc) {
		gc.save();
		gc.translate(tile.x * TS, tile.y * TS);
		gc.setFont(font);
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

	public V2i getTile() {
		return tile;
	}

	public void setTile(V2i tile) {
		this.tile = tile;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public Color getTitleColor() {
		return titleColor;
	}

	public void setTitleColor(Color titleColor) {
		this.titleColor = titleColor;
	}

	public Color getPointsColor() {
		return pointsColor;
	}

	public void setPointsColor(Color pointsColor) {
		this.pointsColor = pointsColor;
	}

	public void setShowPoints(boolean showPoints) {
		this.showPoints = showPoints;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
