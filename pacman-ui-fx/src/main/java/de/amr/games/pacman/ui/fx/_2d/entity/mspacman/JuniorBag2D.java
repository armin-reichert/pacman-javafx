package de.amr.games.pacman.ui.fx._2d.entity.mspacman;

import de.amr.games.pacman.model.mspacman.entities.JuniorBag;
import de.amr.games.pacman.ui.fx._2d.entity.Renderable2D;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D_MsPacMan;
import javafx.scene.canvas.GraphicsContext;

/**
 * The bag containing junior Pac-Man that is dropped by the stork in Ms. Pac-Man
 * intermission scene 3.
 * 
 * @author Armin Reichert
 */
public class JuniorBag2D implements Renderable2D {

	private final JuniorBag bag;
	private final Rendering2D_MsPacMan rendering;

	public JuniorBag2D(JuniorBag bag, Rendering2D_MsPacMan rendering) {
		this.bag = bag;
		this.rendering = rendering;
	}

	@Override
	public void render(GraphicsContext gc) {
		rendering.renderEntity(gc, bag, bag.open ? rendering.getJunior() : rendering.getBlueBag());
	}
}