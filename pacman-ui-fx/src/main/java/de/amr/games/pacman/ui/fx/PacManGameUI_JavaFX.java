package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.ui.fx.common.SceneController.createGameScene;
import static de.amr.games.pacman.ui.fx.common.SceneController.is2DAnd3DVersionAvailable;
import static de.amr.games.pacman.ui.fx.common.SceneController.isSuitableScene;

import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.common.CameraController;
import de.amr.games.pacman.ui.fx.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.GlobalSettings;
import de.amr.games.pacman.ui.fx.common.SceneController;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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

	private boolean use3DScenes;
	private boolean muted;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.controller = controller;

		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> Platform.exit());

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, camControl::handleKeyEvent);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			switch (e.getCode()) {
			case F11:
				stage.setFullScreen(true);
				break;
			case DIGIT3:
				if (e.isControlDown()) {
					toggleUse3DScenes();
				}
				break;
			case I:
				if (e.isControlDown()) {
					GlobalSettings.infoViewVisible = !GlobalSettings.infoViewVisible;
				}
				break;
			case L:
				if (e.isControlDown()) {
					GlobalSettings.drawWallsAsLines = !GlobalSettings.drawWallsAsLines;
				}
			default:
				break;
			}
		});

		infoView.setFill(Color.LIGHTGREEN);
		infoView.setFont(Font.font("Monospace", 14));
		infoView.setText("");
		StackPane.setAlignment(infoView, Pos.TOP_LEFT);

		mainSceneRoot = new StackPane();
		mainScene = new Scene(mainSceneRoot, GameScene.ASPECT_RATIO * height, height, Color.BLACK);

		GameScene newGameScene = createGameScene(controller, height, use3DScenes);
		changeGameScene(newGameScene);
		addResizeHandler(newGameScene);

		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.show();
	}

	private void toggleUse3DScenes() {
		use3DScenes = !use3DScenes;
		String message = String.format("3D scenes %s", use3DScenes ? "ON" : "OFF");
		showFlashMessage(message, clock.sec(1));
		if (is2DAnd3DVersionAvailable(controller.currentlyPlaying(), controller.getGame())) {
			currentGameScene = null; // trigger scene change
			log("Scene must change because 2D and 3D versions are available");
		}
	}

	@Override
	public void show() {
		// done in start()
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
		camControl.setCamera(newGameScene.getCamera());
		if (newGameScene.getCamera() != null) {
			newGameScene.initCamera();
		}
		mainSceneRoot.getChildren().clear();
		mainSceneRoot.getChildren().addAll(newGameScene.getSubScene(), flashMessageView, infoView);
		newGameScene.start();
		log("New game scene '%s' started", newGameScene);
	}

	@Override
	public void onGameChanged(GameModel newGame) {
	}

	@Override
	public void onGameStateChanged(PacManGameState from, PacManGameState to) {
		if (from == PacManGameState.CHANGING_LEVEL) {
			if (currentGameScene != null) {
				currentGameScene.start();
			}
			showFlashMessage("Enter level " + controller.getGame().levelNumber, clock.sec(1));
		}
	}

	@Override
	public void update() {
		if (currentGameScene == null
				|| !isSuitableScene(currentGameScene, controller.currentlyPlaying(), controller.getGame())) {
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			GameScene newGameScene = createGameScene(controller, mainScene.getHeight(), use3DScenes);
			addResizeHandler(newGameScene);
			log("New game scene '%s' created", newGameScene);
			changeGameScene(newGameScene);
		}

		currentGameScene.update();
		flashMessageView.update();

		// update info view
		String text = "";
		text += String.format("Main scene: w=%.0f h=%.0f\n", mainScene.getWidth(), mainScene.getHeight());
		text += String.format("%s: w=%.0f h=%.0f\n", currentGameScene.getClass().getSimpleName(),
				currentGameScene.getSubScene().getWidth(), currentGameScene.getSubScene().getHeight());
		text += camControl.getCameraInfo();
		infoView.setText(text);
		infoView.setVisible(GlobalSettings.infoViewVisible);
	}

	@Override
	public void reset() {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
	}

	@Override
	public void showFlashMessage(String message, long ticks) {
		flashMessageView.showMessage(message, ticks);
	}

	@Override
	public boolean keyPressed(String keySpec) {
		return keyboard.keyPressed(keySpec);
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			return Optional.empty(); // TODO
		}
		return Optional.of(SceneController.SOUND.get(controller.currentlyPlaying()));
	}

	@Override
	public void mute(boolean state) {
		muted = state;
	}

	@Override
	public Optional<PacManGameAnimations> animation() {
		return currentGameScene != null ? currentGameScene.animations() : Optional.empty();
	}
}