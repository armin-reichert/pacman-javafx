package de.amr.games.pacman.ui.fx.scenes.common._2d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
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

	public static final double UNSCALED_SCENE_WIDTH = 28 * TS;
	public static final double UNSCALED_SCENE_HEIGHT = 36 * TS;
	public static final double ASPECT_RATIO = UNSCALED_SCENE_WIDTH / UNSCALED_SCENE_HEIGHT;

	protected final SubScene scene;
	protected final Canvas canvas;
	protected final GraphicsContext gc;
	protected final GameRendering2D rendering;
	protected final SoundManager sounds;

	protected PacManGameController gameController;

	public AbstractGameScene2D(GameRendering2D rendering, SoundManager sounds) {
		this.rendering = rendering;
		this.sounds = sounds;
		canvas = new Canvas(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT);
		gc = canvas.getGraphicsContext2D();
		scene = new SubScene(new Group(canvas), UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT);
		scene.widthProperty().bind(canvas.widthProperty());
		scene.heightProperty().bind(canvas.heightProperty());
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.of(ASPECT_RATIO);
	}

	@Override
	public PacManGameController getGameController() {
		return gameController;
	}

	@Override
	public void setGameController(PacManGameController controller) {
		this.gameController = controller;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + hashCode();
	}

	public GameRendering2D getRendering() {
		return rendering;
	}

	@Override
	public void setAvailableSize(double width, double height) {
		width = aspectRatio().getAsDouble() * height;
		canvas.setWidth(width);
		canvas.setHeight(height);
		double scaling = height / UNSCALED_SCENE_HEIGHT;
		canvas.getTransforms().clear();
		canvas.getTransforms().add(new Scale(scaling, scaling));
	}

	@Override
	public void start() {
		log("Game scene %s: start", this);
	}

	@Override
	public void end() {
		log("Game scene %s: end", this);
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
	}

	@Override
	public void update() {
	}

	public void clearCanvas(Color color) {
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public abstract void render();

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

	@Override
	public void stopAllSounds() {
		sounds.stopAll();
	}
}