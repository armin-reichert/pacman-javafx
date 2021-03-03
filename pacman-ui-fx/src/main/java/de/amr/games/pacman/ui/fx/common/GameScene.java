package de.amr.games.pacman.ui.fx.common;

import java.util.Objects;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Scale;

/**
 * A game scene that gets drawn into a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene {

	protected final PacManGameController controller;
	protected final FXRendering rendering;
	protected final SoundManager sounds;

	protected ControllablePerspectiveCamera cam;

	public GameScene(PacManGameController controller, FXRendering rendering, SoundManager sounds) {
		this.controller = controller;
		this.rendering = Objects.requireNonNull(rendering);
		this.sounds = Objects.requireNonNull(sounds);
		this.cam = new ControllablePerspectiveCamera();
	}

	public abstract void draw(GraphicsContext g);

	public void start() {
	}

	public void update() {
	}

	public void end() {
	}

	public ControllablePerspectiveCamera getCam() {
		return cam;
	}

	public void updateCamera(Scale scale) {
	}
}