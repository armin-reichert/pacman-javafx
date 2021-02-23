package de.amr.games.pacman.ui.fx.mspacman.entities;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.fx.common.FXRendering;
import javafx.scene.canvas.GraphicsContext;

public class Stork extends GameEntity {

	public Stork(Animation<?> flying) {
		animation = flying;
	}

	public void draw(GraphicsContext g, FXRendering rendering) {
		rendering.drawStork(g, this);
	}
}