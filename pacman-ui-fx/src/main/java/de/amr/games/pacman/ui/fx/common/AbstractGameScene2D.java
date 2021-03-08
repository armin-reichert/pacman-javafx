package de.amr.games.pacman.ui.fx.common;

import java.util.Optional;
import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

public abstract class AbstractGameScene2D implements GameScene {

	protected Double aspectRatio;
	protected final SubScene scene;
	protected final Canvas canvas;
	protected final PacManGameController controller;
	protected final FXRendering rendering;
	protected final SoundManager sounds;

	public AbstractGameScene2D(PacManGameController controller, FXRendering rendering, SoundManager sounds) {
		this.controller = controller;
		this.rendering = rendering;
		this.sounds = sounds;
		aspectRatio = (double) WIDTH_UNSCALED / HEIGHT_UNSCALED;
		canvas = new Canvas(WIDTH_UNSCALED, HEIGHT_UNSCALED);
		Group group = new Group(canvas);
		scene = new SubScene(group, WIDTH_UNSCALED, HEIGHT_UNSCALED);
	}

	@Override
	public void resize(double width, double height) {
		width = aspectRatio * height;
		scene.setWidth(width);
		scene.setHeight(height);
		canvas.setWidth(width);
		canvas.setHeight(height);
		double scaling = height / HEIGHT_UNSCALED;
		canvas.getTransforms().clear();
		canvas.getTransforms().add(new Scale(scaling, scaling));
	}

	protected void clearCanvas() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return Optional.of(rendering);
	}

	@Override
	public Camera getCamera() {
		return scene.getCamera();
	}

	@Override
	public SubScene getSubScene() {
		return scene;
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.of(aspectRatio);
	}
}