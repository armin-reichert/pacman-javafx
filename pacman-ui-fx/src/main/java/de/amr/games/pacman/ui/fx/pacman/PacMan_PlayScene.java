package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import de.amr.games.pacman.ui.fx.common.scene2d.Assets2D;
import de.amr.games.pacman.ui.fx.common.scene2d.PlayScene2D;

public class PacMan_PlayScene extends PlayScene2D {

	public PacMan_PlayScene() {
		super(Assets2D.RENDERING_2D.get(PACMAN), Assets2D.SOUND.get(PACMAN));
	}
}