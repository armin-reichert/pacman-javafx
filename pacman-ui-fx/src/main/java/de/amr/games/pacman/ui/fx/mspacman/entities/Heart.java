package de.amr.games.pacman.ui.fx.mspacman.entities;

import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.RENDERING_MSPACMAN;

import de.amr.games.pacman.model.guys.GameEntity;
import javafx.scene.canvas.GraphicsContext;

public class Heart extends GameEntity {

	public void draw(GraphicsContext g) {
		RENDERING_MSPACMAN.drawHeart(g, this);
	}
}
