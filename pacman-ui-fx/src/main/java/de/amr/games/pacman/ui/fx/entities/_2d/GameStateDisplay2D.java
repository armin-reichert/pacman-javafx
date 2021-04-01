package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GameStateDisplay2D {

	private Font font;
	private PacManGameState state;

	public void render(GraphicsContext g) {
		if (state == PacManGameState.GAME_OVER) {
			g.setFont(font);
			g.setFill(Color.RED);
			g.fillText("GAME", t(9), t(21));
			g.fillText("OVER", t(15), t(21));
		} else if (state == PacManGameState.READY) {
			g.setFont(font);
			g.setFill(Color.YELLOW);
			g.fillText("READY", t(11), t(21));
		}
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public PacManGameState getState() {
		return state;
	}

	public void setState(PacManGameState state) {
		this.state = state;
	}
}