package de.amr.games.pacman.ui.fx.common;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Base class of all 2D scenes that use a canvas for being rendered.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameScene2D implements GameScene {

	protected final double aspectRatio;
	protected final SubScene scene;
	protected final Canvas canvas;
	protected final GraphicsContext gc;
	protected final PacManGameController controller;
	protected final PacManGameRendering2D rendering;
	protected final SoundManager sounds;

	public AbstractGameScene2D(PacManGameController controller, PacManGameRendering2D rendering, SoundManager sounds) {
		this.controller = controller;
		this.rendering = rendering;
		this.sounds = sounds;
		aspectRatio = (double) WIDTH_UNSCALED / HEIGHT_UNSCALED;
		canvas = new Canvas(WIDTH_UNSCALED, HEIGHT_UNSCALED);
		gc = canvas.getGraphicsContext2D();
		Group group = new Group(canvas);
		scene = new SubScene(group, WIDTH_UNSCALED, HEIGHT_UNSCALED);
		scene.widthProperty().bind(canvas.widthProperty());
		scene.heightProperty().bind(canvas.heightProperty());
	}

	public PacManGameRendering2D getRendering() {
		return rendering;
	}

	@Override
	public void resize(double width, double height) {
		width = aspectRatio * height;
		canvas.setWidth(width);
		canvas.setHeight(height);
		double scaling = height / HEIGHT_UNSCALED;
		canvas.getTransforms().clear();
		canvas.getTransforms().add(new Scale(scaling, scaling));
	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public void clearCanvas() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public Camera getStaticCamera() {
		return scene.getCamera();
	}

	@Override
	public SubScene getSubScene() {
		return scene;
	}
}