package de.amr.games.pacman.ui.fx.common;

import java.util.Objects;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.scene.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A game scene that gets drawn into a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene {

	public final double scaling;
	public final FXRendering rendering;
	public final SoundManager sounds;
	public boolean cameraAllowed;
	protected GameModel game;

	public GameScene(double scaling, FXRendering rendering, SoundManager sounds) {
		this.scaling = scaling;
		this.rendering = Objects.requireNonNull(rendering);
		this.sounds = Objects.requireNonNull(sounds);
	}

	public void setGame(GameModel game) {
		this.game = game;
	}

	public final void draw(GraphicsContext g) {
		g.save();
		g.scale(scaling, scaling);
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
		drawCanvas(g);
		g.restore();
	}

	protected abstract void drawCanvas(GraphicsContext g);

	public void start() {
	}

	public void update() {
	}

	public void end() {
	}

	public void updateCamera(Camera cam) {
	}
}