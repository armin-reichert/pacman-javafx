package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalDouble;
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
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	public final HUD hud;
	final Stage stage;
	final PacManGameController controller;
	final Keyboard keyboard;
	final SceneFactory sceneFactory;
	final Scene mainScene;
	final StackPane mainSceneRoot;
	final FlashMessageView flashMessageView;
	final Set<GameScene> scenesCreated = new HashSet<>();

	GameScene currentGameScene;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.stage = stage;
		this.controller = controller;
		sceneFactory = new SceneFactory(controller);
		keyboard = new Keyboard();
		flashMessageView = new FlashMessageView();
		mainSceneRoot = new StackPane();
		mainScene = new Scene(mainSceneRoot, GameScene.ASPECT_RATIO * height, height, Color.rgb(20, 20, 60));

		hud = new HUD(this, Pos.TOP_LEFT);

		controller.addStateChangeListener(this::handleGameStateChange);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeys);

		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image(getClass().getResource("/pacman/graphics/pacman.png").toExternalForm()));
		stage.setOnCloseRequest(e -> Platform.exit());
		stage.setScene(mainScene);

		GameScene initialGameScene = sceneFactory.createGameScene(stage, Env.$use3DScenes.get());
		keepMaximizedInParent(initialGameScene, mainScene, initialGameScene.aspectRatio());
		changeGameScene(null, initialGameScene);

		stage.centerOnScreen();
		stage.show();
	}

	private void handleGameStateChange(PacManGameState oldState, PacManGameState newState) {
		log("Handle game state change %s to %s", oldState, newState);
		if (!sceneFactory.isSuitableScene(currentGameScene, Env.$use3DScenes.get())) {
			changeScene();
		}
		currentGameScene.onGameStateChange(oldState, newState);
	}

	private void changeScene() {
		GameScene nextScene = scenesCreated.stream()
				.filter(scene -> sceneFactory.isSuitableScene(scene, Env.$use3DScenes.get())).findFirst()
				.orElseGet(this::createSuitableScene);
		changeGameScene(currentGameScene, nextScene);
	}

	private GameScene createSuitableScene() {
		GameScene scene = sceneFactory.createGameScene(stage, Env.$use3DScenes.get());
		scenesCreated.add(scene);
		scene.setAvailableSize(mainScene.getWidth(), mainScene.getHeight());
		keepMaximizedInParent(scene, mainScene, scene.aspectRatio());
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
		mainSceneRoot.getChildren().addAll(currentGameScene.getFXSubScene(), flashMessageView, hud);
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
		if (sceneFactory.has2DAnd3DSceneForCurrentState()) {
			changeScene();
		}
	}

	private void keepMaximizedInParent(GameScene scene, Scene parentScene, OptionalDouble optionalAspectRatio) {
		if (optionalAspectRatio.isPresent()) {
			double aspectRatio = optionalAspectRatio.getAsDouble();
			parentScene.widthProperty().addListener((s, o, n) -> {
				double newHeight = Math.min(n.doubleValue() / aspectRatio, parentScene.getHeight());
				double newWidth = newHeight * aspectRatio;
				scene.setAvailableSize(newWidth, newHeight);
			});
			parentScene.heightProperty().addListener((s, o, n) -> {
				double newHeight = n.doubleValue();
				double newWidth = Math.min(parentScene.getHeight() * aspectRatio, parentScene.getWidth());
				scene.setAvailableSize(newWidth, newHeight);
			});
		} else {
			scene.getFXSubScene().widthProperty().bind(parentScene.widthProperty());
			scene.getFXSubScene().heightProperty().bind(parentScene.heightProperty());
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
		hud.update();
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