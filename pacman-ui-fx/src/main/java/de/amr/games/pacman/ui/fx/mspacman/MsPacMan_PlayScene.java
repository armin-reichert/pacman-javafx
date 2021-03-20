package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;

import de.amr.games.pacman.ui.fx.common.scene2d.Assets2D;
import de.amr.games.pacman.ui.fx.common.scene2d.PlayScene2D;

public class MsPacMan_PlayScene extends PlayScene2D {

	public MsPacMan_PlayScene() {
		super(Assets2D.RENDERING_2D.get(MS_PACMAN), Assets2D.SOUND.get(MS_PACMAN));
	}
}
