package de.amr.games.pacman.ui.fx.shell;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common.TrashTalk;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3D_Raw;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes;
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

	public final Stage stage;
	public final Scene mainScene;
	public final PacManGameController gameController;
	public final Canvas canvas = new Canvas();
	private final Keyboard keyboard = new Keyboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final HUD hud = new HUD();
	private final Group gameSceneParent = new Group();

	public GameScene currentGameScene;

	private ObjectBinding<Background> $background = Bindings.createObjectBinding(() -> {
		Color color = Env.$drawMode3D.get() == DrawMode.FILL ? Color.CORNFLOWERBLUE : Color.BLACK;
		return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
	}, Env.$drawMode3D);

	public PacManGameUI_JavaFX(Stage stage, PacManGameController gameController, double height) {
		this.stage = stage;
		this.gameController = gameController;

		StackPane root = new StackPane();
		root.backgroundProperty().bind($background);
		root.getChildren().addAll(gameSceneParent, flashMessageView, hud);
		StackPane.setAlignment(hud, Pos.TOP_LEFT);

		boolean _3D = Env.$use3DScenes.get();
		GameScene gameScene = getSceneForCurrentGameState(_3D);
		double aspectRatio = gameScene.aspectRatio().orElse(getScreenAspectRatio());
		mainScene = new Scene(root, aspectRatio * height, height);
		setGameScene(gameScene);

		Env.$totalTicks.addListener(($1, $2, newValue) -> hud.update(this));
		Env.$fps.addListener(($1, $2, fps) -> {
			String gameName = gameController.game().variant() == PACMAN ? "Pac-Man" : "Ms. Pac-Man";
			stage.setTitle(String.format("%s (%d FPS, JavaFX)", gameName, fps));
		});

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);

		stage.getIcons().add(new Image(getClass().getResourceAsStream("/pacman/graphics/pacman.png")));
		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.show();
	}

	private double getScreenAspectRatio() {
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		return bounds.getWidth() / bounds.getHeight();
	}

	public void reset() {
		stopAllSounds();
		currentGameScene.end();
	}

	@Override
	public void update() {
		currentGameScene.update();
		flashMessageView.update();
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
		MsPacManScenes.SOUNDS.stopAll();
		PacManScenes.SOUNDS.stopAll();
	}

	private void toggleUse3DScenes() {
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		if (getSceneForCurrentGameState(false) != getSceneForCurrentGameState(true)) {
			stopAllSounds();
			setGameScene(getSceneForCurrentGameState(Env.$use3DScenes.get()));
		}
	}

	private GameScene getSceneForCurrentGameState(boolean _3D) {
		var game = gameController.game();
		int sceneIndex = -1;
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
			return MsPacManScenes.SCENES[sceneIndex][_3D ? 1 : 0];
		case PACMAN:
			return PacManScenes.SCENES[sceneIndex][_3D ? 1 : 0];
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
				((AbstractGameScene2D) newGameScene).setCanvas(canvas);
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
		setGameScene(getSceneForCurrentGameState(Env.$use3DScenes.get()));
	}

	private void onKeyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			onControlKeyPressed(e);
			return;
		}

		switch (e.getCode()) {
		case A:
			gameController.setAutoControlled(!gameController.isAutoControlled());
			showFlashMessage(1, "Autopilot %s", gameController.isAutoControlled() ? "on" : "off");
			break;

		case E:
			gameController.cheatEatAllPellets();
			break;

		case I:
			gameController.setPlayerImmune(!gameController.isPlayerImmune());
			showFlashMessage(1, "Player is %s", gameController.isPlayerImmune() ? "immune" : "vulnerable");
			break;

		case L:
			gameController.game().changeLivesBy(3);
			showFlashMessage(1, String.format("Player lives increased"));
			break;

		case N:
			if (gameController.isGameRunning()) {
				showFlashMessage(1, TrashTalk.CHEAT_SPELLS.next());
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
			if (currentGameScene instanceof PlayScene3D_Raw) {
				PlayScene3D_Raw playScene = (PlayScene3D_Raw) currentGameScene;
				playScene.nextPerspective();
				showFlashMessage(1, "Perspective: %s", playScene.selectedPerspective());
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
			showFlashMessage(1, "3D scenes are %s", Env.$use3DScenes.get() ? "on" : "off");
			break;

		default:
			break;
		}
	}
}