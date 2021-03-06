package de.amr.games.pacman.ui.fx.common;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.scene.Camera;

public abstract class AbstractGameScene2D implements GameScene2D {

	protected final Camera camera;
	protected final PacManGameController controller;
	protected final FXRendering rendering;
	protected final SoundManager sounds;

	public AbstractGameScene2D(Camera camera, PacManGameController controller, FXRendering rendering,
			SoundManager sounds) {
		this.camera = camera;
		this.controller = controller;
		this.rendering = rendering;
		this.sounds = sounds;
	}

	@Override
	public Camera getCamera() {
		return camera;
	}
}
