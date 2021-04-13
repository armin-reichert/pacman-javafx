package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.function.IntSupplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
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
public class LivesCounter2D<RENDERING extends GameRendering2D> implements Renderable2D<RENDERING> {

	private final RENDERING rendering;
	private V2i tile;
	private IntSupplier lifeCountSupplier;

	public LivesCounter2D(RENDERING rendering) {
		this.rendering = rendering;
	}

	public void setLeftUpperCorner(V2i tile) {
		this.tile = tile;
	}

	public void setLifeCountSupplier(IntSupplier lifeCountSupplier) {
		this.lifeCountSupplier = lifeCountSupplier;
	}

	@Override
	public void render(GraphicsContext g) {
		Rectangle2D sprite = rendering.getLifeImage();
		int numLives = lifeCountSupplier.getAsInt();
		int maxLivesDisplayed = 5;
		double x = tile.x * TS, y = tile.y * TS;
		for (int i = 0; i < Math.min(numLives, maxLivesDisplayed); ++i) {
			rendering.drawSprite(g, sprite, x + t(2 * i), y);
		}
		if (numLives > maxLivesDisplayed) {
			g.setFill(Color.YELLOW);
			g.setFont(Font.font("Sans Serif", FontWeight.BOLD, 6));
			g.fillText("+" + (numLives - maxLivesDisplayed), x + t(10), y + t(1) - 2);
		}
	}
}