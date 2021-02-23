package de.amr.games.pacman.ui.fx.rendering;

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

public interface FXRendering extends Rendering<GraphicsContext, Color, Font, Rectangle2D>, PacManGameAnimations {

	void drawSprite(GraphicsContext g, Rectangle2D nail, float x, float y);

	default void drawFlap(GraphicsContext g, Flap flap) {
	}

	default void drawHeart(GraphicsContext g, Heart heart) {
	}

	default void drawStork(GraphicsContext g, Stork stork) {
	}

	default void drawJuniorBag(GraphicsContext g, JuniorBag bag) {
	}
}