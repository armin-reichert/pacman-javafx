package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.scenes.common.CameraType;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	public final IntegerProperty $fps = new SimpleIntegerProperty();
	public final IntegerProperty $totalTicks = new SimpleIntegerProperty();
	public final Stage stage;
	public final PacManGameController gameController;
	public final HUD hud = new HUD(this);
	public final Scene mainScene;

	public GameScene currentGameScene;

	private final Keyboard keyboard = new Keyboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final Group gameSceneParent = new Group();

	public PacManGameUI_JavaFX(Stage stage, PacManGameController gameController, double height) {
		this.stage = stage;
		this.gameController = gameController;

		StackPane root = new StackPane();
		root.getChildren().addAll(gameSceneParent, flashMessageView, hud);
		StackPane.setAlignment(hud, Pos.TOP_LEFT);

		GameScene gameScene = sceneForCurrentGameState(Env.$use3DScenes.get());
		double aspectRatio = gameScene.aspectRatio().orElse(getScreenAspectRatio());
		mainScene = new Scene(root, aspectRatio * height, height, Color.rgb(20, 20, 60));
		setGameScene(gameScene);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		$fps.addListener((source, oldValue, newValue) -> {
			stage.setTitle(String.format("Pac-Man / Ms. Pac-Man (%d fps, JavaFX)", newValue));
		});
		$totalTicks.addListener((source, oldValue, newValue) -> {
			hud.update();
		});
		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image(getClass().getResource("/pacman/graphics/pacman.png").toExternalForm()));
		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.show();
	}

	@Override
	public void reset() {
		stopAllSounds();
		currentGameScene.end();
	}

	@Override
	public void update() {
		currentGameScene.update();
		flashMessageView.update();
		hud.update();
		if (currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D<?> scene2D = (AbstractGameScene2D<?>) currentGameScene;
			scene2D.clearCanvas(Color.BLACK);
			scene2D.render();
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

	private void stopAllSounds() {
		MsPacManScenes.SOUNDS.stopAll();
		PacManScenes.SOUNDS.stopAll();
	}

	private void toggleUse3DScenes() {
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		if (sceneForCurrentGameState(false) != sceneForCurrentGameState(true)) {
			stopAllSounds();
			setGameScene(sceneForCurrentGameState(Env.$use3DScenes.get()));
		}
	}

	private double getScreenAspectRatio() {
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		return bounds.getWidth() / bounds.getHeight();
	}

	private GameScene sceneForCurrentGameState(boolean use3D) {
		int sceneIndex = gameController.state == PacManGameState.INTRO ? 0
				: gameController.state == PacManGameState.INTERMISSION ? gameController.game().intermissionNumber : 4;
		int sceneVariant = use3D ? 1 : 0;
		if (gameController.gameVariant() == MS_PACMAN) {
			return MsPacManScenes.SCENES[sceneIndex][sceneVariant];
		} else if (gameController.gameVariant() == PACMAN) {
			return PacManScenes.SCENES[sceneIndex][sceneVariant];
		}
		throw new IllegalStateException();
	}

	private void setGameScene(GameScene newGameScene) {
		if (currentGameScene != newGameScene) {
			log("Change game scene from %s to %s", currentGameScene, newGameScene);
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			if (newGameScene.getGameController() == null) {
				newGameScene.setGameController(gameController);
				newGameScene.stretchTo(mainScene.getWidth(), mainScene.getHeight());
				newGameScene.keepStretched(mainScene);
			}
			currentGameScene = newGameScene;
			currentGameScene.start();
			// put game scene into scene graph
			gameSceneParent.getChildren().clear();
			gameSceneParent.getChildren().add(currentGameScene.get());
			// Note: this must be done after adding to the scene graph
			currentGameScene.get().requestFocus();
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent event) {
		log("%s received game event %s", getClass().getSimpleName(), event.getClass().getSimpleName());
		if (event instanceof PacManGameStateChangedEvent) {
			PacManGameStateChangedEvent stateChange = (PacManGameStateChangedEvent) event;
			if (stateChange.newGameState == PacManGameState.INTRO) {
				stopAllSounds();
			}
			setGameScene(sceneForCurrentGameState(Env.$use3DScenes.get()));
		}
		currentGameScene.onGameEvent(event);
	}

	private void onKeyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			onControlKeyPressed(e);
			return;
		}

		switch (e.getCode()) {
		case A:
			gameController.autopilot.enabled = !gameController.autopilot.enabled;
			showFlashMessage(gameController.autopilot.enabled ? "Autopilot ON" : "Autopilot OFF");
			break;

		case E:
			gameController.eatAllPellets();
			break;

		case I:
			gameController.setPlayerImmune(!gameController.isPlayerImmune());
			showFlashMessage(gameController.isPlayerImmune() ? "Player IMMUNE" : "Player VULNERABLE");
			break;

		case L:
			gameController.game().lives++;
			break;

		case N:
			if (gameController.isGameRunning()) {
				gameController.changeState(PacManGameState.LEVEL_COMPLETE);
			}
			break;

		case Q:
			reset();
			gameController.changeState(PacManGameState.INTRO);
			break;

		case V:
			gameController.toggleGameVariant();
			break;

		case X:
			gameController.killGhosts();
			break;

		case DIGIT1:
			if (gameController.state == PacManGameState.INTRO) {
				showFlashMessage("Test Intermission #1");
				gameController.game().intermissionNumber = 1;
				gameController.changeState(PacManGameState.INTERMISSION);
			}
			break;

		case DIGIT2:
			if (gameController.state == PacManGameState.INTRO) {
				showFlashMessage("Test Intermission #2");
				gameController.game().intermissionNumber = 2;
				gameController.changeState(PacManGameState.INTERMISSION);
			}
			break;

		case DIGIT3:
			if (gameController.state == PacManGameState.INTRO) {
				showFlashMessage("Test Intermission #3");
				gameController.game().intermissionNumber = 3;
				gameController.changeState(PacManGameState.INTERMISSION);
			}
			break;

		case F11:
			stage.setFullScreen(true);
			break;

		default:
			break;
		}
	}

	private void onControlKeyPressed(KeyEvent e) {
		switch (e.getCode()) {

		case C:
			int next = Env.$cameraType.get().ordinal() + 1;
			if (next == CameraType.values().length) {
				next = 0;
			}
			Env.$cameraType.set(CameraType.values()[next]);
			currentGameScene.selectCamera(Env.$cameraType.get());
			showFlashMessage(String.format("Use %s camera", Env.$cameraType.get()));
			break;

		case I:
			Env.$hudVisible.set(!Env.$hudVisible.get());
			break;

		case L:
			Env.$drawMode.set(Env.$drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
			break;

		case P:
			Env.$paused.set(!Env.$paused.get());
			break;

		case S:
			if (!e.isShiftDown()) {
				Env.$slowdown.set(Math.max(1, Env.$slowdown.get() - 1));
			} else {
				Env.$slowdown.set(Math.min(10, Env.$slowdown.get() + 1));
			}
			break;

		case T:
			Env.$measureTime.set(!Env.$measureTime.get());
			break;

		case X:
			Env.$showAxes.set(!Env.$showAxes.get());
			break;

		case DIGIT3:
			toggleUse3DScenes();
			String message = String.format("3D scenes %s", Env.$use3DScenes.get() ? "ON" : "OFF");
			showFlashMessage(message);
			break;

		default:
			break;
		}
	}
}