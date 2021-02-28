package de.amr.games.pacman.ui.fx.common;

import java.util.Objects;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.scene.Camera;
import javafx.scene.canvas.GraphicsContext;

/**
 * A game scene that gets drawn into a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene {

	protected final PacManGameController controller;
	protected final FXRendering rendering;
	protected final SoundManager sounds;
	protected double scaling;

	public boolean cameraAllowed;

	public GameScene(PacManGameController controller, double scaling, FXRendering rendering, SoundManager sounds) {
		this.controller = controller;
		this.scaling = scaling;
		this.rendering = Objects.requireNonNull(rendering);
		this.sounds = Objects.requireNonNull(sounds);
	}

	public abstract void draw(GraphicsContext g);

	public void start() {
	}

	public void update() {
	}

	public void end() {
	}

	public void updateCamera(Camera cam) {
	}
}