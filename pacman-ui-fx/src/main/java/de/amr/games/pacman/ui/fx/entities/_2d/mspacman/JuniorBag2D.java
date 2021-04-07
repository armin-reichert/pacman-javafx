package de.amr.games.pacman.ui.fx.entities._2d.mspacman;

import de.amr.games.pacman.model.mspacman.JuniorBag;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_MsPacMan;
import javafx.scene.canvas.GraphicsContext;

/**
 * The bag containing junior Pac-Man that is dropped by the stork in Ms. Pac-Man
 * intermission scene 3.
 * 
 * @author Armin Reichert
 */
public class JuniorBag2D extends Renderable2D<GameRendering2D_MsPacMan> {

	private final JuniorBag bag;

	public JuniorBag2D(JuniorBag bag, GameRendering2D_MsPacMan rendering) {
		super(rendering);
		this.bag = bag;
	}

	@Override
	public void render(GraphicsContext gc) {
		renderEntity(gc, bag, bag.open ? rendering.getJunior() : rendering.getBlueBag());
	}
}