package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

class Stork extends GameEntity {

	final MsPacMan_Rendering rendering = PacManGameUI_JavaFX.RENDERING_MSPACMAN;
	final Animation<Rectangle2D> animation = Animation.of(//
			new Rectangle2D(489, 176, 32, 16), //
			new Rectangle2D(521, 176, 32, 16)//
	).endless().frameDuration(10);

	public void draw(GraphicsContext g) {
		if (visible) {
			Rectangle2D frame = animation.animate();
			rendering.drawRegion(g, frame, position.x + 4 - frame.getWidth() / 2, position.y + 4 - frame.getHeight() / 2);
		}
	}
}