package de.amr.games.pacman.ui.fx.common.scene2d;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.common.GameScene;
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
	protected final PacManGameRendering2D rendering;
	protected final SoundManager sounds;

	protected PacManGameController controller;

	public AbstractGameScene2D(PacManGameRendering2D rendering, SoundManager sounds) {
		this.rendering = rendering;
		this.sounds = sounds;
		aspectRatio = UNSCALED_SCENE_WIDTH / UNSCALED_SCENE_HEIGHT;
		canvas = new Canvas(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT);
		gc = canvas.getGraphicsContext2D();
		Group group = new Group(canvas);
		scene = new SubScene(group, UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT);
		scene.widthProperty().bind(canvas.widthProperty());
		scene.heightProperty().bind(canvas.heightProperty());
	}

	@Override
	public void setController(PacManGameController controller) {
		this.controller = controller;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + hashCode();
	}

	public PacManGameRendering2D getRendering() {
		return rendering;
	}

	@Override
	public void setAvailableSize(double width, double height) {
		width = aspectRatio * height;
		canvas.setWidth(width);
		canvas.setHeight(height);
		double scaling = height / UNSCALED_SCENE_HEIGHT;
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
	public Camera getActiveCamera() {
		return scene.getCamera();
	}

	@Override
	public void useMoveableCamera(boolean use) {
	}

	@Override
	public SubScene getFXSubScene() {
		return scene;
	}
}