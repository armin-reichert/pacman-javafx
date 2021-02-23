package de.amr.games.pacman.ui.fx.mspacman.entities;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.GameEntity;
import javafx.geometry.Rectangle2D;

public class Stork extends GameEntity {

	public final Animation<Rectangle2D> flying = Animation.of(//
			new Rectangle2D(489, 176, 32, 16), //
			new Rectangle2D(521, 176, 32, 16)//
	).endless().frameDuration(10);

}