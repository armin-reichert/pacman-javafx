package de.amr.games.pacman.ui.fx.entities._2d;

import de.amr.games.pacman.model.mspacman.JuniorBag;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class JuniorBag2D extends Renderable2D {

	private final JuniorBag bag;
	private Rectangle2D blueBagSprite;
	private Rectangle2D juniorSprite;

	public JuniorBag2D(JuniorBag bag) {
		this.bag = bag;
	}

	@Override
	public void setRendering(GameRendering2D rendering) {
		super.setRendering(rendering);
		blueBagSprite = rendering.getBlueBag();
		juniorSprite = rendering.getJunior();
	}

	@Override
	public void render(GraphicsContext gc) {
		if (bag.visible) {
			if (bag.open) {
				renderEntity(gc, bag, juniorSprite);
			} else {
				renderEntity(gc, bag, blueBagSprite);
			}
		}
	}
}