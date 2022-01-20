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
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements DefaultPacManGameEventHandler {

	private final Stage stage;
	private final PacManGameController gameController;
	private final Canvas canvas = new Canvas();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final HUD hud = new HUD(this);
	private final Group gameSceneRoot = new Group();
	private final StackPane mainSceneRoot;
	private final BooleanProperty $is3D = new SimpleBooleanProperty();

	private AbstractGameScene currentGameScene;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController gameController, double height, boolean fullscreen) {
		this.stage = stage;
		this.gameController = gameController;

		ScenesPacMan.init(this);
		ScenesMsPacMan.init(this);

		var gameScene = selectScene(gameController.game(), Env.$use3DScenes.get());

		mainSceneRoot = new StackPane(gameSceneRoot, flashMessageView, hud);
		StackPane.setAlignment(hud, Pos.TOP_LEFT);
		defineBackground(mainSceneRoot);

		double aspectRatio = gameScene.aspectRatio()
				.orElse(Screen.getPrimary().getBounds().getWidth() / Screen.getPrimary().getBounds().getHeight());
		double width = aspectRatio * height;
		stage.setScene(new Scene(mainSceneRoot, width, height));
		setGameScene(gameScene);

		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> Env.gameLoop.stop());
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		stage.addEventHandler(ScrollEvent.SCROLL, this::onScrolled);
		stage.getIcons().add(new Image(getClass().getResource("/pacman/graphics/pacman.png").toString()));
		stage.setFullScreen(fullscreen);
		stage.centerOnScreen();
		stage.show();
	}

	private void defineBackground(StackPane mainSceneRoot) {
		Image milkyway = new Image(getClass().getResource("/common/milkyway.jpg").toString());
		Background bgMilkyWay = new Background(new BackgroundImage(milkyway, null, null, null, null));
		Background bgBlack = new Background(new BackgroundFill(Color.BLACK, null, null));
		Background bgBlue = new Background(new BackgroundFill(Color.CORNFLOWERBLUE, null, null));
		mainSceneRoot.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			if ($is3D.get()) {
				return Env.$drawMode3D.get() == DrawMode.LINE ? bgBlack : bgMilkyWay;
			}
			return bgBlue;
		}, Env.$drawMode3D, $is3D));
	}

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

	public Stage getStage() {
		return stage;
	}

	public AbstractGameScene getCurrentGameScene() {
		return currentGameScene;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void showFlashMessage(double seconds, String message, Object... args) {
		flashMessageView.showMessage(String.format(message, args), seconds);
	}

	private void stopAllSounds() {
		ScenesMsPacMan.SOUNDS.stopAll();
		ScenesPacMan.SOUNDS.stopAll();
	}

	private void toggleUse3DScenes() {
		GameModel game = gameController.game();
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		if (selectScene(game, false) != selectScene(game, true)) {
			stopAllSounds();
			setGameScene(selectScene(game, Env.$use3DScenes.get()));
		}
	}

	private AbstractGameScene selectScene(GameModel game, boolean _3D) {
		int sceneIndex;
		int twoOrThreeD = _3D ? 1 : 0;

		switch (gameController.currentStateID) {
		case INTRO:
			sceneIndex = 0;
			break;
		case INTERMISSION:
			sceneIndex = game.intermissionNumber(game.levelNumber);
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

	private void setGameScene(AbstractGameScene newGameScene) {
		if (currentGameScene != newGameScene) {
			if (currentGameScene != null) {
				log("Change game scene from '%s' to '%s'", currentGameScene.name(), newGameScene.name());
				currentGameScene.end();
			} else {
				log("Set game scene to '%s'", newGameScene.name());
			}
			$is3D.set(newGameScene.is3D());
			newGameScene.init(stage.getScene(), gameController);
			log("Game scene '%s' initialized", newGameScene.name());
			gameSceneRoot.getChildren().setAll(newGameScene.getSubSceneFX());
			// Note: this must be done after new scene has been added to scene graph:
			newGameScene.getSubSceneFX().requestFocus();
			currentGameScene = newGameScene;
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
		setGameScene(selectScene(gameController.game(), Env.$use3DScenes.get()));
	}

	private void onKeyPressed(KeyEvent e) {
		final GameModel game = gameController.game();
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
			game.player.immune = !game.player.immune;
			String message = Env.message(game.player.immune ? "player_immunity_on" : "player_immunity_off");
			showFlashMessage(1, message);
			break;
		}

		case L:
			game.player.lives += 3;
			showFlashMessage(2, "You have %d lives", game.player.lives);
			break;

		case N:
			if (gameController.isGameRunning()) {
				showFlashMessage(1, Env.CHEAT_TALK.next());
				gameController.changeState(PacManGameState.LEVEL_COMPLETE);
			}
			break;

		case P:
			if (Env.$paused.get()) {
				Env.gameLoop.runSingleStep(true);
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
			if (currentGameScene.is3D()) {
				if (currentGameScene.camController().isPresent()) {
					Env.nextPerspective();
					String perspective = Env.message(currentGameScene.camController().get().getClass().getSimpleName());
					String message = Env.message("camera_perspective", perspective);
					showFlashMessage(1, message);
				}
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
				showFlashMessage(2, "Game paused\n(CTRL+P = resume, P = single step)");
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

		case Y:
			Env.$tilesVisible.set(!Env.$tilesVisible.get());
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
		if (currentGameScene instanceof PlayScene3D) {
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