package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3D;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
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

	private static final Color SCENE_BACKGROUND_COLOR = Color.rgb(20, 20, 60);

	public static final IntegerProperty $FPS = new SimpleIntegerProperty();
	public static final IntegerProperty $TOTAL_TICKS = new SimpleIntegerProperty();

	public final Stage stage;
	public final Scene mainScene;
	public final PacManGameController gameController;
	public final Canvas canvas2D = new Canvas();
	public GameScene currentGameScene;

	private final Keyboard keyboard = new Keyboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final HUD hud = new HUD(this);
	private final Group gameSceneParent = new Group();

	public PacManGameUI_JavaFX(Stage stage, PacManGameController gameController, double height) {
		this.stage = stage;
		this.gameController = gameController;

		StackPane root = new StackPane();
		root.getChildren().addAll(gameSceneParent, flashMessageView, hud);
		StackPane.setAlignment(hud, Pos.TOP_LEFT);

		GameScene gameScene = sceneForCurrentGameState(Env.$use3DScenes.get());
		double aspectRatio = gameScene.aspectRatio().orElse(getScreenAspectRatio());
		mainScene = new Scene(root, aspectRatio * height, height, SCENE_BACKGROUND_COLOR);
		setGameScene(gameScene);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		$FPS.addListener((source, oldValue, fps) -> {
			String gameName = gameController.isPlaying(PACMAN) ? "Pac-Man" : "Ms. Pac-Man";
			stage.setTitle(String.format("%s (%d fps, JavaFX)", gameName, fps));
		});
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/pacman/graphics/pacman.png")));
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
	}

	@Override
	public void showFlashMessage(String message, double seconds) {
		flashMessageView.showMessage(message, (long) (60 * seconds));
	}

	@Override
	public Optional<Direction> playerDirectionChangeRequested() {
		if (keyboard.keyPressed("Up")) {
			return Optional.of(Direction.UP);
		}
		if (keyboard.keyPressed("Down")) {
			return Optional.of(Direction.DOWN);
		}
		if (keyboard.keyPressed("Left")) {
			return Optional.of(Direction.LEFT);
		}
		if (keyboard.keyPressed("Right")) {
			return Optional.of(Direction.RIGHT);
		}
		return Optional.empty();
	}

	@Override
	public boolean gameStartRequested() {
		return keyboard.keyPressed("Space");
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
				: gameController.state == PacManGameState.INTERMISSION ? gameController.game().intermissionNumber() : 4;
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
			if (newGameScene instanceof AbstractGameScene2D) {
				((AbstractGameScene2D) newGameScene).setCanvas(canvas2D);
			}
			if (newGameScene.getGameController() == null) {
				newGameScene.setGameController(gameController);
			}
			newGameScene.resize(mainScene.getWidth(), mainScene.getHeight());
			newGameScene.keepSizeOf(mainScene);
			currentGameScene = newGameScene;
			currentGameScene.init();
			// replace game scene in scene graph
			gameSceneParent.getChildren().setAll(currentGameScene.getSubSceneFX());
			// Note: this must be done after adding to the scene graph
			currentGameScene.getSubSceneFX().requestFocus();
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent event) {
		log("UI received game event %s", event);
		PacManGameUI.super.onGameEvent(event);
		currentGameScene.onGameEvent(event);
	}

	@Override
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {
		if (e.newGameState == PacManGameState.INTRO) {
			stopAllSounds();
		}
		setGameScene(sceneForCurrentGameState(Env.$use3DScenes.get()));
	}

	private void onKeyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			onControlKeyPressed(e);
			return;
		}

		switch (e.getCode()) {
		case A:
			gameController.autopilotOn = !gameController.autopilotOn;
			showFlashMessage(gameController.autopilotOn ? "Autopilot ON" : "Autopilot OFF");
			break;

		case E:
			gameController.eatAllPellets();
			break;

		case I:
			gameController.setPlayerImmune(!gameController.isPlayerImmune());
			showFlashMessage(gameController.isPlayerImmune() ? "Player IMMUNE" : "Player VULNERABLE");
			break;

		case L:
			gameController.game().addLife();
			showFlashMessage(String.format("Player lives increased"));
			break;

		case N:
			if (gameController.isGameRunning()) {
				showFlashMessage(TrashTalk.CHEAT_SPELLS.nextSpell(), 2);
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
			if (currentGameScene instanceof PlayScene3D) {
				PlayScene3D playScene = (PlayScene3D) currentGameScene;
				playScene.nextCamera();
				showFlashMessage(String.format("Camera: %s", playScene.selectedCamera().get()));
			}
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
				Env.$slowDown.set(Math.max(1, Env.$slowDown.get() - 1));
			} else {
				Env.$slowDown.set(Math.min(10, Env.$slowDown.get() + 1));
			}
			break;

		case T:
			Env.$timeMeasured.set(!Env.$timeMeasured.get());
			break;

		case X:
			Env.$axesVisible.set(!Env.$axesVisible.get());
			break;

		case DIGIT3:
			toggleUse3DScenes();
			String message = String.format("3D scenes are %s", Env.$use3DScenes.get() ? "ON" : "OFF");
			showFlashMessage(message);
			break;

		default:
			break;
		}
	}

}