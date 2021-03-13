package de.amr.games.pacman.ui.fx.rendering;

import de.amr.games.pacman.ui.Rendering;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
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
public interface PacManGameRendering2D extends Rendering<GraphicsContext, Color, Font, Rectangle2D>, PacManGameAnimations2D {
}