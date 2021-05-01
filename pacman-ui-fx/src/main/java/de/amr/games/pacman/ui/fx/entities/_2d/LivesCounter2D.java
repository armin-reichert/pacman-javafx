package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.function.IntSupplier;

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

	public final RENDERING rendering;
	public final V2i leftUpperTile;
	private IntSupplier livesSupplier = () -> 0;

	public LivesCounter2D(V2i leftUpperTile, RENDERING rendering) {
		this.leftUpperTile = leftUpperTile;
		this.rendering = rendering;
	}

	public void setLivesSupplier(IntSupplier livesSupplier) {
		this.livesSupplier = livesSupplier;
	}

	public int lives() {
		return livesSupplier.getAsInt();
	}

	@Override
	public void render(GraphicsContext g) {
		final int lives = lives();
		final Rectangle2D sprite = rendering.getLifeImage();
		final int maxLivesDisplayed = 5;
		final double x = leftUpperTile.x * TS, y = leftUpperTile.y * TS;
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