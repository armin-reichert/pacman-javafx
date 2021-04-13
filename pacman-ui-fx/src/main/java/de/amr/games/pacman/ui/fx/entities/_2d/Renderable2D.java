package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * A 2D renderable thing.
 * 
 * @author Armin Reichert
 */
public interface Renderable2D<R extends GameRendering2D> {

	void render(GraphicsContext g);
}