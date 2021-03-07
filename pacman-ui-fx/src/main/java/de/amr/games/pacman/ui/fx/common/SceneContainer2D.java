package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;

import javafx.scene.PerspectiveCamera;
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
public class SceneContainer2D {

	private final SubScene scene;
	private final Canvas canvas;
	private final PerspectiveCamera perspectiveCamera;
	private Scale scale;
	private GameScene2D gameScene;

	public SceneContainer2D(double sceneWidth, double sceneHeight) {
		double canvasWidth = GameScene.ASPECT_RATIO * sceneHeight;
		double canvasHeight = sceneHeight;
		scale = new Scale(canvasHeight / GameScene.WIDTH_UNSCALED, canvasHeight / GameScene.HEIGHT_UNSCALED);
		canvas = new Canvas(canvasWidth, canvasHeight);
		scene = new SubScene(new StackPane(canvas), sceneWidth, sceneHeight);
		perspectiveCamera = new PerspectiveCamera();
		perspectiveCamera.setTranslateZ(-200);
		log("SubScene2D size: w=%f h=%f, canvas size: w=%f h=%f", sceneWidth, sceneHeight, canvasWidth, canvasHeight);
	}

	public void setGameScene(GameScene2D gameScene) {
		this.gameScene = gameScene;
	}

	public void draw() {
		if (gameScene == null) {
			return;
		}
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gameScene.updateCamera();
		g.save();
		g.scale(scale.getX(), scale.getY());
		gameScene.draw(g);
		g.restore();
	}

	public void resize(double width, double height) {
		double canvasWidth = GameScene.ASPECT_RATIO * height;
		double canvasHeight = height;
		if (canvasWidth <= width && canvasHeight <= height) {
			canvas.setWidth(canvasWidth);
			canvas.setHeight(canvasHeight);
			scene.setWidth(width);
			scene.setHeight(height);
			scale = new Scale(canvasWidth / GameScene.WIDTH_UNSCALED, canvasHeight / GameScene.HEIGHT_UNSCALED);
		}
	}

	public SubScene getSubScene() {
		return scene;
	}

	public PerspectiveCamera getPerspectiveCamera() {
		return perspectiveCamera;
	}

	public boolean isPerspectiveCameraOn() {
		return scene.getCamera() != null;
	}

	public void perspectiveViewOn() {
		scene.setCamera(perspectiveCamera);
	}

	public void perspectiveViewOff() {
		scene.setCamera(null); // use parallel camera
	}
}