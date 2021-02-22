package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Blue bag dropped by the storch in intermission scene 3, contains Pac-Man junior.
 * 
 * @author Armin Reichert
 */
class Bag extends GameEntity {

	final MsPacMan_Rendering rendering = PacManGameUI_JavaFX.RENDERING_MSPACMAN;

	boolean released = false;
	boolean open = false;
	int bounces = 0;

	@Override
	public void move() {
		if (released) {
			velocity = velocity.sum(0, 0.04f); // gravity
		}
		super.move();
	}

	public void draw(GraphicsContext g) {
		if (open) {
			// junior
			rendering.drawRegion(g, new Rectangle2D(509, 200, 8, 8), position.x, position.y);
		} else {
			// blue bag
			rendering.drawRegion(g, new Rectangle2D(488, 199, 8, 8), position.x, position.y);
		}
	}
}