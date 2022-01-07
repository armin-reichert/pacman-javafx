/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.shell;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3DNaked;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
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
public class PacManGameUI_JavaFX implements PacManGameUI, DefaultPacManGameEventHandler {

	private final Stage stage;
	private final PacManGameController gameController;
	private final Canvas canvas = new Canvas();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final HUD hud = new HUD(this);
	private final Group gameSceneRoot = new Group();
	private AbstractGameScene currentGameScene;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController gameController, double height, boolean fullscreen) {
		this.stage = stage;
		this.gameController = gameController;

		// Determine the initial game scene
		AbstractGameScene gameScene = getSceneForCurrentGameState(Env.$use3DScenes.get());
		double aspectRatio = gameScene.aspectRatio()
				.orElse(Screen.getPrimary().getBounds().getWidth() / Screen.getPrimary().getBounds().getHeight());

		// Create the main scene containing all other sub-scenes
		StackPane mainSceneRoot = new StackPane(gameSceneRoot, flashMessageView, hud);
		StackPane.setAlignment(hud, Pos.TOP_LEFT);

		// Set blue background color, use black in wireframe display mode
		mainSceneRoot.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			Color color = Env.$drawMode3D.get() == DrawMode.FILL ? Color.CORNFLOWERBLUE : Color.BLACK;
			return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
		}, Env.$drawMode3D));

		Scene mainScene = new Scene(mainSceneRoot, aspectRatio * height, height);
		stage.setScene(mainScene);

		// Note: Can only be called *after* main scene has been set
		setGameScene(gameScene);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		stage.addEventHandler(ScrollEvent.SCROLL, this::onScrolled);
		stage.getIcons().add(new Image(getClass().getResourceAsStream(Env.APP_ICON_PATH)));
		stage.setFullScreen(fullscreen);
		stage.centerOnScreen();
		stage.show();
	}

	@Override
	public void update() {
		flashMessageView.update();
		hud.update();
	}

	public void reset() {
		stopAllSounds();
		currentGameScene.end();
	}

	public PacManGameController getGameController() {
		return gameController;
	}

	public GameModel game() {
		return gameController.game();
	}

	public Stage getStage() {
		return stage;
	}

	public AbstractGameScene getCurrentGameScene() {
		return currentGameScene;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void showFlashMessage(double seconds, String message, Object... args) {
		flashMessageView.showMessage(String.format(message, args), seconds);
	}

	private void stopAllSounds() {
		ScenesMsPacMan.SOUNDS.stopAll();
		ScenesPacMan.SOUNDS.stopAll();
	}

	private void toggleUse3DScenes() {
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		if (getSceneForCurrentGameState(false) != getSceneForCurrentGameState(true)) {
			stopAllSounds();
			setGameScene(getSceneForCurrentGameState(Env.$use3DScenes.get()));
		}
	}

	private AbstractGameScene getSceneForCurrentGameState(boolean _3D) {
		int sceneIndex;
		int twoOrThreeD = _3D ? 1 : 0;

		switch (gameController.currentStateID) {
		case INTRO:
			sceneIndex = 0;
			break;
		case INTERMISSION:
			sceneIndex = game().intermissionNumber(game().levelNumber);
			break;
		case INTERMISSION_TEST:
			sceneIndex = gameController.intermissionTestNumber;
			break;
		default:
			sceneIndex = 4; // play scene
			break;
		}

		switch (gameController.gameVariant()) {
		case MS_PACMAN:
			return ScenesMsPacMan.SCENES[sceneIndex][twoOrThreeD];
		case PACMAN:
			return ScenesPacMan.SCENES[sceneIndex][twoOrThreeD];
		default:
			throw new IllegalStateException();
		}
	}

	private void setGameScene(AbstractGameScene newScene) {
		if (currentGameScene != newScene) {
			if (currentGameScene != null) {
				log("Change scene from '%s' to '%s'", currentGameScene.getClass().getSimpleName(),
						newScene.getClass().getSimpleName());
				currentGameScene.end();
			} else {
				log("Set scene to '%s'", newScene.getClass().getSimpleName());
			}
			if (newScene instanceof AbstractGameScene2D) {
				((AbstractGameScene2D) newScene).setCanvas(canvas);
			}
			newScene.keepSizeOf(stage.getScene());
			newScene.resize(stage.getScene().getWidth(), stage.getScene().getHeight());
			newScene.init(gameController);
			log("Scene '%s' initialized", newScene.getClass().getSimpleName());
			gameSceneRoot.getChildren().setAll(newScene.getSubSceneFX());
			// Note: this must be done after new scene has been added to scene graph:
			newScene.getSubSceneFX().requestFocus();
			currentGameScene = newScene;
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent event) {
		log("UI received game event %s", event);
		DefaultPacManGameEventHandler.super.onGameEvent(event);
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
			String message = Env.message(gameController.isAutoControlled() ? "autopilot_on" : "autopilot_off");
			showFlashMessage(1, message);
			break;
		}

		case E:
			gameController.cheatEatAllPellets();
			break;

		case I: {
			game().player.immune = !game().player.immune;
			String message = Env.message(game().player.immune ? "player_immunity_on" : "player_immunity_off");
			showFlashMessage(1, message);
			break;
		}

		case L:
			game().player.lives += 3;
			showFlashMessage(2, String.format("3 more lives"));
			break;

		case N:
			if (gameController.isGameRunning()) {
				showFlashMessage(1, Env.CHEAT_TALK.next());
				gameController.changeState(PacManGameState.LEVEL_COMPLETE);
			}
			break;

		case Q:
			reset();
			gameController.changeState(PacManGameState.INTRO);
			break;

		case V:
			if (gameController.currentStateID == PacManGameState.INTRO) {
				gameController.selectGameVariant(gameController.gameVariant().succ());
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
			if (currentGameScene instanceof PlayScene3DNaked) {
				PlayScene3DNaked playScene3D = (PlayScene3DNaked) currentGameScene;
				Env.nextPerspective();
				String cameraType = Env.MESSAGES
						.getString(playScene3D.currentCameraController().getClass().getSimpleName());
				String message = Env.message("camera_perspective", cameraType);
				showFlashMessage(1, message);
			}
			break;

		case H:
			changeMazeWallHeight(!e.isShiftDown());
			break;

		case I:
			if (!hud.isVisible()) {
				hud.show();
			} else {
				hud.hide();
			}
			break;

		case L:
			Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
			break;

		case P:
			Env.$paused.set(!Env.$paused.get());
			if (Env.$paused.get()) {
				showFlashMessage(3, "Game paused (Press CTRL+P to resume)");
			} else {
				showFlashMessage(2, "Game resumed");
			}
			break;

		case R:
			changeMazeResolution(!e.isShiftDown());
			break;

		case S:
			int currentTargetFrameRate = Env.gameLoop.getTargetFrameRate();
			if (!e.isShiftDown()) {
				Env.gameLoop.setTargetFrameRate(currentTargetFrameRate + 10);
			} else {
				Env.gameLoop.setTargetFrameRate(Math.max(10, currentTargetFrameRate - 10));
			}
			showFlashMessage(1, "Target FPS set to %d Hz", Env.gameLoop.getTargetFrameRate());
			break;

		case T:
			Env.$isTimeMeasured.set(!Env.$isTimeMeasured.get());
			break;

		case X:
			Env.$axesVisible.set(!Env.$axesVisible.get());
			break;

		case DIGIT1:
			if (gameController.currentStateID == PacManGameState.INTRO) {
				showFlashMessage(1, "Intermission Scene Test");
				gameController.startIntermissionTest();
			}
			break;

		case DIGIT3: {
			toggleUse3DScenes();
			String message = Env.$use3DScenes.get() ? "Using 3D play scene\nCTRL+C changes perspective"
					: "Using 2D play scene";
			showFlashMessage(2, message);
			break;
		}

		default:
			break;
		}
	}

	private void onScrolled(ScrollEvent e) {
		boolean shift = e.isShiftDown();
		boolean up = shift ? e.getDeltaX() > 0 : e.getDeltaY() > 0;
		if (currentGameScene instanceof PlayScene3DNaked) {
			if (e.isShiftDown()) {
				changeMazeWallHeight(up);
			} else {
				changeMazeResolution(up);
			}
		}
	}

	private void changeMazeResolution(boolean up) {
		int res = Env.$mazeResolution.get();
		if (up) {
			Env.$mazeResolution.set(Math.min(res * 2, 8));
		} else {
			Env.$mazeResolution.set(Math.max(res / 2, 1));
		}
	}

	private void changeMazeWallHeight(boolean up) {
		double height = Env.$mazeWallHeight.get();
		if (up) {
			Env.$mazeWallHeight.set(Math.min(height + 0.2, 8.0));
		} else {
			Env.$mazeWallHeight.set(Math.max(height - 0.2, 0.1));
		}
		log("Maze wall height is now %.2f", Env.$mazeWallHeight.get());
	}
}