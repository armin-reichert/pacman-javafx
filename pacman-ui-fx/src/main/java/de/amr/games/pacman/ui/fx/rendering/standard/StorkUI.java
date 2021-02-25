package de.amr.games.pacman.ui.fx.rendering.standard;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;

public class StorkUI extends GameEntity {

	public final Animation<?> flying;

	public StorkUI(FXRendering rendering) {
		flying = rendering.storkFlying().restart();
	}

}