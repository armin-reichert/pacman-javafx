package de.amr.games.pacman.ui.fx.shell;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common.TrashTalk;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3D;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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

	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("/common/messages");

	public static String message(String pattern, Object... args) {
		return MessageFormat.format(MESSAGES.getString(pattern), args);
	}

	private static final String APP_ICON = "/pacman/graphics/pacman.png";

	private final PacManGameController gameController;
	private final Stage stage;
	private final Scene mainScene;
	private final Canvas canvas = new Canvas();
	private final Keyboard keyboard = new Keyboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final HUD hud = new HUD(this);
	private final Group gameSceneRoot = new Group();
	private GameScene currentGameScene;

	private ObjectBinding<Background> $background = Bindings.createObjectBinding(() -> {
		Color color = Env.$drawMode3D.get() == DrawMode.FILL ? Color.CORNFLOWERBLUE : Color.BLACK;
		return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
	}, Env.$drawMode3D);

	public PacManGameUI_JavaFX(Stage stage, PacManGameController gameController, double height) {
		this.stage = stage;
		this.gameController = gameController;

		StackPane mainSceneRoot = new StackPane(gameSceneRoot, flashMessageView, hud);
		mainSceneRoot.backgroundProperty().bind($background);
		StackPane.setAlignment(hud, Pos.TOP_LEFT);

		boolean use3DScene = Env.$use3DScenes.get();
		GameScene gameScene = getSceneForCurrentGameState(use3DScene);
		double aspectRatio = gameScene.aspectRatio().orElse(getScreenAspectRatio());
		mainScene = new Scene(mainSceneRoot, aspectRatio * height, height);
		setGameScene(gameScene);

		Env.$fps.addListener(($fps, prevFPS, currentFPS) -> {
			String gameName = gameController.game().variant() == PACMAN ? "Pac-Man" : "Ms. Pac-Man";
			stage.setTitle(String.format("%s (%d frames/sec, JavaFX)", gameName, currentFPS));
		});

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);

		stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.show();
	}

	@Override
	public void update() {
		currentGameScene.update();
		flashMessageView.update();
	}

	public void reset() {
		stopAllSounds();
		currentGameScene.end();
	}

	public PacManGameController getGameController() {
		return gameController;
	}

	public Scene getMainScene() {
		return mainScene;
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	private double getScreenAspectRatio() {
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		return bounds.getWidth() / bounds.getHeight();
	}

	@Override
	public void showFlashMessage(double seconds, String message, Object... args) {
		flashMessageView.showMessage(String.format(message, args), (long) (60 * seconds));
	}

	@Override
	public void steer(Pac player) {
		if (keyboard.keyPressed("Up")) {
			player.setWishDir(Direction.UP);
		}
		if (keyboard.keyPressed("Down")) {
			player.setWishDir(Direction.DOWN);
		}
		if (keyboard.keyPressed("Left")) {
			player.setWishDir(Direction.LEFT);
		}
		if (keyboard.keyPressed("Right")) {
			player.setWishDir(Direction.RIGHT);
		}
	}

	private void stopAllSounds() {
		de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes.SOUNDS.stopAll();
		de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes.SOUNDS.stopAll();
	}

	private void toggleUse3DScenes() {
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		if (getSceneForCurrentGameState(false) != getSceneForCurrentGameState(true)) {
			stopAllSounds();
			setGameScene(getSceneForCurrentGameState(Env.$use3DScenes.get()));
		}
	}

	private GameScene getSceneForCurrentGameState(boolean _3D) {
		final PacManGameModel game = gameController.game();

		int sceneIndex;
		switch (gameController.state) {
		case INTRO:
			sceneIndex = 0;
			break;
		case INTERMISSION:
			sceneIndex = game.intermissionAfterLevel(game.level().number).getAsInt();
			break;
		default:
			sceneIndex = 4;
			break;
		}

		switch (game.variant()) {
		case MS_PACMAN:
			return de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes.SCENES[sceneIndex][_3D ? 1 : 0];
		case PACMAN:
			return de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes.SCENES[sceneIndex][_3D ? 1 : 0];
		default:
			throw new IllegalStateException();
		}
	}

	private void setGameScene(GameScene newGameScene) {
		if (currentGameScene != newGameScene) {
			log("Change game scene from %s to %s", currentGameScene, newGameScene);
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			if (newGameScene instanceof AbstractGameScene2D) {
				((AbstractGameScene2D) newGameScene).setGraphicsContext(canvas.getGraphicsContext2D());
			}
			if (newGameScene.getGameController() == null) {
				newGameScene.setGameController(gameController);
			}
			newGameScene.resize(mainScene.getWidth(), mainScene.getHeight());
			newGameScene.keepSizeOf(mainScene);
			newGameScene.init();
			gameSceneRoot.getChildren().setAll(newGameScene.getSubSceneFX());
			// Note: this must be done after new scene has been added to scene graph:
			newGameScene.getSubSceneFX().requestFocus();
			currentGameScene = newGameScene;
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
		setGameScene(getSceneForCurrentGameState(Env.$use3DScenes.get()));
	}

	private void onKeyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			onControlKeyPressed(e);
			return;
		}

		switch (e.getCode()) {
		case A: {
			gameController.setAutoControlled(!gameController.isAutoControlled());
			String message = message(gameController.isAutoControlled() ? "autopilot_on" : "autopilot_off");
			showFlashMessage(1, message);
			break;
		}

		case E:
			gameController.cheatEatAllPellets();
			break;

		case I: {
			gameController.setPlayerImmune(!gameController.isPlayerImmune());
			String message = message(gameController.isPlayerImmune() ? "player_immunity_on" : "player_immunity_off");
			showFlashMessage(1, message);
			break;
		}

		case L:
			gameController.game().changeLivesBy(3);
			showFlashMessage(1, String.format("Player lives increased"));
			break;

		case N:
			if (gameController.isGameRunning()) {
				showFlashMessage(1, TrashTalk.CHEAT_TALK.next());
				gameController.changeState(PacManGameState.LEVEL_COMPLETE);
			}
			break;

		case Q:
			reset();
			gameController.changeState(PacManGameState.INTRO);
			break;

		case V:
			if (gameController.state == PacManGameState.INTRO) {
				gameController.selectGameVariant(gameController.game().variant().succ());
			}
			break;

		case X:
			gameController.cheatKillGhosts();
			break;

		case SPACE:
			gameController.startGame();
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
				playScene.nextCam();
				String camera = MESSAGES.getString(playScene.selectedCam().getClass().getSimpleName());
				String message = message("camera_perspective", camera);
				showFlashMessage(1, message);
			}
			break;

		case H:
			if (!e.isShiftDown()) {
				if (Env.$mazeWallHeight.get() < 16) {
					Env.$mazeWallHeight.set(Env.$mazeWallHeight.get() + 0.2);
				}
			} else {
				if (Env.$mazeWallHeight.get() > 0.2) {
					Env.$mazeWallHeight.set(Env.$mazeWallHeight.get() - 0.2);
				}
			}
			break;

		case I:
			Env.$isHUDVisible.set(!Env.$isHUDVisible.get());
			break;

		case L:
			Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
			break;

		case P:
			Env.$paused.set(!Env.$paused.get());
			break;

		case R:
			if (!e.isShiftDown()) {
				if (Env.$mazeResolution.get() < 8) {
					Env.$mazeResolution.set(Env.$mazeResolution.get() * 2);
				}
			} else {
				if (Env.$mazeResolution.get() > 1) {
					Env.$mazeResolution.set(Env.$mazeResolution.get() / 2);
				}
			}
			break;

		case S:
			if (!e.isShiftDown()) {
				Env.$slowDown.set(Math.max(1, Env.$slowDown.get() - 1));
			} else {
				Env.$slowDown.set(Math.min(10, Env.$slowDown.get() + 1));
			}
			break;

		case T:
			Env.$isTimeMeasured.set(!Env.$isTimeMeasured.get());
			break;

		case X:
			Env.$axesVisible.set(!Env.$axesVisible.get());
			break;

		case DIGIT3:
			toggleUse3DScenes();
			if (Env.$use3DScenes.get()) {
				showFlashMessage(2, "Using 3D play scene\nCTRL+C changes perspective");
			} else {
				showFlashMessage(1, "Using 2D play scene");
			}
			break;

		default:
			break;
		}
	}
}