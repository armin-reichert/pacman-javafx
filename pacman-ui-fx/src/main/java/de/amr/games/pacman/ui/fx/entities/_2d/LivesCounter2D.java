package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 2D lives counter.
 * 
 * @author Armin Reichert
 */
public class LivesCounter2D<RENDERING extends Rendering2D> implements Renderable2D<RENDERING> {

	private final RENDERING rendering;
	private V2i tile;
	private int lives;

	public LivesCounter2D(RENDERING rendering) {
		this.rendering = rendering;
	}

	public void setLeftUpperCorner(V2i tile) {
		this.tile = tile;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	@Override
	public void render(GraphicsContext g) {
		Rectangle2D sprite = rendering.getLifeImage();
		int maxLivesDisplayed = 5;
		double x = tile.x * TS, y = tile.y * TS;
		for (int i = 0; i < Math.min(lives, maxLivesDisplayed); ++i) {
			rendering.renderSprite(g, sprite, x + t(2 * i), y);
		}
		if (lives > maxLivesDisplayed) {
			g.setFill(Color.YELLOW);
			g.setFont(Font.font("Sans Serif", FontWeight.BOLD, 6));
			g.fillText("+" + (lives - maxLivesDisplayed), x + t(10), y + t(1) - 2);
		}
	}
}