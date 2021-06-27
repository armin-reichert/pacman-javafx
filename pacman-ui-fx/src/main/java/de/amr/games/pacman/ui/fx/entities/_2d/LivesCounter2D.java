package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 2D lives counter.
 * 
 * @author Armin Reichert
 */
public class LivesCounter2D implements Renderable2D {

	private final int x;
	private final int y;
	private final Rendering2D rendering;
	private final int maxLivesDisplayed = 5;
	public int lives;

	public LivesCounter2D(V2i leftUpperTile, Rendering2D rendering) {
		x = t(leftUpperTile.x);
		y = t(leftUpperTile.y);
		this.rendering = rendering;
	}

	@Override
	public void render(GraphicsContext g) {
		var sprite = rendering.getLifeImage();
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