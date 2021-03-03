package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2d;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Subscene for the playground.
 * 
 * @author Armin Reichert
 */
public class Playground {

	public static final int WIDTH_UNSCALED = 28 * TS;
	public static final int HEIGHT_UNSCALED = 36 * TS;
	public static final double ASPECT_RATIO = (double) WIDTH_UNSCALED / HEIGHT_UNSCALED;

	private final SubScene scene;
	private final StackPane root;
	private final Canvas canvas;
	private Scale scale;

	public Playground(double width, double height) {
		double canvasWidth = ASPECT_RATIO * height;
		double canvasHeight = height;
		root = new StackPane();
		canvas = new Canvas(canvasWidth, canvasHeight);
		root.getChildren().add(canvas);
		scene = new SubScene(root, width, height);
		scale = new Scale(canvasHeight / WIDTH_UNSCALED, canvasHeight / HEIGHT_UNSCALED);
		log("Playground got initial size: w=%f h=%f, canvas w=%f h=%f", width, height, canvas.getWidth(),
				canvas.getHeight());
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

	public void draw(GameScene gameScene) {
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gameScene.getCam().ifPresent(cam -> {
			gameScene.updateCamera(new V2d(scene.getWidth(), scene.getHeight()), scale);
		});
		g.save();
		g.scale(scale.getX(), scale.getY());
		gameScene.draw(g);
		g.restore();
	}

	public boolean isCameraOn() {
		return scene.getCamera() != null;
	}

	public void cameraOn(GameScene gameScene) {
		gameScene.getCam().ifPresent(cam -> {
			cam.setTranslateZ(-240);
			scene.setCamera(cam);
			gameScene.enableCam(true);
		});
	}

	public void cameraOff(GameScene gameScene) {
		gameScene.getCam().ifPresent(cam -> {
			cam.setTranslateX(0);
			cam.setTranslateY(0);
			cam.setTranslateZ(0);
			cam.setRotate(0);
			gameScene.enableCam(false);
			scene.setCamera(null);
		});
	}
}