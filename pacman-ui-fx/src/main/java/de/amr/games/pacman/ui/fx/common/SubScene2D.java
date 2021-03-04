package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Subscene containing resizable canvas for displaying 2D scenes.
 * 
 * @author Armin Reichert
 */
public class SubScene2D {

	public static final int WIDTH_UNSCALED = 28 * TS;
	public static final int HEIGHT_UNSCALED = 36 * TS;
	public static final double ASPECT_RATIO = (double) WIDTH_UNSCALED / HEIGHT_UNSCALED;

	private final SubScene scene;
	private final Canvas canvas;
	private Scale scale;

	public SubScene2D(double sceneWidth, double sceneHeight) {
		double canvasWidth = ASPECT_RATIO * sceneHeight;
		double canvasHeight = sceneHeight;
		scale = new Scale(canvasHeight / WIDTH_UNSCALED, canvasHeight / HEIGHT_UNSCALED);
		canvas = new Canvas(canvasWidth, canvasHeight);
		scene = new SubScene(new StackPane(canvas), sceneWidth, sceneHeight);
		log("SubScene2D size: w=%f h=%f, canvas size: w=%f h=%f", sceneWidth, sceneHeight, canvasWidth, canvasHeight);
	}

	public void resize(double width, double height) {
		double canvasWidth = ASPECT_RATIO * height;
		double canvasHeight = height;
		if (canvasWidth <= width && canvasHeight <= height) {
			canvas.setWidth(canvasWidth);
			canvas.setHeight(canvasHeight);
			scene.setWidth(width);
			scene.setHeight(height);
			scale = new Scale(canvasWidth / WIDTH_UNSCALED, canvasHeight / HEIGHT_UNSCALED);
			log("Playground got new size: w=%f h=%f, canvas w=%f h=%f", width, height, canvas.getWidth(), canvas.getHeight());
		}
	}

	public SubScene getScene() {
		return scene;
	}

	public void drawSceneContent(GameScene gameScene) {
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gameScene.getCamera().ifPresent(cam -> {
			gameScene.updateCamera();
		});
		g.save();
		g.scale(scale.getX(), scale.getY());
		gameScene.draw(g);
		g.restore();
	}

	public boolean isCameraOn() {
		return scene.getCamera() != null;
	}

	public void cameraOn(ControllableCamera cam) {
		scene.setCamera(cam);
	}

	public void cameraOff(ControllableCamera cam) {
		scene.setCamera(null);
	}
}