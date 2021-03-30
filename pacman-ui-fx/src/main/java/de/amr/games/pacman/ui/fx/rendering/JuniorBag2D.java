package de.amr.games.pacman.ui.fx.rendering;

import de.amr.games.pacman.model.mspacman.JuniorBag;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class JuniorBag2D extends GameEntity2D {

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

	public void render(GraphicsContext gc) {
		if (bag.visible) {
			if (bag.open) {
				render(gc, bag, juniorSprite);
			} else {
				render(gc, bag, blueBagSprite);
			}
		}
	}
}