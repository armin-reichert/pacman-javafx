package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.ui.fx.common.SceneController.createGameScene;
import static de.amr.games.pacman.ui.fx.common.SceneController.is2DAnd3DVersionAvailable;
import static de.amr.games.pacman.ui.fx.common.SceneController.isSuitableScene;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.common.CameraController;
import de.amr.games.pacman.ui.fx.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.SceneController;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

	private GameModel game;
	private GameScene currentGameScene;

	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();
	private final CameraController camControl = new CameraController();

	private final Text infoView = new Text();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final StackPane mainSceneRoot = new StackPane();
	private final Scene mainScene;

	private boolean use3DScenes;
	private BooleanProperty infoVisibleProperty = new SimpleBooleanProperty(true);
	private boolean muted;

	private boolean sceneMustChange;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.controller = controller;
		double width = GameScene.ASPECT_RATIO * height;
		game = controller.getGame();

		mainSceneRoot.getChildren().addAll(flashMessageView, infoView);
		mainScene = new Scene(mainSceneRoot, width, height, Color.BLACK);
		sceneMustChange = true;

		stage.setScene(mainScene);
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
					infoVisibleProperty.set(!infoVisibleProperty.get());
				}
				break;
			default:
				break;
			}
		});

		infoView.setFill(Color.WHITE);
		infoView.setFont(Font.font("Monospace", 14));
		infoView.setText("");
		infoView.visibleProperty().bind(infoVisibleProperty);
		StackPane.setAlignment(infoView, Pos.TOP_LEFT);

		stage.centerOnScreen();
		stage.show();
	}

	private void toggleUse3DScenes() {
		use3DScenes = !use3DScenes;
		String message = String.format("3D scenes %s", use3DScenes ? "ON" : "OFF");
		showFlashMessage(message, clock.sec(1));
		if (is2DAnd3DVersionAvailable(currentGameType(), game)) {
			sceneMustChange = true;
			log("Scene must change because 2D and 3D versions are available");
		}
	}

	@Override
	public void show() {
		// done in start()
	}

	private void addResizeHandler(GameScene scene) {
		if (scene.aspectRatio().isPresent()) {
			// scene must keep aspect ratio
			mainScene.widthProperty().addListener((s, o, n) -> {
				double newHeight = mainScene.getWidth() / scene.aspectRatio().getAsDouble();
				scene.resize(mainScene.getWidth(), Math.min(newHeight, mainScene.getHeight()));
			});
			mainScene.heightProperty().addListener((s, o, n) -> {
				double newWidth = mainScene.getHeight() * scene.aspectRatio().getAsDouble();
				scene.resize(Math.min(newWidth, mainScene.getWidth()), mainScene.getHeight());
			});
		} else {
			mainScene.widthProperty().addListener((s, o, n) -> {
				scene.resize(mainScene.getWidth(), mainScene.getHeight());
			});
			mainScene.heightProperty().addListener((s, o, n) -> {
				scene.resize(mainScene.getWidth(), mainScene.getHeight());
			});
		}
	}

	private void setGameScene(GameScene newGameScene) {
		newGameScene.resize(mainScene.getWidth(), mainScene.getHeight());
		addResizeHandler(newGameScene);
		camControl.setCamera(newGameScene.getCamera());
		if (newGameScene.getCamera() != null) {
			newGameScene.initCamera();
		}
		mainSceneRoot.getChildren().clear();
		mainSceneRoot.getChildren().addAll(newGameScene.getSubScene(), flashMessageView, infoView);
		newGameScene.start();
		currentGameScene = newGameScene;
		log("New game scene '%s' started", newGameScene);
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
		controller.setAutopilot(true);
	}

	@Override
	public void onGameStateChanged(PacManGameState from, PacManGameState to) {
		if (from == PacManGameState.CHANGING_LEVEL) {
			currentGameScene.start();
			showFlashMessage("Enter level " + game.levelNumber, clock.sec(1));
		}
	}

	@Override
	public void update() {

		// must scene be changed?
		if (sceneMustChange || !isSuitableScene(currentGameType(), game, currentGameScene)) {
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			GameScene newGameScene = createGameScene(controller, mainScene.getHeight(), use3DScenes);
			log("Scene changes from '%s' to '%s'", currentGameScene, newGameScene);
			setGameScene(newGameScene);
			sceneMustChange = false;
		}
		currentGameScene.update();
		flashMessageView.update();
		updateInfoView();
	}

	private void updateInfoView() {
		String text = "";
		text += String.format("Main scene:    w=%.0f h=%.0f\n", mainScene.getWidth(), mainScene.getHeight());
		text += String.format("%s: w=%.0f h=%.0f\n", currentGameScene.getClass().getSimpleName(),
				currentGameScene.getSubScene().getWidth(), currentGameScene.getSubScene().getHeight());
		if (camControl != null) {
			text += camControl.getCameraInfo();
		}
		infoView.setText(text);
	}

	private GameType currentGameType() {
		return Stream.of(GameType.values()).filter(controller::isPlaying).findFirst().get();
	}

	@Override
	public void reset() {
		currentGameScene.end();
		onGameChanged(game);
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
		return Optional.of(SceneController.SOUND.get(currentGameType()));
	}

	@Override
	public void mute(boolean state) {
		muted = state;
	}

	@Override
	public Optional<PacManGameAnimations> animation() {
		return currentGameScene.animations();
	}
}