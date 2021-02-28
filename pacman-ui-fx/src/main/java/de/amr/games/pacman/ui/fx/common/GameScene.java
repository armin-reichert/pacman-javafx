package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.UNSCALED_SCENE_HEIGHT_PX;
import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.UNSCALED_SCENE_WIDTH_PX;

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A game scene.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene {

	public final double width, height;
	public final double scaling;

	public final Group content;
	protected final Canvas canvas;

	public final FXRendering rendering;
	public final SoundManager sounds;

	protected GameModel game;

	public boolean cameraAllowed;

	public GameScene(double scaling, FXRendering rendering, SoundManager sounds) {
		this.scaling = scaling;
		this.rendering = Objects.requireNonNull(rendering);
		this.sounds = Objects.requireNonNull(sounds);
		width = UNSCALED_SCENE_WIDTH_PX * scaling;
		height = UNSCALED_SCENE_HEIGHT_PX * scaling;
		canvas = new Canvas(width, height);
		canvas.setViewOrder(1);
		content = new Group(canvas);
	}

	public void start() {
	}

	public abstract void update();

	public void end() {
	}

	protected abstract void drawCanvas();

	public final void draw() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.save();
		g.scale(scaling, scaling);
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		drawCanvas();
		g.restore();
	}

	public void updateCamera(Camera cam) {
	}

	public void setGame(GameModel game) {
		this.game = game;
	}

	public Optional<GameModel> game() {
		return Optional.ofNullable(game);
	}
}