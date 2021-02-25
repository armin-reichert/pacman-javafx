package de.amr.games.pacman.ui.fx.rendering;

import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.Rendering;
import de.amr.games.pacman.ui.fx.mspacman.entities.Flap;
import de.amr.games.pacman.ui.fx.mspacman.entities.Heart;
import de.amr.games.pacman.ui.fx.mspacman.entities.JuniorBag;
import de.amr.games.pacman.ui.fx.mspacman.entities.Stork;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Interface for drawing all scenes and accessing all animations of the Pac-Man and Ms. Pac-Man
 * games.
 * 
 * @author Armin Reichert
 */
public interface FXRendering extends Rendering<GraphicsContext, Color, Font, Rectangle2D>, PacManGameAnimations {

	void drawSprite(GraphicsContext g, Rectangle2D sprite, float x, float y);

	// Pac-Man game only:

	void drawNail(GraphicsContext g, GameEntity nail);

	void drawStretchedBlinky(GraphicsContext g, Ghost blinky, V2f nailPosition, int stretching);

	void drawPatchedBlinky(GraphicsContext g, Ghost blinky);

	void drawNakedBlinky(GraphicsContext g, Ghost blinky);

	// Ms. Pac-Man game only:

	void drawFlap(GraphicsContext g, Flap flap);

	void drawHeart(GraphicsContext g, Heart heart);

	void drawStork(GraphicsContext g, Stork stork);

	void drawJuniorBag(GraphicsContext g, JuniorBag bag);

}