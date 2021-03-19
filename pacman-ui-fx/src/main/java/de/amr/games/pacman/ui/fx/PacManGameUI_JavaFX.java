package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.ui.fx.common.SceneFactory.createGameScene;
import static de.amr.games.pacman.ui.fx.common.SceneFactory.is2DAnd3DVersionAvailable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.fx.common.Env;
import de.amr.games.pacman.ui.fx.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.SceneFactory;
import de.amr.games.pacman.ui.fx.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.common.scene2d.Assets2D;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Camera;
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

	public class InfoPane extends Text {

		private String text;

		public InfoPane() {
			setFill(Color.LIGHTGREEN);
			setFont(Font.font("Monospace", 14));
			visibleProperty().bind(Env.$infoViewVisible);
		}

		private void line(String line, Object... args) {
			text += String.format(line, args) + "\n";
		}

		public void update() {
			text = "";
			line(Env.$paused.get() ? "PAUSED" : "RUNNING");
			line("Window:          w=%.0f h=%.0f", mainScene.getWindow().getWidth(), mainScene.getWindow().getHeight());
			line("Main scene:      w=%.0f h=%.0f", mainScene.getWidth(), mainScene.getHeight());
			line("Game scene:      w=%.0f h=%.0f (%s)", currentGameScene.getFXSubScene().getWidth(),
					currentGameScene.getFXSubScene().getHeight(), currentGameScene.getClass().getSimpleName());
			if (currentGameScene instanceof AbstractGameScene2D) {
				AbstractGameScene2D scene2D = (AbstractGameScene2D) currentGameScene;
				line("Canvas2D:        w=%.0f h=%.0f", scene2D.getCanvas().getWidth(), scene2D.getCanvas().getHeight());
			}
			line("Camera:          " + cameraInfo(currentGameScene.getActiveCamera()));
			line("Autopilot:       " + (controller.autopilot.enabled ? "ON" : "OFF") + " (Key: 'A')");
			line("Player Immunity: " + (controller.selectedGame().player.immune ? "ON" : "OFF") + " (Key: 'I')");
			line("3D scenes:       " + (Env.$use3DScenes.get() ? "ON" : "OFF") + " (Key: CTRL+'3')");
			setText(text);
		}

		private String cameraInfo(Camera camera) {
			return camera == null ? "No camera"
					: String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", camera.getTranslateX(), camera.getTranslateY(),
							camera.getTranslateZ(), camera.getRotate());
		}
	}

	private final Stage stage;
	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();

	private final Scene mainScene;
	private final StackPane mainSceneRoot;
	public final InfoPane infoPane;
	private final FlashMessageView flashMessageView;

	private final Set<GameScene> scenesCreated = new HashSet<>();
	private GameScene currentGameScene;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.stage = stage;
		this.controller = controller;

		controller.addStateChangeListener(this::handleGameStateChange);

		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image(getClass().getResource("/pacman/graphics/pacman.png").toExternalForm()));
		stage.setOnCloseRequest(e -> Platform.exit());

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeys);

		flashMessageView = new FlashMessageView();

		infoPane = new InfoPane();
		StackPane.setAlignment(infoPane, Pos.TOP_LEFT);

		double width = GameScene.ASPECT_RATIO * height;
		mainSceneRoot = new StackPane();
		mainScene = new Scene(mainSceneRoot, width, height, Color.rgb(20, 20, 60));

		GameScene initialGameScene = createGameScene(stage, controller, Env.$use3DScenes.get());
		initialGameScene.setAvailableSize(width, height);
		addResizeHandler(initialGameScene);
		changeGameScene(null, initialGameScene);

		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.show();
	}

	private void handleGameStateChange(PacManGameState oldState, PacManGameState newState) {
		log("Handle game state change %s to %s", oldState, newState);
		if (!SceneFactory.isSuitableScene(currentGameScene, controller, Env.$use3DScenes.get())) {
			changeScene();
		}
		currentGameScene.onGameStateChange(oldState, newState);
	}

	private void changeScene() {
		GameScene nextScene = scenesCreated.stream()
				.filter(scene -> SceneFactory.isSuitableScene(scene, controller, Env.$use3DScenes.get())).findFirst()
				.orElseGet(this::createSuitableScene);
		changeGameScene(currentGameScene, nextScene);
	}

	private GameScene createSuitableScene() {
		GameScene scene = SceneFactory.createGameScene(stage, controller, Env.$use3DScenes.get());
		scenesCreated.add(scene);
		scene.setAvailableSize(mainScene.getWidth(), mainScene.getHeight());
		addResizeHandler(scene);
		log("New game scene '%s' created", scene);
		return scene;
	}

	private void changeGameScene(GameScene oldGameScene, GameScene newGameScene) {
		if (oldGameScene == newGameScene) {
			log("Scene change not needed for %s", oldGameScene);
			return;
		}
		log("Scene change: %s to %s", oldGameScene, newGameScene);
		if (oldGameScene != null) {
			oldGameScene.end();
		}
		// now using new game scene
		currentGameScene = newGameScene;
		mainSceneRoot.getChildren().clear();
		mainSceneRoot.getChildren().addAll(currentGameScene.getFXSubScene(), flashMessageView, infoPane);
		currentGameScene.setAvailableSize(mainScene.getWidth(), mainScene.getHeight());
		if (Env.$useStaticCamera.get()) {
			currentGameScene.useMoveableCamera(false);
		} else {
			currentGameScene.useMoveableCamera(true);
		}
		currentGameScene.start();
	}

	private void handleKeys(KeyEvent e) {
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
				if (Env.$useStaticCamera.get()) {
					currentGameScene.useMoveableCamera(false);
					showFlashMessage("Static Camera");
				} else {
					currentGameScene.useMoveableCamera(true);
					showFlashMessage("Moveable Camera");
				}
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
	}

	private void toggleUse3DScenes() {
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		String message = String.format("3D scenes %s", Env.$use3DScenes.get() ? "ON" : "OFF");
		showFlashMessage(message);
		if (is2DAnd3DVersionAvailable(controller)) {
			log("Scene must change because 2D and 3D versions are available");
			changeScene();
		}
	}

	private void addResizeHandler(GameScene scene) {
		if (scene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) scene;
			mainScene.widthProperty().addListener((s, o, n) -> {
				double newHeight = Math.min(n.doubleValue() / scene2D.getAspectRatio(), mainScene.getHeight());
				double newWidth = newHeight * scene2D.getAspectRatio();
				scene.setAvailableSize(newWidth, newHeight);
			});
			mainScene.heightProperty().addListener((s, o, n) -> {
				double newHeight = n.doubleValue();
				double newWidth = Math.min(mainScene.getHeight() * scene2D.getAspectRatio(), mainScene.getWidth());
				scene.setAvailableSize(newWidth, newHeight);
			});
		} else {
			scene.getFXSubScene().widthProperty().bind(mainScene.widthProperty());
			scene.getFXSubScene().heightProperty().bind(mainScene.heightProperty());
		}
	}

	@Override
	public void update() {
		if (currentGameScene == null) {
			log("Cannot update scene, scene is NULL");
			return;
		}
		if (currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) currentGameScene;
			scene2D.clearCanvas();
		}
		currentGameScene.update();
		flashMessageView.update();
		infoPane.update();
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
		return Optional.of(Assets2D.SOUND.get(controller.selectedGameType()));
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