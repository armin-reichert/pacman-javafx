package de.amr.games.pacman.ui.fx.mspacman.entities;

import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.fx.common.FXRendering;
import javafx.scene.canvas.GraphicsContext;

public class Heart extends GameEntity {

	public Heart() {
	}

	public void draw(GraphicsContext g, FXRendering rendering) {
		rendering.drawHeart(g, this);
	}
}