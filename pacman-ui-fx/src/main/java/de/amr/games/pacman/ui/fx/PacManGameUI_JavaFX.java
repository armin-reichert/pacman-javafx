package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.ui.fx.common.SceneController.createGameScene;
import static de.amr.games.pacman.ui.fx.common.SceneController.is2DAnd3DVersionAvailable;

import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.fx.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.common.CameraController;
import de.amr.games.pacman.ui.fx.common.Env;
import de.amr.games.pacman.ui.fx.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.SceneController;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	private GameScene currentGameScene;

	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();
	private final CameraController camControl = new CameraController();

	private final Text infoView = new Text();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final Scene mainScene;
	private final StackPane mainSceneRoot;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.controller = controller;

		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image(getClass().getResource("/pacman/graphics/pacman.png").toExternalForm()));
		stage.setOnCloseRequest(e -> Platform.exit());

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, camControl::handleKeyEvent);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			boolean control = e.isControlDown();
			switch (e.getCode()) {
			case F11:
				stage.setFullScreen(true);
				break;
			case DIGIT3:
				if (control) {
					toggleUse3DScenes();
				}
				break;
			case I:
				if (control) {
					Env.$infoViewVisible.set(!Env.$infoViewVisible.get());
				}
				break;
			case L:
				if (control) {
					Env.$drawMode.set(Env.$drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
				}
				break;
			case P:
				if (control) {
					Env.$paused.set(!Env.$paused.get());
				}
				break;
			case V:
				controller.toggleGameType();
				break;
			case S:
				if (control) {
					Env.$useStaticCamera.set(!Env.$useStaticCamera.get());
				}
				break;
			case T:
				if (control) {
					Env.$measureTime.set(!Env.$measureTime.get());
				}
				break;
			case X:
				if (control)
					Env.$showAxes.set(!Env.$showAxes.get());
				break;
			default:
				break;
			}
		});

		infoView.visibleProperty().bind(Env.$infoViewVisible);
		infoView.setFill(Color.LIGHTGREEN);
		infoView.setFont(Font.font("Monospace", 14));
		infoView.setText("");
		StackPane.setAlignment(infoView, Pos.TOP_LEFT);

		mainSceneRoot = new StackPane();
		mainScene = new Scene(mainSceneRoot, GameScene.ASPECT_RATIO * height, height, Color.rgb(20, 20, 60));

		GameScene newGameScene = createGameScene(controller, height, Env.$use3DScenes.get());
		changeGameScene(newGameScene);
		addResizeHandler(newGameScene);

		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.show();
	}

	private void toggleUse3DScenes() {
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		String message = String.format("3D scenes %s", Env.$use3DScenes.get() ? "ON" : "OFF");
		showFlashMessage(message);
		if (is2DAnd3DVersionAvailable(controller)) {
			currentGameScene = null; // trigger scene change
			log("Scene must change because 2D and 3D versions are available");
		}
	}

	private void addResizeHandler(GameScene scene) {
		if (scene.aspectRatio().isPresent()) {
			// keep aspect ratio when resizing
			double aspectRatio = scene.aspectRatio().getAsDouble();
			mainScene.widthProperty().addListener((s, o, n) -> {
				double newHeight = Math.min(n.doubleValue() / aspectRatio, mainScene.getHeight());
				double newWidth = newHeight * aspectRatio;
				scene.resize(newWidth, newHeight);
			});
			mainScene.heightProperty().addListener((s, o, n) -> {
				double newHeight = n.doubleValue();
				double newWidth = Math.min(mainScene.getHeight() * aspectRatio, mainScene.getWidth());
				scene.resize(newWidth, newHeight);
			});
		} else {
			ChangeListener<? super Number> adaptGameSceneToMainScene = (s, o, n) -> {
				scene.resize(mainScene.getWidth(), mainScene.getHeight());
			};
			mainScene.widthProperty().addListener(adaptGameSceneToMainScene);
			mainScene.heightProperty().addListener(adaptGameSceneToMainScene);
		}
	}

	private void changeGameScene(GameScene newGameScene) {
		// TODO what if new and current scene are equal?
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		currentGameScene = newGameScene;
		double width = newGameScene.aspectRatio().isPresent()
				? newGameScene.aspectRatio().getAsDouble() * mainScene.getHeight()
				: mainScene.getWidth();
		newGameScene.resize(width, mainScene.getHeight());
		camControl.setCamera(newGameScene.getStaticCamera());
		mainSceneRoot.getChildren().clear();
		mainSceneRoot.getChildren().addAll(newGameScene.getSubScene(), flashMessageView, infoView);
		newGameScene.start();
	}

	@Override
	public void update() {
		if (currentGameScene == null || !SceneController.isSuitableScene(currentGameScene, controller)) {
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			GameScene newGameScene = createGameScene(controller, mainScene.getHeight(), Env.$use3DScenes.get());
			addResizeHandler(newGameScene);
			log("New game scene '%s' created", newGameScene);
			changeGameScene(newGameScene);
		}

		if (currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) currentGameScene;
			scene2D.clearCanvas();
		}
		currentGameScene.update();
		flashMessageView.update();
	}

	public void updateInfoView() {
		String text = "";
		text += Env.$paused.get() ? "PAUSED\n" : "RUNNING\n";
		text += String.format("Window w=%.0f h=%.0f\n", mainScene.getWindow().getWidth(),
				mainScene.getWindow().getHeight());
		text += String.format("Main scene: w=%.0f h=%.0f\n", mainScene.getWidth(), mainScene.getHeight());
		text += String.format("%s: w=%.0f h=%.0f\n", currentGameScene.getClass().getSimpleName(),
				currentGameScene.getSubScene().getWidth(), currentGameScene.getSubScene().getHeight());
		if (currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) currentGameScene;
			text += String.format("Canvas2D: w=%.0f h=%.0f\n", scene2D.getCanvas().getWidth(),
					scene2D.getCanvas().getHeight());
		}
		if (Env.$useStaticCamera.get()) {
			text += camControl.getCameraInfo() + "\n";
		} else {
			text += "Dynamic Camera\n";
		}
		text += "Autopilot " + (controller.autopilot.enabled ? "ON" : "OFF") + " (Key 'A')\n";
		text += "Player is " + (controller.selectedGame().player.immune ? "IMMUNE" : "VULNERABLE") + " (Key 'I')\n";
		text += "3D scenes " + (Env.$use3DScenes.get() ? "ON" : "OFF") + " (Key CTRL+'3')\n";
		infoView.setText(text);
	}

	@Override
	public void reset() {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
	}

	@Override
	public void showFlashMessage(String message, double seconds) {
		flashMessageView.showMessage(message, (long) (60 * seconds));
	}

	@Override
	public boolean keyPressed(String keySpec) {
		return keyboard.keyPressed(keySpec);
	}

	@Override
	public Optional<SoundManager> sound() {
		return Optional.of(SceneController.SOUND.get(controller.selectedGameType()));
	}

	@Override
	public Optional<PacManGameAnimations2D> animation() {
		if (currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) currentGameScene;
			return Optional.of(scene2D.getRendering());
		}
		return Optional.empty();
	}
}