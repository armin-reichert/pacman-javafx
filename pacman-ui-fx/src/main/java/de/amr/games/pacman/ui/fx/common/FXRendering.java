package de.amr.games.pacman.ui.fx.common;

import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.Rendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public interface FXRendering extends Rendering<GraphicsContext, Color, Font, Rectangle2D>, PacManGameAnimations {

	void drawSprite(GraphicsContext g, Rectangle2D nail, float x, float y);

}
