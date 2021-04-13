package de.amr.games.pacman.ui.fx.rendering;

import de.amr.games.pacman.model.common.GameVariant;

public class Rendering2D_Impl {

	public static final Rendering2D_MsPacMan RENDERING_MS_PACMAN = new Rendering2D_MsPacMan();
	public static final Rendering2D_PacMan RENDERING_PACMAN = new Rendering2D_PacMan();

	public static Rendering2D get(GameVariant gameVariant) {
		if (gameVariant == GameVariant.MS_PACMAN) {
			return RENDERING_MS_PACMAN;
		}
		if (gameVariant == GameVariant.PACMAN) {
			return RENDERING_PACMAN;
		}
		throw new IllegalArgumentException("Unknown game variant: " + gameVariant);
	}
}